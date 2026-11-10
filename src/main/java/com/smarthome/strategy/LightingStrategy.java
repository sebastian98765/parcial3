package com.smarthome.strategy;

public interface LightingStrategy {

    String name();

    int brightness(int hour);
}
