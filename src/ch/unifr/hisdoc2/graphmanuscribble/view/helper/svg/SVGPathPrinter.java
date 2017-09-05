package ch.unifr.hisdoc2.graphmanuscribble.view.helper.svg;


/**
 * creates the svg path string.
 */
public class SVGPathPrinter{

    /**
     * Contains the actual svg path
     */
    StringBuffer sB;

    public SVGPathPrinter(){
        this.sB = new StringBuffer();
    }

    /**
     * Adds an edge to the svg path string.
     *
     * @param x1 - x coordinate of the source of the edge
     * @param x2 - x coordinate of the target of the edge
     * @param y1 - y coordinate of the source of the edge
     * @param y2 - y coordinate of the target of the edge
     */
    public void addEdge(double x1, double x2, double y1, double y2){
        sB.append("M ")
                .append(x1)
                .append(",")
                .append(y1)
                .append(" L")
                .append(x2)
                .append(",")
                .append(y2);
    }

    private void addPointToLine(double x1, double y1){
        sB.append(" L")
                .append(x1)
                .append(" ,")
                .append(y1);
    }

    /**
     * Draws a polyline form the given x and y coordinates.
     *
     * @param x - the x coordinates of the points
     * @param y - the y coordinates of the points
     * @param nbOfPoints - number of points
     */
    public void addPolyLine(double[] x, double[] y, int nbOfPoints){
        sB.append("M")
                .append(x[0])
                .append(" ,")
                .append(y[0]);
        for(int i = 1; i < nbOfPoints - 1; i++){
            addPointToLine(x[i], y[i]);
        }
    }

    /**
     * Fills a given polygon and draws it outlines
     *
     * @param x - x coordinates of the points
     * @param y - y coordinates of the points
     */
    public void addPolygon(double[] x, double[] y){
        //fill area
        sB.append("M ")
                .append(x[0])
                .append(" ,")
                .append(y[0]);
        for(int i = 1; i < x.length - 1; i++){
            sB.append("L")
                    .append(x[i])
                    .append(" ,")
                    .append(y[i]);
        }
        sB.append(" Z");
    }

    /**
     * Clears the svg path string
     */
    public void clear(){
        sB.delete(0, sB.length());
    }

    @Override
    public String toString(){
        return sB.toString();
    }

}
