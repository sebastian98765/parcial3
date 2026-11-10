package com.smarthome.command;

import java.util.ArrayList;
import java.util.List;

public class MacroCommand implements Command {

    private final String name;
    private final List<Command> commands;

    public MacroCommand(String name, List<Command> commands) {
        this.name = name;
        this.commands = new ArrayList<>(commands);
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String description() {
        return name;
    }
}
