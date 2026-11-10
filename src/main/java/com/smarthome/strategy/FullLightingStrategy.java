package com.smarthome.strategy;

public class FullLightingStrategy implements LightingStrategy {

    @Override
    public String name() {
        return "Máximo";
    }

    @Override
    public int brightness(int hour) {
        return 100;
    }
}
