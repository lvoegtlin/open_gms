package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This object holds a graph and the related concave hull of the graph. This saves time while annotating the graph.
 */
public class LarsGraph{
    /**
     * The graph
     */
    UndirectedSubgraph<GraphVertex, GraphEdge> graph;

    /**
     * The concave hull of the graph
     */
    List<PointHD2> concaveHull;

    public LarsGraph(UndirectedSubgraph<GraphVertex, GraphEdge> graph){
        this(graph, null);
    }

    public LarsGraph(UndirectedSubgraph<GraphVertex, GraphEdge> graph, ArrayList<PointHD2> concaveHull){
        this.graph = graph;
        this.concaveHull = concaveHull;
    }

    /**
     * Removes the given edge form the graph
     *
     * @param e - the edge to remove
     * @return - if it was successful
     */
    public void removeEdge(GraphEdge e){
        graph.removeEdge(e);
    }

    /**
     * Adds the given edge to the graph
     *
     * @param e - the edge to add
     * @return - if it was successful
     */
    public void addEdge(GraphEdge e, GraphVertex source, GraphVertex target){
        graph.addVertex(source);
        graph.addVertex(target);
        graph.addEdge(source, target, e);
    }

    /**
     * Checks if the graph contains the given edge e
     *
     * @param e - edge to check
     * @return - true if the graph contains the edge
     */
    public boolean containsEdge(GraphEdge e){
        return graph.containsEdge(e);
    }

    /**
     * Returns the undirected graph
     *
     * @return - the graph
     */
    public UndirectedSubgraph<GraphVertex, GraphEdge> getGraph(){
        return graph;
    }

    /**
     * Sets the value of the undirected graph to the given one
     *
     * @param graph - the new graph
     */
    public void setGraph(UndirectedSubgraph<GraphVertex, GraphEdge> graph){
        this.graph = graph;
    }

    /**
     * Returns the concave hull of the graph as a list of points
     *
     * @return - concave hull
     */
    public List<PointHD2> getConcaveHull(){
        return concaveHull;
    }

    /**
     * Sets the concave hull to the given one
     *
     * @param concaveHull - the new hull
     */
    public void setConcaveHull(List<PointHD2> concaveHull){
        this.concaveHull = concaveHull;
    }
}
