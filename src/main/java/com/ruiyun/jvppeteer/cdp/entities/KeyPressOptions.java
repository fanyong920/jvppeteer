package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class KeyPressOptions {
    private String text;
    private List<String> commands;
    private long delay;

    public KeyPressOptions() {
    }

    public KeyPressOptions(String text, List<String> commands, int delay) {
        this.text = text;
        this.commands = commands;
        this.delay = delay;
    }

    public String getText() {
        return text;
    }

    public KeyPressOptions setText(String text) {
        this.text = text;
        return this;
    }

    public KeyPressOptions setCommands(List<String> commands) {
        this.commands = commands;
        return this;
    }

    public KeyPressOptions setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public long getDelay() {
        return delay;
    }

    public List<String> getCommands() {
        return commands;
    }
}
