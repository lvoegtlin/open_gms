package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.Undoable;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.*;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.shape.Polygon;
import org.apache.logging.log4j.Level;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and starts the services to first cut the graph and then calculate the concave hull of the two
 * newly created graphs.
 */
public class DeleteEdgeCommand implements Command, Undoable{

    /**
     * id of the command
     */
    private final int id;
    /**
     * The whole graph
     */
    private AngieMSTGraph graph;

    private Controller cnt;
    /**
     * that polygon that represents this command
     */
    private Polygon polygon;
    /**
     * Ref to the current running hull calculations
     */
    private List<ConcaveHullExtractionService> currentHullCalculations;
    /**
     * The map of all polygons
     */
    private AnnotationPolygonMap polygonMap;
    /**
     * Edge to delete
     */
    private GraphEdge edge;

    //undo and redo
    //undo
    private LarsGraphCollection oldLarsGraphCollection;
    private LarsGraphCollection newLarsGraphCollection;
    private List<PointHD2> oldHull;
    //redo
    private boolean redo = false;
    private boolean executed = false;

    public DeleteEdgeCommand(Controller cnt, GraphEdge edge, Polygon p, int id){
        this.cnt = cnt;
        this.graph = cnt.getGraph();
        this.polygon = p;
        this.currentHullCalculations = cnt.getCurrentHullCalculations();
        this.polygonMap = cnt.getPolygonMap();
        this.edge = edge;
        this.id = id;
    }

    @Override
    public void execute(){
        if(redo){
            graph.removeEdge(edge);
        }

        //get the corresponding larsgraph and create the extraction service
        LarsGraphCollection currentLarsGraphCollection = graph.getLarsGraphFromEdge(edge, false);
        GraphExtractionService gES = new GraphExtractionService();

        if(currentLarsGraphCollection == null){
            return;
        }

        //saving hull for undo
        oldHull = new ArrayList<>(currentLarsGraphCollection.getConcaveHull());

        //remove the edge
        currentLarsGraphCollection.removeEdge(edge);

        //set the larsgraph in the service
        gES.setCurrentLarsGraphCollection(currentLarsGraphCollection);
        gES.setOnSucceeded(event -> {
            calculateHullAfterDelete(edge, currentLarsGraphCollection, event);
            cnt.updatePolygonView();
        });

        //if the service fails it adds the edge again.
        gES.setOnFailed(event -> {
            graph.addEdge(edge);
            currentLarsGraphCollection.addEdge(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
            gES.getException().printStackTrace(System.err);
            cnt.updatePolygonView();
            Controller.logger.log(Level.getLevel("DeleteCommandFail - GraphExtractionService"), gES.getException().getMessage());
        });

        //start the service
        gES.start();
    }

    @Override
    public boolean canExecute(){
        return !executed && !(graph == null || cnt == null || currentHullCalculations == null || polygonMap == null || edge == null);
    }

    @Override
    public void undo(){
        reenterEdgeAndMergeGraphs();

        //set old hull
        oldLarsGraphCollection.getLarsGraphByVertex(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)).setConcaveHull(oldHull);
        oldLarsGraphCollection.update();

        //undo scribble
        cnt.getUserInput().undoRedoScribble(polygon, true);
    }

    @Override
    public void redo(){
        //the code below would implement the redo functionality but it is buggy.
        throw new UnsupportedOperationException("Not implemented yet!");
        /*//draw delete scribble
        cnt.getUserInput().undoRedoScribble(polygon, false);

        redo = true;

        execute();
        //update views -> done outside*/
    }

    @Override
    public int getId(){
        return id;
    }

    @Override
    public String getUndoRedoName(){
        return null;
    }

    /**
     * Includes the edge that was deleted by this command. It also removes the after deletion newly created LGC
     * form the subgraphs list and deletes the existing annotationPolygon.
     */
    private void reenterEdgeAndMergeGraphs(){
        //reenter deleted edge
        GraphVertex source = graph.getEdgeSource(edge);
        GraphVertex target = graph.getEdgeTarget(edge);
        LarsGraph lG = oldLarsGraphCollection.getLarsGraphByVertex(source, target);
        if(lG == null){
            return;
        }

        LarsGraph currentLg = newLarsGraphCollection.getLarsGraphByVertex(source, target);
        if(currentLg == null){
            currentLg = oldLarsGraphCollection.getLarsGraphByVertex(source, target);
        }

        Graphs.addGraph(lG.getGraph(), currentLg.getGraph());
        lG.addEdge(edge, source, target);
        graph.addEdge(edge);

        //transfer all graphs of the new one to the olf one and delete the new one in the subGraphList
        LarsGraph[] lgArray = new LarsGraph[newLarsGraphCollection.getGraphs().size()];
        List<LarsGraph> toDelete = newLarsGraphCollection.getGraphs();
        toDelete.remove(currentLg);
        lgArray = toDelete.toArray(lgArray);
        if(toDelete.size() > 0){
            oldLarsGraphCollection.addGraph(lgArray);
            newLarsGraphCollection.removeGraph(lgArray);
            newLarsGraphCollection.removeGraph(currentLg);
        }

        //delete the old annotationPolygon
        polygonMap.removeAnnotationPolygon(newLarsGraphCollection);
        graph.removeSubgraph(newLarsGraphCollection);
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

        //undo reference
        oldLarsGraphCollection = currentLarsGraphCollection;
        newLarsGraphCollection = larsGraphCollection;

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
                        cnt.updatePolygonView();
                        stateRefreshed[0] = true;
                    }
                });
        cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES2.stateProperty().isEqualTo(Worker.State.FAILED))
                .addListener((observable, oldValue, newValue) -> {
                    undo();
                    cnt.updatePolygonView();
                });
        cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES1.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
                .addListener((observable, oldValue, newValue) -> {
                    if(!stateRefreshed[0]){
                        doLGCGroupingByHull(currentLarsGraphCollection, larsGraphCollection);
                        cnt.updatePolygonView();
                        stateRefreshed[0] = true;
                    }
                });
        cHES2.stateProperty().isEqualTo(Worker.State.SUCCEEDED)
                .and(cHES1.stateProperty().isEqualTo(Worker.State.FAILED))
                .addListener((observable, oldValue, newValue) -> {
                    undo();
                    cnt.updatePolygonView();
                });

        cHES1.setOnFailed(event1 -> {
            cHES1.getException().printStackTrace(System.err);
            undo();
        });
        cHES2.setOnFailed(event1 ->{
            cHES2.getException().printStackTrace(System.err);
            undo();
        });

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
                LarsGraph[] lgArray = new LarsGraph[newlyCreatedLGC.getGraphs().size()];
                currentLGC.addGraph(newlyCreatedLGC.getGraphs().toArray(lgArray));
            }

        } else {
            annotation.parallelStream().forEach(anno -> {
                //because the big graph is already in the LGC we dont have to do anything
                if(newlyGraph.isIntersectingWith(anno)){
                    currentLGC.removeGraph(currentGraph);
                    currentLGC.addGraph(newlyGraph);
                    //the newly created LGC has just one graph
                    newlyCreatedLGC.removeGraph(newlyGraph);
                    newlyCreatedLGC.addGraph(currentGraph);
                }
            });
        }

        executed = true;
        //re-arrange all the graphs
        newlyCreatedLGC.update();
        currentLGC.update();
    }

    /**
     * Adds all the graphs which are connected by a annotationGraph into a given list.
     * The call can be executed in parallel.
     *
     * @param lG - The start LarsGraph
     * @param annotation - the annotation graphs
     * @param nonAnnotation - the nonAnnotation graphs
     * @param result - list that contains the connected graphs
     */
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
}
