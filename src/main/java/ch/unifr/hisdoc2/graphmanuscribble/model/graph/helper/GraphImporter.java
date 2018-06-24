package ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper;

import ch.unifr.hisdoc2.graphmanuscribble.io.LoadedGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.ConcaveHullExtractionService;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Mka eit possible to import a gxml graph into the graphmanuscribble system
 */
public class GraphImporter{

    private static List<GraphEdge> deletedEdges = new ArrayList<>();
    private static SimpleGraph<GraphVertex, GraphEdge> graph;
    private static UndirectedSubgraph<GraphVertex, GraphEdge> undirectedGraph;

    /**
     * Reads in a given gxml file and returns a LoadedGraph object.
     * This object contains all information needed for the system to use this graph.
     *
     * @param xmlFile - gxml file
     * @return - the loadedGraph object
     */
    public static LoadedGraph xml2Graph(File xmlFile){
        //read the graph from xml
        Element root = getRootFromFile(xmlFile);
        //create subgraph
        graph = createSubgraph(root);
        //make copy of graph
        Subgraph<GraphVertex, GraphEdge, SimpleGraph<GraphVertex, GraphEdge>> clone =
                new Subgraph<>(graph, graph.vertexSet(), graph.edgeSet());
        //delete deledable edges
        clone.removeAllEdges(deletedEdges);
        //check connectivity
        undirectedGraph = new UndirectedSubgraph<>(clone.getBase(), clone.vertexSet(), clone.edgeSet());
        ConnectivityInspector<GraphVertex, GraphEdge> cI = new ConnectivityInspector<>(undirectedGraph);

        return new LoadedGraph(graph, createForest(cI), cI.isGraphConnected());
    }

    /**
     * Creates a forest out of the given ConnectivityInspector with the help of the GraphUtil method createGraphFromVertices.
     * It returns a list of LarsGraphs
     *
     * @param cI - the inspector
     * @return - the forest as a lit of larsgraphs
     */
    private static List<LarsGraphCollection> createForest(ConnectivityInspector<GraphVertex,GraphEdge> cI){
        ArrayList<LarsGraphCollection> forest = new ArrayList<>();
        List<Set<GraphVertex>> trees = cI.connectedSets();

        for(Set<GraphVertex> graphVertices : trees){
            LarsGraphCollection lGC = new LarsGraphCollection(
                    new LarsGraph(GraphUtil.createGraphFromVertices(undirectedGraph, graphVertices), false)
            );
            //start hull calc
            ConcaveHullExtractionService cHES = new ConcaveHullExtractionService();
            cHES.setOnFailed(event ->
                    cHES.getException().printStackTrace(System.err)
            );
            cHES.setLarsGraphCollection(lGC);
            cHES.start();
            //add to list
            forest.add(lGC);
        }

        return forest;
    }

    /**
     * Extracts the rot node out of a given gxml file and returns it.
     *
     * @param xmlFile - the gxml file
     * @return - the root node
     */
    private static Element getRootFromFile(File xmlFile){
        SAXBuilder builder = new SAXBuilder();
        Document xml = null;
        try {
            xml = builder.build(xmlFile);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return Objects.requireNonNull(xml).getRootElement();
    }

    /**
     * Creates a graph out of a given gxml root element.
     *
     * @param root - the graph as gxml root element
     * @return - a simpleGraph
     */
    private static SimpleGraph<GraphVertex, GraphEdge> createSubgraph(Element root){
        //to get the node with the right id for the edge
        Map<Integer, GraphVertex> nodeMap = new HashMap<>();
        SimpleGraph<GraphVertex, GraphEdge> graph = new SimpleGraph<>(GraphEdge.class);

        List<Element> nodes = root.getChildren("node");
        for(Element node : nodes){
            int id = Integer.parseInt(node.getAttributeValue("id"));
            double x = Double.parseDouble(node.getAttributeValue("x"));
            double y = Double.parseDouble(node.getAttributeValue("y"));
            GraphVertex v = new GraphVertex(x, y);
            graph.addVertex(v);
            nodeMap.put(id, v);
        }

        List<Element> edges = root.getChildren("edge");
        for(Element edge : edges){
            boolean deleted = Boolean.valueOf(edge.getAttributeValue("deleted"));
            int source = Integer.parseInt(edge.getAttributeValue("source"));
            int target = Integer.parseInt(edge.getAttributeValue("target"));
            GraphEdge e = new GraphEdge();
            e.setDeleted(deleted);
            if(deleted){
                deletedEdges.add(e);
            }
            graph.addEdge(nodeMap.get(source), nodeMap.get(target), e);
        }

        return graph;
    }
}
