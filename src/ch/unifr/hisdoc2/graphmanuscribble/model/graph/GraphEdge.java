package ch.unifr.hisdoc2.graphmanuscribble.model.graph;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import javafx.scene.shape.Polygon;
import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * An OrientedWeighted Edge
 */
public class GraphEdge extends DefaultWeightedEdge{

    /**
     * If the vertex was deleted or not
     */
    private boolean deleted = false;

    /**
     * If the vertex is generated or inserted by the user interaction.
     */
    private boolean userAdded = false;

    /**
     * The Edge as a polygon
     */
    private Polygon polygonRepresentation;

    /**
     * checks if the polygon representation is already done.
     */
    private boolean polyRepCreated = false;

    private double angle = -1;

    private static double computeAngle(PointHD2 p1, PointHD2 p2){
        if(p2.y < p1.y){
            return new LineSegment(new Coordinate(p2.x, p2.y), new Coordinate(p1.x, p1.y)).angle();
        }
        return new LineSegment(new Coordinate(p1.x, p1.y), new Coordinate(p2.x, p2.y)).angle();
    }

    public double getOrientation(PointHD2 p1, PointHD2 p2){
        if(angle == -1){
            this.angle = Angle.normalizePositive(computeAngle(p1, p2));

        }
        return this.angle;
    }

    public double getOrientation(){
        if(this.angle < 0) throw new IllegalArgumentException("angle not computed yet.");
        return this.angle;
    }

    /**
     * Tells if the user deleted this edge or not.
     *
     * @return - boolean (default false)
     */
    public boolean isDeleted(){
        return deleted;
    }

    /**
     * Sets if the user deleted the edge.
     *
     * @param deleted - boolean
     */
    public void setDeleted(boolean deleted){
        this.deleted = deleted;
    }

    /**
     * Tells if the user added this edge.
     *
     * @return - boolean (default false)
     */
    public boolean isUserAdded(){
        return userAdded;
    }

    /**
     * Sets if the user added this edge.
     *
     * @param userAdded - boolean
     */
    public void setUserAdded(boolean userAdded){
        this.userAdded = userAdded;
    }

    /**
     * creates a Polygon representation of the edge. If the representation is already done
     * this method wont do anything.
     *
     * @param v1 - GraphVertex source
     * @param v2 - GraphVertex target
     */
    public void createPolygonRepresentation(GraphVertex v1, GraphVertex v2){
        if(!polyRepCreated){
            polyRepCreated = true;
            polygonRepresentation = new Polygon(v1.getX(), v1.getY(), v2.getX(), v2.getY());
        }
    }

    /**
     * Returns the polygon representation of the edge
     *
     * @return - Polygon
     */
    public Polygon getPolygonRepresentation(){
        return polygonRepresentation;
    }
}
