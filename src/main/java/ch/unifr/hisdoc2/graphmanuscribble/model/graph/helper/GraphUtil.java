package ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.HashSet;
import java.util.Set;

/**
 * several Utility methods for graphs
 */
public class GraphUtil{

    /**
     * comparator for two edges
     *
     * @param graph graph the edges are from
     * @param o1    edge 1
     * @param o2    edge 2
     * @return -1 if o1 > o2, 0 if o1==02, 1 otherwise
     */
    public static <N extends PointHD2, E extends GraphEdge, SG extends Subgraph<N,E,SimpleWeightedGraph<N, E>>> int compareEdgeWeights(final SG graph, E o1, E o2) {
        return Double.compare(graph.getEdgeWeight(o2), graph.getEdgeWeight(o1));
    }

    /**
     * Creates a graph based on the vertices from subtrees and then takes the edges from subgraphGraph that are between
     * these edges
     *
     * @param subgraphGraph - original subtree we are cutting
     * @param subtrees - the 2 vertex sets of subtrees
     * @return a new subGraph referencing on already existing vertices and edges
     */
    public static UndirectedSubgraph<GraphVertex, GraphEdge> createGraphFromVertices(
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
}
