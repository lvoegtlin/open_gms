package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import javafx.concurrent.Task;

import java.util.List;

/**
 * Unites a list of LarsGraphs to one LarsGraphCollection
 */
public class GraphUniteService{

    private List<LarsGraphCollection> larsGraphCollection;

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
     * Sets the value for the LarsGraphCollection object from which the service has to calculate the concave hull.
     *
     * @param larsGraphCollection - larsGraphCollection to get the hull from
     */
    public void setLarsGraphs(List<LarsGraphCollection> larsGraphCollection){
        this.larsGraphCollection = larsGraphCollection;
    }
}
