package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.dom.DOMWorld;
import com.ruiyun.jvppeteer.protocol.dom.ElementHandle;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
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

    public Response waitForNavigation(PageNavigateOptions options){
        return this.frameManager.waitForFrameNavigation(this,options);
    }

    public ExecutionContext executionContext(){
        return this.mainWorld.executionContext();
    }
    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        return this.mainWorld.evaluateHandle(pageFunction,type,args);
    }

    public Object evaluate(String pageFunction,PageEvaluateType type, Object... args) {
        return this.mainWorld.evaluate(pageFunction, type,args);
    }

    public ElementHandle $(String selector) {
        return this.mainWorld.$(selector);
    }

    public List<ElementHandle> $x(String expression) {
        return this.mainWorld.$x(expression);
    }

    public Object  $eval(String selector,String pageFunction,PageEvaluateType type,Object... args) {
        return this.mainWorld.$eval(selector,pageFunction,type,args);
    }

    public Object $$eval(String selector,String pageFunction,PageEvaluateType type,Object... args) {
        return this.mainWorld.$$eval(selector, pageFunction,type,args);
    }

    public ElementHandle $$(String selector) {
        return this.mainWorld.$$(selector);
    }

    public ElementHandle addScriptTag(ScriptTagOptions options) {
        return this.mainWorld.addScriptTag(options);
    }

    public ElementHandle addStyleTag(StyleTagOptions options) {
        return this.mainWorld.addStyleTag(options);
    }

    public void click(String selector, ClickOptions options) {
         this.secondaryWorld.click(selector, options);
    }

    public void focus(String selector) {
        this.secondaryWorld.focus(selector);
    }

    //TODO 还有很多方法要实现
    public void onLoadingStopped() {
    }

    public Response goTo(String url, PageNavigateOptions options) throws InterruptedException {
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

    public void setContent(String html, PageNavigateOptions options) {
         this.secondaryWorld.setContent(html, options);
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

    public void onLifecycleEvent(String loaderId, String name) {
    }
}
