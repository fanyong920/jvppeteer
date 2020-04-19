package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.options.PageOptions;
import com.ruiyun.jvppeteer.protocol.dom.DOMWorld;
import com.ruiyun.jvppeteer.protocol.page.network.Response;
import com.ruiyun.jvppeteer.protocol.page.payload.FramePayload;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Frame {

    private String id;

    private String loaderId;

    private FrameManager frameManager;

    private CDPSession client;

    private Frame parentFrame;

    private String url;

    private boolean detached;

    private Set<String> lifecycleEvents;

    private DOMWorld mainWorld;

    private DOMWorld secondaryWorld;

    private Set<Frame> childFrames;

    public Frame(FrameManager frameManager, CDPSession client, Frame parentFrame, String frameId) {
        this.frameManager = frameManager;
        this.client = client;
        this.parentFrame = parentFrame;
        this.url = "";
        this.id = frameId;
        this.detached = false;
        this.loaderId = "";
        this.lifecycleEvents = new HashSet<>();
        this.mainWorld = new DOMWorld(frameManager, this, frameManager.getTimeoutSettings());
        this.secondaryWorld = new DOMWorld(frameManager, this, frameManager.getTimeoutSettings());
        this.childFrames = new HashSet<>();
        if(this.parentFrame != null)
            this.parentFrame.getChildFrames().add(this);
    }



   public List<Frame> getChildFrames() {
        return null;
    }

    public void detach() {
    }

    /**
     * @param {!Protocol.Page.Frame} framePayload
     */
    public void navigated(FramePayload framePayload) {
    }

    public void navigatedWithinDocument(String url) {
    }

    public void onLoadingStopped() {
    }

    public Response goTo(String url, PageOptions options) throws InterruptedException {
       return this.frameManager.navigateFrame(this,url,options);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String content() {
        return this.secondaryWorld.content();
    }

    public FrameManager getFrameManager() {
        return frameManager;
    }

    public void setFrameManager(FrameManager frameManager) {
        this.frameManager = frameManager;
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Frame getParentFrame() {
        return parentFrame;
    }

    public void setParentFrame(Frame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getDetached() {
        return detached;
    }

    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    public Set<String> getLifecycleEvents() {
        return lifecycleEvents;
    }

    public void setLifecycleEvents(Set<String> lifecycleEvents) {
        this.lifecycleEvents = lifecycleEvents;
    }

    public DOMWorld getMainWorld() {
        return mainWorld;
    }

    public void setMainWorld(DOMWorld mainWorld) {
        this.mainWorld = mainWorld;
    }

    public DOMWorld getSecondaryWorld() {
        return secondaryWorld;
    }

    public void setSecondaryWorld(DOMWorld secondaryWorld) {
        this.secondaryWorld = secondaryWorld;
    }

    public void setChildFrames(Set<Frame> childFrames) {
        this.childFrames = childFrames;
    }
}
