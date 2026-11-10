package com.smarthome.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Room implements Controllable {

    private final String name;
    private final List<Controllable> children = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void add(Controllable child) {
        children.add(child);
    }

    public void remove(Controllable child) {
        children.remove(child);
    }

    public List<Controllable> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /** Subhabitaciones directas. */
    public List<Room> rooms() {
        return children.stream()
                .filter(Room.class::isInstance)
                .map(Room.class::cast)
                .toList();
    }

    @Override
    public void turnOn() {
        children.forEach(Controllable::turnOn);
    }

    @Override
    public void turnOff() {
        children.forEach(Controllable::turnOff);
    }

    @Override
    public List<Device> devices() {
        return children.stream().flatMap(child -> child.devices().stream()).toList();
    }

    /** Habitación más específica que contiene directamente al dispositivo. */
    public Optional<Room> roomOf(Device device) {
        if (children.contains(device)) {
            return Optional.of(this);
        }
        for (Room sub : rooms()) {
            Optional<Room> found = sub.roomOf(device);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return name;
    }
}
