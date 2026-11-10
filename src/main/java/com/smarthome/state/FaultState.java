package com.smarthome.state;

import com.smarthome.model.Device;

public final class FaultState implements DeviceState {

    public static final FaultState INSTANCE = new FaultState();

    private FaultState() {
    }

    @Override
    public String name() {
        return "Con falla";
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void turnOn(Device device) {
        device.reject("orden de encendido rechazada: el dispositivo tiene una falla");
    }

    @Override
    public void turnOff(Device device) {
        device.reject("orden de apagado rechazada: el dispositivo tiene una falla");
    }
}
