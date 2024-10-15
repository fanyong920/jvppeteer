package com.ruiyun.jvppeteer.entities;

import java.util.ArrayList;
import java.util.List;

public class KeyDownOptions {
    private String text;
    private List<String> commands = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
