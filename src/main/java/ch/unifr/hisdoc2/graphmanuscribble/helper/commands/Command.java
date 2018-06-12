package ch.unifr.hisdoc2.graphmanuscribble.helper.commands;

/**
 * source: http://torgen-engineering.blogspot.ch/2016/05/how-to-bring-undoredo-features-to-your.html
 */
public interface Command {
    /** Executes the command. */
    void execute();

    /** Checks whether the command can be executed. */
    boolean canExecute();
}