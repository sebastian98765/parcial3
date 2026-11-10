package com.smarthome.command;

import com.smarthome.model.Controllable;
import com.smarthome.model.Device;

import java.util.LinkedHashMap;
import java.util.Map;

public class PowerCommand implements Command {

    private final Controllable target;
    private final boolean turnOn;
    private final Map<Device, Boolean> previous = new LinkedHashMap<>();

    public PowerCommand(Controllable target, boolean turnOn) {
        this.target = target;
        this.turnOn = turnOn;
    }

    @Override
    public void execute() {
        previous.clear();
        for (Device device : target.devices()) {
            previous.put(device, device.isOn());
        }
        if (turnOn) {
            target.turnOn();
        } else {
            target.turnOff();
        }
    }

    @Override
    public void undo() {
        previous.forEach((device, wasOn) -> {
            if (wasOn) {
                device.turnOn();
            } else {
                device.turnOff();
            }
        });
    }

    @Override
    public String description() {
        return (turnOn ? "Encender " : "Apagar ") + target.getName();
    }
}
