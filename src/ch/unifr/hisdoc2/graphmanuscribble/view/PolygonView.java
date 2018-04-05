package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;
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
    private void drawPoly(List<PointHD2> pts, Color color){
        double[] x = new double[pts.size()];
        double[] y = new double[pts.size()];
        int i = 0;
        for(PointHD2 p : pts){
            x[i] = p.getX();
            y[i] = p.getY();
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
                if(polygon.getHull() == null){
                    continue;
                }
                drawPoly(polygon.getHull(), polygonType.getColor());
            }
            //set the newly created string
            setSVGPath(polygonType.getColor());
        }
    }
}
