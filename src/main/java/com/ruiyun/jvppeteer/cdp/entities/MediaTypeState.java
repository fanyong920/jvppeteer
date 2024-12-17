package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.common.MediaType;

public class MediaTypeState extends ActiveProperty {

    public MediaType type;

    public MediaTypeState(boolean active, MediaType type) {
        super(active);
        this.type = type;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MediaTypeState{" +
                "type='" + type + '\'' +
                ", active=" + active +
                '}';
    }
}
