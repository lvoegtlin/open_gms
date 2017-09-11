package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import com.vividsolutions.jts.geom.LinearRing;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calculates for a given larsGraph in the LarsGraph object the concave hull.
 */
public class ConcaveHullExtractionService extends Service<Void>{

    private LarsGraph larsGraph;

    @Override
    protected Task<Void> createTask(){
        return new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                larsGraph.setConcaveHull(calculateConcaveHull(larsGraph.getGraph().vertexSet()));
                return null;
            }
        };
    }

    /**
     * Sets the value for the LarsGraph object from which the service has to calculate the concave hull.
     *
     * @param larsGraph - larsGraph to get the hull from
     */
    public void setLarsGraph(LarsGraph larsGraph){
        this.larsGraph = larsGraph;
    }

    /**
     * Tells you if the graph of the used LarsGraph is containing the given edge. If so the method returns
     * true else it returns false.
     *
     * @param edge - checking the graph on containment
     * @return - if the graph contains the given edge
     */
    public boolean containsEdge(GraphEdge edge){
        return larsGraph.containsEdge(edge);
    }

    /**
     * Calculates the concave hull of a given points cloud (vertices of the larsGraph).
     *
     * @param vertices - points cloud
     * @return - concave hull of the point cloud
     */
    private LinearRing calculateConcaveHull(Set<GraphVertex> vertices){
        return TopologyUtil.pointListToConcaveHull(new ArrayList<>(vertices), Constants.CONCAVE_TIGHTNESS);
    }

}
