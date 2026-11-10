package com.smarthome.mediator;

import com.smarthome.model.Controllable;
import com.smarthome.model.Device;
import com.smarthome.model.DeviceType;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.Thermostat;
import com.smarthome.observer.ActivityLog;
import com.smarthome.observer.DeviceEvent;
import com.smarthome.observer.DeviceObserver;
import com.smarthome.strategy.EcoClimateStrategy;

import java.util.List;

public class SmartHomeMediator implements HomeMediator, DeviceObserver {

    private final Room home;
    private final HomeEnvironment environment;
    private final ActivityLog log;
    private boolean automationEnabled = true;
    private boolean reacting;

    public SmartHomeMediator(Room home, HomeEnvironment environment, ActivityLog log) {
        this.home = home;
        this.environment = environment;
        this.log = log;
    }

    public void register(Device device) {
        device.addObserver(this);
    }

    @Override
    public void onDeviceEvent(DeviceEvent event) {
        colleagueChanged(event.device(), event);
    }

    @Override
    public void colleagueChanged(Device source, DeviceEvent event) {
        if (!automationEnabled || reacting || event.kind() != DeviceEvent.Kind.STATE_CHANGED) {
            return;
        }
        reacting = true; // evita que las reacciones del mediador lo disparen de nuevo
        try {
            if (source.isFaulty()) {
                log.info("ALERTA: " + source.getName() + " dejó de responder, requiere revisión");
            } else if (source.getType() == DeviceType.DOOR_LOCK) {
                if (source.isOn()) {
                    awayMode(source);
                } else {
                    welcomeHome(source);
                }
            }
        } finally {
            reacting = false;
        }
    }

    private void awayMode(Device door) {
        log.info("Automatización: " + door.getName() + " asegurada → modo ausente");
        for (Device device : home.devices()) {
            if (device instanceof Light light && light.isOn()) {
                light.turnOff();
            } else if (device instanceof Thermostat thermostat && !thermostat.isFaulty()) {
                thermostat.setStrategy(new EcoClimateStrategy());
                thermostat.setTargetTemperature(thermostat.suggestedTemperature(
                        environment.currentHour(), environment.getOutsideTemperature()));
            }
        }
    }

    private void welcomeHome(Device door) {
        if (!environment.isDark()) {
            log.info("Automatización: " + door.getName() + " liberada (es de día, luces sin cambios)");
            return;
        }
        log.info("Automatización: " + door.getName() + " liberada de noche → luces de bienvenida");
        List<Device> sameRoom = home.roomOf(door).map(Controllable::devices).orElse(List.of());
        for (Device device : sameRoom) {
            if (device instanceof Light light && !light.isFaulty()) {
                light.turnOn();
                light.setBrightness(light.suggestedBrightness(environment.currentHour()));
            }
        }
    }

    @Override
    public boolean isAutomationEnabled() {
        return automationEnabled;
    }

    @Override
    public void setAutomationEnabled(boolean enabled) {
        this.automationEnabled = enabled;
        log.info("Automatización " + (enabled ? "activada" : "desactivada"));
    }

    @Override
    public void withoutAutomation(Runnable action) {
        boolean previous = automationEnabled;
        automationEnabled = false;
        try {
            action.run();
        } finally {
            automationEnabled = previous;
        }
    }
}
