package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import javafx.concurrent.Task;

import java.util.List;

/**
 * Unites a list of LarsGraphs to one LarsGraph
 */
public class GraphUniteService{

    private List<LarsGraph> larsGraph;

    //TODO
    protected Task<Void> createTask(){
        return new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                int x = 0;
                return null;
            }
        };
    }

    /**
     * Sets the value for the LarsGraph object from which the service has to calculate the concave hull.
     *
     * @param larsGraph - larsGraph to get the hull from
     */
    public void setLarsGraphs(List<LarsGraph> larsGraph){
        this.larsGraph = larsGraph;
    }
}
