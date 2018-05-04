package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.AngieMSTGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import javafx.scene.shape.Polygon;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds all the polygonMap scribbles to a graph. So it represents the current scribble as a graph and not as a polygonMap.
 * It also starts the concave hull extraction service.
 * If annotate is true it will delete the annotation of graph that the scribble is hitting
 */
public class AnnotateCommand implements Command{

    private boolean annotate;
    private LarsGraph currentAnnotationGraph;
    private ArrayList<LarsGraphCollection> hitByCurrentAnnotation;
    private AnnotationPolygonMap polygonMap;
    private AngieMSTGraph graph;
    private AnnotationType currentAnnotation;
    private Controller cnt;

    private boolean executed = false;

    /**
     *
     * @param cnt
     * @param annotate - true if we annotate something, false if we delete an annotation
     */
    public AnnotateCommand(Controller cnt, Polygon polygon, boolean annotate){
        this.annotate = annotate;
        this.currentAnnotationGraph = cnt.getCurrentAnnotationGraph();
        this.hitByCurrentAnnotation = cnt.getHitByCurrentAnnotation();
        this.polygonMap = cnt.getPolygonMap();
        this.graph = cnt.getGraph();
        this.currentAnnotation = cnt.getCurrentAnnotationType();
        this.cnt = cnt;
    }

    @Override
    public void execute(){
        GraphVertex last = null;

        List<PointHD2> vertices = TopologyUtil.reducePointsInDoubleList(cnt.getAnnotationPoints());

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
        // copie the current annotation graph for the undo

        //calc the hull of the newly created annotation graph
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

                    LarsGraph[] lgArray = new LarsGraph[larsGraphCollection.getGraphs().size()];
                    currentCollection.addGraph(larsGraphCollection.getGraphs().toArray(lgArray));
                    graph.removeSubgraph(larsGraphCollection);
                });

                currentCollection.update();

                polygonMap.addEdgeSourceToAnnoPolygonAndDeleteAnnoPolygons(hitByCurrentAnnotation,
                        currentCollection,
                        currentAnnotation);

                cnt.updatePolygonView();
            } else {
                hitByCurrentAnnotation.parallelStream().forEach(cnt::deleteAnnotation);
                cnt.deleteScribble(currentCollection);
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

        cnt.clearAnnotationPoints();
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
            ArrayList<LarsGraph> remove = new ArrayList<>();
            larsGraphCollection.getAnnotationGraphs().parallelStream().forEach(annoGraph -> {
                if(currentCollection.getEditedGraph().isIntersectingWith(annoGraph)){
                    //merge graphs
                    Graphs.addGraph(currentAnnotationGraph.getGraph(), annoGraph.getGraph());
                    // to avoid recalculating the hull we just make a union of them
                    currentAnnotationGraph.setConcaveHull(
                            TopologyUtil.getUnionOfTwoHulls(currentAnnotationGraph, annoGraph)
                    );

                    remove.add(annoGraph);
                }
            });

            LarsGraph[] lgArray = new LarsGraph[remove.size()];
            larsGraphCollection.removeGraph(remove.toArray(lgArray));
        }
    }

    @Override
    public boolean canExecute(){
        return !executed && (cnt != null &&
                (currentAnnotationGraph != null ||
                        hitByCurrentAnnotation != null ||
                        polygonMap != null ||
                        graph != null ||
                        currentAnnotation != null
                )
        );
    }
}
