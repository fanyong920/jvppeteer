package com.ruiyun.jvppeteer.bidi.entities;

public class MessageParameters {
    private String channel;
    private RemoteValue data;
    private Source source;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public RemoteValue getData() {
        return data;
    }

    public void setData(RemoteValue data) {
        this.data = data;
    }
}
