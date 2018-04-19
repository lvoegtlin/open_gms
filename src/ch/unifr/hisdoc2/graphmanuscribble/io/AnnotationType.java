package ch.unifr.hisdoc2.graphmanuscribble.io;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents an annotation type that the user defines in the settings.xml file as well as standard types like delete.
 */
public class AnnotationType{

    private String name;
    private Color color;
    private boolean delete;

    AnnotationType(String name, Color color, boolean delete){
        this.name = name;
        this.color = color;
        this.delete = delete;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public boolean isDelete(){
        return delete;
    }

    /**
     * Creates the list of all the given AnnotationTypes as colors.
     *
     * @param list - annotationsTypes we cant the colors of
     * @return - annotationTypes as color list
     */
    public static ArrayList<Color> annotationTypeToColorList(ArrayList<AnnotationType> list){
        ArrayList<Color> colors = new ArrayList<>();
        list.parallelStream().forEach(annotationType -> colors.add(annotationType.getColor()));
        return colors;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(obj == null){
            return false;
        }

        if(getClass() != obj.getClass()){
            return false;
        }

        AnnotationType t = (AnnotationType) obj;

        return Objects.equals(t.delete, this.delete)
                || Objects.equals(t.name, this.name)
                || Objects.equals(t.color, this.color);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, color, delete);
    }
}
