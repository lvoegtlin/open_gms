package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import org.jgrapht.UndirectedGraph;
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
    private UndirectedGraph<GraphVertex, GraphEdge> graph;

    /**
     * The concave hull of the graph
     */
    private List<PointHD2> concaveHull;

    /**
     * Holds the information if the graph is a annotationgraph
     */
    private boolean annotation = false;

    /**
     * Tells if the larsgraph is annotated
     */
    private boolean annotated = false;

    public LarsGraph(UndirectedGraph<GraphVertex, GraphEdge> graph){
        this(graph, new ArrayList<>());
    }

    public LarsGraph(UndirectedGraph<GraphVertex, GraphEdge> graph, ArrayList<PointHD2> concaveHull){
        this.graph = graph;
        this.concaveHull = concaveHull;
    }

    public LarsGraph(UndirectedGraph<GraphVertex, GraphEdge> graph, boolean annotation){
        this(graph, new ArrayList<>());
        this.annotation = annotation;
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
    public synchronized UndirectedGraph<GraphVertex, GraphEdge> getGraph(){
        return graph;
    }

    /**
     * Sets the value of the undirected graph to the given one
     *
     * @param graph - the new graph
     */
    public synchronized void setGraph(UndirectedSubgraph<GraphVertex, GraphEdge> graph){
        this.graph = graph;
    }

    /**
     * Returns the concave hull of the graph as a list of points
     *
     * @return - concave hull
     */
    public synchronized List<PointHD2> getConcaveHull(){
        return concaveHull;
    }

    /**
     * Sets the concave hull to the given one
     *
     * @param concaveHull - the new hull
     */
    public synchronized void setConcaveHull(List<PointHD2> concaveHull){
        this.concaveHull = concaveHull;
    }

    /**
     * Is this graph an annotation
     * @return
     */
    public boolean isAnnotationGraph(){
        return annotation;
    }

    /**
     * Sets if this graph is a annotation
     * @param annotation
     */
    public void setAnnotationGraph(boolean annotation){
        this.annotation = annotation;
    }

    /**
     * Is the graph already annotated
     * @return
     */
    public boolean isAnnotated(){
        return annotated;
    }

    /**
     * Sets the annotated status of the graph
     * @param annotated
     */
    public void setAnnotated(boolean annotated){
        this.annotated = annotated;
    }
}
