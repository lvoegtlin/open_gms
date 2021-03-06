package ch.unifr.hisdoc2.graphmanuscribble.controller;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.LoadImageStatus;
import ch.unifr.hisdoc2.graphmanuscribble.helper.binarization.BinarizationAlgos;
import ch.unifr.hisdoc2.graphmanuscribble.helper.binarization.BinaryPageImageProcessing;
import ch.unifr.hisdoc2.graphmanuscribble.helper.commands.AnnotateCommand;
import ch.unifr.hisdoc2.graphmanuscribble.helper.commands.DeleteEdgeCommand;
import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.UndoCollector;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.LoadedGraph;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.io.helper.LoadResult;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.helper.PolygonExporter;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.AngieMSTGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.GraphExporter;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.GraphImporter;
import ch.unifr.hisdoc2.graphmanuscribble.model.image.GraphImage;
import ch.unifr.hisdoc2.graphmanuscribble.model.scribble.UserInput;
import ch.unifr.hisdoc2.graphmanuscribble.view.GraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.ImageGraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.PolygonView;
import ch.unifr.hisdoc2.graphmanuscribble.view.UserInteractionView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static ch.unifr.hisdoc2.graphmanuscribble.helper.LoadImageStatus.ONLY_IMAGE;

/**
 * Created by larsvoegtlin on 16.01.17.
 */
public class Controller{

    public static Logger logger = LogManager.getLogger("GraphManuscribbleLog");

    //FXML
    @FXML
    private StackPane stackPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ChoiceBox<String> annotationBox;
    @FXML
    public ToggleButton deannotateButton;
    @FXML
    public BorderPane borderPane;

    //images
    private BufferedImage binarizedImage;
    private BufferedImage originalImage;
    private String fileNameWithExtension;

    //Models
    private AngieMSTGraph graph;
    private AnnotationPolygonMap polygonMap;
    private GraphImage graphImage;
    private UserInput userInput;

    //Views
    private GraphView graphView;
    private PolygonView polygonView;
    private UserInteractionView interactionView;
    private ImageGraphView imageView;
    private Group zoomTarget;

    //Usefull values
    private double width;
    private double height;
    private Canvas glassPanel;
    private boolean mouseDragged = false;

    //colors
    private AnnotationType currentAnnotation;
    private List<AnnotationType> possibleAnnotations;

    //current values
    private long lastTime;
    private ArrayList<Double> deletePoints = new ArrayList<>();
    private ArrayList<Double> annotationPoints = new ArrayList<>();
    private LarsGraph currentAnnotationGraph;
    private ArrayList<LarsGraphCollection> hitByCurrentAnnotation = new ArrayList<>();
    private int deleteId = 0;
    /**
     * trackes if we are annotating or deleting a annotation
     */
    private boolean deleteAnnotation = false;
    private boolean delete = false;

    //concurrency variables
    private List<ConcaveHullExtractionService> currentHullCalculations = new ArrayList<>();

    public Controller(){
        //chooses the first annotation as staring type
        this.possibleAnnotations = SettingReader.getInstance().getAnnotations();
        this.currentAnnotation = possibleAnnotations.get(0);
    }

    /**
     *
     * @param ori
     * @param bin
     * @param dim
     */
    private void setupNewImage(BufferedImage ori, BufferedImage bin, Dimension2D dim, LoadedGraph loadedGraph){
        Image img = SwingFXUtils.toFXImage(bin, null);
        Image img2 = SwingFXUtils.toFXImage(ori, null);

        this.graphImage = new GraphImage(img2, img);

        originalImage = ori;
        binarizedImage = bin;

        AngieMSTGraph graph = new AngieMSTGraph(30,
                true,
                dim.getWidth(),
                dim.getHeight());
        if(loadedGraph != null){
            graph.createGraph(binarizedImage, originalImage, loadedGraph.getOriginal(), loadedGraph.getForest());
        } else {
            graph.createGraph(binarizedImage, originalImage, null, null);
        }
        SettingReader settingReader = SettingReader.getInstance();
        List<AnnotationType> types = settingReader.getAnnotations();
        UserInput uI = new UserInput(types);

        ArrayList<AnnotationPolygonType> poly = new ArrayList<>();
        types.forEach(annotationType ->
                poly.add(new AnnotationPolygonType(annotationType))
        );
        AnnotationPolygonMap model = new AnnotationPolygonMap(poly);

        //annotationBox.getSelectionModel().selectFirst();

        //important values and views
        this.width = dim.getWidth();
        this.height = dim.getHeight();
        this.graph = graph;
        this.polygonMap = model;
        this.userInput = uI;

        graphView = new GraphView(this, SettingReader.getInstance().getGraphColor());
        polygonView = new PolygonView(polygonMap, this);
        interactionView = new UserInteractionView(userInput, this);
        imageView = new ImageGraphView(graphImage, this);

        stackPane.getChildren().clear();
        zoomTarget = new Group();
        stackPane.getChildren().add(zoomTarget);

        imageView.addToGroup(zoomTarget);
        graphView.addToGroup(zoomTarget);
        polygonView.addToGroup(zoomTarget);
        interactionView.addToGroup(zoomTarget);
        //the glass panel on top of everything else
        this.glassPanel = new Canvas(getWidth(), getHeight());
        zoomTarget.getChildren().add(glassPanel);

        initHandlers();

        borderPane.getScene().getWindow().setWidth(800 + 187);
        borderPane.getScene().getWindow().setHeight(724);
        scrollPane.setPrefViewportWidth(800);
        scrollPane.setPrefViewportHeight(600);
    }

    @FXML
    private void initialize(){
        List<AnnotationType> types = SettingReader.getInstance().getAnnotations();

        for(AnnotationType type : types){
            annotationBox.getItems().add(type.getName());
        }
        //gui init stuff
        annotationBox.setTooltip(new Tooltip("Select your annotation type"));
        annotationBox.getSelectionModel().selectFirst();
        currentAnnotation = getAnnotationTypeByName(annotationBox.getSelectionModel().getSelectedItem());
        annotationBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentAnnotation = getAnnotationTypeByName(observable.getValue());
        });
    }

    /**
     * Initializes all the event handlers.
     * LM + CTL = DELETE
     * LM + ALT = ANNOTATE
     * <p>
     * MOUSEWHEEL = ZOOM
     */
    private void initHandlers(){
        //the user starts dragging a lone
        glassPanel.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                    mouseDragged = false;
                    //delete
                    if(delete){
                        deletePoints.clear();
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());
                        currentAnnotation = SettingReader.getInstance().getDeletion();
                    } else {
                        currentAnnotationGraph = new LarsGraph(new SimpleGraph<>(GraphEdge.class), true);
                        currentAnnotationGraph.setAnnotationGraph(true);
                        hitByCurrentAnnotation.clear();
                        deletePoints.clear();
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());
                    }
                }
        );

        //handels the input during the drag
        glassPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                    mouseDragged = true;
                    //delete
                    if(delete){
                        if((System.currentTimeMillis() - lastTime < Constants.REFRESH_TIME)){
                            deletePoints.add(event.getX());
                            deletePoints.add(event.getY());
                        } else {
                            deleteEdges(getPolygonFromEventPoints(event, true));
                            lastTime = System.currentTimeMillis();
                        }
                        event.consume();
                    } else {
                        if((System.currentTimeMillis() - lastTime < Constants.REFRESH_TIME)){
                            deletePoints.add(event.getX());
                            deletePoints.add(event.getY());
                        } else {
                            processPolygons(getPolygonFromEventPoints(event, false));
                            lastTime = System.currentTimeMillis();
                        }
                        event.consume();
                    }
                }
        );

        //handels when the scribble ends
        glassPanel.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                    mouseDragged = false;
                    //delete
                    if(delete){
                        deleteEdges(getPolygonFromEventPoints(event, true));
                        lastTime = System.currentTimeMillis();
                        //important increament of the delete id
                        //done to bundle delete commands done from the same scribble
                        deleteId++;
                    }else if(deleteAnnotation){
                        processPolygons(getPolygonFromEventPoints(event, false));
                        lastTime = System.currentTimeMillis();
                        AnnotateCommand cmd = new AnnotateCommand(this,false);
                        if(cmd.canExecute()){
                            cmd.execute();
                        }
                    } else { //annotate
                        processPolygons(getPolygonFromEventPoints(event, false));
                        lastTime = System.currentTimeMillis();
                        AnnotateCommand cmd = new AnnotateCommand(this,true);
                        if(cmd.canExecute()){
                            cmd.execute();
                        }
                    }
                }
        );


        //zomming
        glassPanel.addEventHandler(ScrollEvent.SCROLL, event -> {
                    if(event.getDeltaY() == 0){
                        return;
                    }

                    event.consume();

                    final double zoomFactor = event.getDeltaY() > 0 ? 1.2 : 1 / 1.2;

                    Bounds groupBounds = zoomTarget.getLayoutBounds();
                    final Bounds viewportBounds = scrollPane.getViewportBounds();

                    // calculate pixel offsets from [0, 1] range
                    double valX = scrollPane.getHvalue() * (groupBounds.getWidth() - viewportBounds.getWidth());
                    double valY = scrollPane.getVvalue() * (groupBounds.getHeight() - viewportBounds.getHeight());

                    // convert content coordinates to zoomTarget coordinates
                    Point2D posInZoomTarget = zoomTarget.parentToLocal(zoomTarget.parentToLocal(new Point2D(event.getX(), event.getY())));

                    // calculate adjustment of scroll position (pixels)
                    Point2D adjustment = zoomTarget.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

                    // do the resizing
                    zoomTarget.setScaleX(zoomFactor * zoomTarget.getScaleX());
                    zoomTarget.setScaleY(zoomFactor * zoomTarget.getScaleY());

                    // refresh ScrollPane scroll positions & content bounds
                    scrollPane.layout();

                    // convert back to [0, 1] range
                    // (too large/small values are automatically corrected by ScrollPane)
                    groupBounds = stackPane.getLayoutBounds();
                    scrollPane.setHvalue((valX + adjustment.getX()) / (groupBounds.getWidth() - viewportBounds.getWidth()));
                    scrollPane.setVvalue((valY + adjustment.getY()) / (groupBounds.getHeight() - viewportBounds.getHeight()));
                }
        );
    }

    @FXML
    private void closeApplication(){
        Platform.exit();
    }

    @FXML
    public void toggleGraphView(ActionEvent actionEvent){
        if(graphView.isShown()){
            graphView.hide();
        } else {
            graphView.show();
        }
    }

    @FXML
    public void toggleUserInteractionView(ActionEvent actionEvent){
        if(interactionView.isShown()){
            interactionView.hide();
        } else {
            interactionView.show();
        }
    }

    @FXML
    public void togglePolygonView(ActionEvent actionEvent){
        if(polygonView.isShown()){
            polygonView.hide();
        } else {
            polygonView.show();
        }
    }

    @FXML
    public void toggleImageView(ActionEvent actionEvent){
        graphImage.setSeeOrgImg(!graphImage.isSeeOrgImg());

        imageView.update();
    }

    @FXML
    private void changeAnnotationModus(){
        deleteAnnotation = !deleteAnnotation;
    }

    @FXML
    public void aboutDialog(){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("About GraphManuscribble");
        info.setContentText("Master Thesis by: Lars Vögtlin\n" +
                "Supervisors: Angelika Garz, Mathias Seuret, Andreas Fischer und Rolf Ingold\n" +
                "Based on the paper: \"Creating Ground Truth for Historical Manuscripts with Document Graphs and Scribbling Interaction\" by Garz et. al.");
        info.showAndWait();
    }

    @FXML
    public void deleteEdge(ActionEvent actionEvent){
        delete = !delete;
        deannotateButton.setDisable(delete);
        annotationBox.setDisable(delete);
        if(!delete){
            currentAnnotation = getAnnotationTypeByName(annotationBox.getSelectionModel().getSelectedItem());
            deletePoints.clear();
        }
    }

    @FXML
    public void undoAction(ActionEvent actionEvent){
        UndoCollector.getInstance().undo();
        currentAnnotation = SettingReader.getInstance().getDeletion();
        updateViews();
        currentAnnotation = getAnnotationTypeByName(annotationBox.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void saveDialog(ActionEvent actionEvent) throws IOException{
        //TODO if nothing is loaded dont show up
        DirectoryChooser dC = new DirectoryChooser();
        File dir = dC.showDialog(stackPane.getScene().getWindow());
        String ext = FilenameUtils.getExtension(fileNameWithExtension);
        String baseName = FilenameUtils.getBaseName(fileNameWithExtension);
        //save original file
        ImageIO.write(originalImage, ext, new File(dir, fileNameWithExtension));
        //save binary file
        ImageIO.write(binarizedImage, ext, new File(dir, baseName + "_binary."+ext));
        //save graph
        try{
            GraphExporter.export2XML(graph.getGraph(), dir.getAbsolutePath(), baseName+"_graph", baseName);
        } catch(ParserConfigurationException | TransformerException e){
            e.printStackTrace();
        }
        //save polygons
        PolygonExporter.exportXML(this, new File(dir, baseName + "_polygons.gxml"));
    }

    @FXML
    public void loadImageDialog(ActionEvent actionEvent){
        FileChooser.ExtensionFilter jpg = new FileChooser.ExtensionFilter("JPG", "*.jpg");
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("png", "*.png");

        Dialog<LoadResult> dialog = new Dialog<>();
        dialog.setTitle("Create New Annotation");

        //buttons
        ButtonType saveButtonType = new ButtonType("Load", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        //create Lables and fields +picker
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // write down the paths
        TextField oriPath = new TextField();
        oriPath.setPromptText("Path");
        TextField binPath = new TextField();
        binPath.setPromptText("Path");
        TextField graphPath = new TextField();
        graphPath.setPromptText("Path");

        //open the file picker
        Button filePickerOri = new Button("Browse...");
        filePickerOri.setOnAction(event -> {
            oriPath.clear();
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(jpg, png);
            File f = fc.showOpenDialog(filePickerOri.getScene().getWindow());
            oriPath.setText(f.getAbsolutePath());
        });
        Button filePickerBin = new Button("Browse...");
        filePickerBin.setOnAction(event -> {
            binPath.clear();
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(jpg, png);
            File f = fc.showOpenDialog(filePickerBin.getScene().getWindow());
            binPath.setText(f.getAbsolutePath());
        });
        Button filePickerGraph = new Button("Browse...");
        filePickerGraph.setOnAction(event -> {
            graphPath.clear();
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Graph XML", "*.gxml"));
            File f = fc.showOpenDialog(filePickerGraph.getScene().getWindow());
            graphPath.setText(f.getAbsolutePath());
        });

        grid.add(new Label("Original image"), 0, 0);
        grid.add(oriPath, 1, 0);
        grid.add(filePickerOri, 2, 0);
        grid.add(new Label("Binary Image (optional)"), 0, 1);
        grid.add(binPath, 1,1);
        grid.add(filePickerBin, 2,1);
        grid.add(new Label("Graph xml (gxml, optional: require Binary)"), 0, 2);
        grid.add(graphPath, 1,2);
        grid.add(filePickerGraph, 2,2);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // enable save if we have a name for the new annotation
        oriPath.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(param -> {
            LoadImageStatus status = LoadImageStatus.NOTHING;
            if(param == saveButtonType){
                if(!oriPath.getText().isEmpty() && binPath.getText().isEmpty() && graphPath.getText().isEmpty()){
                    status = ONLY_IMAGE;
                } else if(!oriPath.getText().isEmpty() && !binPath.getText().isEmpty() && graphPath.getText().isEmpty()){
                    status = LoadImageStatus.IMAGE_BINARY;
                } else if(!oriPath.getText().isEmpty() && !binPath.getText().isEmpty() && !graphPath.getText().isEmpty()){
                    status = LoadImageStatus.IMAGE_BINARY_GRAPH;
                }
                return new LoadResult(status, oriPath.getText(), binPath.getText(), graphPath.getText());
            }
            return null;
        });

        Optional<LoadResult> result = dialog.showAndWait();

        result.ifPresent(this::loadImage);
    }

    /*
     * GETTERS
     *
     */

    public AngieMSTGraph getGraph(){
        return graph;
    }

    public AnnotationPolygonMap getPolygonMap(){
        return polygonMap;
    }

    public UserInput getUserInput(){
        return userInput;
    }

    public List<ConcaveHullExtractionService> getCurrentHullCalculations(){
        return currentHullCalculations;
    }

    public ArrayList<LarsGraphCollection> getHitByCurrentAnnotation(){
        return hitByCurrentAnnotation;
    }

    public LarsGraph getCurrentAnnotationGraph(){
        return currentAnnotationGraph;
    }

    public ArrayList<Double> getAnnotationPoints(){
        return annotationPoints;
    }

    /**
     * Creates from out of the existing deletePoints in the deletePoints list and the current event a new polygonMap.
     * It clears the list after creating the polygonMap
     *
     * @param event - the mouse event
     * @return the created polygonMap
     */
    private Polygon getPolygonFromEventPoints(MouseEvent event, boolean isDelete){
        deletePoints.add(event.getX());
        deletePoints.add(event.getY());

        Polygon p = new Polygon();
        p.getPoints().addAll(deletePoints);

        if(!isDelete){
            annotationPoints.addAll(deletePoints);
        }

        deletePoints.clear();//clear the list to start a new polygonMap

        return p;
    }

    /**
     * Width of the image
     *
     * @return - width
     */
    public double getWidth(){
        return width;
    }

    /**
     * Height of the image
     *
     * @return - height
     */
    public double getHeight(){
        return height;
    }

    /**
     * Returns the annotation type that is currently selected by the user
     *
     * @return - the current annotation type
     */
    public AnnotationType getCurrentAnnotationType(){
        return currentAnnotation;
    }


    /**
     * Does all the annotation work. It takes a polygonMap and gets the Color of the scribble that is used at the moment. It
     * adds this scribble to the userinteraction. In first case it adds the subgraph that is annotated to the polygonMap model
     * and so updates the polygonMap view.
     *
     * @param p - that input scribble
     */
    private void processPolygons(Polygon p){
        //graph that contains the scribble in its hull
        LarsGraphCollection larsGraphCollection = graph.getLarsGraphPolygonIsInHull(p);
        if(larsGraphCollection != null){
            //add it to the list of hit graphs with the current annotation
            addHitGraphByCurrentAnnotation(larsGraphCollection);

            if(polygonMap.addNewScribble(larsGraphCollection,
                    currentAnnotationGraph,
                    currentAnnotation)){
                polygonView.update();
            }
        }

        userInput.addScribble(currentAnnotation, p, mouseDragged, false);
        interactionView.update();
    }

    /**
     * Adds a graph that was hit by the current annotation scribble to the list of hit grahs
     *
     * @param lGC - the hit graph
     */
    private void addHitGraphByCurrentAnnotation(LarsGraphCollection lGC){
        if(!hitByCurrentAnnotation.contains(lGC) && lGC != null){
            hitByCurrentAnnotation.add(lGC);
            lGC.setAnnotated(true);
        }
    }

    /**
     * Deletes all edges the polygonMap p is intersection with. It also starts the thread that calculates the concave
     * hull for the two newly created graphs.
     *
     * @param p - Polygon scribble
     */
    private void deleteEdges(Polygon p){
        GraphEdge edge = graph.getIntersectionFromScribble(p, null);

        //if we hit an edge
        if(edge != null){
            //deletes edge in the original graph (labels deleted)
            graph.removeEdge(edge);

            DeleteEdgeCommand cmd = new DeleteEdgeCommand(this, edge, p, deleteId);

            if(cmd.canExecute()){
                cmd.execute();
                UndoCollector.getInstance().add(cmd);
            }
        }

        //add the scribble to the user input scribbles
        userInput.addScribble(currentAnnotation, p, mouseDragged, true);

        //update the graph and also the interaction view
        interactionView.update();
        graphView.update();
    }

    /**
     * Deletes the visual representation of a LGC. These are the annotation scribble NOT the graphs itself.
     *
     * @param lGC - the lgc to delete the annotation scribbles
     */
    public void deleteScribble(LarsGraphCollection lGC){
        //delete visual representation of the hit annotations
        userInput.deleteAnnotationScribbles(lGC, currentAnnotation);
        interactionView.update();
    }

    /**
     * Deletes the annotation of a annotated LGC. This means that all the annotation scribble gets deleted
     * and the WHOLE LGC gets unconnected.
     *
     * @param lGC - Annotated LGC to delete annotation
     */
    public void deleteAnnotation(LarsGraphCollection lGC){
        deleteScribble(lGC);

        //delete the annotationPolygon
        polygonMap.removeAnnotationPolygon(lGC);
        polygonView.update();

        //remove graphs from lgc
        LarsGraph[] lgArray = new LarsGraph[lGC.getAnnotationGraphs().size()];
        lGC.removeGraph(lGC.getAnnotationGraphs().toArray(lgArray));

        //get all nonAnnotationgaphs and make new lgcs out of them
        //copy is dont because of modification exception. Iterator not working because we need to remove the element in both lists (graphs and non/annotationGraphs)
        List<LarsGraph> copy = new ArrayList<>(lGC.getNonAnnotationGraphs());
        for(ListIterator<LarsGraph> it = copy.listIterator(); it.hasNext();){
            LarsGraph n = it.next();
            graph.addNewSubgraph(new LarsGraphCollection(n, n.getConcaveHull()), true);
            lGC.removeGraph(n);
            it.remove();
        }

        lGC.update();
    }

    /**
     * Updates the three most important view.
     * - PolygonView
     * - InteractionView
     * - GraphView
     */
    private void updateViews(){
        updatePolygonView();
        interactionView.update();
        graphView.update();
    }

    /**
     * Updates the polygonView
     */
    public void updatePolygonView(){
        polygonView.update();
    }

    /**
     * clears the list of points that represent the current annottion or delete scribble.
     */
    public void clearAnnotationPoints(){
        annotationPoints.clear();
    }

    /*public void createNewAnnotation(AnnotationType type){
        //TODO write into the settings.xml file
        possibleAnnotations.add(new AnnotationType(type.getName(), type.getColor()));
        annotationBox.getItems().add(type.getName());
        //add it to the userinteraction view to see the scribble
        interactionView.addNewAnnotationType(type);
        //add it to the polygon view to make the annotations visible
        polygonView.addNewAnnotationType(type);
        //add a new annotationpolygontype to the map
        polygonMap.addNewAnnotation(type);
    }*/

    /**
     * Returns a annotationType by name or null instead.
     *
     * @param name
     * @return
     */
    private AnnotationType getAnnotationTypeByName(String name){
        for(AnnotationType type : possibleAnnotations){
            if(type.getName().equals(name)){
                return type;
            }
        }
        return null;
    }

    private void loadImage(LoadResult res){
        switch(res.getStatus()){
            case NOTHING: Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("You did not match the requirements!");
                alert.setContentText("The Requirements to load a file are:\n" +
                        "1. Just a original image (uses otsu binarization)\n" +
                        "2. A original image and a binary image\n" +
                        "3. A original image, a binary image and a graph xml");
                alert.showAndWait();
                break;
            case ONLY_IMAGE:
                try{
                    binarizedImage = BinaryPageImageProcessing.binariseImage(res.getOri(),
                            false,
                            BinarizationAlgos.DOG,
                            new float[1]);
                } catch(IOException e){
                    e.printStackTrace();
                }

                fileNameWithExtension = res.getFileName();
                //create Graph
                setupNewImage(res.getOri(), binarizedImage, res.getDim(), null);
                break;
            case IMAGE_BINARY:
                //just create graph
                fileNameWithExtension = res.getFileName();
                setupNewImage(res.getOri(), res.getBin(), res.getDim(), null);
                break;
            case IMAGE_BINARY_GRAPH:
                LoadedGraph loadedGraph = GraphImporter.xml2Graph(res.getGraph());
                //set the information in the angieGraph
                fileNameWithExtension = res.getFileName();
                setupNewImage(res.getOri(), res.getBin(), res.getDim(), loadedGraph);
                break;
        }
    }
}
