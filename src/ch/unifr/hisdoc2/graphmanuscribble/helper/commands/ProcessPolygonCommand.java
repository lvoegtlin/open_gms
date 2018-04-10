package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

import ch.unifr.hisdoc2.graphmanuscribble.helper.undo.Undoable;

public class ProcessPolygonCommand implements Command, Undoable{
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
