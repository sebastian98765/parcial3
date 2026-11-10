package com.smarthome.command;

import com.smarthome.model.Thermostat;

public class SetTemperatureCommand implements Command {

    private final Thermostat thermostat;
    private final double newTemperature;
    private double previousTemperature;

    public SetTemperatureCommand(Thermostat thermostat, double newTemperature) {
        this.thermostat = thermostat;
        this.newTemperature = newTemperature;
    }

    @Override
    public void execute() {
        previousTemperature = thermostat.getTargetTemperature();
        thermostat.setTargetTemperature(newTemperature);
    }

    @Override
    public void undo() {
        thermostat.setTargetTemperature(previousTemperature);
    }

    @Override
    public String description() {
        return String.format("Ajustar %s a %.1f °C", thermostat.getName(), newTemperature);
    }
}
