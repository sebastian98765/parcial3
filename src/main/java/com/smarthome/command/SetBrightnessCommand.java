package com.smarthome.command;

import com.smarthome.model.Light;

public class SetBrightnessCommand implements Command {

    private final Light light;
    private final int newBrightness;
    private int previousBrightness;

    public SetBrightnessCommand(Light light, int newBrightness) {
        this.light = light;
        this.newBrightness = newBrightness;
    }

    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.setBrightness(newBrightness);
    }

    @Override
    public void undo() {
        light.setBrightness(previousBrightness);
    }

    @Override
    public String description() {
        return "Ajustar brillo de " + light.getName() + " a " + newBrightness + "%";
    }
}
