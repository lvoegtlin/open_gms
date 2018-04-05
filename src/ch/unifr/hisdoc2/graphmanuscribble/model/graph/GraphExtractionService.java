package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splits the given graph into 2 graphs. By deleting one edge of the currentLarsGraphCollection it always creates a second graph.
 * This is because we are cutting edges from a tree.
 */
public class GraphExtractionService extends Service<LarsGraphCollection>{

    private LarsGraphCollection currentLarsGraphCollection;

    /**
     *
     * @return - The collection which contains sourceEdge or the smaller one
     */
    @Override
    protected Task<LarsGraphCollection> createTask(){
        return new Task<LarsGraphCollection>(){
            @Override
            protected LarsGraphCollection call() throws Exception{
                UndirectedSubgraph<GraphVertex, GraphEdge> subgraphGraph;
                LarsGraphCollection newLarsGraphCollection;

                if(currentLarsGraphCollection == null || currentLarsGraphCollection.getGraphs().size() == 0){
                    return null;
                }

                subgraphGraph = (UndirectedSubgraph<GraphVertex, GraphEdge>) currentLarsGraphCollection.getEditedGraph().getGraph();

                ConnectivityInspector<GraphVertex, GraphEdge> cI = new ConnectivityInspector<>(subgraphGraph);
                //checks if the graph is still connected.
                if(cI.isGraphConnected()){
                    return null;
                }

                //TODO delete small graph from big graph
                List<Set<GraphVertex>> subtrees = cI.connectedSets();

                int indexOfSmallTree = getIndexOfSmallTree(subtrees);
                Set<GraphVertex> smallGraphVertices = subtrees.get(indexOfSmallTree);
                UndirectedSubgraph<GraphVertex, GraphEdge> smallGraph;
                smallGraph = createGraphFromVertices(subgraphGraph, smallGraphVertices);

                //delete the small graph from the big one
                subgraphGraph.removeAllVertices(smallGraphVertices);

                newLarsGraphCollection = new LarsGraphCollection(new LarsGraph(smallGraph));

                currentLarsGraphCollection.update();
                newLarsGraphCollection.update();

                System.out.println("number of nodes small graph: " + smallGraph.vertexSet().size());
                System.out.println("number of nodes big graph: " + subgraphGraph.vertexSet().size());

                return newLarsGraphCollection;
            }
        };
    }

    /**
     * Creates a graph based on the vertices from subtrees and then takes the edges from subgraphGraph that are between
     * these edges
     *
     * @param subgraphGraph - original subtree we are cutting
     * @param subtrees - the 2 vertex sets of subtrees
     * @return a new subGraph referencing on already existing vertices and edges
     */
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
            if(subtrees.get(i).size() < nbrOfVertices || nbrOfVertices == 0){
                nbrOfVertices = subtrees.get(i).size();
                indexOfSmallTree = i;
            }
        }
        return indexOfSmallTree;
    }

    /**
     * Setting the current working graph. If the current graph is not set the service will not work.
     *
     * @param graph
     */
    public void setCurrentLarsGraphCollection(LarsGraphCollection graph){
        this.currentLarsGraphCollection = graph;
    }
}
