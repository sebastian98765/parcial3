package com.smarthome.command;

public interface Command {

    void execute();

    void undo();

    String description();
}
