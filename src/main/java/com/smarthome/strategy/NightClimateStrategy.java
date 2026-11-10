package com.smarthome.strategy;

public class NightClimateStrategy implements ClimateStrategy {

    @Override
    public String name() {
        return "Nocturno";
    }

    @Override
    public double targetTemperature(int hour, double outsideTemperature) {
        return (hour >= 22 || hour < 6) ? 19.0 : 21.0;
    }
}
