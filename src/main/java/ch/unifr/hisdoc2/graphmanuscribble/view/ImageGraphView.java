package ch.unifr.hisdoc2.graphmanuscribble.view;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.model.image.GraphImage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

/**
 * Created by larsvoegtlin on 16.01.17.
 */
public class ImageGraphView extends AbstractView{

    /**
     * The Imageview (scene) we add the image to.
     */
    private ImageView view;
    private GraphImage imageModel;

    public ImageGraphView(GraphImage model, Controller controller){
        super(controller, new ArrayList<>());
        this.imageModel = model;
        this.view = new ImageView();

        show();
    }

    public void setImageModel(GraphImage imageModel){
        this.imageModel = imageModel;
    }

    @Override
    public void addToStackPane(StackPane pane){
        pane.getChildren().add(view);
    }

    @Override
    public void update(){
        view.setImage(imageModel.getCurrentImage());
    }


}
