package com.smarthome.facade;

import com.smarthome.command.Command;
import com.smarthome.command.CommandInvoker;
import com.smarthome.command.MacroCommand;
import com.smarthome.command.PowerCommand;
import com.smarthome.command.SetBrightnessCommand;
import com.smarthome.command.SetTemperatureCommand;
import com.smarthome.command.SnapshotCommand;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.factory.DoorLockFactory;
import com.smarthome.factory.LightFactory;
import com.smarthome.factory.ThermostatFactory;
import com.smarthome.mediator.HomeEnvironment;
import com.smarthome.mediator.SmartHomeMediator;
import com.smarthome.model.Controllable;
import com.smarthome.model.Device;
import com.smarthome.model.DeviceType;
import com.smarthome.model.DoorLock;
import com.smarthome.model.Light;
import com.smarthome.model.Room;
import com.smarthome.model.Thermostat;
import com.smarthome.observer.ActivityLog;
import com.smarthome.observer.DeviceObserver;
import com.smarthome.strategy.ClimateStrategy;
import com.smarthome.strategy.LightingStrategy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SmartHomeFacade {

    public enum Scene {
        GOOD_NIGHT("Buenas noches"),
        MOVIE("Modo cine"),
        LEAVE_HOME("Salir de casa");

        private final String label;

        Scene(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Room home = new Room("Casa");
    private final HomeEnvironment environment = new HomeEnvironment();
    private final ActivityLog log = new ActivityLog();
    private final CommandInvoker invoker = new CommandInvoker();
    private final SmartHomeMediator mediator = new SmartHomeMediator(home, environment, log);
    private final Map<DeviceType, DeviceFactory> factories = new EnumMap<>(DeviceType.class);
    private final List<Runnable> changeListeners = new ArrayList<>();
    private final DeviceObserver notifier = event -> changeListeners.forEach(Runnable::run);

    public SmartHomeFacade() {
        factories.put(DeviceType.LIGHT, new LightFactory());
        factories.put(DeviceType.THERMOSTAT, new ThermostatFactory());
        factories.put(DeviceType.DOOR_LOCK, new DoorLockFactory());
        buildSampleHome();
    }

    private void buildSampleHome() {
        Room living = addRoom("Sala");
        Room kitchen = addRoom("Cocina");
        Room entrance = addRoom("Entrada");
        Room bedroom = addRoom("Dormitorio");
        addDevice(DeviceType.LIGHT, "Luz sala", living);
        addDevice(DeviceType.THERMOSTAT, "Termostato sala", living);
        addDevice(DeviceType.LIGHT, "Luz cocina", kitchen);
        addDevice(DeviceType.LIGHT, "Luz entrada", entrance);
        addDevice(DeviceType.DOOR_LOCK, "Puerta principal", entrance);
        addDevice(DeviceType.LIGHT, "Luz dormitorio", bedroom);
        log.info("Hogar inicializado: " + home.devices().size() + " dispositivos");
    }

    // ---------- Consulta ----------

    public Room getHome() {
        return home;
    }

    public List<Device> devices() {
        return home.devices();
    }

    public List<String> activityLog() {
        return log.entries();
    }

    public HomeEnvironment getEnvironment() {
        return environment;
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    // ---------- Construcción del hogar ----------

    public Room addRoom(String name) {
        Room room = new Room(name == null || name.isBlank() ? "Habitación" : name.trim());
        home.add(room);
        notifyChanged();
        return room;
    }

    public Device addDevice(DeviceType type, String name, Room room) {
        Device device = factories.get(type).create(name);
        room.add(device);
        device.addObserver(log);
        device.addObserver(notifier);
        mediator.register(device);
        log.info("Nuevo dispositivo: " + device.getName() + " (" + type + ") en " + room.getName());
        notifyChanged();
        return device;
    }

    // ---------- Acciones del usuario (todas pasan por Command) ----------

    public void togglePower(Controllable target) {
        boolean anyOn = target.devices().stream().anyMatch(Device::isOn);
        run(new PowerCommand(target, !anyOn));
    }

    public void setBrightness(Light light, int value) {
        run(new SetBrightnessCommand(light, value));
    }

    public void setTemperature(Thermostat thermostat, double value) {
        run(new SetTemperatureCommand(thermostat, value));
    }

    public void setLightingStrategy(Light light, LightingStrategy strategy) {
        light.setStrategy(strategy);
        notifyChanged();
    }

    public void setClimateStrategy(Thermostat thermostat, ClimateStrategy strategy) {
        thermostat.setStrategy(strategy);
        notifyChanged();
    }

    public void applySuggestion(Device device) {
        if (device instanceof Light light) {
            run(new SetBrightnessCommand(light, light.suggestedBrightness(environment.currentHour())));
        } else if (device instanceof Thermostat thermostat) {
            run(new SetTemperatureCommand(thermostat, thermostat.suggestedTemperature(
                    environment.currentHour(), environment.getOutsideTemperature())));
        }
    }

    public void toggleFault(Device device) {
        if (device.isFaulty()) {
            device.repair();
        } else {
            device.reportFault();
        }
        notifyChanged();
    }

    public void setAutomationEnabled(boolean enabled) {
        mediator.setAutomationEnabled(enabled);
        notifyChanged();
    }

    public boolean isAutomationEnabled() {
        return mediator.isAutomationEnabled();
    }

    // ---------- Escenas (MacroCommand) ----------

    public void runScene(Scene scene) {
        List<Command> steps = new ArrayList<>();
        switch (scene) {
            case GOOD_NIGHT -> {
                // La puerta va primero: el mediador apaga luces y pone ahorro; luego se fija 19 °C.
                devicesOf(DoorLock.class).forEach(d -> steps.add(new PowerCommand(d, true)));
                devicesOf(Light.class).forEach(l -> steps.add(new PowerCommand(l, false)));
                devicesOf(Thermostat.class).forEach(t -> steps.add(new SetTemperatureCommand(t, 19.0)));
            }
            case MOVIE -> home.rooms().stream()
                    .filter(r -> r.getName().equals("Sala"))
                    .flatMap(r -> r.devices().stream())
                    .filter(Light.class::isInstance)
                    .map(Light.class::cast)
                    .forEach(l -> {
                        steps.add(new PowerCommand(l, true));
                        steps.add(new SetBrightnessCommand(l, 20));
                    });
            case LEAVE_HOME -> devicesOf(DoorLock.class).forEach(d -> steps.add(new PowerCommand(d, true)));
        }
        MacroCommand macro = new MacroCommand("Escena: " + scene, steps);
        if (macro.isEmpty()) {
            log.info("La escena \"" + scene + "\" no tiene dispositivos que controlar");
        } else {
            run(macro);
        }
    }

    private <T extends Device> List<T> devicesOf(Class<T> type) {
        return home.devices().stream().filter(type::isInstance).map(type::cast).toList();
    }

    // ---------- Historial ----------

    public boolean canUndo() {
        return invoker.canUndo();
    }

    public boolean canRedo() {
        return invoker.canRedo();
    }

    public void undo() {
        mediator.withoutAutomation(() -> {
            Optional<Command> undone = invoker.undo();
            undone.ifPresent(c -> log.info("Deshacer: " + c.description()));
        });
        notifyChanged();
    }

    public void redo() {
        Optional<Command> redone = invoker.redo();
        redone.ifPresent(c -> log.info("Rehacer: " + c.description()));
        notifyChanged();
    }

    private void run(Command command) {
        log.info("Acción: " + command.description());
        invoker.execute(new SnapshotCommand(command, home::devices));
        notifyChanged();
    }

    private void notifyChanged() {
        changeListeners.forEach(Runnable::run);
    }
}
