package com.ruiyun.jvppeteer.protocol.console;

import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ConsoleMessage {

    private String type;

    private List<JSHandle> args;

    private Object location;

    private String text;

    public ConsoleMessage() {
    }

    public ConsoleMessage(String type, String text, List<JSHandle> args, Object location) {
        super();
        this.type = type;
        this.text = text;
        this.args = args;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<JSHandle> getArgs() {
        return args;
    }

    public void setArgs(List<JSHandle> args) {
        this.args = args;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
