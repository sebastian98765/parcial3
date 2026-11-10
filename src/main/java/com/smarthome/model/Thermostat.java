package com.smarthome.model;

import com.smarthome.strategy.ClimateStrategy;
import com.smarthome.strategy.ComfortClimateStrategy;

import java.util.Objects;

public class Thermostat extends Device {

    public static final double MIN_TEMPERATURE = 16.0;
    public static final double MAX_TEMPERATURE = 30.0;

    private double targetTemperature = 22.0;
    private ClimateStrategy strategy = new ComfortClimateStrategy();

    public Thermostat(String name) {
        super(name, DeviceType.THERMOSTAT);
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(double value) {
        if (!canConfigure()) {
            return;
        }
        this.targetTemperature = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, value));
        notifyChange(String.format("Temperatura objetivo: %.1f °C", targetTemperature));
    }

    public ClimateStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ClimateStrategy strategy) {
        if (!canConfigure()) {
            return;
        }
        this.strategy = Objects.requireNonNull(strategy);
        notifyChange("Estrategia de clima: " + strategy.name());
    }

    public double suggestedTemperature(int hour, double outsideTemperature) {
        return strategy.targetTemperature(hour, outsideTemperature);
    }

    @Override
    public String describe() {
        return String.format("Objetivo %.1f °C · %s", targetTemperature, strategy.name());
    }
}
