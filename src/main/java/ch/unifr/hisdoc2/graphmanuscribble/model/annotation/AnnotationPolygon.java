package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents just one polygon of a given annotation type.
 */
public class AnnotationPolygon{

    /**
     * the source graphs of the annotation
     */
    private List<LarsGraph> source;
    private LarsGraphCollection polyGraph;

    AnnotationPolygon(LarsGraph graphSource,
                      LarsGraphCollection polyGraph){
        this.source = new ArrayList<>();
        this.polyGraph = polyGraph;
        source.add(graphSource);
    }

    /**
     * Returns the contour points of this polygon witch are given by the subgraph it represents
     *
     * @return - List of PointHD2 points
     */
    public List<PointHD2> getHull(){
        return polyGraph.getConcaveHull();
    }

    /**
     * Returns the larsGraph witch the polygon is surrounding.
     *
     * @return - a LarsGraphCollection
     */
    public LarsGraphCollection getPolyGraph(){
        return polyGraph;
    }

    /**
     * Sets the LarsGraphCollection the polygon is surrounding.
     *
     * @param larsGraphCollection - the new larsgraph
     */
    public void setLarsGraph(LarsGraphCollection larsGraphCollection){
        this.polyGraph = larsGraphCollection;
    }

    /**
     * Returns the the sources as a list of LarsGraphs
     *
     * @return - list of LarsGraphs
     */
    public List<LarsGraph> getSources(){
        return source;
    }

    /**
     * Returns the annotationpolygon that is around a given larsgraph
     *
     * @param larsGraphCollection - To find the annotationpolygon from
     * @return - the annotationpolygon
     */
    public AnnotationPolygon getAnnotationPolygonByLarsGraph(LarsGraphCollection larsGraphCollection){
        return this.polyGraph == larsGraphCollection ? this : null;
    }

    /**
     * Check if a given edge is part of this polygon graph
     *
     * @param edge - to check
     * @return true if its part of the graph
     */
    public boolean isEdgePartofPolygon(GraphEdge edge){
        return polyGraph.containsEdge(edge, true);
    }

    /**
     * Adds a source to the AnnotationPolygon.
     *
     * @param newSource - the new source
     */
    public void addSource(LarsGraph newSource){
        if(!source.contains(newSource)){
            source.add(newSource);
        }
    }

    /**
     * Adds a list of sources to the current AnnotationPolygon.
     *
     * @param newSources - the list with sources
     */
    public void addSources(List<LarsGraph> newSources){
        for(LarsGraph lG : newSources){
            addSource(lG);
        }
    }

    /**
     * Removes a graph out of the source list
     *
     * @param graph - that gets removed
     */
    private void removeSource(LarsGraph graph){
        source.remove(graph);
    }

    /**
     * Removes a list of graphs out of the source list
     *
     * @param graphs - that gets removed
     */
    public void removeSources(List<LarsGraph> graphs){
        for(LarsGraph graph : graphs){
            removeSource(graph);
        }
    }
}
