package com.ruiyun.jvppeteer.bidi.entities;

public class ResponseContent {
    private long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ResponseContent{" +
                "size=" + size +
                '}';
    }
}
