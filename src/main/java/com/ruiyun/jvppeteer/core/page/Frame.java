package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.page.FramePayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


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

    private String name;

    private String navigationURL;

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
        this.childFrames = new CopyOnWriteArraySet<>();
        if (this.parentFrame != null)
            this.parentFrame.getChildFrames().add(this);
    }


    public Set<Frame> getChildFrames() {
        return this.childFrames;
    }

    public void detach() {
        this.detached = true;
        this.mainWorld.detach();
        this.secondaryWorld.detach();
        if (this.parentFrame != null)
            this.parentFrame.childFrames.remove(this);
        this.parentFrame = null;
    }


    public void navigatedWithinDocument(String url) {
        this.url = url;
    }

    public Response waitForNavigation(PageNavigateOptions options, AtomicBoolean start) {
        return this.frameManager.waitForFrameNavigation(this, options,start);
    }

    public ExecutionContext executionContext() {
        return this.mainWorld.executionContext();
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        return this.mainWorld.evaluateHandle(pageFunction, type, args);
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        return this.mainWorld.evaluate(pageFunction, type, args);
    }

    public ElementHandle $(String selector) {
        return this.mainWorld.$(selector);
    }

    public List<ElementHandle> $x(String expression) {
        return this.mainWorld.$x(expression);
    }

    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object... args)   {
        return this.mainWorld.$eval(selector, pageFunction, type, args);
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object... args)   {
        return this.mainWorld.$$eval(selector, pageFunction, type, args);
    }

    public List<ElementHandle> $$(String selector) {
        return this.mainWorld.$$(selector);
    }

    public ElementHandle addScriptTag(ScriptTagOptions options) throws IOException {
        return this.mainWorld.addScriptTag(options);
    }

    public ElementHandle addStyleTag(StyleTagOptions options) throws IOException {
        return this.mainWorld.addStyleTag(options);
    }

    public void click(String selector, ClickOptions options,boolean isBlock) throws InterruptedException, ExecutionException {
        this.secondaryWorld.click(selector, options,isBlock);
    }

    public void focus(String selector) {
        this.secondaryWorld.focus(selector);
    }

    public void hover(String selector) throws ExecutionException, InterruptedException {
        this.secondaryWorld.hover(selector);
    }

    public List<String> select(String selector, List<String> values) {
        return this.secondaryWorld.select(selector, values);
    }

    public void tap(String selector,boolean isBlock) {
        this.secondaryWorld.tap(selector,isBlock);
    }

    public void type(String selector, String text, int delay) throws InterruptedException {
        this.mainWorld.type(selector, text, delay);
    }

    /**
     * @param selectorOrFunctionOrTimeout 元素选择器，函数或者超时时间
     * @param options 可配置等待选项
     * @param args functions时对应的function参数
     * @throws InterruptedException 打断异常
     * @return 元素处理器
     */
    public JSHandle waitFor(String selectorOrFunctionOrTimeout, WaitForSelectorOptions options, Object... args) throws InterruptedException {
        String xPathPattern = "//";

        if (Helper.isFunction(selectorOrFunctionOrTimeout)) {
            return this.waitForFunction(selectorOrFunctionOrTimeout, PageEvaluateType.FUNCTION, options, args);
        }else if (Helper.isNumber(selectorOrFunctionOrTimeout)) {
//            return new Promise(fulfill => setTimeout(fulfill, /** @type {number} */ (selectorOrFunctionOrTimeout)));
            Thread.sleep(Long.parseLong(selectorOrFunctionOrTimeout));
            return null;
        }else {
            if (selectorOrFunctionOrTimeout.startsWith(xPathPattern)) {
                return this.waitForXPath(selectorOrFunctionOrTimeout, options);
            }
            return this.waitForSelector(selectorOrFunctionOrTimeout, options);
        }

    }

    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws InterruptedException {
        ElementHandle handle = this.secondaryWorld.waitForSelector(selector, options);
        if (handle == null)
            return null;
        ExecutionContext mainExecutionContext = this.mainWorld.executionContext();
        ElementHandle result = mainExecutionContext.adoptElementHandle(handle);
        handle.dispose();
        return result;
    }

    public JSHandle waitForFunction(String pageFunction, PageEvaluateType type, WaitForSelectorOptions options, Object[] args) throws InterruptedException {
        return this.mainWorld.waitForFunction(pageFunction, type, options, args);
    }

    public String title() {
        return this.secondaryWorld.title();
    }

    public void navigated(FramePayload framePayload) {
        this.name = framePayload.getName();
        this.url = framePayload.getUrl();
    }

    public JSHandle waitForXPath(String xpath, WaitForSelectorOptions options) throws InterruptedException {
        ElementHandle handle = this.secondaryWorld.waitForXPath(xpath, options);
        if (handle == null)
            return null;
        ExecutionContext mainExecutionContext = this.mainWorld.executionContext();
        ElementHandle result = mainExecutionContext.adoptElementHandle(handle);
        handle.dispose();
        return result;
    }

    public void onLoadingStopped() {
        this.lifecycleEvents.add("DOMContentLoaded");
        this.lifecycleEvents.add("load");
    }

    public Response goTo(String url, PageNavigateOptions options,boolean isBlock) throws InterruptedException {
        return this.frameManager.navigateFrame(this, url, options,isBlock);
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
        if ("init".equals(name)) {
            this.loaderId = loaderId;
            this.lifecycleEvents.clear();
        }
        this.lifecycleEvents.add(name);
    }

    public String getName() {
        if (this.name == null) {
            return "";
        }
        return this.name;
    }

    public boolean isDetached() {
        return this.detached;
    }

    public String url() {
        return this.url;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getNavigationURL() {
        return navigationURL;
    }

    public void setNavigationURL(String navigationURL) {
        this.navigationURL = navigationURL;
    }

    public String name() {
        return this.getName();
    }


    public Frame parentFrame() {
        return this.getParentFrame();
    }

    public Set<Frame> childFrames() {
        return this.getChildFrames();
    }

}
