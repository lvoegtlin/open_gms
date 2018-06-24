package ch.unifr.hisdoc2.graphmanuscribble.io;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;

import java.util.List;

/**
 * Used to transport all the needed information from the importer into the system.
 */
public class LoadedGraph{
    private Subgraph<GraphVertex, GraphEdge, SimpleWeightedGraph<GraphVertex, GraphEdge>> original;
    private List<LarsGraphCollection> forest;
    private boolean connected;

    public LoadedGraph(SimpleWeightedGraph<GraphVertex, GraphEdge> original, List<LarsGraphCollection> forest, boolean connected){
        this.original = new Subgraph<>(original, original.vertexSet(), original.edgeSet());
        this.forest = forest;
        this.connected = connected;
    }

    public Subgraph<GraphVertex, GraphEdge, SimpleWeightedGraph<GraphVertex, GraphEdge>> getOriginal(){
        return original;
    }

    public boolean isConnected(){
        return connected;
    }

    public List<LarsGraphCollection> getForest(){
        return forest;
    }
}
