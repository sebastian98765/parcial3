package com.smarthome.command;

import com.smarthome.model.Device;

import java.util.List;
import java.util.function.Supplier;

public class SnapshotCommand implements Command {

    private final Command inner;
    private final Supplier<List<Device>> devices;
    private HomeSnapshot before;

    public SnapshotCommand(Command inner, Supplier<List<Device>> devices) {
        this.inner = inner;
        this.devices = devices;
    }

    @Override
    public void execute() {
        before = HomeSnapshot.capture(devices.get());
        inner.execute();
    }

    @Override
    public void undo() {
        inner.undo();
        if (before != null) {
            before.restore();
        }
    }

    @Override
    public String description() {
        return inner.description();
    }
}
