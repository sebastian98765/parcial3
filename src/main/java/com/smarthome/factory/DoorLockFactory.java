package com.smarthome.factory;

import com.smarthome.model.Device;
import com.smarthome.model.DoorLock;

public class DoorLockFactory extends DeviceFactory {

    @Override
    protected Device createDevice(String name) {
        return new DoorLock(name);
    }

    @Override
    protected String defaultName() {
        return "Cerradura";
    }
}
