package com.smarthome.state;

import com.smarthome.model.Device;

public final class OffState implements DeviceState {

    public static final OffState INSTANCE = new OffState();

    private OffState() {
    }

    @Override
    public String name() {
        return "Apagado";
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void turnOn(Device device) {
        device.changeState(OnState.INSTANCE);
    }

    @Override
    public void turnOff(Device device) {
        // Ya está apagado: no hay transición.
    }
}
