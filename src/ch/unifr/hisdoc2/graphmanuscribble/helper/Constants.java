package ch.unifr.hisdoc2.graphmanuscribble.helper;


import javafx.scene.paint.Color;

/**
 * Created by larsvoegtlin on 30.01.17.
 */
public class Constants{

    //TODO TEST CONSTANTS
    public static final int CONCAVE_TIGHTNESS = 50;
    public static final double EDGE_CUT_PRECENTAGE = 2.5;

    //graphview
    public static final int VERTEX_DIAMETER = 5;
    public static final Color STROKE_COLOR = Color.color(0,1,0);

    //polygonview
    public static final int STROKE_LINE_WIDTH = 2;

    //UserInteractionView
    public static final Color INTERACTION_STROKE_COLOR = Color.RED;

    //Grid
    public static final int GRID_SQUARE_WIDTH = 100;
    public static final int GRID_SQUARE_HEIGHT = 100;

    //Quadtree
    public static final int MAX_OBJECTS = 10;
    public static final int MAX_LEVELS = 5;

    //input
    public static final int REFRESH_TIME = 50; //in ms
    public static final double SCALE_DELTA = 1.1;

}
