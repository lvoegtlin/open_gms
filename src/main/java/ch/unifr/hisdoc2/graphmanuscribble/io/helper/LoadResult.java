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
    private BufferedImage ori;
    private BufferedImage bin;
    private File graph;
    private Dimension2D dim;
    private String fileName;

    public LoadResult(LoadImageStatus status, String ori, String bin, String graph){
        this.statu = status;
        try{
            if(!ori.isEmpty()){
                fileName = FilenameUtils.getName(ori);
                this.ori = ImageIO.read(new File(ori));
            }
            if(!bin.isEmpty()){
                this.bin = ImageIO.read(new File(bin));
            }
            if(!graph.isEmpty()){
                this.graph = new File(graph);
            }

            dim = new Dimension2D(this.ori.getWidth(), this.ori.getHeight());
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public LoadImageStatus getStatus(){
        return statu;
    }

    public String getFileName(){
        return fileName;
    }

    public BufferedImage getOri(){
        return ori;
    }

    public BufferedImage getBin(){
        return bin;
    }

    public File getGraph(){
        return graph;
    }

    public Dimension2D getDim(){
        return dim;
    }
}
