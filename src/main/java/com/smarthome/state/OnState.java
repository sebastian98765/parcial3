package com.smarthome.state;

import com.smarthome.model.Device;

public final class OnState implements DeviceState {

    public static final OnState INSTANCE = new OnState();

    private OnState() {
    }

    @Override
    public String name() {
        return "Encendido";
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void turnOn(Device device) {
        // Ya está encendido: no hay transición.
    }

    @Override
    public void turnOff(Device device) {
        device.changeState(OffState.INSTANCE);
    }
}
