package com.smarthome.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;


public class CommandInvoker {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public Optional<Command> undo() {
        if (undoStack.isEmpty()) {
            return Optional.empty();
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return Optional.of(command);
    }

    public Optional<Command> redo() {
        if (redoStack.isEmpty()) {
            return Optional.empty();
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return Optional.of(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
