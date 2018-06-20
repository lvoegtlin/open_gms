package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.model.scribble.UserInput;
import ch.unifr.hisdoc2.graphmanuscribble.view.helper.svg.SVGPathPrinter;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

/**
 * Draws the input scribbles of the user to the screen.
 */
public class UserInteractionView extends AbstractView{

    private UserInput userInput;

    public UserInteractionView(UserInput input, Controller controller){
        super(controller, SettingReader.getInstance().getAllAnnotations());
        this.userInput = input;

        show();
    }

    public void addNewAnnotationType(AnnotationType type){
        addNewAnnotation(type, false);
    }

    @Override
    public void update(){
        AnnotationType type = controller.getCurrentAnnotationType();
        SVGPathPrinter printer = svgPathPrinters.get(type);

        if(printer == null){
            //TODO log something went wrong
            //todo show dialog
            return;
        }

        ArrayList<Polygon> scribbles;
        if(type.isDelete()){
            scribbles = userInput.getDeleteScribbles();
        } else {
            scribbles = userInput.getAnnotationScribbles().get(type);
        }

        printer.clear();
        for(Polygon polygon : scribbles){
            double[] x = new double[polygon.getPoints().size() / 2];
            double[] y = new double[polygon.getPoints().size() / 2];

            for(int i = 0; i < polygon.getPoints().size(); i += 2){
                x[i / 2] = polygon.getPoints().get(i);
                y[i / 2] = polygon.getPoints().get(i + 1);
            }

            printer.addPolyLine(x, y, x.length);
        }

        setSVGPath(type);
    }
}
