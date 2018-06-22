package ch.unifr.hisdoc2.graphmanuscribble.io.helper;

import ch.unifr.hisdoc2.graphmanuscribble.helper.LoadImageStatus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.geometry.Dimension2D;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;

public class LoadResult{
    private LoadImageStatus statu;
    private File ori;
    private File bin;
    private File graph;
    private Dimension2D dim;

    public LoadResult(LoadImageStatus status, String ori, String bin, String graph){
        this.statu = status;
        if(ori != null){
            this.ori = new File(ori);
        }
        if(bin != null){
            this.bin = new File(bin);
        }
        if(graph != null){
            this.graph = new File(graph);
        }

        BufferedImage bOri = null;
        try{
            bOri = ImageIO.read(this.ori);
            dim = new Dimension2D(bOri.getWidth(), bOri.getHeight());
        } catch(IOException e){
            dim = new Dimension2D(0,0);
            e.printStackTrace();
        }
    }

    public LoadImageStatus getStatus(){
        return statu;
    }

    public File getOri(){
        return ori;
    }

    public File getBin(){
        return bin;
    }

    public File getGraph(){
        return graph;
    }

    public String getOriExtension(){
        return FilenameUtils.getExtension(ori.getName());
    }

    public Dimension2D getDim(){
        return dim;
    }
}
