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
        List<GraphEdge> edgesList = new ArrayList<>();
        if(edgeSource != null){
            edgesList.add(edgeSource);
        }
        addIntersection(graphSource, edgesList);
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
     * @param edges - the edges the graph hits
     */
    public void addIntersection(LarsGraph g, List<GraphEdge> edges){
        if(g == null || edges == null){
            return;
        }
        if(source.containsKey(g)){
            List<GraphEdge> edgesSource = source.get(g);
            for(GraphEdge e : edges){
                if(!edgesSource.contains(e) && e != null){
                    source.get(g).add(e);
                }
            }
        } else {
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

        for(List<GraphEdge> l : source.values()){
            result.addAll(l);
        }

        return result;
    }

    /**
     * Returns all the intersection of a source graph scribble with the graph. This is a list of graphEdges
     *
     * @param g - the sourceGraph I want the intersections of
     * @return the edges it intersects with
     */
    public List<GraphEdge> getEdgesFromSourceGraph(LarsGraph g){
        return source.get(g);
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
        for(LarsGraph lG : newSources.keySet()){
            if(source.containsKey(lG)){
                source.get(lG).addAll(newSources.get(lG));
            } else {
                source.put(lG, newSources.get(lG));
            }
        }
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
