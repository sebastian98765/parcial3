package com.smarthome.strategy;

public interface ClimateStrategy {

    String name();

    double targetTemperature(int hour, double outsideTemperature);
}
