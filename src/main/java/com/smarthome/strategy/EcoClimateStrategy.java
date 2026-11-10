package com.smarthome.strategy;

public class EcoClimateStrategy implements ClimateStrategy {

    @Override
    public String name() {
        return "Ahorro";
    }

    @Override
    public double targetTemperature(int hour, double outsideTemperature) {
        if (hour >= 23 || hour < 6) {
            return 18.0;
        }
        return outsideTemperature > 26.0 ? 26.0 : 20.0;
    }
}
