package ch.unifr.hisdoc2.graphmanuscribble.helper.undo;

/**
 * source: http://torgen-engineering.blogspot.ch/2016/05/how-to-bring-undoredo-features-to-your.html
 */
public interface Undoable{
    /**
     * undo the last action performed
     */
    void undo();

    /**
     * redo the last undone action
     */
    void redo();

    /**
     * Information about the action that can be performed. Can be used as tooltip
     *
     * @return - description of the current action
     */
    String getUndoRedoName();
}
