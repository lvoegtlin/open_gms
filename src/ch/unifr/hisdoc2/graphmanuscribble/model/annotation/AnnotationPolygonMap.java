package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Holds the collection (List) of all possible annotations.
 */
public class AnnotationPolygonMap{

    /**
     * holds all the different types of annotations.
     */
    private HashMap<String, AnnotationPolygonType> polygonMap;

    public AnnotationPolygonMap(List<AnnotationPolygonType> list){
        this.polygonMap = new HashMap<>();

        init(list);
    }

    /**
     * adds the graphpolygons in a list to the list
     *
     * @param list - the list with graphpolygons to add
     */
    private void init(List<AnnotationPolygonType> list){
        for(AnnotationPolygonType p : list){
            addGraphPolygonType(p);
        }
    }

    /**
     * Add a polygon to the map.
     *
     * @param polygon - The to add polygon
     */
    private void addGraphPolygonType(AnnotationPolygonType polygon){
        if(polygonMap.containsKey(polygon.getName())){
            return;
        }

        polygonMap.put(polygon.getName(), polygon);
    }

    /**
     * Returns the graphpolygon by a given name. If there is no graphpolygon name who matches the given name
     * it returns null
     *
     * @param name - the name of the graphpolygon you are looking for
     * @return - graphpolygon or null
     */
    public AnnotationPolygonType getPolygonByName(String name){
        return polygonMap.get(name);
    }

    public AnnotationPolygonType getPolygonTypeByPolygon(AnnotationPolygon p){
        for(AnnotationPolygonType t : polygonMap.values()){
            if(t.getAnnotationPolygons().contains(p)){
                return t;
            }
        }

        return null;
    }

    /**
     * Returns the whole list with all the graphpolygons
     *
     * @return - the polygonMap
     */
    public HashMap<String, AnnotationPolygonType> getPolygonMap(){
        return polygonMap;
    }

    /**
     * Add a annotated subgraph to a already given graphpolygon. The hit edge from the scribble gets saved as
     * source of the polygon.
     *
     * @param polyGraph - the polygon graph
     * @param edge      - the hit edge
     * @param gPolygon  - the annotation class
     * @return boolean - if the scribble got added
     */
    public boolean addNewScribble(LarsGraph polyGraph, GraphEdge edge, AnnotationPolygonType gPolygon){
        return edge != null && polyGraph != null && gPolygon.addScribble(polyGraph, edge);
    }

    /**
     * Gets the AnnotationPolygon that has the given edge as a part of it.
     *
     * @param edge - to get the AnnotationPolygon
     * @return a AnnotationPolygon or null
     */
    public AnnotationPolygon getPolygonByEdge(GraphEdge edge){
        if(edge == null){
            return null;
        }
        AnnotationPolygon polygon;
        for(AnnotationPolygonType type : polygonMap.values()){
            if((polygon = type.edgeIsPartOfAPolygon(edge)) != null){
                return polygon;
            }
        }
        return null;
    }

    /**
     * Checks if a given edge is part of any annotation. If that is true it returns true else false.
     *
     * @param edge - the edge we want to check
     * @return - true if the edge is part of a annotation
     */
    public boolean isEdgeInAPolygon(GraphEdge edge){
        return getPolygonByEdge(edge) != null;
    }

    /**
     * Checks in all GraphPolygonTypes if they hold a polygon with the given LarsGraph.
     * If true it returns the AnnotationPolygon else it returns null.
     *
     * @param lG - LarsGraph we are looking for
     * @return - Searched AnnotationPolygon or null
     */
    public AnnotationPolygon getGraphPolygonByLarsGraph(LarsGraph lG){
        for(AnnotationPolygonType p : polygonMap.values()){
            AnnotationPolygon gP;
            if((gP = p.getGraphPolygonByLarsGraph(lG)) != null){
                return gP;
            }
        }

        return null;
    }

    /**
     * returns all the colors that are used for the annotations
     *
     * @return - the colors
     */
    public ArrayList<Color> getAllColors(){
        ArrayList<Color> colors = new ArrayList<>();
        for(AnnotationPolygonType p : polygonMap.values()){
            colors.add(p.getColor());
        }

        return colors;
    }
}
