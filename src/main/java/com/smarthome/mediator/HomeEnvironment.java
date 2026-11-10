package com.smarthome.mediator;

import java.time.Clock;

public class HomeEnvironment {

    private final Clock clock;
    private double outsideTemperature = 24.0;

    public HomeEnvironment() {
        this(Clock.systemDefaultZone());
    }

    public HomeEnvironment(Clock clock) {
        this.clock = clock;
    }

    public int currentHour() {
        return LocalTime.now(clock).getHour();
    }

    public boolean isDark() {
        int hour = currentHour();
        return hour >= 18 || hour < 6;
    }

    public double getOutsideTemperature() {
        return outsideTemperature;
    }

    public void setOutsideTemperature(double outsideTemperature) {
        this.outsideTemperature = outsideTemperature;
    }
}
