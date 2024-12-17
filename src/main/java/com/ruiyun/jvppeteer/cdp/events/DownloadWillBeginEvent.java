package com.ruiyun.jvppeteer.cdp.events;

public class DownloadWillBeginEvent {
    /**
     * Id of the frame that caused the download to begin.
     */
    private String frameId;
    /**
     * Global unique identifier of the download.
     */
    private  String guid;
    /**
     * URL of the resource being downloaded.
     */
    private  String url;
    /**
     * Suggested file name of the resource (the actual name of the file saved on disk may differ).
     */
    private  String suggestedFilename;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSuggestedFilename() {
        return suggestedFilename;
    }

    public void setSuggestedFilename(String suggestedFilename) {
        this.suggestedFilename = suggestedFilename;
    }

    @Override
    public String toString() {
        return "DownloadWillBeginEvent{" +
                "frameId='" + frameId + '\'' +
                ", guid='" + guid + '\'' +
                ", url='" + url + '\'' +
                ", suggestedFilename='" + suggestedFilename + '\'' +
                '}';
    }
}
