package ch.unifr.hisdoc2.graphmanuscribble.io;

import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Represents an annotation type that the user defines in the settings.xml file as well as standart types like delete.
 */
public class AnnotationType{

    String name;
    Color color;

    public AnnotationType(String name, Color color){
        this.name = name;
        this.color = color;
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


    public static ArrayList<Color> annotationTypeToColorList(ArrayList<AnnotationType> list){
        ArrayList<Color> colors = new ArrayList<>();
        list.forEach(annotationType -> colors.add(annotationType.getColor()));
        return colors;
    }
}
