package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;

import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import javafx.scene.paint.Color;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent all polygon of a given annotation.
 * E.G. All text line polygons are save in one object like this one.
 */
public class AnnotationPolygonType{

    private AnnotationType type;
    private List<AnnotationPolygon> annotationPolygons;

    public AnnotationPolygonType(AnnotationType type){
        this.type = type;
        this.annotationPolygons = new ArrayList<>();
    }

    /**
     * The fill color the polygon should have on the polygon view canvas
     *
     * @return - javafx Color
     */
    public Color getColor(){
        return type.getColor();
    }

    /**
     * Returns the name of the annotation
     *
     * @return - string name
     */
    public String getName(){
        return type.getName();
    }

    /**
     * Returns the annotationPolygonType with the given name
     *
     * @param n - name of the annotationPolygonType
     * @return - the annotationPolygonType
     */
    public AnnotationPolygonType getByName(String n){
        if(type.getName().equals(n)){
            return this;
        }

        return null;
    }

    /**
     * Returns the list of polygons that are represented by this annotation.
     *
     * @return - list of polygons
     */
    public List<AnnotationPolygon> getAnnotationPolygons(){
        return annotationPolygons;
    }

    /**
     * Checks if a edge is part of this annotation type and returns the GraPolygon the edge belongs to.
     * If the edge does not belong to this annotation type it returns null.
     *
     * @param edge - edge to check
     * @return - AnnotationPolygon or null
     */
    public AnnotationPolygon edgeIsPartOfAPolygon(GraphEdge edge){
        for(AnnotationPolygon p : annotationPolygons){
            if(p.isEdgePartofPolygon(edge)){
                return p;
            }
        }
        return null;
    }

    /**
     * Adds a new polgyon base on a graph to the annotation.
     *
     * @param polyGraph - the graph the polygon will surround
     * @param source    - the annotation graph
     * @return boolean - does it add the scribble or not.
     */
    public boolean addScribble(LarsGraph polyGraph, SimpleGraph source){
        AnnotationPolygon annotationPolygon = annotationPolygonByLarsGraph(polyGraph);
        if(annotationPolygon != null){
            //add the new hitting to the list of sources
            annotationPolygon.addSource(source);
            return false;
        } else {
            annotationPolygons.add(new AnnotationPolygon(source, polyGraph));
            polyGraph.setAnnotated(true);
            return true;
        }
    }

    private AnnotationPolygon annotationPolygonByLarsGraph(LarsGraph graph){
        for(AnnotationPolygon p : annotationPolygons){
            if(p.getLarsGraph() == graph){
                return p;
            }
        }

        return null;
    }

    /**
     * Gets a annotation polygon by its LarsGraph. If there is non it returns null
     *
     * @param lG - LarsGraph we are looking for
     * @return - Searched AnnotationPolygon or null
     */
    public AnnotationPolygon getGraphPolygonByLarsGraph(LarsGraph lG){
        for(AnnotationPolygon p : annotationPolygons){
            if(p.getLarsGraph() == lG){
                return p;
            }
        }

        return null;
    }
}
