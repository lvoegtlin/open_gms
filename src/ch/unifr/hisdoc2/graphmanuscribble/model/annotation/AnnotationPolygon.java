package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

import java.util.List;

/**
 * This class represents just one polygon of a given annotation type.
 */
public class AnnotationPolygon{

    private GraphEdge source;
    private LarsGraph larsGraph;

    public AnnotationPolygon(GraphEdge source,
                             LarsGraph larsGraph){
        this.source = source;
        this.larsGraph = larsGraph;
    }

    /**
     * Sets the concave hull of the current graph.
     *
     * @param concavePoints - the concave hull for the graph
     */
    public void setConcavePoints(List<PointHD2> concavePoints){
        this.larsGraph.setConcaveHull(concavePoints);
    }

    /**
     * Returns the contour points of this polygon witch are given by the subgraph it represents
     *
     * @return - List of PointHD2 points
     */
    public List<PointHD2> getContourPoints(){
        return larsGraph.getConcaveHull();
    }

    /**
     * Returns the larsGraph witch the polygon is surrounding.
     *
     * @return - a LarsGraph
     */
    public LarsGraph getLarsGraph(){
        return larsGraph;
    }

    /**
     * Sets the LarsGraph the polygon is surrounding.
     *
     * @param larsGraph - the new larsgraph
     */
    public void setLarsGraph(LarsGraph larsGraph){
        this.larsGraph = larsGraph;
    }

    /**
     * Returns the source of the polygon
     *
     * @return - the source as GraphEdge
     */
    public GraphEdge getSource(){
        return source;
    }

    /**
     * Check if a given edge is part of this polygon graph
     *
     * @param edge - to check
     * @return true if its part of the graph
     */
    public boolean isEdgePartofPolygon(GraphEdge edge){
        return larsGraph.containsEdge(edge);
    }

    /**
     * Checks if the current source is still part of the LarsGraph.
     *
     * @return - if the graph contains the edge
     */
    public boolean sourceInLarsGraph(){
        return larsGraph.containsEdge(source);
    }
}
