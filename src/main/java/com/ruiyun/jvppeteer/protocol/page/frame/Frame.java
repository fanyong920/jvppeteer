package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.options.PageOptions;
import com.ruiyun.jvppeteer.protocol.page.network.Response;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.List;

/**
 * Information about the Frame on the page.
 */
public class Frame {
    /**
     * Frame unique identifier.
     */
    private String id;

    /**
     * Parent frame identifier.
     */
    private String parentId;
    /**
     * Identifier of the loader associated with this frame.
     */
    private String loaderId;
    /**
     * Frame's name as specified in the tag.
     */
    private String name;
    /**
     * Frame document's URL without fragment.
     */
    private String url;
    /**
     * Frame document's URL fragment including the '#'.
     */
    private String urlFragment;
    /**
     * Frame document's security origin.
     */
    private String securityOrigin;
    /**
     * Frame document's mimeType as determined by the browser.
     */
    private String mimeType;
    /**
     * If the frame failed to load, this contains the URL that could not be loaded. Note that unlike url above, this URL may contain a fragment.
     */
    private String unreachableUrl;

    private FrameManager frameManager;

    public Frame(FrameManager frameManager, CDPSession client, Frame parentFrame, String frameId) {
        this.frameManager = frameManager;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlFragment() {
        return urlFragment;
    }

    public void setUrlFragment(String urlFragment) {
        this.urlFragment = urlFragment;
    }

    public String getSecurityOrigin() {
        return securityOrigin;
    }

    public void setSecurityOrigin(String securityOrigin) {
        this.securityOrigin = securityOrigin;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUnreachableUrl() {
        return unreachableUrl;
    }

    public void setUnreachableUrl(String unreachableUrl) {
        this.unreachableUrl = unreachableUrl;
    }

   public List<Frame> getChildFrames() {
        return null;
    }

    public void detach() {
    }

    /**
     * @param {!Protocol.Page.Frame} framePayload
     */
    public void navigated(Frame framePayload) {
    }

    public void navigatedWithinDocument(String url) {
    }

    public void onLoadingStopped() {
    }

    public Response go2(String url, PageOptions options) {
       return this.frameManager.navigateFrame(this,url,options);
    }
}
