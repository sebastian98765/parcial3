package com.smarthome.strategy;

public class AdaptiveLightingStrategy implements LightingStrategy {

    @Override
    public String name() {
        return "Adaptativa";
    }

    @Override
    public int brightness(int hour) {
        if (hour >= 6 && hour < 18) {
            return 60;
        }
        if (hour >= 18 && hour < 22) {
            return 85;
        }
        return 25;
    }
}
