package com.smarthome.model;

import com.smarthome.strategy.AdaptiveLightingStrategy;
import com.smarthome.strategy.LightingStrategy;

import java.util.Objects;

public class Light extends Device {

    private int brightness = 80;
    private LightingStrategy strategy = new AdaptiveLightingStrategy();

    public Light(String name) {
        super(name, DeviceType.LIGHT);
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int value) {
        if (!canConfigure()) {
            return;
        }
        this.brightness = Math.max(0, Math.min(100, value));
        notifyChange("Brillo: " + brightness + "%");
    }

    public LightingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LightingStrategy strategy) {
        if (!canConfigure()) {
            return;
        }
        this.strategy = Objects.requireNonNull(strategy);
        notifyChange("Estrategia de iluminación: " + strategy.name());
    }

    public int suggestedBrightness(int hour) {
        return strategy.brightness(hour);
    }

    @Override
    public String describe() {
        return "Brillo " + brightness + "% · " + strategy.name();
    }
}
