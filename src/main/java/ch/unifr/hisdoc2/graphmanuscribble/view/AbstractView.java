package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.view.helper.svg.SVGPathPrinter;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The standard interface for all the views of the Graphmanuscribble
 */
public abstract class AbstractView{

    private Group group;
    private HashMap<AnnotationType, SVGPath> svgPaths;

    Controller controller;
    HashMap<AnnotationType, SVGPathPrinter> svgPathPrinters;

    /**
     * Manages all the svg drawing and provides them to the different views.
     * There wont be a layer for colors with just different opacity
     *
     * @param cnt - the controller
     * @param layerColors - the different colors that the layer should draw in
     * @param polygon - is it the polygonview
     */
    AbstractView(Controller cnt, ArrayList<AnnotationType> layerColors, boolean polygon){
        this.controller = cnt;
        Canvas can = new Canvas(cnt.getWidth(), cnt.getHeight());
        this.svgPaths = new HashMap<>();
        this.svgPathPrinters = new HashMap<>();

        this.group = new Group();
        group.getChildren().add(can);

        for(AnnotationType a : layerColors){
            //paths
            SVGPath path = new SVGPath();
            Color c = a.getColor();
            if(polygon){
                path.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.4f));
                path.setStroke(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.8f));
            } else {
                path.setFill(null);
                path.setStroke(c);
            }
            svgPaths.put(a, path);
            group.getChildren().add(path);
            path.setStrokeWidth(Constants.STROKE_LINE_WIDTH);

            //printers
            SVGPathPrinter printer = new SVGPathPrinter();
            svgPathPrinters.put(a, printer);
        }
    }

    /**
     * isPolygonview is per default false
     *
     * @param cnt - the controller
     * @param layerColors - the different colors that the layer should draw in
     */
    AbstractView(Controller cnt, ArrayList<AnnotationType> layerColors){
        this(cnt,layerColors, false);
    }

    /**
     * Adds a new anotation to the system. So it will be added to the svg part.
     *
     * @param type - the new type
     * @param polygon - if its called from the polygonView
     */
    protected void addNewAnnotation(AnnotationType type, boolean polygon){
        //paths
        SVGPath path = new SVGPath();
        Color c = type.getColor();
        if(polygon){
            path.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.4f));
            path.setStroke(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.8f));
        } else {
            path.setFill(null);
            path.setStroke(c);
        }
        svgPaths.put(type, path);
        group.getChildren().add(path);
        path.setStrokeWidth(Constants.STROKE_LINE_WIDTH);

        //printers
        SVGPathPrinter printer = new SVGPathPrinter();
        svgPathPrinters.put(type, printer);
    }

    /**
     * Tells the user if the view is currently visible or not
     *
     * @return boolean if its shown (true)
     */
    public boolean isShown(){
        return group.isVisible();
    }

    /**
     * Sets the constructed svg path string as content of the svg path object.
     * Needs to be called to display the svg path on the javafx node
     *
     * @param c - the color it has to be drawn
     */
    void setSVGPath(AnnotationType c){
        if(!svgPaths.containsKey(c)){
            return;
        }

        svgPaths.get(c).setContent(svgPathPrinters.get(c).toString());
    }


    /**
     * The View adds itself to a given stack pane
     */
    public void addToStackPane(StackPane pane){
        pane.getChildren().add(group);
    }

    /**
     * updates the view
     */
    protected abstract void update();

    /**
     * hides the View
     */
    public void hide(){
        group.setVisible(false);
    }

    /**
     * Shows the view
     */
    public void show(){
        group.setVisible(true);
        update();
    }
}
