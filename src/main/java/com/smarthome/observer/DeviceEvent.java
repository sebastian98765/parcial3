package com.smarthome.observer;

import com.smarthome.model.Device;

import java.time.LocalDateTime;

public record DeviceEvent(Device device, Kind kind, String message, LocalDateTime time) {

    public enum Kind {
        STATE_CHANGED,
        PROPERTY_CHANGED,
        REJECTED
    }

    public DeviceEvent(Device device, Kind kind, String message) {
        this(device, kind, message, LocalDateTime.now());
    }
}
