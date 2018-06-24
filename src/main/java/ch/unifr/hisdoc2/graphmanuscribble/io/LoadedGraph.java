package ch.unifr.hisdoc2.graphmanuscribble.io;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;

/**
 * Used to transport all the needed information from the importer into the system.
 */
public class LoadedGraph{
    private SimpleGraph<GraphVertex, GraphEdge> original;
    private List<LarsGraph> forest;
    private boolean connected;

    public SimpleGraph<GraphVertex, GraphEdge> getOriginal(){
        return original;
    }

    public void setOriginal(SimpleGraph<GraphVertex, GraphEdge> original){
        this.original = original;
    }

    public List<LarsGraph> getForest(){
        return forest;
    }

    public void setForest(List<LarsGraph> forest){
        this.forest = forest;
    }

    public boolean isConnected(){
        return connected;
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }
}
