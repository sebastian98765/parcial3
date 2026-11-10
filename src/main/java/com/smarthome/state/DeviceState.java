package com.smarthome.state;

import com.smarthome.model.Device;

public interface DeviceState {

    String name();

    boolean isActive();

    void turnOn(Device device);

    void turnOff(Device device);
}
