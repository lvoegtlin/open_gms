package ch.unifr.hisdoc2.graphmanuscribble.io.helper;

import org.jdom2.Element;

import java.awt.event.MouseEvent;

/**
 * This class is used for testing if the input pattern corresponds to something.
 */
public class MouseInputTest{
    private boolean button1 = false;
    private boolean button2 = false;
    private boolean button3 = false;
    private boolean shift = false;
    private boolean control = false;
    private boolean alt = false;

    public MouseInputTest(Element tag) {
        button1 = tag.getChild("button1") != null;
        button2 = tag.getChild("button2") != null;
        button3 = tag.getChild("button3") != null;
        shift = tag.getChild("shift") != null;
        control = tag.getChild("control") != null;
    }

    /**
     * Checks if the event corresponds to the stored pattern.
     *
     * @param me mouse event to process
     * @return the value of the test
     */
    public boolean test(MouseEvent me) {
        return (button1 == ((me.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0))
                && (button2 == ((me.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0))
                && (button3 == ((me.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0))
                && (shift == me.isShiftDown())
                && (control == me.isControlDown());
    }
}
