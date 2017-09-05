package ch.unifr.hisdoc2.graphmanuscribble.view.helper;


import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphVertex;

import java.util.ArrayList;

/**
 * Created by larsvoegtlin on 20.02.17.
 */
public class GraphGrid{

    private ArrayList<GraphGridSquare> squares;
    private ArrayList<GraphGridSquare> recentChanges;
    private GraphGridSquare lastSquareUsed;

    public GraphGrid(double width, double height){
        this.squares = new ArrayList<>();
        this.recentChanges = new ArrayList<>();
        createSquares(width, height);
    }

    /**
     * Creates the Squares which are part of the helper.
     * The dimension of the squares is based on the images width and height.
     *
     * @param width  - Of the Image
     * @param height - Of the Image
     */
    private void createSquares(double width, double height){

        int nbOfSquaresWidth = 0;
        int nbOfSquaresHeight = 0;

        if(width % Constants.GRID_SQUARE_WIDTH == 0){
            nbOfSquaresWidth = (int) (width / Constants.GRID_SQUARE_WIDTH);
        } else {
            nbOfSquaresWidth = (int) (width / Constants.GRID_SQUARE_WIDTH) + 1;
        }

        if(height % Constants.GRID_SQUARE_HEIGHT == 0){
            nbOfSquaresHeight = (int) (height / Constants.GRID_SQUARE_HEIGHT);
        } else {
            nbOfSquaresHeight = (int) (height / Constants.GRID_SQUARE_HEIGHT) + 1;
        }

        for(int i = 0; i <= nbOfSquaresWidth; i++){
            for(int j = 0; j <= nbOfSquaresHeight; j++){
                squares.add(new GraphGridSquare(i * Constants.GRID_SQUARE_WIDTH,
                        j * Constants.GRID_SQUARE_HEIGHT));
            }
        }
        System.out.println("Number of helper suqares: " + squares.size());
    }

    /**
     * Returns the recent changed squares from this helper
     *
     * @return - changed squares
     */
    public ArrayList<GraphGridSquare> getRecentChanges(){
        return recentChanges;
    }

    /**
     * Adds a Vertex to the right helper square in the helper. First it checks if the lastUsedSquare contains
     * the vertex. If that is true it will add it to that square else it iterates over all the squares to
     * find the right one.
     *
     * @param v - GraphVertex to add
     */
    public void addVertexToGrid(GraphVertex v){
        if(lastSquareUsed != null && lastSquareUsed.contains(v.getX(), v.getY())){
            lastSquareUsed.addVertex(v);
        } else {
            for(GraphGridSquare s : squares){
                if(s.contains(v.getX(), v.getY()) && s != lastSquareUsed){
                    s.addVertex(v);
                    lastSquareUsed = s;
                    return;
                }
            }
        }
    }

    /**
     * Adds a edge to the right helper square in the helper. First it checks if the lastUsedSquare contains
     * the edge. If that is true it will add it to that square else it iterates over all the squares to
     * find the right one.
     *
     * @param e - GraphEdge to add
     */
    public void addEdgeToGrid(GraphEdge e){
        if(lastSquareUsed != null){
            if(lastSquareUsed.intersects(e.getPolygonRepresentation().getLayoutBounds())){
                lastSquareUsed.addEdge(e);
            }
        }

        for(GraphGridSquare s : squares){
            if(s.intersects(e.getPolygonRepresentation().getLayoutBounds())){
                s.addEdge(e);
            }

        }
    }

    /**
     * Adds a grid square to the recent changes list which will then be redrawn on the view.
     *
     * @param e - GraphEdge to add
     */
    public void addRecentChange(GraphEdge e){
        for(GraphGridSquare s : squares){
            if(s.isEdgeOfThisSquare(e)){
                recentChanges.add(s);
            }
        }
    }

    /**
     * Clears the recentChanges list.
     */
    public void clearRecentChanges(){
        recentChanges.clear();
    }
}
