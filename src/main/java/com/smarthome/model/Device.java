package com.smarthome.model;

import com.smarthome.observer.DeviceEvent;
import com.smarthome.observer.DeviceObserver;
import com.smarthome.state.DeviceState;
import com.smarthome.state.FaultState;
import com.smarthome.state.OffState;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Device implements Controllable {

    private final String id = UUID.randomUUID().toString().substring(0, 8);
    private final String name;
    private final DeviceType type;
    private DeviceState state = OffState.INSTANCE;
    private final List<DeviceObserver> observers = new CopyOnWriteArrayList<>();

    protected Device(String name, DeviceType type) {
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    // ---------- State ----------

    public DeviceState getState() {
        return state;
    }

    public boolean isOn() {
        return state.isActive();
    }

    public boolean isFaulty() {
        return state == FaultState.INSTANCE;
    }

    @Override
    public void turnOn() {
        state.turnOn(this);
    }

    @Override
    public void turnOff() {
        state.turnOff(this);
    }

    public void reportFault() {
        changeState(FaultState.INSTANCE);
    }

    public void repair() {
        if (isFaulty()) {
            changeState(OffState.INSTANCE);
        }
    }

    public void changeState(DeviceState next) {
        if (next == state) {
            return;
        }
        DeviceState previous = state;
        state = next;
        publish(new DeviceEvent(this, DeviceEvent.Kind.STATE_CHANGED,
                previous.name() + " → " + next.name()));
    }

    public void reject(String reason) {
        publish(new DeviceEvent(this, DeviceEvent.Kind.REJECTED, reason));
    }

    protected boolean canConfigure() {
        if (isFaulty()) {
            reject("cambio de configuración rechazado: el dispositivo tiene una falla");
            return false;
        }
        return true;
    }

    @Override
    public List<Device> devices() {
        return List.of(this);
    }

    public abstract String describe();

    // ---------- Observer ----------

    public void addObserver(DeviceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(DeviceObserver observer) {
        observers.remove(observer);
    }

    protected void notifyChange(String detail) {
        publish(new DeviceEvent(this, DeviceEvent.Kind.PROPERTY_CHANGED, detail));
    }

    private void publish(DeviceEvent event) {
        for (DeviceObserver observer : observers) {
            observer.onDeviceEvent(event);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
