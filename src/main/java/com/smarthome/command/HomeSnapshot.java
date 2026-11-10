package com.smarthome.command;

import com.smarthome.model.Device;
import com.smarthome.model.Light;
import com.smarthome.model.Thermostat;
import com.smarthome.strategy.ClimateStrategy;
import com.smarthome.strategy.LightingStrategy;

import java.util.List;

public final class HomeSnapshot {

    private record Memento(Device device, boolean on, Integer brightness, LightingStrategy lighting,
                           Double temperature, ClimateStrategy climate) {
    }

    private final List<Memento> mementos;

    private HomeSnapshot(List<Memento> mementos) {
        this.mementos = mementos;
    }

    public static HomeSnapshot capture(List<Device> devices) {
        return new HomeSnapshot(devices.stream().map(HomeSnapshot::memento).toList());
    }

    private static Memento memento(Device device) {
        if (device instanceof Light light) {
            return new Memento(device, device.isOn(), light.getBrightness(), light.getStrategy(), null, null);
        }
        if (device instanceof Thermostat thermostat) {
            return new Memento(device, device.isOn(), null, null,
                    thermostat.getTargetTemperature(), thermostat.getStrategy());
        }
        return new Memento(device, device.isOn(), null, null, null, null);
    }

    /** Restaura solo lo que cambió; los dispositivos con falla se dejan como están. */
    public void restore() {
        for (Memento m : mementos) {
            Device device = m.device();
            if (device.isFaulty()) {
                continue;
            }
            if (m.on() != device.isOn()) {
                if (m.on()) {
                    device.turnOn();
                } else {
                    device.turnOff();
                }
            }
            if (device instanceof Light light) {
                if (m.brightness() != light.getBrightness()) {
                    light.setBrightness(m.brightness());
                }
                if (m.lighting() != light.getStrategy()) {
                    light.setStrategy(m.lighting());
                }
            } else if (device instanceof Thermostat thermostat) {
                if (m.temperature() != thermostat.getTargetTemperature()) {
                    thermostat.setTargetTemperature(m.temperature());
                }
                if (m.climate() != thermostat.getStrategy()) {
                    thermostat.setStrategy(m.climate());
                }
            }
        }
    }
}
