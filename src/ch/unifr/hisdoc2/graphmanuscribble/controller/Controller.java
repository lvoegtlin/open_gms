package ch.unifr.hisdoc2.graphmanuscribble.controller;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.*;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import ch.unifr.hisdoc2.graphmanuscribble.model.image.GraphImage;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.scribble.UserInput;
import ch.unifr.hisdoc2.graphmanuscribble.view.GraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.ImageGraphView;
import ch.unifr.hisdoc2.graphmanuscribble.view.PolygonView;
import ch.unifr.hisdoc2.graphmanuscribble.view.UserInteractionView;
import javafx.concurrent.Worker;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by larsvoegtlin on 16.01.17.
 */
public class Controller{

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
    private ScrollPane scrollPane;
    private Canvas glassPanel;
    private boolean mouseDragged = false;

    //colors
    private AnnotationType currentAnnotation;

    //current values
    private long lastTime;
    private ArrayList<Double> deletePoints = new ArrayList<>();
    private ArrayList<Double> annotationPoints = new ArrayList<>();
    private LarsGraph currentAnnotationGraph;
    private ArrayList<LarsGraphCollection> hitByCurrentAnnotation = new ArrayList<>();

    //concurrency variables
    private List<ConcaveHullExtractionService> currentHullCalculations = new ArrayList<>();

    public Controller(AngieMSTGraph graph, AnnotationPolygonMap polygonMap, GraphImage img, UserInput userInput,
                      ScrollPane scrollPane){
        this.scrollPane = scrollPane;

        this.width = img.getWidth();
        this.height = img.getHeight();

        this.graph = graph;
        this.polygonMap = polygonMap;
        this.graphImage = img;
        this.userInput = userInput;
        //chooses the first annotation as staring type
        this.currentAnnotation = SettingReader.getInstance().getAnnotations().get(0);

        graphView = new GraphView(graph, this, SettingReader.getInstance().getGraphColor());
        polygonView = new PolygonView(polygonMap, this);
        interactionView = new UserInteractionView(userInput, this);
        imageView = new ImageGraphView(graphImage, this);

        StackPane pane = (StackPane) scrollPane.getContent();
        imageView.addToStackPane(pane);
        graphView.addToStackPane(pane);
        polygonView.addToStackPane(pane);
        interactionView.addToStackPane(pane);
        //the glass panel on top of everything else
        glassPanel = new Canvas(getWidth(), getHeight());
        pane.getChildren().add(glassPanel);

        //Event handlers
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
                    if(event.isControlDown()){
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());
                        currentAnnotation = SettingReader.getInstance().getDeletion();
                    }

                    if(event.isAltDown()){
                        currentAnnotationGraph = new LarsGraph(new SimpleGraph<>(GraphEdge.class), true);
                        currentAnnotationGraph.setAnnotationGraph(true);
                        currentAnnotationGraph.setAnnotated(true);
                        hitByCurrentAnnotation.clear();
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());

                        //TODO just for testing
                        currentAnnotation = SettingReader.getInstance().getAnnotations().get(0);
                    }
                }
        );

        //handels the input during the drag
        glassPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                    mouseDragged = true;
                    if(event.isControlDown()){
                        if((System.currentTimeMillis() - lastTime < Constants.REFRESH_TIME)){
                            deletePoints.add(event.getX());
                            deletePoints.add(event.getY());
                        } else {
                            deleteEdges(getPolygonFromEventPoints(event, true));
                            lastTime = System.currentTimeMillis();
                        }
                        event.consume();
                    }

                    if(event.isAltDown()){
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
                    if(event.isControlDown()){
                        deleteEdges(getPolygonFromEventPoints(event, true));
                        lastTime = System.currentTimeMillis();
                    }

                    if(event.isAltDown()){
                        processPolygons(getPolygonFromEventPoints(event, false));
                        lastTime = System.currentTimeMillis();
                        addVertexAndEdgesToGraph();
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
        scrollPane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
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

                    event.consume();
                }
        );
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
        //if we hit an edge
        GraphEdge sourceEdge = graph.getIntersectionFromScribble(p);
        if(larsGraphCollection != null){
            //add it to the list of hit graphs with the current annotation
            addHitGraphByCurrentAnnotation(larsGraphCollection);
        }

        userInput.addScribble(currentAnnotation, p, mouseDragged, false);
        interactionView.update();

        if(polygonMap.addNewScribble(larsGraphCollection,
                currentAnnotationGraph,
                sourceEdge,
                polygonMap.getPolygonByAnnotationType(currentAnnotation))){
            polygonView.update();
        }
    }

    /**
     * Adds a graph that was hit by the current annotation scribble to the list of hit grahs
     *
     * @param lGC - the hit graph
     */
    private void addHitGraphByCurrentAnnotation(LarsGraphCollection lGC){
        if(!hitByCurrentAnnotation.contains(lGC)){
            hitByCurrentAnnotation.add(lGC);
        }
    }

    /**
     * Adds all the polygonMap scribbles to a graph. So it represents the current scribble as a graph and not as a polygonMap.
     * It also starts the concave hull extraction service.
     */
    private void addVertexAndEdgesToGraph(){
        GraphVertex last = null;

        List<PointHD2> vertices = TopologyUtil.reducePointsInDoubleList(annotationPoints);

        for(PointHD2 p : vertices){
            GraphVertex v = new GraphVertex(p.getX(), p.getY());
            //when mouse released it creates a point at the same place as the last
            if(v.equals(last)){
                continue;
            }

            if(currentAnnotationGraph.getGraph().vertexSet().isEmpty()){
                currentAnnotationGraph.getGraph().addVertex(v);
            } else {
                currentAnnotationGraph.getGraph().addVertex(v);
                currentAnnotationGraph.getGraph().addEdge(last, v);
            }

            last = v;
        }

        LarsGraphCollection currentCollection = new LarsGraphCollection(currentAnnotationGraph);
        currentCollection.updateVertices();

        //calc the hull of the newly created graph
        ConcaveHullExtractionService cHES = new ConcaveHullExtractionService();
        cHES.setOnSucceeded(event -> {
            //adding the annotation graph as scribble to the annotationPolygons
            if(polygonMap.addNewScribble(currentCollection,
                    currentAnnotationGraph,
                    null, //we dont have an edge source in this case
                    polygonMap.getPolygonByAnnotationType(currentAnnotation))){

                //adding all the hulls of the hit graphs to the list
                hitByCurrentAnnotation.forEach(larsGraphCollection -> {
                    if(larsGraphCollection != null){
                        currentCollection.addGraphs(larsGraphCollection.getGraphs());
                        graph.removeSubgraph(larsGraphCollection);
                    }
                });

                currentCollection.updateHull();
                polygonMap.addEdgeSourceToAnnoPolygonAndDeleteAnnoPolygons(hitByCurrentAnnotation,
                        currentCollection,
                        currentAnnotation);
                polygonView.update();
            }
        });

        //set the variable
        cHES.setLarsGraphCollection(currentCollection);
        //start the service
        cHES.start();

        graph.addNewSubgraph(currentCollection);
        annotationPoints.clear();
    }

    /**
     * Deletes all edges the polygonMap p is intersection with. It also starts the thread that calculates the concave
     * hull for the two newly created graphs.
     *
     * @param p - Polygon scribble
     */
    private void deleteEdges(Polygon p){
        GraphEdge edge = graph.getIntersectionFromScribble(p);

        //if we hit an edge
        if(edge != null){
            //deletes edge in the original graph (labels deleted)
            graph.deleteEdges(edge);

            deleteService(edge);
        }

        //add the scribble to the user input scribbles
        userInput.addScribble(currentAnnotation, p, mouseDragged, true);

        //update the graph and also the interaction view
        interactionView.update();
        graphView.update();
    }

    /**
     * Creates and starts the services to first cut the graph and then calculate the concave hull of the two
     * newly created graphs.
     *
     * @param edge - that cuts the graph
     */
    private synchronized void deleteService(GraphEdge edge){
        //get the corresponding larsgraph and create the extraction service
        LarsGraphCollection currentLarsGraphCollection = graph.getLarsGraphFromEdge(edge);
        GraphExtractionService gES = new GraphExtractionService();

        if(currentLarsGraphCollection == null){
            return;
        }

        //remove the edge
        currentLarsGraphCollection.removeEdge(edge);

        //set the larsgraph in the service
        gES.setCurrentLarsGraphCollection(currentLarsGraphCollection);
        gES.setAnnotationPolygonMap(polygonMap);
        gES.setOnSucceeded(event -> {
            //the new undirected graph our service created
            LarsGraphCollection larsGraphCollection;
            //works because we know it returns a LarsGraphCollection
            if((larsGraphCollection = (LarsGraphCollection) event.getSource().getValue()) != null){
                graph.addNewSubgraph(larsGraphCollection);

                //if there is a thread running we stop it because we start a new one
                currentHullCalculations.forEach(concaveHullExtractionService -> {
                    if(concaveHullExtractionService.containsEdge(edge)){
                        concaveHullExtractionService.cancel();
                        currentHullCalculations.remove(concaveHullExtractionService);
                    }
                });

                //creating two concaveHullExtractionServices
                ConcaveHullExtractionService cHES1 = new ConcaveHullExtractionService();
                cHES1.setCheckEdited(true);
                ConcaveHullExtractionService cHES2 = new ConcaveHullExtractionService();

                //setting the corresponding larsgraphs
                cHES1.setLarsGraphCollection(currentLarsGraphCollection);
                cHES2.setLarsGraphCollection(larsGraphCollection);

                //update the polygonMap view if both threads are finished
                cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                        .and(cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
                        .addListener((observable, oldValue, newValue) -> {
                            polygonView.update();
                        });
                cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                        .and(cHES2.stateProperty().isEqualTo(Worker.State.FAILED))
                        .addListener((observable, oldValue, newValue) -> {
                            polygonView.update();
                        });
                cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                        .and(cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
                        .addListener((observable, oldValue, newValue) -> {
                            polygonView.update();
                        });
                cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                        .and(cHES1.stateProperty().isEqualTo(Worker.State.FAILED))
                        .addListener((observable, oldValue, newValue) -> {
                            polygonView.update();
                        });

                cHES1.start();
                cHES2.start();
            }
        });

        //if the service fails it adds the edge again.
        gES.setOnFailed(event -> {
            graph.addEdges(edge);
            currentLarsGraphCollection.addEdge(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
            //TODO Log error
        });

        //start the service
        gES.start();
    }

    private void reorganizeLGCs(LarsGraphCollection currentLarsGraphCollection, LarsGraphCollection larsGraphCollection){
        //check in which hulls they are
        for(LarsGraph annotation : currentLarsGraphCollection.getAnnotationGraphs()){
            for(LarsGraph lG : currentLarsGraphCollection.getNonAnnotationGraphs()){
                if(TopologyUtil.isPolygonInPolygon(lG.getConcaveHull(), annotation.asPolygon())){
                    currentLarsGraphCollection.removeGraph(lG);
                } else {
                    larsGraphCollection.addGraph(lG);
                }
            }

            for(LarsGraph lG : larsGraphCollection.getNonAnnotationGraphs()){
                if(TopologyUtil.isPolygonInPolygon(lG.getConcaveHull(), annotation.asPolygon())){
                    currentLarsGraphCollection.addGraph(lG);
                } else {
                    larsGraphCollection.removeGraph(lG);
                }
            }
        }
        //re-arrange all the graphs
        larsGraphCollection.update();
        currentLarsGraphCollection.update();
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
    public AnnotationType getCurrentAnnotationColor(){
        return currentAnnotation;
    }
}
