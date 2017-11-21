package ch.unifr.hisdoc2.graphmanuscribble.helper;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interface to the JTS Java Topology Suite - convert data structures to their format and use their algorithms
 */
public final class TopologyUtil{
    public static int[] statisticCounter = new int[2];

    private TopologyUtil() {
    }

    public static List<PointHD2> simplifyPointList(final List<PointHD2> list, double dst) {

        Geometry geo = createGeometryFromPointList(list);

        LarsConcaveHull ch = new LarsConcaveHull(geo, dst);
        geo = ch.getConcaveHull();

        statisticCounter[0] += geo.getNumPoints();

        geo = TopologyPreservingSimplifier.simplify(geo, 0.5);

        statisticCounter[1] += geo.getNumPoints();
        return PointHD2.coordinateList2pointList(Arrays.asList(geo.getCoordinates()));
    }


    /**
     * Creates the concave hull out of a given point cloud. If the parameter is an empty list the hull will be
     * calculated based on the whole set in the hull variable.
     *
     * @param list - we want the concave hull from
     * @return - the concave hull
     */
    public static List<PointHD2> pointListToConcaveHull(List<? extends PointHD2> list, double dst) {

        Geometry geo = createGeometryFromPointList(list);

        if(geo == null){
            return (List<PointHD2>) list;
        }

        LarsConcaveHull hull = new LarsConcaveHull(geo, dst);

        try {
            geo = hull.getConcaveHull();
        } catch (IndexOutOfBoundsException e) {
            list.clear();
            throw e;
        }

        statisticCounter[0] += geo.getNumPoints();
        //System.out.println("Concave hull points: " + geo.getNumPoints());

        geo = geo.buffer(3, 1);
        //System.out.println("geo hull points: " + geo.getNumPoints());
        geo = TopologyPreservingSimplifier.simplify(geo, 1);
        //System.out.println("simplified hull points: " + geo.getNumPoints());
        statisticCounter[1] += geo.getNumPoints();
        return PointHD2.coordinateList2pointList(Arrays.asList(geo.getCoordinates()));
    }

    /**
     * Checks if a given point is in a given concave hull.
     *
     * @param hull - as list of points
     * @param p - polygon to check
     * @return - true if the point is in the hull else false
     */
    public static boolean isPolygonInPolygon(List<PointHD2> hull, javafx.scene.shape.Polygon p){
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> cords = PointHD2.pointList2coordinateList(hull);
        try{
            Polygon poly = gf.createPolygon(cords.toArray(new Coordinate[cords.size()]));
            return shapePolygon2VidPolygon(p).intersects(poly);
        } catch (IllegalArgumentException e){
            //TODO loggin that the linering is not closed
        }

        return false;
    }

    /**
     * Reduces the amount of points in a scibble.
     *
     * @param points - points to reduce
     * @return - reduced point list
     */
    public static List<PointHD2> reducePointsInDoubleList(List<Double> points){
        Coordinate[] cords = new Coordinate[points.size() / 2];

        int i = 0;
        for(int j = 0; j < points.size(); j += 2){
            cords[i] = new Coordinate(points.get(j), points.get(j+1));
            i++;
        }

        Geometry geo = new GeometryFactory().createLineString(cords);

        geo = TopologyPreservingSimplifier.simplify(geo, 0.5);

        return PointHD2.coordinateList2pointList(Arrays.asList(geo.getCoordinates()));
    }

    /**
     * Creates the union of a list of points which represents hulls.
     *
     * @param hulls - the hulls
     * @return - the union of all hulls
     */
    public static List<PointHD2> getUnionOfHulls(List<List<PointHD2>> hulls){
        List<Geometry> geoms = new ArrayList<>();
        hulls.forEach(list -> geoms.add(createGeometryFromPointList(list)));

        Geometry geom = geoms.get(0);
        geoms.remove(0);
        for(Geometry g : geoms){
            geom = geom.union(g);
        }

        if(geom == null){
            return hulls.get(0);
        }

        return PointHD2.coordinateList2pointList(Arrays.asList(geom.getCoordinates()));
    }

    /**
     * Creates out of a list of PointHD2 a Geometry (JTS Polygon).
     *
     * @param list - points to create the geometry
     * @return - the geometry representation
     */
    private static Geometry createGeometryFromPointList(List<? extends PointHD2> list){
        int ringsize = Math.max(list.size() + 1, 4);

        Coordinate[] coordinates = new Coordinate[ringsize];
        int i = 0;
        for (PointHD2 p : list) {
            coordinates[i++] = p.toCoordinate();
        }
        coordinates[i] = list.get(0).toCoordinate();

        if(i < 4){
            return null;
        }

        LinearRing ring = new GeometryFactory().createLinearRing(coordinates);

        return new GeometryFactory().createPolygon(ring, null);
    }

    /**
     * Translates a javafx polygon to a vividsolutions polygon.
     *
     * @param p - the javafx polygon
     * @return - the vivid polygon
     */
    private static Geometry shapePolygon2VidPolygon(javafx.scene.shape.Polygon p){
        Coordinate[] cords = new Coordinate[p.getPoints().size()/2];
        for(int i = 0; i < p.getPoints().size(); i += 2){
            Coordinate cor = new Coordinate(p.getPoints().get(i), p.getPoints().get(i+1));
            cords[i/2] = cor;
        }

        if(cords.length == 1){
            return new GeometryFactory().createPoint(cords[0]);
        } else {
            return new GeometryFactory().createLineString(cords);
        }
    }
}
