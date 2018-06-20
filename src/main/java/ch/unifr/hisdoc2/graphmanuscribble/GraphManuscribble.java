package ch.unifr.hisdoc2.graphmanuscribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class GraphManuscribble extends Application{

    private URL fxmlURL;

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void init() throws Exception{
        super.init();

        File f = getResource("/configs/settings.xml");

        if(f == null){
            System.err.println("settings.xml file is missing!");
            System.exit(1);
        }

        fxmlURL = getClass().getClassLoader().getResource("fxml/graphManuscribble.fxml");

        if(fxmlURL == null){
            System.err.println("Main fxml file not found!");
            System.exit(1);
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException{

        // load the fxml and add it to the primstage
        Parent root = FXMLLoader.load(fxmlURL);

        primaryStage.setTitle("Registration Form FXML Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static File getResource(String fileName){
        URL url = GraphManuscribble.class.getClass().getResource((fileName));

        if(url == null){
            //TODO logging
            return null;
        }

        try{
            return new File(url.toURI());
        } catch(URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }
}
