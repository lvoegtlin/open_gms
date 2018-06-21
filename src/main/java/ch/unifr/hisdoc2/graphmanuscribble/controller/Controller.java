package ch.unifr.hisdoc2.graphmanuscribble.controller;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.commands.AnnotateCommand;
import ch.unifr.hisdoc2.graphmanuscribble.helper.commands.DeleteEdgeCommand;
import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.UndoCollector;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.AngieMSTGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.image.GraphImage;
import ch.unifr.hisdoc2.graphmanuscribble.model.scribble.UserInput;
import ch.unifr.hisdoc2.graphmanuscribble.view.GraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.ImageGraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.PolygonView;
import ch.unifr.hisdoc2.graphmanuscribble.view.UserInteractionView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    //images
    private BufferedImage bi;
    private BufferedImage ori;

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

    @FXML
    private void initialize(){
        //s = small, m = medium, l = large, h = huge
        String picString = "";
        String binPath = "";
        String oriPath = "";

        switch(picString){
            case "m":
                binPath = "binary_testpage_medium.png";
                oriPath = "testpage_medium.jpg";
                break;
            case "l":
                binPath = "binary_testpage_large.png";
                oriPath = "testpage_large.jpg";
                break;
            case "h":
                binPath = "binary_testpage_huge.png";
                oriPath = "testpage_huge.jpg";
                break;
            case "t":
                binPath = "e-codices_csg-0018_160_max_binary.png";
                oriPath = "e-codices_csg-0018_160_max.jpg";
                break;
            case "p":
                binPath = "e-codices_fmb-cb-0055_0098v_max_binary.png";
                oriPath = "e-codices_fmb-cb-0055_0098v_max.jpg";
                break;
            default:
                binPath = "binary_cpl.png";
                oriPath = "cpl.JPG";
                break;
        }

        Image img = new Image("file:"+binPath);
        Image img2 = new Image("file:"+oriPath);

        this.graphImage = new GraphImage(img2, img);

        //primaryStage.setHeight(img.getHeight());
        //primaryStage.setWidth(img.getWidth());

        File biF = null;
        File oriF = null;
        try{
            biF = new File(binPath);

            oriF = new File(oriPath);

            bi = ImageIO.read(biF);
            ori = ImageIO.read(oriF);
        } catch(IOException e){
            e.printStackTrace();
        }

        AngieMSTGraph graph = new AngieMSTGraph(30,
                true,
                "",
                binPath,
                "gt_testpage.xml",
                img.getWidth(),
                img.getHeight());

        boolean useRelEOnly = graph.getRelevantEdges();
        graph.setRelevantEdges(true);
        graph.setRelevantEdges(useRelEOnly);
        graph.createGraph(bi, ori);
        SettingReader settingReader = SettingReader.getInstance();
        List<AnnotationType> types = settingReader.getAnnotations();
        UserInput uI = new UserInput(types);

        ArrayList<AnnotationPolygonType> poly = new ArrayList<>();
        //TODO testing
        types.forEach(annotationType ->
                poly.add(new AnnotationPolygonType(annotationType))
        );
        AnnotationPolygonMap model = new AnnotationPolygonMap(poly);


        for(AnnotationType type : types){
            annotationBox.getItems().add(type.getName());
        }
        //gui init stuff
        //TODO not working
        annotationBox.setTooltip(new Tooltip("Select your annotation type"));
        annotationBox.getSelectionModel().selectFirst();
        currentAnnotation = getAnnotationTypeByName(annotationBox.getSelectionModel().getSelectedItem());
        annotationBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentAnnotation = getAnnotationTypeByName(observable.getValue());
        });
        //annotationBox.getSelectionModel().selectFirst();

        //important values and views
        this.width = this.graphImage.getWidth();
        this.height = this.graphImage.getHeight();
        this.graph = graph;
        this.polygonMap = model;
        this.userInput = uI;

        graphView = new GraphView(this, SettingReader.getInstance().getGraphColor());
        polygonView = new PolygonView(polygonMap, this);
        interactionView = new UserInteractionView(userInput, this);
        imageView = new ImageGraphView(graphImage, this);

        imageView.addToStackPane(stackPane);
        graphView.addToStackPane(stackPane);
        polygonView.addToStackPane(stackPane);
        interactionView.addToStackPane(stackPane);
        //the glass panel on top of everything else
        this.glassPanel = new Canvas(getWidth(), getHeight());
        stackPane.getChildren().add(glassPanel);

        initHandlers();
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
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());
                        currentAnnotation = SettingReader.getInstance().getDeletion();
                    } else {
                        currentAnnotationGraph = new LarsGraph(new SimpleGraph<>(GraphEdge.class), true);
                        currentAnnotationGraph.setAnnotationGraph(true);
                        hitByCurrentAnnotation.clear();
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
                    }

                    //delete annotation
                    if(deleteAnnotation){
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

                    double scaleFactor = (event.getDeltaY() > 0)
                            ? Constants.SCALE_DELTA
                            : 1 / Constants.SCALE_DELTA;

                    scrollPane.getContent().setScaleX(scrollPane.getContent().getScaleX() * scaleFactor);
                    scrollPane.getContent().setScaleY(scrollPane.getContent().getScaleY() * scaleFactor);

                    event.consume();
                }
        );

        //handles the user keyboard input
        glassPanel.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.I){ //switches the image (bin, ori)
                        if(graphImage.isSeeOrgImg()){
                            graphImage.setSeeOrgImg(false);
                            imageView.update();
                        } else {
                            graphImage.setSeeOrgImg(true);
                            imageView.update();
                        }
                    }

                    if(event.getCode() == KeyCode.G){ //shows/hides the graph
                        if(graphView.isShown()){
                            graphView.hide();
                        } else {
                            graphView.show();
                        }
                    }

                    if(event.getCode() == KeyCode.U){ //shows/hides the userInput
                        if(interactionView.isShown()){
                            interactionView.hide();
                        } else {
                            interactionView.show();
                        }
                    }

                    if(event.getCode() == KeyCode.P){ //shows/hides the polygonMap
                        if(polygonView.isShown()){
                            polygonView.hide();
                        } else {
                            polygonView.show();
                        }
                    }

                    //TODO just for testing
                    if(event.getCode() == KeyCode.Y && event.isControlDown()){
                        UndoCollector.getInstance().undo();
                        updateViews();
                    }
                    event.consume();
                }
        );
    }

    @FXML
    private void closeApplication(){
        Platform.exit();
    }

    @FXML
    private void changeAnnotationModus(){
        deleteAnnotation = !deleteAnnotation;
    }

    @FXML
    private void exportGraph(){
        //TODO
    }

    /*@FXML
    private void createNewAnnotationDialog() throws IOException{
        Dialog<Pair<String, Color>> dialog = new Dialog<>();
        dialog.setTitle("Create New Annotation");

        //buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        //create Lables and fields +picker
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField annotationName = new TextField();
        annotationName.setPromptText("Annotation Name");
        ColorPicker annotationColor = new ColorPicker();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(annotationName, 1, 0);
        grid.add(new Label("Color"), 0, 1);
        grid.add(annotationColor, 1,1);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // enable save if we have a name for the new annotation
        annotationName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        //focus on annotationName text field
        Platform.runLater(annotationName::requestFocus);

        dialog.setResultConverter(param -> {
            if(param == saveButtonType){
                return new Pair<>(annotationName.getText(), annotationColor.getValue());
            }
            return null;
        });

        Optional<Pair<String, Color>> result = dialog.showAndWait();

        result.ifPresent(stringColorPair ->
                createNewAnnotation(new AnnotationType(stringColorPair.getKey(), stringColorPair.getValue()))
        );

        dialog.show();
    }*/

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

    public void createNewAnnotation(AnnotationType type){
        //TODO write into the settings.xml file
        possibleAnnotations.add(new AnnotationType(type.getName(), type.getColor()));
        annotationBox.getItems().add(type.getName());
        //add it to the userinteraction view to see the scribble
        interactionView.addNewAnnotationType(type);
        //add it to the polygon view to make the annotations visible
        polygonView.addNewAnnotationType(type);
        //add a new annotationpolygontype to the map
        polygonMap.addNewAnnotation(type);
    }

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
}
