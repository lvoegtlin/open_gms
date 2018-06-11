package ch.unifr.hisdoc2.graphmanuscribble.model.annotation.helper;

import ch.unifr.hisdoc2.graphmanuscribble.controller.Controller;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygon;
import ch.unifr.hisdoc2.graphmanuscribble.model.annotation.AnnotationPolygonType;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.helper.PointHD2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PolygonExporter{

    /**
     * Exports all the AnnotationPolygons out of the annotationpolygonmap and saves them in a xml file. The format of the xml
     * is PcGts (http://www.ocr-d.de/sites/all/gt_guidelines/pagecontent_xsd_Element_pc_PcGts.html)
     *
     * @param cnt - the controller
     * @param imageName - the name of the image
     * @param gtOutputName - the name of the file
     * @return
     */
    public static boolean exportXML(Controller cnt, String imageName, String gtOutputName) {

        String[] objButtons = {"Yes", "No"};

        try {
            String outName = imageName;
            if (outName.contains(".")) {
                outName = outName.substring(0, outName.lastIndexOf("."));
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(gtOutputName + ".xml"));

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<PcGts xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            writer.write("\txsi:schemaLocation=\"http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15 http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15/pagecontent.xsd\"\n");
            writer.write("\tpcGtsId=\"\">\n");
            writer.write("\t<Metadata>\n");
            writer.write("\t\t<Creator></Creator>\n");
            writer.write("\t\t<Comments/>\n");
            writer.write("\t</Metadata>\n");
            writer.write("\t<Page imageWidth=\"" + cnt.getWidth() + "\" imageHeight=\"" + cnt.getHeight() + "\" imageFilename=\"" + imageName + "\">\n");

            int uid = 0;

            // for each polygon type, write an entry
            for (AnnotationPolygonType type : cnt.getPolygonMap().getPolygonMap().values()) {
                for (AnnotationPolygon poly : type.getAnnotationPolygons()) {
                    writer.write("\t\t<TextRegion type=\"" + type.getAnnotationType().getName() + "\" id=\"" + (uid++) + "\" custom=\"0\" comments=\"\">\n");
                    writer.write("\t\t\t<Coords>\n");
                    for (PointHD2 pt : poly.getHull()) {
                        writer.write("\t\t\t\t<Point x=\"" + Math.round(pt.x()) + "\" y=\"" + Math.round(pt.y()) + "\"/>\n");
                    }
                    writer.write("\t\t\t</Coords>\n");
                    writer.write("\t\t</TextRegion>\n");
                }
            }
            writer.write("\t</Page>\n");
            writer.write("</PcGts>\n");
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }//GEN-LAST:event_btnExportActionPerformed
}
