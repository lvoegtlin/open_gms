package ch.unifr.hisdoc2.graphmanuscribble.view.helper;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

/**
 * Represents a Square in the grind.
 */
public class GraphGridSquare extends Polygon{

    private ArrayList<GraphVertex> vertices;
    private ArrayList<GraphEdge> edges;
    private double x;
    private double y;

    /**
     * Creates the GraphGridSquare based on the four given points from the points array.
     *
     * @param x - x coordinate of the top left corner of the square
     * @param y - y coordinate of the top left corner of the square
     */
    public GraphGridSquare(double x, double y){
        super(new double[]{x, y,
                x, y + Constants.GRID_SQUARE_HEIGHT,
                x + Constants.GRID_SQUARE_WIDTH, y + Constants.GRID_SQUARE_HEIGHT,
                x + Constants.GRID_SQUARE_WIDTH, y});
        this.x = x;
        this.y = y;
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    /**
     * Adds an vertex to the vertex array of this helper square
     *
     * @param v - GraphVertex to add
     */
    public void addVertex(GraphVertex v){
        vertices.add(v);
    }

    /**
     * Adds an edge to the edge array of this helper square
     *
     * @param e - GraphEdge to add
     */
    public void addEdge(GraphEdge e){
        edges.add(e);
    }

    /**
     * Returns the vertices which are part of this GraphGridSquare
     *
     * @return - ArrayList<GraphVertex>
     */
    public ArrayList<GraphVertex> getVertices(){
        return vertices;
    }

    /**
     * Returns the edges (also partial) which are part of this GraphGridSquare
     *
     * @return - ArrayList<GraphEdge>
     */
    public ArrayList<GraphEdge> getEdges(){
        return edges;
    }

    /**
     * Returns the x coordinate of the top left corner.
     *
     * @return x - x coordinate of the top left corner of the square
     */
    public double getX(){
        return x;
    }

    /**
     * Returns the y coordinate of the top left corner.
     *
     * @return y - y coordinate of the top left corner of the square
     */
    public double getY(){
        return y;
    }

    /**
     * Gives the information if a given edge is a part of this square.
     *
     * @param e - GraphEdge you are looking for
     * @return - boolean Square contains the edge
     */
    public boolean isEdgeOfThisSquare(GraphEdge e){
        return edges.contains(e);
    }
}
