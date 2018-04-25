package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.Undoable;

public class AnnotateCommand implements Command, Undoable{

    @Override
    public void execute(){

    }

    @Override
    public boolean canExecute(){
        return false;
    }

    @Override
    public void undo(){
        throw new UnsupportedOperationException();
    }

    @Override
    public void redo(){
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUndoRedoName(){
        return null;
    }
}
