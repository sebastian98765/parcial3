package com.smarthome.strategy;

public class ComfortClimateStrategy implements ClimateStrategy {

    @Override
    public String name() {
        return "Confort";
    }

    @Override
    public double targetTemperature(int hour, double outsideTemperature) {
        return outsideTemperature >= 28.0 ? 23.0 : 22.0;
    }
}
