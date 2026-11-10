package com.smarthome.mediator;

import com.smarthome.model.Device;
import com.smarthome.observer.DeviceEvent;

public interface HomeMediator {

    void colleagueChanged(Device source, DeviceEvent event);

    boolean isAutomationEnabled();

    void setAutomatio
    void withoutAutomation(Runnable action);
}
