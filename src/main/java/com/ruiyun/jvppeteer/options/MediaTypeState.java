package com.ruiyun.jvppeteer.options;

public class MediaTypeState extends ActiveProperty {

    public String type;

    public MediaTypeState(boolean active, String type) {
        super(active);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
