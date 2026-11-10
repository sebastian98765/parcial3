package com.smarthome.faccom.smarthome.model.Device;
import com.smarthome.model.Thermostat;

public class ThermostatFactory extends DeviceFactory {

    @Override
    protected Device createDevice(String name) {
        Thermostat thermostat = new Thermostat(name);
        thermostat.setTargetTemperature(22.0);
        return thermostat;
    }

    @Override
    protected String defaultName() {
        return "Termostato";
    }
}
