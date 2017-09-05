/**
 * *******************************************************************************************************************
 * Copyright HisDoc 2.0 Project                                                                                       *
 * *
 * Copyright (c) University of Fribourg, 2015                                                                         *
 * *
 *
 * @author: Angelika Garz                                                                                             *
 * angelika.garz@unifr.ch                                                                                    *
 * http://diuf.unifr.ch/main/diva/home/people/angelika-garz                                                  *
 * ********************************************************************************************************************
 */

package ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * HisDoc2Java
 * Package: ch.unifr.hisdoc2.graphProcessing
 * Date: 30.04.15 9:17 AM
 */

/**
 * several Utility methods for graphs
 */
public class GraphUtil{

    /**
     * comparator for two edges
     *
     * @param graph graph the edges are from
     * @param o1    edge 1
     * @param o2    edge 2
     * @return -1 if o1 > o2, 0 if o1==02, 1 otherwise
     */
    public static <N extends PointHD2, E extends GraphEdge, SG extends Subgraph<N,E,SimpleWeightedGraph<N, E>>> int compareEdgeWeights(final SG graph, E o1, E o2) {
        if (graph.getEdgeWeight(o1) > graph.getEdgeWeight(o2))
            return -1;
        if (graph.getEdgeWeight(o1) < graph.getEdgeWeight(o2))
            return 1;
        return 0;
    }
}
