package com.ruiyun.jvppeteer.cdp.entities;

public class EntryPreview {
    /**
     * Preview of the key. Specified for map-like collection entries.
     */
    private ObjectPreview key;
    /**
     * Preview of the value.
     */
    private ObjectPreview value;

    public ObjectPreview getKey() {
        return key;
    }

    public void setKey(ObjectPreview key) {
        this.key = key;
    }

    public ObjectPreview getValue() {
        return value;
    }

    public void setValue(ObjectPreview value) {
        this.value = value;
    }
}
