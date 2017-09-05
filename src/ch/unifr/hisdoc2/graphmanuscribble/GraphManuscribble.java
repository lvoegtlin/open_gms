package ch.unifr.hisdoc2.graphmanuscribble;/**
 * Created by larsvoegtlin on 25.01.17.
 */

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.io.AnnotationType;
import ch.unifr.hisdoc2.graphmanuscribble.io.SettingReader;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.AngieMSTGraph;
import ch.unifr.hisdoc2.graphmanuscribble.model.image.GraphImage;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonMap;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.scribble.UserInput;
import ch.unifr.hisdoc2.graphmanuscribble.view.GraphView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphManuscribble extends Application{

    private AngieMSTGraph graph;
    private GraphView graphView;
    private GraphImage graphImage;
    private BufferedImage bi;
    private BufferedImage ori;

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage){

        //s = small, m = medium, l = large, h = huge
        String picString = "";
        String binPath = "";
        String oriPath = "";

        switch(picString){
            case "m":
                binPath = "binary_testpage_medium.png";
                oriPath = "testpage_medium.jpg";
                break;
            case "l":
                binPath = "binary_testpage_large.png";
                oriPath = "testpage_large.jpg";
                break;
            case "h":
                binPath = "binary_testpage_huge.png";
                oriPath = "testpage_huge.jpg";
                break;
            default:
                binPath = "binary_cpl.png";
                oriPath = "cpl.JPG";
                break;
        }

        Image img = new Image("file:"+binPath);
        Image img2 = new Image("file:"+oriPath);

        graphImage = new GraphImage(img2, img);

        primaryStage.setHeight(img.getHeight());
        primaryStage.setWidth(img.getWidth());

        File biF = null;
        File oriF = null;
        try{
            biF = new File(binPath);

            oriF = new File(oriPath);

            bi = ImageIO.read(biF);
            ori = ImageIO.read(oriF);
        } catch(IOException e){
            e.printStackTrace();
        }

        graph = new AngieMSTGraph(30,
                true,
                "",
                binPath,
                "gt_testpage.xml",
                img.getWidth(),
                img.getHeight());

        boolean useRelEOnly = graph.getRelevantEdges();
        graph.setRelevantEdges(true);
        graph.setRelevantEdges(useRelEOnly);
        graph.createGraph(bi, ori);
        StackPane pane = new StackPane();
        UserInput uI = new UserInput(SettingReader.getInstance().getAllAnnotations());

        ArrayList<AnnotationPolygonType> poly = new ArrayList<>();
        poly.add(new AnnotationPolygonType(new AnnotationType("test", Color.BLUE)));
        AnnotationPolygonMap model = new AnnotationPolygonMap(poly);

        ScrollPane sP = new ScrollPane();
        sP.setContent(pane);

        Scene s = new Scene(sP);

        primaryStage.setScene(s);

        new Controller(graph, model, graphImage, uI, sP);
        primaryStage.show();
    }
}
