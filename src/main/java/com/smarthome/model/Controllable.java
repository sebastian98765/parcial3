package com.smarthome.model;

import java.util.List;

public interface Controllable {

    String getName();

    void turnOn();

    void turnOff();

    List<Device> devices();
}
