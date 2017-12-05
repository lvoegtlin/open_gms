package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;
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
                UndirectedSubgraph<GraphVertex, GraphEdge> subgraphGraph;
                Boolean choice;
                LarsGraphCollection newLarsGraphCollection = null;

                if(currentLarsGraphCollection == null || currentLarsGraphCollection.getGraphs().size() == 0){
                    return null;
                }

                subgraphGraph = (UndirectedSubgraph<GraphVertex, GraphEdge>) currentLarsGraphCollection.getEditedGraph().getGraph();
                currentLarsGraphCollection.deleteEditedGraph();

                ConnectivityInspector<GraphVertex, GraphEdge> cI = new ConnectivityInspector<>(subgraphGraph);
                //checks if the graph is still connected.
                if(cI.isGraphConnected()){
                    return null;
                }

                List<Set<GraphVertex>> subtrees = cI.connectedSets();

                int indexOfSmallTree = getIndexOfSmallTree(subtrees);
                UndirectedSubgraph<GraphVertex, GraphEdge> smallGraph;
                smallGraph = createGraphFromVertices(subgraphGraph, subtrees.get(indexOfSmallTree));
                UndirectedSubgraph<GraphVertex, GraphEdge> bigGraph;
                bigGraph = createGraphFromVertices(subgraphGraph, subtrees.get((subtrees.size() -1) -indexOfSmallTree));

                //checking if one of the graphs is annotated. If yes we have to check witch one will contain witch
                //edge source after the deletion. If they just have a graphSource we will do that after the hull calc
                if((choice = annotationCheckEdges(smallGraph, bigGraph)) != null){
                    if(choice){
                        newLarsGraphCollection = new LarsGraphCollection(new LarsGraph(bigGraph));
                    } else {
                        newLarsGraphCollection = new LarsGraphCollection(new LarsGraph(smallGraph));
                    }
                }

                System.out.println("number of nodes small graph: " + smallGraph.vertexSet().size());
                System.out.println("number of nodes big graph: " + subgraphGraph.vertexSet().size());

                return newLarsGraphCollection;
            }
        };
    }

    private UndirectedSubgraph<GraphVertex, GraphEdge> createGraphFromVertices(
            UndirectedSubgraph<GraphVertex, GraphEdge> subgraphGraph,
            Set<GraphVertex> subtrees){
        //create a new graph
        UndirectedSubgraph<GraphVertex, GraphEdge> newGraph = new UndirectedSubgraph<>(subgraphGraph.getBase(),
                subtrees, new HashSet<>());
        //fill in the edges
        for(GraphVertex v : subtrees){
            for(GraphEdge e : subgraphGraph.edgesOf(v)){
                newGraph.addEdge(subgraphGraph.getEdgeSource(e), subgraphGraph.getEdgeTarget(e), e);
            }
        }
        return newGraph;
    }

    /**
     * Gets the index of the smallest subtree in the set
     *
     * @param subtrees - the list of subtree vertices
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
     * @param smallGraph - the smaller of the two graphs
     * @param bigGraph - the bigger of the two graphs
     */
    private Boolean annotationCheckEdges(UndirectedSubgraph<GraphVertex, GraphEdge> smallGraph,
                                      UndirectedSubgraph<GraphVertex, GraphEdge> bigGraph){
        AnnotationPolygon annotationPolygon = annotationPolygonMap.getGraphPolygonByLarsGraph(currentLarsGraphCollection, null);

        ArrayList<GraphEdge> sourcesToRemove = new ArrayList<>();
        ArrayList<LarsGraph> graphsToRemove = new ArrayList<>();
        boolean result = false;

        if(annotationPolygon == null || annotationPolygon.getEdgeSources().isEmpty()){
            return null;
        }

        for(GraphEdge edge : annotationPolygon.getEdgeSources()){
            if(smallGraph.containsEdge(edge)){
                currentLarsGraphCollection.addGraph(new LarsGraph(smallGraph));
                result = true;
            }
            if(bigGraph.containsEdge(edge)){
                currentLarsGraphCollection.addGraph(new LarsGraph(bigGraph));
                result = false;
            }
        }

        currentLarsGraphCollection.updateVertices();

        return result;

        /*
        for(GraphEdge graphEdge : annotationPolygon.getEdgeSources()){
            if(!annotationPolygon.isEdgePartofPolygon(graphEdge)){
                //remove the sources that are not longer part of this annotationPolygon
                sourcesToRemove.add(graphEdge);
            }
        }

        if(sourcesToRemove.size() == annotationPolygon.getEdgeSources().size()){
            currentLarsGraphCollection.setAnnotated(false);
            annotationPolygon.setLarsGraph(newLarsGraphCollection);
        } else {
            annotationPolygon.removeEdgeSources(sourcesToRemove);
            if(!sourceInCurrentGraph){
                for(LarsGraph lG : currentLarsGraphCollection.getGraphs()){
                    if(lG.getGraph() != currentGraph){
                        graphsToRemove.add(lG);
                        newLarsGraphCollection.addGraph(lG);
                    }
                }

                annotationPolygon.setLarsGraph(newLarsGraphCollection);
                currentLarsGraphCollection.removeGraphs(graphsToRemove);
            }
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
        */
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
