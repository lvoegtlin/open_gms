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
public class UserInput{

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

    //undo/redo
    private Set<Polygon> undoneDeleteScribbles;

    public UserInput(List<AnnotationType> list){
        this.deleteScribbles = new ArrayList<>();
        this.undoneDeleteScribbles = new HashSet<>();
        this.annotationScribbles = new HashMap<>();
        init(list);
    }

    /**
     * Initializes the hashmap with all the annotations that were loaded from the settings file.
     *
     * @param list
     */
    private void init(List<AnnotationType> list){
        list.forEach(type -> annotationScribbles.put(type, new ArrayList<>()));
    }

    public HashMap<AnnotationType, ArrayList<Polygon>> getAnnotationScribbles(){
        return annotationScribbles;
    }

    public ArrayList<Polygon> getDeleteScribbles(){
        return deleteScribbles;
    }

    /**
     * Adds a scribble to the hashmap. It needs the scribble as polygon, an annotationType and a flag if the polygon
     * is connected to the last one.
     *
     * @param a         - the current annotationType
     * @param s         - the scribble the user did
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
    public void deleteAnnotationScribbles(LarsGraphCollection lGC, AnnotationType type){
        lGC.getAnnotationGraphs().forEach(larsGraph -> deleteAnnotationScribble(larsGraph, type));
    }

    /**
     * Deletes a scribble from the scribble list so that it will not longer be displayed on screen.
     *
     * @param lG -
     */
    private void deleteAnnotationScribble(LarsGraph lG, AnnotationType type){
        ArrayList<Polygon> scribbles = annotationScribbles.get(type);
        scribbles.removeIf(lG::isIntersectingWith);
    }

    /**
     * Undo a scribble or redo it. It can be an annotation or a delete scribble.
     *
     * @param polygon       - the polygon that represents the scribble
     * @param undo          - true if you want to undo the scribble. False if redo
     */
    public void undoRedoScribble(Polygon polygon, boolean undo){
        Polygon p;
        if(polygon == null){
            return;
        }

        if((p = getPolygonByFragment(polygon, undo)) == null){
            return;
        }

        if(undo){
            deleteScribbles.remove(p);
            undoneDeleteScribbles.add(p);
        } else {
            deleteScribbles.add(p);
            undoneDeleteScribbles.remove(p);
        }
    }

    /**
     * Searches in the given space (delete or not) for a polygon where the given polygon fragment is a subset of.
     * Returns the polygon if found else null
     *
     * @param p - the polygon that represents the scribble
     * @param undo - undo or a redo
     * @return
     */
    private Polygon getPolygonByFragment(Polygon p, boolean undo){
        Optional<Polygon> scribblePolygon;
        if(undo){
            scribblePolygon = deleteScribbles
                    .parallelStream()
                    .filter(polygon -> polygon.getPoints().containsAll(p.getPoints()))
                    .findFirst();
        } else {
            scribblePolygon = undoneDeleteScribbles
                    .parallelStream()
                    .filter(polygon -> polygon.getPoints().containsAll(p.getPoints()))
                    .findFirst();
        }

        return scribblePolygon.orElse(null);

    }
}
