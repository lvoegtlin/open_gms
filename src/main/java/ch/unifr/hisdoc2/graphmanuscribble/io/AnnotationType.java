package ch.unifr.hisdoc2.graphmanuscribble.io;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Represents an annotation type that the user defines in the settings.xml file as well as standard types like delete.
 */
public class AnnotationType{

    private String name;
    private Color color;
    private boolean delete;
    private boolean deleteAnnotation;

    public AnnotationType(String name, Color color, boolean deleteAnnotation, boolean delete){
        this.name = name;
        this.color = color;
        this.delete = delete;
        this.deleteAnnotation = deleteAnnotation;
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

    public boolean isDeleteAnnotation(){
        return deleteAnnotation;
    }

    public boolean isDelete(){
        return delete;
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
        return Objects.hash(name, color, delete, deleteAnnotation);
    }
}
