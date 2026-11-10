package com.smarthome.factory;

import com.smarthome.model.Device;
import com.smarthome.model.Light;

public class LightFactory extends DeviceFactory {

    @Override
    protected Device createDevice(String name) {
        Light light = new Light(name);
        light.setBrightness(80);
        return light;
    }

    @Override
    protected String defaultName() {
        return "Luz";
    }
}
