package ch.unifr.hisdoc2.graphmanuscribble.model.annotation;

import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.LarsGraphCollection;
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
    private HashMap<AnnotationType, AnnotationPolygonType> polygonMap;

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
        if(polygonMap.containsKey(polygon.getType())){
            return;
        }

        polygonMap.put(polygon.getType(), polygon);
    }

    /**
     * Returns the graphpolygon by a given annotationType. If there is no graphpolygon name who matches the given type
     * it returns null
     *
     * @param type - the AnnotationType of the graphpolygon you are looking for
     * @return - graphpolygon or null
     */
    public AnnotationPolygonType getPolygonByAnnotationType(AnnotationType type){
        return polygonMap.get(type);
    }

    /**
     * Gives the annotationPolygonType from a given annotationPolygon
     *
     * @param p - AnnotationPolygon we want the type of
     * @return The annotationPolygonType
     */
    public AnnotationPolygonType getAnnotationPolygonTypeByPolygon(AnnotationPolygon p){
        for(AnnotationPolygonType t : polygonMap.values()){
            if(t.getAnnotationPolygons().contains(p)){
                return t;
            }
        }

        return null;
    }

    /**
     * Gives the annotationType from a given annotationPolygon
     *
     * @param p - AnnotationPolygon we want the type of
     * @return The annotationPolygonType
     */
    public AnnotationType getAnnotationTypeByPolygon(AnnotationPolygon p){
        for(AnnotationPolygonType t : polygonMap.values()){
            if(t.getAnnotationPolygons().contains(p)){
                return t.getType();
            }
        }

        return null;
    }

    /**
     * Returns the whole list with all the graphpolygons
     *
     * @return - the polygonMap
     */
    public HashMap<AnnotationType, AnnotationPolygonType> getPolygonMap(){
        return polygonMap;
    }

    /**
     * Add a annotated subgraph to a already given graphpolygon. The hit edge from the scribble gets saved as
     * source of the polygon.
     *
     * @param polyGraph - the polygon graph
     * @param annotationGraph - the graph that is the source of the annotation
     * @param type  - the annotation type
     * @return boolean - if a new annotation
     */
    public boolean addNewScribble(LarsGraphCollection polyGraph,
                                  LarsGraph annotationGraph,
                                  AnnotationType type){
        return annotationGraph != null
                && polyGraph != null
                && getPolygonByAnnotationType(type).addScribble(polyGraph, annotationGraph);
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
     * Checks in all GraphPolygonTypes if they hold a polygon with the given LarsGraphCollection.
     * If true it returns the AnnotationPolygon else it returns null.
     *
     * @param lG     - LarsGraphCollection we are looking for
     * @param filter - limits the search list. null if no filter
     * @return - Searched AnnotationPolygon or null
     */
    public AnnotationPolygon getGraphPolygonByLarsGraph(LarsGraphCollection lG, AnnotationType filter){
        if(filter != null){
            AnnotationPolygon gP;
            if((gP = polygonMap.get(filter).getGraphPolygonByLarsGraph(lG)) != null){
                return gP;
            }
        } else {
            for(AnnotationPolygonType p : polygonMap.values()){
                AnnotationPolygon gP;
                if((gP = p.getGraphPolygonByLarsGraph(lG)) != null){
                    return gP;
                }
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

    /**
     * Gets all the edge sources and graph sources of an annotationPolygon and ads them to e given destination polygon.
     * After the AnnotationPolygons get deleted
     *
     * @param lGs - list with larsgraph which polygons have to be deleted
     * @param currentAnnotation - the current annotationtype
     */
    public void addEdgeSourceToAnnoPolygonAndDeleteAnnoPolygons(ArrayList<LarsGraphCollection> lGs,
                                                                LarsGraphCollection dest,
                                                                AnnotationType currentAnnotation){
        AnnotationPolygon destPoly = getGraphPolygonByLarsGraph(dest, currentAnnotation);
        lGs.forEach(larsGraph -> polygonMap.get(currentAnnotation).transferAndDeleteAnnotationPolygon(larsGraph, destPoly));
    }
}
