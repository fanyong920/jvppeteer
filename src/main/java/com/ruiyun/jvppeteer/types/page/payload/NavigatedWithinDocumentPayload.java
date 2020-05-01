package com.ruiyun.jvppeteer.types.page.payload;

/**
 * Fired when same-document navigation happens, e.g. due to history API usage or anchor navigation.
 */
public class NavigatedWithinDocumentPayload {

    /**
     * Id of the frame.
     */
    private String frameId;
    /**
     * Frame's new url.
     */
    private String url;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
