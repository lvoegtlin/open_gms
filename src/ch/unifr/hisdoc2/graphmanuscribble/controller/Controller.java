package ch.unifr.hisdoc2.graphmanuscribble.controller;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.helper.commands.DeleteEdgeCommand;
import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.UndoCollector;
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
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import org.jgrapht.Graphs;
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
    private List<AnnotationType> possibleAnnotations;
    //TODO just for testing
    private int annotation = 0;

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
        this.possibleAnnotations = SettingReader.getInstance().getAnnotations();
        this.currentAnnotation = possibleAnnotations.get(0);

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
                    //delete
                    if(event.isControlDown()){
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());
                        currentAnnotation = SettingReader.getInstance().getDeletion();
                    }

                    //annotate
                    if(event.isAltDown() || event.isShiftDown()){
                        currentAnnotationGraph = new LarsGraph(new SimpleGraph<>(GraphEdge.class), true);
                        currentAnnotationGraph.setAnnotationGraph(true);
                        hitByCurrentAnnotation.clear();
                        deletePoints.add(event.getX());
                        deletePoints.add(event.getY());

                        //TODO just for testing
                        currentAnnotation = possibleAnnotations.get(annotation);
                    }
                }
        );

        //handels the input during the drag
        glassPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                    mouseDragged = true;
                    //delete
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

                    //annotate
                    if(event.isAltDown() || event.isShiftDown()){
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
                    //annotate
                    if(event.isAltDown()){
                        processPolygons(getPolygonFromEventPoints(event, false));
                        lastTime = System.currentTimeMillis();
                        addVertexAndEdgesToGraph(true);
                    }

                    //delete annotation
                    if(event.isShiftDown()){
                        processPolygons(getPolygonFromEventPoints(event, false));
                        lastTime = System.currentTimeMillis();
                        addVertexAndEdgesToGraph(false);
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

                    //TODO just for testing
                    if(event.getCode() == KeyCode.Y && event.isControlDown()){
                        UndoCollector.getInstance().undo();
                        updateViews();
                    }

                    //TODO just for testing
                    if(event.getCode() == KeyCode.Z && event.isControlDown()){
                        UndoCollector.getInstance().redo();
                        updateViews();
                    }

                    //TODO just for testing
                    if(event.getCode() == KeyCode.C){ //change annotations
                        if(annotation == 0){
                            annotation = 1;
                        } else {
                            annotation = 0;
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
     * Adds all the polygonMap scribbles to a graph. So it represents the current scribble as a graph and not as a polygonMap.
     * It also starts the concave hull extraction service.
     *
     * @param annotate - true if annotation; false if delete annotation
     */
    private void addVertexAndEdgesToGraph(boolean annotate){
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

        //calc the hull of the newly created graph
        ConcaveHullExtractionService cHES = new ConcaveHullExtractionService();
        cHES.setOnSucceeded(event -> {
            if(annotate){
                //adding the annotation graph as scribble to the annotationPolygons
                if(!polygonMap.addNewScribble(currentCollection,
                        currentAnnotationGraph,
                        currentAnnotation)){
                    return;
                }

                //adding all the hulls of the hit graphs to the list
                hitByCurrentAnnotation.parallelStream().forEach(larsGraphCollection -> {
                    checkAndMergeAnnoGraphs(currentCollection, larsGraphCollection);

                    currentCollection.addGraphs(larsGraphCollection.getGraphs());
                    graph.removeSubgraph(larsGraphCollection);
                });

                currentCollection.update();

                polygonMap.addEdgeSourceToAnnoPolygonAndDeleteAnnoPolygons(hitByCurrentAnnotation,
                        currentCollection,
                        currentAnnotation);
                polygonView.update();
            } else {
                hitByCurrentAnnotation.parallelStream().forEach(this::deleteAnnotation);
                deleteScribble(currentCollection);
            }

        });

        cHES.setOnFailed(event -> cHES.getException().printStackTrace(System.err));

        //set the variable
        cHES.setLarsGraphCollection(currentCollection);
        //start the service
        cHES.start();

        if(annotate){
            graph.addNewSubgraph(currentCollection, true);
        }
        annotationPoints.clear();
    }

    /**
     * This method Get two LGC where one of them is just an annotation graph. It checks if the annotationLGC
     * hits a annotation graph of the other LGC. If that is the case it merges this two graphs into one annotation graph.
     * If the not-annotationLGC is not annotated this method does nothing.
     *
     * @param currentCollection   - The annotionLGC (LGC has just one graph that is a annotation graph)
     * @param larsGraphCollection - The LGC to check intersection with
     */
    private void checkAndMergeAnnoGraphs(LarsGraphCollection currentCollection, LarsGraphCollection larsGraphCollection){
        if(larsGraphCollection.isAnnotated()){
            //currentCollection is a LGC with just one graph, the newly created annotation graph
            ArrayList<LarsGraph> graphsToRemove = new ArrayList<>();
            larsGraphCollection.getAnnotationGraphs().parallelStream().forEach(annoGraph -> {
                if(currentCollection.getEditedGraph().isIntersectingWith(annoGraph)){
                    //merge graphs
                    Graphs.addGraph(currentAnnotationGraph.getGraph(), annoGraph.getGraph());
                    // to avoid recalculating the hull we just make a union of them
                    currentAnnotationGraph.setConcaveHull(
                            TopologyUtil.getUnionOfTwoHulls(currentAnnotationGraph, annoGraph)
                    );

                    graphsToRemove.add(annoGraph);
                }
            });

            larsGraphCollection.removeGraphs(graphsToRemove);
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

            DeleteEdgeCommand cmd = new DeleteEdgeCommand(graph, polygonView, userInput, currentHullCalculations, polygonMap, edge);

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

    /**
     * Deletes the visual representation of a LGC. These are the annotation scribble NOT the graphs itself.
     *
     * @param lGC - the lgc to delete the annotation scribbles
     */
    private void deleteScribble(LarsGraphCollection lGC){
        //delete visual representation of the hit annotations
        userInput.deleteScribbles(lGC, currentAnnotation);
        interactionView.update();
    }

    /**
     * Deletes the annotation of a annotated LGC. This means that all the annotation scribble gets deleted
     * and the WHOLE LGC gets unconnected.
     *
     * @param lGC - Annotated LGC to delete annotation
     */
    private void deleteAnnotation(LarsGraphCollection lGC){
        deleteScribble(lGC);

        //delete the annotationPolygon
        polygonMap.removeAnnotationPolygon(lGC);
        polygonView.update();

        //remove graphs from lgc
        lGC.removeGraphs(lGC.getAnnotationGraphs());

        //get all nonAnnotationgaphs and make new lgcs out of them
        List<LarsGraph> nonAnnoGraphs = lGC.getNonAnnotationGraphs();
        int annoLength = nonAnnoGraphs.size();
        for(int i = 0; i < annoLength - 1; i++){
            graph.addNewSubgraph(new LarsGraphCollection(nonAnnoGraphs.get(i), nonAnnoGraphs.get(i).getConcaveHull()), false);
            lGC.removeGraph(nonAnnoGraphs.get(i));
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
        polygonView.update();
        interactionView.update();
        graphView.update();
    }
}
