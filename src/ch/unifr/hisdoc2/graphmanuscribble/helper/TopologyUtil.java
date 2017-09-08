package ch.unifr.hisdoc2.graphmanuscribble.helper;

import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

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

        int ringsize = Math.max(list.size() + 1, 4);

        Coordinate[] coordinates = new Coordinate[ringsize];
        int i = 0;
        for (PointHD2 p : list) {
            coordinates[i++] = p.toCoordinate();
        }
        coordinates[i] = list.get(0).toCoordinate();
        LinearRing ring = new GeometryFactory().createLinearRing(coordinates);

        Geometry geo = new GeometryFactory().createPolygon(ring, null);

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

        int ringsize = Math.max(list.size() + 1, 4);

        Coordinate[] coordinates = new Coordinate[ringsize];
        int i = 0;
        for (PointHD2 p : list) {
            coordinates[i++] = p.toCoordinate();
        }
        coordinates[i] = list.get(0).toCoordinate();
        LinearRing ring = new GeometryFactory().createLinearRing(coordinates);

        Geometry geo = new GeometryFactory().createPolygon(ring, null);

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
}
