package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.AngieMSTGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays a given AngieMSTGraph in a canvas.
 */
public class GraphView extends AbstractView{

    private AngieMSTGraph graph;
    private Color color;

    /**
     * Creates a new GraphView which shows the graph AngieMSTGraph {@param mstGraph}.
     *
     * @param mstGraph - The AngieMSTGraph to display
     */
    public GraphView(AngieMSTGraph mstGraph, Controller controller, ArrayList<Color> cls){
        super(controller, cls);
        this.graph = mstGraph;
        if(cls.size() != 1){
            throw new IllegalArgumentException("Graph can just have one color");
        }

        this.color = cls.get(0);

        drawGraph();
        show();
    }

    /**
     * Creates a svg path representation of the graph
     */
    private void drawGraph(){
        //draw the edges
        drawEdges(graph.getEdges());

        setSVGPath(color);
    }

    /**
     * Draws the given edges.
     *
     * @param edges - List<GraphEdge>
     */
    private void drawEdges(List<GraphEdge> edges){
        for(GraphEdge e : edges){
            if(!e.isDeleted()){
                GraphVertex v1 = graph.getEdgeSource(e);
                GraphVertex v2 = graph.getEdgeTarget(e);
                svgPathPrinters.get(color).addEdge(v1.getX(), v2.getX(), v1.getY(), v2.getY());
            }
        }
    }

    @Override
    public void update(){
        svgPathPrinters.get(color).clear();
        drawGraph();
    }
}
