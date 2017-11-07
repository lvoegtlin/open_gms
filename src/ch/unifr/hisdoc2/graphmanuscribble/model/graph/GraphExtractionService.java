package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splits the given graph into 2 graphs. By deleting one edge of the currentLarsGraphCollection it always creates a second graph.
 * This is because we are cutting edges from a tree.
 */
public class GraphExtractionService extends Service<LarsGraphCollection>{

    private LarsGraphCollection currentLarsGraphCollection;
    private AnnotationPolygonMap annotationPolygonMap;

    @Override
    protected Task<LarsGraphCollection> createTask(){
        return new Task<LarsGraphCollection>(){
            @Override
            protected LarsGraphCollection call() throws Exception{
                if(currentLarsGraphCollection == null || currentLarsGraphCollection.getGraphs().size() == 0){
                    return null;
                }

                UndirectedSubgraph<GraphVertex, GraphEdge> currentGraph =
                        (UndirectedSubgraph<GraphVertex, GraphEdge>) currentLarsGraphCollection.getEditedGraph().getGraph();

                ConnectivityInspector<GraphVertex, GraphEdge> cI = new ConnectivityInspector<>(currentGraph);
                //checks if the graph is still connected.
                if(cI.isGraphConnected()){
                    return null;
                }

                List<Set<GraphVertex>> subtrees = cI.connectedSets();

                int indexOfSmallTree = getIndexOfSmallTree(subtrees);

                //create a new graph
                UndirectedSubgraph<GraphVertex, GraphEdge> newGraph = new UndirectedSubgraph<>(currentGraph.getBase(),
                        subtrees.get(indexOfSmallTree), new HashSet<>());
                //fill in the edges
                for(GraphVertex v : subtrees.get(indexOfSmallTree)){
                    for(GraphEdge e : currentGraph.edgesOf(v)){
                        newGraph.addEdge(currentGraph.getEdgeSource(e), currentGraph.getEdgeTarget(e), e);
                    }
                }

                LarsGraphCollection newLarsGraphCollection = new LarsGraphCollection(new LarsGraph(newGraph));

                //deletes all the vertices in the bigger graph from the smaller subtree
                currentGraph.removeAllVertices(subtrees.get(indexOfSmallTree));

                //checking if one of the graphs is annotated. If yes we have to check witch one will contain witch
                //edge source after the deletion. If they just have a graphSource we will do that after the hull calc
                annotationCheckEdges(newLarsGraphCollection);

                System.out.println("number of nodes small graph: " + newGraph.vertexSet().size());
                System.out.println("number of nodes big graph: " + currentGraph.vertexSet().size());

                return newLarsGraphCollection;
            }
        };
    }

    /**
     * Gets the index of the smallest subtree in the set
     *
     * @param subtrees - the list of subtree vetices
     * @return - the list index
     */
    private int getIndexOfSmallTree(List<Set<GraphVertex>> subtrees){
        int indexOfSmallTree = 0;
        int nbrOfVertices = 0;
        for(int i = 0; i < subtrees.size(); i++){
            if(subtrees.get(i).size() < nbrOfVertices){
                nbrOfVertices = subtrees.get(i).size();
                indexOfSmallTree = i;
            }
        }
        return indexOfSmallTree;
    }

    /**
     * Checks for a given collection if it is annotated. If yes it transfers the source edges to the right graph.
     *
     * @param newLarsGraphCollection - where we want to check
     */
    private void annotationCheckEdges(LarsGraphCollection newLarsGraphCollection){
        AnnotationPolygon annotationPolygon = annotationPolygonMap.getGraphPolygonByLarsGraph(currentLarsGraphCollection, null);
        if(annotationPolygon != null){
            if(!annotationPolygon.getEdgeSources().isEmpty()){
                ArrayList<GraphEdge> sourcesToRemove = new ArrayList<>();
                for(GraphEdge graphEdge : annotationPolygon.getEdgeSources()){
                    if(!annotationPolygon.isEdgePartofPolygon(graphEdge)){
                        //remove the sources that are not longer part of this annotationPolygon
                        sourcesToRemove.add(graphEdge);
                    }
                }

                if(sourcesToRemove.size() == annotationPolygon.getEdgeSources().size()){
                    annotationPolygon.setLarsGraph(newLarsGraphCollection);
                } else {
                    annotationPolygon.removeEdgeSources(sourcesToRemove);
                    //add a new annotation polygon to the map
                    for(GraphEdge e : sourcesToRemove){
                        annotationPolygonMap.addNewScribble(newLarsGraphCollection,
                                null,
                                e,
                                annotationPolygonMap.getPolygonTypeByPolygon(annotationPolygon));
                    }
                }

                currentLarsGraphCollection.updateVertices();
                newLarsGraphCollection.updateVertices();
            }
        }
    }

    /**
     * Setting the current working graph. If the current graph is not set the service will not work.
     *
     * @param graph
     */
    public void setCurrentLarsGraphCollection(LarsGraphCollection graph){
        this.currentLarsGraphCollection = graph;
    }

    /**
     * Setting the current AnnotationPolygon. Required to avoid strange behavior.
     *
     * @param annotationPolygonMap
     */
    public void setAnnotationPolygonMap(AnnotationPolygonMap annotationPolygonMap){
        this.annotationPolygonMap = annotationPolygonMap;
    }
}
