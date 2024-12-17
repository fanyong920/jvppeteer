package com.ruiyun.jvppeteer.common;

public enum WebPermission {
    GEOLOCATION("geolocation"),
    MIDI("midi"),
    NOTIFICATIONS("notifications"),
    CAMERA("camera"),
    MICROPHONE("microphone"),
    BACKGROUND_SYNC("background-sync"),
    AMBIENT_LIGHT_SENSOR("ambient-light-sensor"),
    ACCELEROMETER("accelerometer"),
    GYROSCOPE("gyroscope"),
    MAGNETOMETER("magnetometer"),
    ACCESSIBILITY_EVENTS("accessibility-events"),
    CLIPBOARD_READ("clipboard-read"),
    CLIPBOARD_WRITE("clipboard-write"),
    CLIPBOARD_SANITIZED_WRITE("clipboard-sanitized-write"),
    PAYMENT_HANDLER("payment-handler"),
    PERSISTENT_STORAGE("persistent-storage"),
    IDLE_DETECTION("idle-detection"),
    MIDI_SYSEX("midi-sysex");

    private final String permission;

    WebPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
