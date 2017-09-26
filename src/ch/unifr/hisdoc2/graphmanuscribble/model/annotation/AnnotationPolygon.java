package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents just one polygon of a given annotation type.
 */
public class AnnotationPolygon{

    /**
     * represents all the edges that got hit by the annotation scribble
     */
    private List<SimpleGraph> sources;
    private LarsGraph larsGraph;

    public AnnotationPolygon(SimpleGraph source,
                             LarsGraph larsGraph){
        this.sources = new ArrayList<>();
        this.sources.add(source);
        this.larsGraph = larsGraph;
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
    public List<SimpleGraph> getSources(){
        return sources;
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
     * Adds a edge as an other source to the list of sources
     *
     * @param source - another source of this annotation polygon
     */
    public void addSource(SimpleGraph source){
        if(!sources.contains(source)){
            sources.add(source);
        }
    }

    /**
     * Removes a list of edge out of the source list
     *
     * @param source - that gets removed
     */
    public void removeSources(List<GraphEdge> source){
        sources.removeAll(source);
    }
}
