package ch.unifr.hisdoc2.graphmanuscribble.helper.undo;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Singleton class
 * <p>
 * edited by: Lars Vögtlin
 * <p>
 * source: http://torgen-engineering.blogspot.ch/2016/05/how-to-bring-undoredo-features-to-your.html
 */
public final class UndoCollector{

    /**
     * instance of the class
     */
    private static final UndoCollector INSTANCE = new UndoCollector();

    /**
     * undo elements
     */
    private final Deque<Undoable> undo;

    /**
     * redo elements
     */
    private final Deque<Undoable> redo;

    /**
     * MAximum number of undoes possible
     */
    private int maxSize;

    private UndoCollector(){
        undo = new ArrayDeque<>();
        redo = new ArrayDeque<>();
        maxSize = 10;
    }

    /**
     * Retruns the instance of the UndoCollector class
     *
     * @return - The singleton instance
     */
    public static UndoCollector getInstance(){
        return INSTANCE;
    }

    /**
     * @return The last undoable object name or "".
     */
    public String getLastUndoMessage(){
        return undo.isEmpty() ? "" : undo.peek().getUndoRedoName();
    }

    /**
     * @return The last redoable object name or "".
     */
    public String getLastRedoMessage(){
        return redo.isEmpty() ? "" : redo.peek().getUndoRedoName();
    }

    /**
     * @return The last undoable object or null.
     */
    public Undoable getLastUndo(){
        return undo.peek();
    }

    /**
     * @return The last redoable object or null.
     */
    public Undoable getLastRedo(){
        return redo.peek();
    }

    /**
     * @param max The max number of saved undoable objects. Must be great than 0.
     */
    public void setSizeMax(final int max){
        if(max >= 0){
            for(int i = 0, nb = undo.size() - max; i < nb; i++){
                undo.removeLast();
            }
            this.maxSize = max;
        }
    }

    /**
     * Adds an undoable object to the collector.
     *
     * @param undoable The undoable object to add.
     */
    public void add(final Undoable undoable){
        if(undoable != null && maxSize > 0){
            if(undo.size() == maxSize){
                undo.removeLast();
            }

            undo.push(undoable);
            redo.clear(); /* The redoable objects must be removed. */
        }
    }

    /**
     * Undoes the last undoable object.
     */
    public void undo(){
        if(!undo.isEmpty()){
            final Undoable undoable = undo.pop();
            undoable.undo();
            redo.push(undoable);
        }
    }

    /**
     * Redoes the last undoable object.
     */
    public void redo(){
        if(!redo.isEmpty()){
            final Undoable undoable = redo.pop();
            undoable.redo();
            undo.push(undoable);
        }
    }


}
