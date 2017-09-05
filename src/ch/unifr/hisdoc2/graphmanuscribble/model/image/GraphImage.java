package ch.unifr.hisdoc2.graphmanuscribble.model.image;

import javafx.scene.image.Image;

/**
 * Created by larsvoegtlin on 02.03.17.
 */
public class GraphImage{

    private Image binImage;
    private Image orgImage;
    private boolean seeOrgImg;
    private double width;
    private double height;

    public GraphImage(Image original, Image binary){
        this.orgImage = original;
        this.binImage = binary;
        this.seeOrgImg = true;
        this.width = original.getWidth();
        this.height = original.getHeight();
    }

    public Image getCurrentImage(){
        if(seeOrgImg){
            return orgImage;
        } else {
            return binImage;
        }
    }

    public void setBinImage(Image binImage) {
        this.binImage = binImage;
    }

    public void setOrgImage(Image orgImage) {
        this.orgImage = orgImage;
    }

    public void setSeeOrgImg(boolean seeOrgImg) {
        this.seeOrgImg = seeOrgImg;
    }

    public boolean isSeeOrgImg() {
        return seeOrgImg;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
