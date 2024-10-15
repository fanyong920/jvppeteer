package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.JSHandle;

import java.util.List;

/**
 * ConsoleMessage objects are dispatched by page via the 'console' event.
 */
public class ConsoleMessage {

    private ConsoleMessageType type;

    private List<JSHandle> args;

    private List<ConsoleMessageLocation> stackTraceLocations;

    private String text;

    public ConsoleMessage() {
    }

    public ConsoleMessage(ConsoleMessageType type, String text, List<JSHandle> args, List<ConsoleMessageLocation> stackTraceLocations) {
        super();
        this.type = type;
        this.text = text;
        this.args = args;
        this.stackTraceLocations = stackTraceLocations;
    }

    /**
     * One of the following values: 'log', 'debug', 'info', 'error', 'warning', 'dir', 'dirxml', 'table', 'trace', 'clear', 'startGroup', 'startGroupCollapsed', 'endGroup', 'assert', 'profile', 'profileEnd', 'count', 'timeEnd'.
     *
     * @return type
     */
    public ConsoleMessageType type() {
        return type;
    }

    public void setType(ConsoleMessageType type) {
        this.type = type;
    }

    public List<JSHandle> args() {
        return args;
    }

    public void setArgs(List<JSHandle> args) {
        this.args = args;
    }

    public ConsoleMessageLocation location() {
        return this.stackTraceLocations.isEmpty() ? null : this.stackTraceLocations.get(0);
    }

    public void setLocation(List<ConsoleMessageLocation> stackTraceLocations) {
        this.stackTraceLocations = stackTraceLocations;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<ConsoleMessageLocation> stackTrace() {
        return this.stackTraceLocations;
    }

    @Override
    public String toString() {
        return "ConsoleMessage{" +
                "type=" + type +
                ", args=" + args +
                ", stackTraceLocations=" + stackTraceLocations +
                ", text='" + text + '\'' +
                '}';
    }
}
