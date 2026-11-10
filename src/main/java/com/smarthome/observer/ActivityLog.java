package com.smarthome.observer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ActivityLog implements DeviceObserver {

    private static final int MAX_ENTRIES = 200;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Deque<String> entries = new ArrayDeque<>();

    @Override
    public void onDeviceEvent(DeviceEvent event) {
        String prefix = event.kind() == DeviceEvent.Kind.REJECTED ? "(rechazado) " : "";
        add(event.time().format(TIME_FORMAT), event.device().getName() + ": " + prefix + event.message());
    }

    public void info(String message) {
        add(java.time.LocalTime.now().format(TIME_FORMAT), message);
    }

    public synchronized List<String> entries() {
        return new ArrayList<>(entries);
    }

    private synchronized void add(String time, String message) {
        entries.addFirst("[" + time + "] " + message);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }
}
