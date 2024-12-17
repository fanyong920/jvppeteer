package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.DownloadState;

import java.math.BigDecimal;

public class DownloadProgressEvent {

    private String guid;
    /**
     * Total expected bytes to download.
     */
    private BigDecimal totalBytes;
    /**
     * Total bytes received.
     */
    private BigDecimal receivedBytes;

    private DownloadState state;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigDecimal getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(BigDecimal totalBytes) {
        this.totalBytes = totalBytes;
    }

    public BigDecimal getReceivedBytes() {
        return receivedBytes;
    }

    public void setReceivedBytes(BigDecimal receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public DownloadState getState() {
        return state;
    }

    public void setState(DownloadState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "DownloadProgressEvent{" +
                "guid='" + guid + '\'' +
                ", totalBytes=" + totalBytes +
                ", receivedBytes=" + receivedBytes +
                ", state=" + state +
                '}';
    }
}
