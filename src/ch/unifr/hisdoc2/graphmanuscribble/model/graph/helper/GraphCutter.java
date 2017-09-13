package ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import org.apache.commons.lang.time.StopWatch;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;

import java.util.*;


/**
 * HisDoc2Java
 * Package: ch.unifr.hisdoc2.graphProcessing
 * Date: 29.04.15 4:43 PM
 */
public class GraphCutter{

    /**
     * cut those edges with their edge weight exceeding a given threshold
     *  @param mstGraph  graph of the page - it will be changed!
     * @param list      edges sorted according to their weight (biggest first)
     * @param threshold edges with a weight > threshold will be removed from the graph. If its -1 it deletes the adjusted percentage of the edges
     */
    public static void cutHighCostEdges(Subgraph<GraphVertex, GraphEdge, SimpleWeightedGraph<GraphVertex, GraphEdge>> mstGraph, List<GraphEdge> list, double threshold) {
        StopWatch sw = new StopWatch();
        sw.start();
        int nb = 0;
        double sum = 0;
        //TODO change to histogramm style
        try {
            if (list.size() == 0) {
                list.addAll(getSortedEdges(mstGraph, mstGraph.edgeSet()));
            }
            //if the threshold is -1 we delete the first 2.5%
            if(threshold == -1){
                int relevantIndex = (int) (list.size() / (100 / Constants.EDGE_CUT_PRECENTAGE));
                System.out.println("relevant: " +relevantIndex);
                threshold = mstGraph.getEdgeWeight(list.get(relevantIndex));
                System.out.println(threshold);
            }

            int i = 0;
            GraphEdge edge = list.get(0);
            double w = mstGraph.getEdgeWeight(edge);
            while (w > threshold) {
                mstGraph.removeEdge(edge);
                edge.setDeleted(true);
                edge = list.get(++i);
                nb++;
                w = mstGraph.getEdgeWeight(edge);
                sum += Math.abs(w);
            }
        } catch (IndexOutOfBoundsException ignore) {}
        sw.stop();

        //System.out.println(mstGraph.hashCode());
        System.out.println(nb + " edges were cut due to their high cost, their sum was " + sum);
    }

    /**
     * get the list of edges of the graph sorted by their weight
     *
     * @param graph
     * @param edges
     * @return
     */
    static List<GraphEdge> getSortedEdges(final Subgraph<GraphVertex, GraphEdge, SimpleWeightedGraph<GraphVertex, GraphEdge>> graph, Set<GraphEdge> edges) {
        List<GraphEdge> edgelist = new ArrayList<>();
        edgelist.addAll(edges);
        edgelist.sort((GraphEdge o1, GraphEdge o2) -> GraphUtil.compareEdgeWeights(graph, o1, o2));
        return edgelist;
    }

}
