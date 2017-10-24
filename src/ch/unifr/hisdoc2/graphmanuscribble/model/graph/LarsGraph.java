package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import org.jgrapht.UndirectedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This object holds a graph and the related concave hull of the graph. This saves time while annotating the graph.
 */
public class LarsGraph{
    /**
     * The graph
     */
    private List<UndirectedGraph<GraphVertex, GraphEdge>> graphs;

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

    /**
     * last graph that was edited
     */
    private UndirectedGraph<GraphVertex, GraphEdge> editedGraph;

    /**
     * all graphs from the graphs list united
     */
    private Set<GraphVertex> allVertices;

    public LarsGraph(UndirectedGraph<GraphVertex, GraphEdge> graph){
        this(graph, new ArrayList<>());
    }

    public LarsGraph(UndirectedGraph<GraphVertex, GraphEdge> graph, ArrayList<PointHD2> concaveHull){
        this.graphs = new ArrayList<>();
        this.concaveHull = concaveHull;
        this.allVertices = new HashSet<>();

        if(graph != null){
            graphs.add(graph);
            allVertices.addAll(graph.vertexSet());
        }
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
        for(UndirectedGraph<GraphVertex, GraphEdge> g : graphs){
            if(g.containsEdge(e)){
                g.removeEdge(e);
                editedGraph = g;
            }
        }
    }

    /**
     * Adds the given edge to the graph
     *
     * @param e - the edge to add
     * @return - if it was successful
     */
    public void addEdge(GraphEdge e, GraphVertex source, GraphVertex target){
        editedGraph.addVertex(source);
        editedGraph.addVertex(target);
        editedGraph.addEdge(source, target, e);
    }

    /**
     * Checks if the graph contains the given edge e
     *
     * @param e - edge to check
     * @return - true if the graph contains the edge
     */
    public boolean containsEdge(GraphEdge e){
        for(UndirectedGraph<GraphVertex, GraphEdge> g : graphs){
            if(g.containsEdge(e)){
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the undirected graph
     *
     * @return - the graph
     */
    public synchronized List<UndirectedGraph<GraphVertex, GraphEdge>> getGraphs(){
        return graphs;
    }

    /**
     * Sets the value of the undirected graph to the given one. Dont forget to recalculate the hull!
     *
     * @param graphs - the new graphs
     */
    public synchronized void setGraph(List<UndirectedGraph<GraphVertex, GraphEdge>> graphs){
        this.graphs = graphs;
        allVertices.clear();
        for(UndirectedGraph<GraphVertex, GraphEdge> g : graphs){
            allVertices.addAll(g.vertexSet());
        }
    }

    public UndirectedGraph<GraphVertex, GraphEdge> getEditedGraph(){
        return editedGraph;
    }

    /**
     * adds a graph to the graphs list. Dont forget to recalculate the hull!
     *
     * @param graph
     */
    public void addGraph(UndirectedGraph<GraphVertex, GraphEdge> graph){
        this.graphs.add(graph);
        allVertices.addAll(graph.vertexSet());
    }

    /**
     * returns the unite of all the graphs in the graphs list
     *
     * @return - a graph unite
     */
    public Set<GraphVertex> getAllVertices(){
        return allVertices;
    }

    public void setAllVertices(Set<GraphVertex> allVertices){
        this.allVertices = allVertices;
    }

    /**
     * refreshes the allVertex List which is a set with all
     * the vertices from all the graphs which this larsGraph represents
     */
    public void updateVertices(){
        allVertices.clear();
        for(UndirectedGraph<GraphVertex, GraphEdge> g : graphs){
            allVertices.addAll(g.vertexSet());
        }
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
     *
     * @return
     */
    public boolean isAnnotationGraph(){
        return annotation;
    }

    /**
     * Sets if this graph is a annotation
     *
     * @param annotation
     */
    public void setAnnotationGraph(boolean annotation){
        this.annotation = annotation;
    }

    /**
     * Is the graph already annotated
     *
     * @return
     */
    public boolean isAnnotated(){
        return annotated;
    }

    /**
     * Sets the annotated status of the graph
     *
     * @param annotated
     */
    public void setAnnotated(boolean annotated){
        this.annotated = annotated;
    }
}
