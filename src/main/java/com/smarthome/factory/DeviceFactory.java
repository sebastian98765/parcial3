package com.smarthome.factory;

import com.smarthome.model.Device;

public abstract class DeviceFactory {

    public final Device create(String requestedName) {
        String name = (requestedName == null || requestedName.isBlank())
                ? defaultName()
                : requestedName.trim();
        return createDevice(name);
    }

    /** Método de fábrica: lo implementa cada creador concreto. */
    protected abstract Device createDevice(String name);

    protected abstract String defaultName();
}
