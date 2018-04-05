package ch.unifr.hisdoc2.graphmanuscribble.controller;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
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
import javafx.concurrent.WorkerStateEvent;
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
        }

        userInput.addScribble(currentAnnotation, p, mouseDragged, false);
        interactionView.update();

        if(polygonMap.addNewScribble(larsGraphCollection,
                currentAnnotationGraph,
                currentAnnotation)){
            polygonView.update();
        }
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
        currentCollection.updateVertices();

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
        LarsGraphCollection currentLarsGraphCollection = graph.getLarsGraphFromEdge(edge, false);
        GraphExtractionService gES = new GraphExtractionService();

        if(currentLarsGraphCollection == null){
            return;
        }

        //remove the edge
        currentLarsGraphCollection.removeEdge(edge);

        //set the larsgraph in the service
        gES.setCurrentLarsGraphCollection(currentLarsGraphCollection);
        gES.setOnSucceeded(event -> {
            calculateHullAfterDelete(edge, currentLarsGraphCollection, event);
            polygonView.update();
        });

        //if the service fails it adds the edge again.
        gES.setOnFailed(event -> {
            graph.addEdges(edge);
            currentLarsGraphCollection.addEdge(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
            gES.getException().printStackTrace(System.err);
            polygonView.update();
            //TODO Log error
        });

        //start the service
        gES.start();
    }

    /**
     * This method creates hull calculation threads and manages all the preparation and also all the result handling.
     *
     * @param edge                       - the removed edge
     * @param currentLarsGraphCollection - the collection in which we deleted an edge
     * @param event                      - the event of the deletion service
     */
    private void calculateHullAfterDelete(GraphEdge edge, LarsGraphCollection currentLarsGraphCollection, WorkerStateEvent event){
        //the new undirected graph our service created
        LarsGraphCollection larsGraphCollection;
        //works because we know it returns a LarsGraphCollection
        if((larsGraphCollection = (LarsGraphCollection) event.getSource().getValue()) == null){
            return;
        }

        graph.addNewSubgraph(larsGraphCollection, false);

        //if there is a thread running we stop it because we start a new one
        currentHullCalculations.forEach(concaveHullExtractionService -> {
            if(concaveHullExtractionService.containsEdge(edge)){
                concaveHullExtractionService.cancel();
                currentHullCalculations.remove(concaveHullExtractionService);
            }
        });

        //creating two concaveHullExtractionServices
        ConcaveHullExtractionService cHES1 = new ConcaveHullExtractionService();
        ConcaveHullExtractionService cHES2 = new ConcaveHullExtractionService();

        //setting the corresponding larsgraphs
        cHES1.setLarsGraphCollection(currentLarsGraphCollection);
        cHES2.setLarsGraphCollection(larsGraphCollection);

        final boolean[] stateRefreshed = {false};
        //update the polygonMap view if both threads are finished
        //and check in which hull the sourceGraphs are
        cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
                .addListener((observable, oldValue, newValue) -> {
                    if(!stateRefreshed[0]){
                        doLGCGroupingByHull(currentLarsGraphCollection, larsGraphCollection);
                        polygonView.update();
                        stateRefreshed[0] = true;
                    }
                });
        cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES2.stateProperty().isEqualTo(Worker.State.FAILED))
                .addListener((observable, oldValue, newValue) -> {
                    //TODO undo
                    polygonView.update();
                });
        cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
                .addListener((observable, oldValue, newValue) -> {
                    if(!stateRefreshed[0]){
                        doLGCGroupingByHull(currentLarsGraphCollection, larsGraphCollection);
                        polygonView.update();
                        stateRefreshed[0] = true;
                    }
                });
        cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES1.stateProperty().isEqualTo(Worker.State.FAILED))
                .addListener((observable, oldValue, newValue) -> {
                    //TODO undo
                    polygonView.update();
                });

        cHES1.setOnFailed(event1 ->
                cHES1.getException().printStackTrace(System.err));
        //TODO undo
        cHES2.setOnFailed(event1 ->
                cHES2.getException().printStackTrace(System.err));
        //TODO undo

        cHES1.start();
        cHES2.start();
    }

    /**
     * Rearranges the graphs that are involved in a cutting. It checks if one or many source graphs are inside the hull of the
     * graphs of the currentLGC.
     *
     * @param currentLGC      - the to check graph
     * @param newlyCreatedLGC - the other graph
     */
    private void doLGCGroupingByHull(LarsGraphCollection currentLGC, LarsGraphCollection newlyCreatedLGC){
        //get the big and small graph
        LarsGraph currentGraph = currentLGC.getEditedGraph();
        LarsGraph newlyGraph = newlyCreatedLGC.getEditedGraph();
        AnnotationPolygon annotationPolygon = polygonMap.getGraphPolygonByLarsGraph(currentLGC, null);

        //if we dont have any annotation we dont have to regroup the graphs
        if(annotationPolygon == null){
            return;
        }

        List<LarsGraph> annotation = annotationPolygon.getSources();
        List<LarsGraph> nonAnnotation = annotationPolygon.getPolyGraph().getNonAnnotationGraphs();


        // check for both graphs if they hit a annotation
        // if one of them doesnt intersect with a annotationGraph
        //      the other has all the annotation graphs and so on all the connections to other graphs
        //      --> default (code we already have)
        // if both have a intersection with an annotationgraph
        //      go down the tree and check all connections
        // -> we get a second annotationPolygon
        if(currentGraph.isIntersectingWith(annotation) && newlyGraph.isIntersectingWith(annotation)){
            //TODO optimization (service?)
            // add the new graph so that all the graphs are in the list
            nonAnnotation.add(newlyGraph);
            List<LarsGraph> lGsForCurrent = new ArrayList<>();
            getIntersectionTree(currentGraph, annotation, nonAnnotation, lGsForCurrent);
            List<LarsGraph> lGsForNewly = new ArrayList<>();
            getIntersectionTree(newlyGraph, annotation, nonAnnotation, lGsForNewly);
            AnnotationType annotationType = polygonMap.getAnnotationTypeByPolygon(annotationPolygon);

            //change the LGCs
            currentLGC.setGraphs(lGsForCurrent);
            newlyCreatedLGC.setGraphs(lGsForNewly);

            // if there is no cycle
            if(!lGsForNewly.contains(currentGraph)){
                //make new scrible from newly
                polygonMap.addNewScribble(newlyCreatedLGC,
                        lGsForNewly.get(0),
                        annotationType);
                AnnotationPolygon newAnnoPoly = polygonMap.getGraphPolygonByLarsGraph(newlyCreatedLGC, annotationType);
                newAnnoPoly.addSources(newAnnoPoly.getSources());
            } else {
                currentLGC.addGraphs(newlyCreatedLGC.getGraphs());
            }

        } else {
            annotation.parallelStream().forEach(anno -> {
                //because the big graph is already in the LGC we dont have to do anything
                if(newlyGraph.isIntersectingWith(anno)){
                    currentLGC.removeGraph(currentGraph, true);
                    currentLGC.addGraph(newlyGraph);
                    //the newly created LGC has just one graph
                    newlyCreatedLGC.removeGraph(newlyGraph, true);
                    newlyCreatedLGC.addGraph(currentGraph);
                }
            });
        }

        //re-arrange all the graphs
        newlyCreatedLGC.update();
        currentLGC.update();
    }

    private void getIntersectionTree(LarsGraph lG, List<LarsGraph> annotation, List<LarsGraph> nonAnnotation, List<LarsGraph> result){
        List<LarsGraph> copyAnnotation = new ArrayList<>(annotation);
        List<LarsGraph> copyNonAnnotation = new ArrayList<>(nonAnnotation);
        boolean annotated = lG.isAnnotationGraph();

        //TODO refactoring
        if(annotated){
            nonAnnotation.parallelStream().forEach(source -> {
                if(lG.isIntersectingWith(source)){
                    result.add(source);
                    copyNonAnnotation.remove(source);
                    getIntersectionTree(source, annotation, copyNonAnnotation, result);
                }
            });
        } else {
            annotation.parallelStream().forEach(source -> {
                if(lG.isIntersectingWith(source)){
                    result.add(source);
                    copyAnnotation.remove(source);
                    getIntersectionTree(source, copyAnnotation, nonAnnotation, result);
                }
            });
        }
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

    private void deleteScribble(LarsGraphCollection lGC){
        //delete visual representation of the hit annotations
        userInput.deleteScribbles(lGC, currentAnnotation);
        //TODO delete the deletePolygon
        interactionView.update();
    }

    private void deleteAnnotation(LarsGraphCollection lGC){
        deleteScribble(lGC);

        //delete the annotationPolygon
        polygonMap.removeAnnotationPolygon(lGC);
        polygonView.update();

        //remove graphs from lgc
        lGC.removeGraphs(lGC.getAnnotationGraphs());

        //get all nonAnnotationgaphs and make new lgcs out of them
        List<LarsGraph> nonAnnoGraphs = lGC.getNonAnnotationGraphs();
        //TODO graphs should be annotatable after deletion
        for(int i = 0; i < nonAnnoGraphs.size() - 1; i++){
            graph.addNewSubgraph(new LarsGraphCollection(nonAnnoGraphs.get(i), nonAnnoGraphs.get(i).getConcaveHull()), true);
            lGC.removeGraph(nonAnnoGraphs.get(i), false);
        }

        lGC.update();
    }
}
