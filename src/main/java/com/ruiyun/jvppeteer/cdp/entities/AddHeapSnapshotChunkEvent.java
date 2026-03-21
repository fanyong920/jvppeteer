package com.ruiyun.jvppeteer.cdp.entities;

public class AddHeapSnapshotChunkEvent {

    private String chunk;

    public AddHeapSnapshotChunkEvent() {
    }

    public AddHeapSnapshotChunkEvent(String chunk) {
        this.chunk = chunk;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }
}
