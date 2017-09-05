package ch.unifr.hisdoc2.graphmanuscribble.io;

import ch.unifr.hisdoc2.graphmanuscribble.io.helper.MouseInputTest;
import javafx.scene.paint.Color;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A Singleton settings class that reads the settings and provides read-only access for this settings fields.
 */
public class SettingReader {

    private static MouseInputTest moveTest = null;
    private static MouseInputTest drawTest = null;
    private static MouseInputTest drawLineTest = null;
    private static MouseInputTest eraseTest = null;
    private static MouseInputTest eraseLineTest = null;
    private static ArrayList<Color> graphColor = new ArrayList<>();
    private static AnnotationType deletionType;
    private static ArrayList<AnnotationType> annotationTypes = new ArrayList<>();


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
            xml = builder.build(new File("settings.xml"));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        Element root = xml.getRootElement();
        moveTest = new MouseInputTest(root.getChild("move-view"));
        drawTest = new MouseInputTest(root.getChild("draw"));
        drawLineTest = new MouseInputTest(root.getChild("draw-line"));
        eraseTest = new MouseInputTest(root.getChild("erase"));
        eraseLineTest = new MouseInputTest(root.getChild("erase-line"));

        //colors
        Element presColors = root.getChild("presentation-colors");
        graphColor.add(createColorFromString(presColors.getChild("graph").getAttributeValue("rgb")));
        deletionType = new AnnotationType("delete",
                createColorFromString(presColors.getChild("delete").getAttributeValue("rgb")));
        //annotations
        for(Element e : root.getChild("annotations").getChildren()){
            annotationTypes.add(new AnnotationType(e.getName(),
                    createColorFromString(e.getAttributeValue("rgb"))));
        }
    }

    private static Color createColorFromString(String color){
        String[] rgbVals = color.split(",");
        if(rgbVals.length != 3){
            throw new IllegalArgumentException("Wrong RGB values");
        }
        return Color.rgb(Integer.parseInt(rgbVals[0]), Integer.parseInt(rgbVals[1]), Integer.parseInt(rgbVals[2]));
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

    public ArrayList<Color> getGraphColor(){
        return graphColor;
    }

    public AnnotationType getDeletion(){
        return deletionType;
    }

    public ArrayList<AnnotationType> getAnnotation(){
        return annotationTypes;
    }

    /**
     * All Annotations PLUS the deletion
     *
     */
    public ArrayList<AnnotationType> getAllAnnotations(){
        ArrayList<AnnotationType> all = new ArrayList<>(annotationTypes);
        all.add(deletionType);
        return all;
    }
}
