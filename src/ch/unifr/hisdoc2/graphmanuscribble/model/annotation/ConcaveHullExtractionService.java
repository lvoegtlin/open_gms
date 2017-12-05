package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calculates for a given larsGraphCollection in the LarsGraphCollection object the concave hull.
 */
public class ConcaveHullExtractionService extends Service<Void>{

    private LarsGraphCollection larsGraphCollection;
    private boolean checkEdited = false;

    @Override
    protected Task<Void> createTask(){
        return new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                if(checkEdited){
                    LarsGraph editGraph = larsGraphCollection.getEditedGraph();
                    if(editGraph == null){
                        larsGraphCollection.getGraphs().forEach(larsGraph ->
                                larsGraph.setConcaveHull(calculateConcaveHull(larsGraph.getGraph().vertexSet()))
                        );
                    } else {
                        editGraph.setConcaveHull(calculateConcaveHull(editGraph.getGraph().vertexSet()));
                    }
                } else {
                    larsGraphCollection.getGraphs().forEach(larsGraph ->
                            larsGraph.setConcaveHull(calculateConcaveHull(larsGraph.getGraph().vertexSet()))
                    );
                }
                larsGraphCollection.updateHull();
                return null;
            }
        };
    }

    /**
     * Sets the value for the LarsGraphCollection object from which the service has to calculate the concave hull.
     *
     * @param larsGraphCollection - larsGraphCollection to get the hull from
     */
    public void setLarsGraphCollection(LarsGraphCollection larsGraphCollection){
        this.larsGraphCollection = larsGraphCollection;
    }

    /**
     * When this is true, that service just calculates the hull for the edited graph of the
     * LarsGraphCollection
     *
     * @param checkEdited
     */
    public void setCheckEdited(boolean checkEdited){
        this.checkEdited = checkEdited;
    }

    /**
     * Tells you if the graph of the used LarsGraphCollection is containing the given edge. If so the method returns
     * true else it returns false.
     *
     * @param edge - checking the graph on containment
     * @return - if the graph contains the given edge
     */
    public boolean containsEdge(GraphEdge edge){
        return larsGraphCollection.containsEdge(edge);
    }

    /**
     * Calculates the concave hull of a given points cloud (vertices of the larsGraphCollection).
     *
     * @param vertices - points cloud
     * @return - concave hull of the point cloud
     */
    private List<PointHD2> calculateConcaveHull(Set<GraphVertex> vertices){
        return TopologyUtil.pointListToConcaveHull(new ArrayList<>(vertices), Constants.CONCAVE_TIGHTNESS);
    }

}
