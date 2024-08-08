package com.ruiyun.jvppeteer.events;

public class TracingCompleteEvent {

    private String stream;

    public TracingCompleteEvent() {
    }

    public TracingCompleteEvent(String stream) {
        this.stream = stream;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "TracingCompleteEvent{" +
                "stream='" + stream + '\'' +
                '}';
    }
}
