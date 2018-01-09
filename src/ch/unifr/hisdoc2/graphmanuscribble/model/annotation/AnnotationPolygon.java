package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents just one polygon of a given annotation type.
 */
public class AnnotationPolygon{

    /**
     * represents all the edges that got hit by the annotation scribble
     */
    private HashMap<LarsGraph, List<GraphEdge>> source;
    private LarsGraphCollection polyGraph;

    public AnnotationPolygon(LarsGraph graphSource,
                             GraphEdge edgeSource,
                             LarsGraphCollection polyGraph){
        this.source = new HashMap<>();
        this.polyGraph = polyGraph;
        addIntersection(graphSource, edgeSource);
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
     * Adds a new intersection pair to the map of sources of this polygon.
     *
     * @param g - the graph source
     * @param e - the edges the graph hits
     */
    public void addIntersection(LarsGraph g, GraphEdge e){
        if(source.containsKey(g)){
            List<GraphEdge> edges = source.get(g);
            if(!edges.contains(e)){
                source.get(g).add(e);
            }
        } else {
            List<GraphEdge> edges = new ArrayList<>();
            edges.add(e);
            source.put(g, edges);
        }
    }

    /**
     * Returns the source of the polygon
     *
     * @return - the source as GraphEdge
     */
    public List<LarsGraph> getGraphSources(){
        return new ArrayList<>(source.keySet());
    }

    /**
     * Returns, if that annotationPolygon has, edge sources.
     *
     * @return the list with edgeSources
     */
    public List<GraphEdge> getEdgeSources(){
        List<GraphEdge> result = new ArrayList<>();

        for(List l : source.values()){
            result.addAll(l);
        }

        return result;
    }

    /**
     * Returns the source HashMap
     *
     * @return - a map with graphs as keys and list of graphEdges as values
     */
    public HashMap<LarsGraph, List<GraphEdge>> getSource(){
        return source;
    }

    /**
     * Adds a HashMap to the source map of the current AnnotationPolygon.
     *
     * @param newSources - the map to add
     */
    public void addSource(HashMap<LarsGraph, List<GraphEdge>> newSources){
        source.putAll(newSources);
    }

    /**
     * Check if a given edge is part of this polygon graph
     *
     * @param edge - to check
     * @return true if its part of the graph
     */
    public boolean isEdgePartofPolygon(GraphEdge edge){
        return polyGraph.containsEdge(edge);
    }

    /**
     * Removes a list of graphs out of the source list
     *
     * @param graphs - that gets removed
     */
    public void removeGraphSources(List<LarsGraph> graphs){
        for(LarsGraph graph : graphs){
            source.remove(graph);
        }
    }

    /**
     * Removes a list of edge out of the source list
     *
     * @param edges - that gets removed
     */
    public void removeEdgeSources(List<GraphEdge> edges){
        for(List<GraphEdge> list : source.values()){
            list.removeAll(edges);
        }
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
}
