package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.console.Location;

import java.util.List;

/**
 * ConsoleMessage objects are dispatched by page via the 'console' event.
 */
public class  ConsoleMessage {

    private String type;

    private List<JSHandle> args;

    private Location location;

    private String text;

    public ConsoleMessage() {
    }

    public ConsoleMessage(String type, String text, List<JSHandle> args, Location location) {
        super();
        this.type = type;
        this.text = text;
        this.args = args;
        this.location = location;
    }

    /**
     * One of the following values: 'log', 'debug', 'info', 'error', 'warning', 'dir', 'dirxml', 'table', 'trace', 'clear', 'startGroup', 'startGroupCollapsed', 'endGroup', 'assert', 'profile', 'profileEnd', 'count', 'timeEnd'.
     * @return type
     */
    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<JSHandle> args() {
        return args;
    }

    public void setArgs(List<JSHandle> args) {
        this.args = args;
    }

    public Location location() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
