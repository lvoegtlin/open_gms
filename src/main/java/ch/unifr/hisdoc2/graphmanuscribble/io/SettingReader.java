package ch.unifr.hisdoc2.graphmanuscribble.io;

import ch.unifr.hisdoc2.graphmanuscribble.GraphManuscribble;
import ch.unifr.hisdoc2.graphmanuscribble.io.helper.MouseInputTest;
import javafx.scene.paint.Color;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A Singleton settings class that reads the settings and provides read-only access for this settings fields.
 */
public class SettingReader {

    private static MouseInputTest moveTest = null;
    private static MouseInputTest drawTest = null;
    private static MouseInputTest drawLineTest = null;
    private static MouseInputTest eraseTest = null;
    private static MouseInputTest eraseLineTest = null;
    private static ArrayList<AnnotationType> graphColor = new ArrayList<>();
    private static AnnotationType deletionType;
    private static ArrayList<AnnotationType> annotationTypes = new ArrayList<>();
    private static ArrayList<AnnotationType> deleteAnnotationTypes = new ArrayList<>();


    /**
     * Singleton instance.
     */
    private static SettingReader settingReader;

    /**
     * To make a Singleton we just need to have a private consructor.
     */
    private SettingReader() {
        readSettings();
    }

    /**
     * Gets the only instance of the class
     *
     * @return - SettingsReader instance
     */
    public static SettingReader getInstance() {
        if (settingReader == null) {
            settingReader = new SettingReader();
        }

        return settingReader;
    }

    /**
     * Reads in the Settings and sets the Values for the settingsfields
     */
    private static void readSettings() {
        SAXBuilder builder = new SAXBuilder();
        Document xml = null;
        try {
            xml = builder.build(GraphManuscribble.getResource("/configs/settings.xml"));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        Element root = Objects.requireNonNull(xml).getRootElement();
        moveTest = new MouseInputTest(root.getChild("move-view"));
        drawTest = new MouseInputTest(root.getChild("draw"));
        drawLineTest = new MouseInputTest(root.getChild("draw-line"));
        eraseTest = new MouseInputTest(root.getChild("erase"));
        eraseLineTest = new MouseInputTest(root.getChild("erase-line"));

        //colors
        Element presColors = root.getChild("presentation-colors");
        graphColor.add(new AnnotationType("graphColor", createColorFromString(presColors.getChild("graph").getAttributeValue("rgb"),
                presColors.getChild("graph").getAttributeValue("alpha"))));
        deletionType = new AnnotationType("delete",
                createColorFromString(presColors.getChild("delete").getAttributeValue("rgb"),
                        presColors.getChild("delete").getAttributeValue("alpha")),
                false, true);
        //annotations
        for(Element e : root.getChild("annotations").getChildren()){
            annotationTypes.add(new AnnotationType(e.getName(),
                    createColorFromString(e.getAttributeValue("rgb"), e.getAttributeValue("alpha"))));

            deleteAnnotationTypes.add(new AnnotationType(e.getName(),
                    createColorFromString(e.getAttributeValue("rgb"), e.getAttributeValue("alpha")),
                    false, true));
        }
    }

    /**
     * Creates an JavaFX Color out of a given rgb string and the opacity string value.
     * E.g. "0,255,0" will return a color object that represents the color green.
     *
     * @param color - the color in rgb encoded as a string
     * @param opacity - the opacity as string
     * @return - JavaFX color
     */
    private static Color createColorFromString(String color, String opacity){
        String[] rgbVals = color.split(",");
        if(rgbVals.length != 3){
            throw new IllegalArgumentException("Wrong RGB values");
        }
        if(opacity == null){
            return Color.rgb(Integer.parseInt(rgbVals[0]), Integer.parseInt(rgbVals[1]), Integer.parseInt(rgbVals[2]));
        } else {

            return Color.rgb(Integer.parseInt(rgbVals[0]),
                    Integer.parseInt(rgbVals[1]),
                    Integer.parseInt(rgbVals[2]),
                    Double.parseDouble(opacity));
        }

    }

    public MouseInputTest getMoveTest() {
        return moveTest;
    }

    public MouseInputTest getDrawTest() {
        return drawTest;
    }

    public MouseInputTest getDrawLineTest() {
        return drawLineTest;
    }

    public MouseInputTest getEraseTest() {
        return eraseTest;
    }

    public MouseInputTest getEraseLineTest() {
        return eraseLineTest;
    }

    public ArrayList<AnnotationType> getGraphColor(){
        return graphColor;
    }

    public AnnotationType getDeletion(){
        return deletionType;
    }

    public static ArrayList<AnnotationType> getDeleteAnnotationTypes(){
        return deleteAnnotationTypes;
    }

    public ArrayList<AnnotationType> getAnnotations(){
        return annotationTypes;
    }

    public ArrayList<AnnotationType> getAllAnnotations(){
        ArrayList<AnnotationType> res = new ArrayList<>(annotationTypes);
        res.add(deletionType);
        return res;
    }
}
