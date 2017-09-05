package ch.unifr.hisdoc2.graphmanuscribble.model.graph;


import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

/**
 * Created by larsvoegtlin on 09.12.16.
 */
public class GraphVertex extends PointHD2{

    /**
     * If the vertex was deleted or not
     */
    private boolean deleted = false;

    /**
     * If the vertex is generated or inserted by the user interaction.
     */
    private boolean userAdded = false;

    /**
     * Creates a new GraphVertex Object at {@param x} and {@param y}
     * with the polygon type {@param type}.
     *
     * @param x - double x-coordinate position
     * @param y - double y-coordinate position
     */
    public GraphVertex(float x, float y){
        super(x, y);
    }

    public GraphVertex(double x, double y){
        this((float) x, (float) y);
    }

    public GraphVertex(double x, float y){
        this((float) x, y);
    }

    /**
     * Tells if the user deleted this vertex or not.
     *
     * @return - boolean (default false)
     */
    public boolean isDeleted(){
        return deleted;
    }

    /**
     * Sets if the user deleted the vertex
     *
     * @param deleted - boolean
     */
    public void setDeleted(boolean deleted){
        this.deleted = deleted;
    }

    /**
     * Tells if the user added this vertex.
     *
     * @return - boolean (default false)
     */
    public boolean isUserAdded(){
        return userAdded;
    }

    /**
     * Sets if the user added this vertex.
     *
     * @param userAdded - boolean
     */
    public void setUserAdded(boolean userAdded){
        this.userAdded = userAdded;
    }

}
