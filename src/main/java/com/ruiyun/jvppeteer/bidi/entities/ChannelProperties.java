package com.ruiyun.jvppeteer.bidi.entities;

public class ChannelProperties {
    private String channel;
    private SerializationOptions serializationOptions;
    private ResultOwnership ownership;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public SerializationOptions getSerializationOptions() {
        return serializationOptions;
    }

    public void setSerializationOptions(SerializationOptions serializationOptions) {
        this.serializationOptions = serializationOptions;
    }

    public ResultOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(ResultOwnership ownership) {
        this.ownership = ownership;
    }
}
