package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Created by larsvoegtlin on 29.12.16.
 */
public class PolygonView extends AbstractView{

    private AnnotationPolygonMap graphPolygon;

    public PolygonView(AnnotationPolygonMap graphPolygon, Controller controller){
        super(controller, graphPolygon.getAllColors(), true);
        this.graphPolygon = graphPolygon;

        show();
    }

    /**
     * Draws the polygon given by the {@param pts}.
     *
     * @param pts - GraphVertex List that describes the polygon
     * @param color - the color of the polygon
     */
    public void drawPoly(LinearRing pts, Color color){
        double[] x = new double[pts.getNumPoints()];
        double[] y = new double[pts.getNumPoints()];
        int i = 0;
        for(Coordinate c : pts.getCoordinates()){
            x[i] = c.x;
            y[i] = c.y;
            i++;
        }

        svgPathPrinters.get(color).addPolygon(x,y);
    }

    @Override
    public void update(){
        for(AnnotationPolygonType polygonType : graphPolygon.getPolygonMap().values()){
            //clear the string of these annotationType
            svgPathPrinters.get(polygonType.getColor()).clear();
            for(AnnotationPolygon polygon : polygonType.getAnnotationPolygons()){
                if(polygon.getContourPoints() == null){
                    continue;
                }
                drawPoly(polygon.getContourPoints(), polygonType.getColor());
            }
            //set the newly created string
            setSVGPath(polygonType.getColor());
        }
    }
}
