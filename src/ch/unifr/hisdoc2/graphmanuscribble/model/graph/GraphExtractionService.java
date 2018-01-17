package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splits the given graph into 2 graphs. By deleting one edge of the currentLarsGraphCollection it always creates a second graph.
 * This is because we are cutting edges from a tree.
 */
public class GraphExtractionService extends Service<LarsGraphCollection>{

    private LarsGraphCollection currentLarsGraphCollection;
    private AnnotationPolygonMap annotationPolygonMap;

    /**
     *
     * @return - The collection which contains sourceEdge or the smaller one
     */
    @Override
    protected Task<LarsGraphCollection> createTask(){
        return new Task<LarsGraphCollection>(){
            @Override
            protected LarsGraphCollection call() throws Exception{
                UndirectedSubgraph<GraphVertex, GraphEdge> subgraphGraph;
                LarsGraphCollection choice;
                LarsGraphCollection newLarsGraphCollection;

                if(currentLarsGraphCollection == null || currentLarsGraphCollection.getGraphs().size() == 0){
                    return null;
                }

                subgraphGraph = (UndirectedSubgraph<GraphVertex, GraphEdge>) currentLarsGraphCollection.getEditedGraph().getGraph();
                currentLarsGraphCollection.removeGraph(currentLarsGraphCollection.getEditedGraph());

                ConnectivityInspector<GraphVertex, GraphEdge> cI = new ConnectivityInspector<>(subgraphGraph);
                //checks if the graph is still connected.
                if(cI.isGraphConnected()){
                    return null;
                }

                List<Set<GraphVertex>> subtrees = cI.connectedSets();

                int indexOfSmallTree = getIndexOfSmallTree(subtrees);
                UndirectedSubgraph<GraphVertex, GraphEdge> smallGraph;
                smallGraph = createGraphFromVertices(subgraphGraph, subtrees.get(indexOfSmallTree));
                UndirectedSubgraph<GraphVertex, GraphEdge> bigGraph;
                bigGraph = createGraphFromVertices(subgraphGraph, subtrees.get((subtrees.size() - 1) -indexOfSmallTree));

                //checking if one of the graphs is annotated. If yes we have to check witch one will contain witch
                //edge source after the deletion. If they just have a graphSource we will do that after the hull calc
                if((choice = annotationCheckEdges(smallGraph, bigGraph)) != null){
                    newLarsGraphCollection = choice;
                } else {
                    //when we dont have edgeSources we just set the bigger graph as part on the polygon and the smaller
                    //as a new LGC

                    currentLarsGraphCollection.addGraph(new LarsGraph(bigGraph));
                    newLarsGraphCollection = new LarsGraphCollection(new LarsGraph(smallGraph));
                }

                System.out.println("number of nodes small graph: " + smallGraph.vertexSet().size());
                System.out.println("number of nodes big graph: " + subgraphGraph.vertexSet().size());

                return newLarsGraphCollection;
            }
        };
    }

    /**
     * Creates a graph based on the vertices from subtrees and then takes the edges from subgraphGraph that are between
     * these edges
     *
     * @param subgraphGraph - original subtree we are cutting
     * @param subtrees - the 2 vertex sets of subtrees
     * @return a new subGraph referencing on already existing vertices and edges
     */
    private UndirectedSubgraph<GraphVertex, GraphEdge> createGraphFromVertices(
            UndirectedSubgraph<GraphVertex, GraphEdge> subgraphGraph,
            Set<GraphVertex> subtrees){
        //create a new graph
        UndirectedSubgraph<GraphVertex, GraphEdge> newGraph = new UndirectedSubgraph<>(subgraphGraph.getBase(),
                subtrees, new HashSet<>());
        //fill in the edges
        for(GraphVertex v : subtrees){
            for(GraphEdge e : subgraphGraph.edgesOf(v)){
                newGraph.addEdge(subgraphGraph.getEdgeSource(e), subgraphGraph.getEdgeTarget(e), e);
            }
        }

        return newGraph;
    }

    /**
     * Gets the index of the smallest subtree in the set
     *
     * @param subtrees - the list of subtree vertices
     * @return - the list index
     */
    private int getIndexOfSmallTree(List<Set<GraphVertex>> subtrees){
        int indexOfSmallTree = 0;
        int nbrOfVertices = 0;
        for(int i = 0; i < subtrees.size(); i++){
            if(subtrees.get(i).size() < nbrOfVertices){
                nbrOfVertices = subtrees.get(i).size();
                indexOfSmallTree = i;
            }
        }
        return indexOfSmallTree;
    }

    /**
     * Checks for a given collection is annotated.
     *
     * @param smallGraph - the smaller of the two graphs
     * @param bigGraph - the bigger of the two graphs
     *
     * @return returns the new LGC, so the one that does not contain sources edges. Else, so f there are no edge sources, null.
     */
    private LarsGraphCollection annotationCheckEdges(UndirectedSubgraph<GraphVertex, GraphEdge> smallGraph,
                                      UndirectedSubgraph<GraphVertex, GraphEdge> bigGraph){
        AnnotationPolygon annotationPolygon = annotationPolygonMap.getGraphPolygonByLarsGraph(currentLarsGraphCollection, null);

        LarsGraphCollection result = new LarsGraphCollection(null);

        if(annotationPolygon == null || annotationPolygon.getEdgeSources().isEmpty()){
            return null;
        }

        if(annotationPolygon.getGraphSources().size() == 1){
            for(GraphEdge edge : annotationPolygon.getEdgeSources()){
                if(smallGraph.containsEdge(edge)){
                    currentLarsGraphCollection.addGraph(new LarsGraph(smallGraph));
                    result.addGraph(new LarsGraph(bigGraph));
                }
                if(bigGraph.containsEdge(edge)){
                    currentLarsGraphCollection.addGraph(new LarsGraph(bigGraph));
                    result.addGraph(new LarsGraph(smallGraph));
                }
            }
        } else {
            //TODO optimize by checking witch LGC will be the biggest
            Set<LarsGraph> recheckList = new HashSet<>();
            List<LarsGraph> notCutGraphs = new ArrayList<>(currentLarsGraphCollection.getNonAnnotationGraphs());
            currentLarsGraphCollection.removeGraphs(notCutGraphs);
            //initial we set the big graph to be part of the big LGC
            currentLarsGraphCollection.addGraph(new LarsGraph(bigGraph));
            result.addGraph(new LarsGraph(smallGraph));

            for(LarsGraph g : annotationPolygon.getGraphSources()){
                //preparation
                List<GraphEdge> intersectionEdges = new ArrayList<>(annotationPolygon.getEdgesFromSourceGraph(g));
                boolean noIntersectionWithCutGraph = true;
                boolean inSmall = false;

                List<GraphEdge> deleteInters = new ArrayList<>();
                if(intersectionEdges.isEmpty()){
                    continue;
                }

                for(GraphEdge e : intersectionEdges){
                    if(currentLarsGraphCollection.containsEdge(e)){
                        noIntersectionWithCutGraph = false;
                        deleteInters.add(e);
                    }
                    if(result.containsEdge(e)){
                        noIntersectionWithCutGraph = false;
                        deleteInters.add(e);
                        inSmall = true;
                    }
                }

                intersectionEdges.removeAll(deleteInters);

                //start algo
                if(noIntersectionWithCutGraph){
                    recheckList.add(g);
                } else {
                    for(GraphEdge e : intersectionEdges){
                        for(LarsGraph noCut : notCutGraphs){
                            if(!noCut.containsEdge(e)){
                                continue;
                            }

                            if(inSmall){
                                //make a new annotation polygon for smaller graph
                                //also delete it in the annotationpolaygon
                                result.addGraph(noCut);
                                currentLarsGraphCollection.removeGraph(g);
                                result.addGraph(g);
                                //get all the edges from the annotation polygon
                                List<GraphEdge> intEdges = new ArrayList<>(annotationPolygon.getEdgesFromSourceGraph(g));
                                //delete sourceGraph out of the annotation polygon
                                annotationPolygon.removeGraphSource(g);
                                //create a new scribble
                                if(intEdges.size() == 0){
                                    annotationPolygonMap.addNewScribble(result,
                                            g,
                                            null,
                                            annotationPolygonMap.getAnnotationPolygonTypeByPolygon(annotationPolygon));
                                } else {
                                    for(GraphEdge intEdge : intEdges){
                                        annotationPolygonMap.addNewScribble(result,
                                                g,
                                                intEdge,
                                                annotationPolygonMap.getAnnotationPolygonTypeByPolygon(annotationPolygon));
                                    }
                                }
                            } else {
                                currentLarsGraphCollection.addGraph(noCut);
                            }
                        }
                    }
                }
            }
        }

        currentLarsGraphCollection.updateVertices();
        result.updateVertices();

        return result;
    }

    /**
     * Setting the current working graph. If the current graph is not set the service will not work.
     *
     * @param graph
     */
    public void setCurrentLarsGraphCollection(LarsGraphCollection graph){
        this.currentLarsGraphCollection = graph;
    }

    /**
     * Setting the current AnnotationPolygon. Required to avoid strange behavior.
     *
     * @param annotationPolygonMap
     */
    public void setAnnotationPolygonMap(AnnotationPolygonMap annotationPolygonMap){
        this.annotationPolygonMap = annotationPolygonMap;
    }
}
