package ch.unifr.hisdoc2.graphmanuscribble.model.scribble;

import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
import javafx.scene.shape.Polygon;

import java.util.*;

/**
 * Manages the annotationScribbles of the user. It saves all the annotationScribbles as polygon. The class is the model class
 * of the userInteractionView
 */
public class UserInput {

    /**
     * List with the user annotationScribbles as polygons
     */
    private HashMap<AnnotationType, ArrayList<Polygon>> annotationScribbles;
    /**
     * The annotationtype of the delete
     */
    private ArrayList<Polygon> deleteScribbles;
    /**
     * If the mouse is dragged that represents the current drawn polygon
     */
    private Polygon current;

    public UserInput(ArrayList<AnnotationType> list){
        this.deleteScribbles = new ArrayList<>();
        this.annotationScribbles = new HashMap<>();
        init(list);
    }

    /**
     * Initializes the hashmap with all the annotations that were loaded from the settings file.
     *
     * @param list
     */
    private void init(ArrayList<AnnotationType> list){
        list.forEach(type -> annotationScribbles.put(type, new ArrayList<>()));
    }

    public HashMap<AnnotationType, ArrayList<Polygon>> getAnnotationScribbles() {
        return annotationScribbles;
    }

    public ArrayList<Polygon> getDeleteScribbles(){
        return deleteScribbles;
    }

    /**
     * Adds a scribble to the hashmap. It needs the scribble as polygon, an annotationType and a flag if the polygon
     * is connected to the last one.
     *
     * @param a - the current annotationType
     * @param s - the scribble the user did
     * @param connected - if its connected with the last scribble
     */
    public void addScribble(AnnotationType a, Polygon s, boolean connected, boolean delete){
        if(!connected){
            current = null;
        }

        //if the annotationType is already in the map
        if(annotationScribbles.containsKey(a) || delete){
            //if we have a current dragged polygon
            if(current != null){
                current.getPoints().addAll(s.getPoints());
            } else {
                //checks if its connected with the last scribble
                if(connected && !delete){
                    current = s;
                    annotationScribbles.get(a).add(s);
                } else if(connected){
                    current = s;
                    deleteScribbles.add(s);
                }
            }
        } else {
            ArrayList<Polygon> list = new ArrayList<>();
            list.add(s);
            current = s;
            annotationScribbles.put(a, list);
        }
    }

    /**
     * Deletes all the annotation scribbles from a given larsGraphCollection.
     *
     * @param lGC
     * @param type
     */
    public void deleteScribbles(LarsGraphCollection lGC, AnnotationType type){
        lGC.getAnnotationGraphs().forEach(larsGraph -> deleteScribble(larsGraph, type));
    }

    /**
     * Deletes a scribble from the scribble list so that it will not longer be displayed on screen.
     *
     * @param lG -
     */
    private void deleteScribble(LarsGraph lG, AnnotationType type){
        ArrayList<Polygon> scribbles = annotationScribbles.get(type);
        scribbles.removeIf(lG::isIntersectingWith);
    }
}
