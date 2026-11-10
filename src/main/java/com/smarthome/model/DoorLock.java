package com.smarthome.model;

public class DoorLock extends Device {

    public DoorLock(String name) {
        super(name, DeviceType.DOOR_LOCK);
    }

    @Override
    public String describe() {
        return isOn() ? "Puerta asegurada" : "Puerta sin asegurar";
    }
}
