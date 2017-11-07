package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.helper.TopologyUtil;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import org.jgrapht.UndirectedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This object holds a graph and the related concave hull of the graph. This saves time while annotating the graph.
 */
public class LarsGraphCollection{
    /**
     * The graph
     */
    private List<LarsGraph> graphs;

    /**
     * The concave hull of all the graphs
     */
    private List<PointHD2> concaveHull;

    /**
     * Tells if the larsgraph is annotated. Thats when all is graphs are annotated
     */
    private boolean annotated = false;

    /**
     * last graph that was edited
     */
    private LarsGraph editedGraph;

    /**
     * all graphs from the graphs list united
     */
    private Set<GraphVertex> allVertices;

    public LarsGraphCollection(LarsGraph graph){
        this(graph, new ArrayList<>());
    }

    public LarsGraphCollection(LarsGraph graph, ArrayList<PointHD2> concaveHull){
        this.graphs = new ArrayList<>();
        this.concaveHull = concaveHull;
        this.allVertices = new HashSet<>();

        if(graph != null){
            graphs.add(graph);
            allVertices.addAll(graph.getGraph().vertexSet());
        }

        annotationCheck();
    }

    /**
     * Checks if the collection is annotated or not.
     * This is done by checking if each graph in the graphs list is annotated. If so its true else false.
     */
    private void annotationCheck(){
        for(LarsGraph g : graphs){
            if(!g.isAnnotated()){
                annotated = false;
                return;
            }
        }

        annotated = true;
    }

    /**
     * Removes the given edge form the graph
     *
     * @param e - the edge to remove
     * @return - if it was successful
     */
    public void removeEdge(GraphEdge e){
        for(LarsGraph g : graphs){
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
        UndirectedGraph<GraphVertex, GraphEdge> g = editedGraph.getGraph();
        g.addVertex(source);
        g.addVertex(target);
        g.addEdge(source, target, e);
    }

    /**
     * Checks if the graph contains the given edge e
     *
     * @param e - edge to check
     * @return - true if the graph contains the edge
     */
    public boolean containsEdge(GraphEdge e){
        for(LarsGraph g : graphs){
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
    public synchronized List<LarsGraph> getGraphs(){
        return graphs;
    }

    /**
     * Sets the value of the undirected graph to the given one. Dont forget to recalculate the hull!
     *
     * @param graphs - the new graphs
     */
    public synchronized void setGraph(List<LarsGraph> graphs){
        this.graphs = graphs;
        allVertices.clear();
        for(LarsGraph g : graphs){
            allVertices.addAll(g.getGraph().vertexSet());
        }
        annotationCheck();
        updateHull();
    }

    public LarsGraph getEditedGraph(){
        return editedGraph;
    }

    /**
     * adds a graph to the graphs list. Dont forget to recalculate the hull!
     *
     * @param graphs
     */
    public void addGraph(List<LarsGraph> graphs){
        for(LarsGraph graph : graphs){
            this.graphs.add(graph);
            allVertices.addAll(graph.getGraph().vertexSet());
        }

        annotationCheck();
    }

    /**
     * returns the unite of all the graphs in the graphs list
     *
     * @return - a graph unite
     */
    public Set<GraphVertex> getAllVertices(){
        return allVertices;
    }

    /**
     * refreshes the allVertex List which is a set with all
     * the vertices from all the graphs which this larsGraph represents
     */
    public void updateVertices(){
        allVertices.clear();
        for(LarsGraph g : graphs){
            allVertices.addAll(g.getGraph().vertexSet());
        }
    }

    /**
     * Updates the hull of this collection.
     * It unites all the hulls of the lasrGraphs in the graphs list
     */
    public void updateHull(){
        List<List<PointHD2>> hulls = new ArrayList<>();
        graphs.forEach(larsGraph -> {
            hulls.add(larsGraph.getConcaveHull());
        });

        concaveHull = TopologyUtil.getUnionOfHulls(hulls);
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
     * Is the graph already annotated
     *
     * @return
     */
    public boolean isAnnotated(){
        return annotated;
    }
}
