package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import java.util.List;
import java.util.Objects;

/**
 * ConsoleMessage objects are dispatched by page via the 'console' event.
 */
public class ConsoleMessage {
    private ConsoleMessageType type;
    private List<JSHandle> args;
    private List<ConsoleMessageLocation> stackTraceLocations;
    private String text;
    private Frame frame;

    public ConsoleMessage() {
    }

    public ConsoleMessage(ConsoleMessageType type, String text, List<JSHandle> args, List<ConsoleMessageLocation> stackTraceLocations, Frame frame) {
        super();
        this.type = type;
        this.text = text;
        this.args = args;
        this.stackTraceLocations = stackTraceLocations;
        this.frame = frame;
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
        return this.stackTraceLocations.isEmpty() ? (Objects.nonNull(this.frame) ? new ConsoleMessageLocation(this.frame.url(), 0) : new ConsoleMessageLocation()) : this.stackTraceLocations.get(0);
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
