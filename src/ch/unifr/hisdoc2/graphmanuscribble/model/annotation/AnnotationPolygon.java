package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents just one polygon of a given annotation type.
 */
public class AnnotationPolygon{

    /**
     * represents all the edges that got hit by the annotation scribble
     */
    private List<LarsGraph> graphSources;
    private List<GraphEdge> edgeSources;
    private LarsGraph polyGraph;

    public AnnotationPolygon(LarsGraph graphSource,
                             GraphEdge edgeSource,
                             LarsGraph polyGraph){
        this.graphSources = new ArrayList<>();
        this.edgeSources = new ArrayList<>();
        this.polyGraph = polyGraph;
        if(edgeSource != null){
            this.edgeSources.add(edgeSource);
        }
        if(graphSource != null){
            this.graphSources.add(graphSource);
        }
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
     * @return - a LarsGraph
     */
    public LarsGraph getPolyGraph(){
        return polyGraph;
    }

    /**
     * Sets the LarsGraph the polygon is surrounding.
     *
     * @param larsGraph - the new larsgraph
     */
    public void setLarsGraph(LarsGraph larsGraph){
        this.polyGraph = larsGraph;
    }

    /**
     * Returns the source of the polygon
     *
     * @return - the source as GraphEdge
     */
    public List<LarsGraph> getGraphSources(){
        return graphSources;
    }

    /**
     * Returns, if that annotationPolygon has, edge sources.
     *
     * @return the list with edgeSources
     */
    public List<GraphEdge> getEdgeSources(){
        return edgeSources;
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
     * Adds a graph as an other source to the list of graphSources
     *
     * @param source - another graph source of this annotation polygon
     */
    public void addGraphSource(LarsGraph source){
        if(!graphSources.contains(source) && source != null){
            graphSources.add(source);
        }
    }

    /**
     * Adds a edge as an other source to the list of graphSources
     *
     * @param source - another edge source of this annotation polygon
     */
    public void addEdgeSource(GraphEdge source){
        if(!edgeSources.contains(source) && source != null){
            edgeSources.add(source);
        }
    }

    /**
     * Adds a list of edge as sources to the list of graphSources
     *
     * @param sources - list with edges
     */
    public void addEdgeSources(List<GraphEdge> sources){
        for(GraphEdge e : sources){
            if(!edgeSources.contains(e) && e != null){
                edgeSources.add(e);
            }
        }
    }


    /**
     * Removes a list of graphs out of the source list
     *
     * @param source - that gets removed
     */
    public void removeGraphSources(List<LarsGraph> source){
        graphSources.removeAll(source);
    }

    /**
     * Removes a list of edge out of the source list
     *
     * @param source - that gets removed
     */
    public void removeEdgeSources(List<GraphEdge> source){
        edgeSources.removeAll(source);
    }

    /**
     * Returns the annotationpolygon that is around a given larsgraph
     *
     * @param larsGraph - To find the annotationpolygon from
     * @return - the annotationpolygon
     */
    public AnnotationPolygon getAnnotationPolygonByLarsGraph(LarsGraph larsGraph){
        return this.polyGraph == larsGraph ? this : null;
    }
}
