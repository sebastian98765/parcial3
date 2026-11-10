package com.smarthome.model;

public enum DeviceType {
    LIGHT("Luz"),
    THERMOSTAT("Termostato"),
    DOOR_LOCK("Cerradura");

    private final String label;

    DeviceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
