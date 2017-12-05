package ch.unifr.hisdoc2.graphmanuscribble;

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
            case "t":
                binPath = "e-codices_csg-0018_160_max_binary.png";
                oriPath = "e-codices_csg-0018_160_max.jpg";
                break;
            case "p":
                binPath = "e-codices_fmb-cb-0055_0098v_max_binary.png";
                oriPath = "e-codices_fmb-cb-0055_0098v_max.jpg";
                break;
            default:
                binPath = "binary_cpl.png";
                oriPath = "cpl.JPG";
                break;
        }

        Image img = new Image("file:"+binPath);
        Image img2 = new Image("file:"+oriPath);

        GraphImage graphImage = new GraphImage(img2, img);

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

        AngieMSTGraph graph = new AngieMSTGraph(30,
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
        SettingReader settingReader = SettingReader.getInstance();
        UserInput uI = new UserInput(settingReader.getAnnotations());

        ArrayList<AnnotationPolygonType> poly = new ArrayList<>();
        //TODO testing
        settingReader.getAnnotations().forEach(annotationType ->
            poly.add(new AnnotationPolygonType(annotationType))
        );
        AnnotationPolygonMap model = new AnnotationPolygonMap(poly);

        ScrollPane sP = new ScrollPane();
        sP.setContent(pane);

        Scene s = new Scene(sP);

        primaryStage.setScene(s);

        new Controller(graph, model, graphImage, uI, sP);
        primaryStage.show();
    }
}
