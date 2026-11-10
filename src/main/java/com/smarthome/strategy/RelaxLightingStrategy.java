package com.smarthome.strategy;

public class RelaxLightingStrategy implements LightingStrategy {

    @Override
    public String name() {
        return "Relajante";
    }

    @Override
    public int brightness(int hour) {
        return 30;
    }
}
