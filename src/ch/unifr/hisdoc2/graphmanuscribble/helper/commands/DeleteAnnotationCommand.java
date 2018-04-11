package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.Undoable;

public class DeleteAnnotationCommand implements Undoable, Command{

    @Override
    public void execute(){

    }

    @Override
    public boolean canExecute(){
        return false;
    }

    @Override
    public void undo(){

    }

    @Override
    public void redo(){

    }

    @Override
    public String getUndoRedoName(){
        return null;
    }
}
