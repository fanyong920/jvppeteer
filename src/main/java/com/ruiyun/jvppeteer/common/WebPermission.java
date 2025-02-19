package com.ruiyun.jvppeteer.common;

public enum WebPermission {
    Accelerometer("accelerometer"),
    Ambient_light_sensor("ambient-light-sensor"),
    Background_sync("background-sync"),
    Camera("camera"),
    Clipboard_read("clipboard-read"),
    Clipboard_sanitized_write("clipboard-sanitized-write"),
    Clipboard_write("clipboard-write"),
    Geolocation("geolocation"),
    Gyroscope("gyroscope"),
    Idle_detection("idle-detection"),
    Keyboard_lock("keyboard-lock"),
    Magnetometer("magnetometer"),
    Microphone("microphone"),
    Midi("midi"),
    Notifications("notifications"),
    Payment_handler("payment-handler"),
    Persistent_storage("persistent-storage"),
    Pointer_lock("pointer-lock"),
    Midi_sysex("midi-sysex");




    private final String permission;

    WebPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
