package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.protocol.page.frame.Frame;
import com.ruiyun.jvppeteer.protocol.page.frame.FrameManager;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

public class DOMWorld {

    private FrameManager frameManager;

    private Frame frame;

    private TimeoutSettings timeoutSettings;

    private boolean detached;

    private Set<WaitTask> waitTasks;
    
    private ElementHandle documentPromise;

    private boolean hasContext;

    private ExecutionContext contextPromise;

    public DOMWorld() {
        super();
    }

    public DOMWorld(FrameManager frameManager, Frame frame, TimeoutSettings timeoutSettings) {
        super();
        this.frameManager = frameManager;
        this.frame = frame;
        this.timeoutSettings = timeoutSettings;
        this.detached = false;
        this.hasContext = false;
    }

    public String content() {
//        return  this.evaluate();
        return null;
    }

//    private Object evaluate(String pageFunction, PageEvaluateType type Object ...args) {
//    const context =  this.executionContext();
//        return context.evaluate(pageFunction, ...args);
//    }
//
//    public ExecutionContext executionContext() {
//        if (this.detached)
//            throw new RuntimeException("Execution Context is not available in detached frame "+this.frame.getUrl()+" (are you trying to evaluate?)");
//        return this.contextPromise;
//    }
    public void setContext(ExecutionContext context) {
        if (context != null) {
            this.contextResolveCallback(null, context);
            hasContext = false;
            for (WaitTask waitTask : this.waitTasks) {
                waitTask.rerun();
            }
        } else {
            this.documentPromise = null;
            this.hasContext = true;
        }
    }

    private void contextResolveCallback(Object o, ExecutionContext context) {
    }

    public boolean hasContext() {
        return !hasContext;
    }

    public ExecutionContext executionContext() {
        if (this.detached)
            throw new RuntimeException(MessageFormat.format("Execution Context is not available in detached frame {0} (are you trying to evaluate?)",this.frame.getUrl()));
        return this.contextPromise;
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object[] args) {
        return  null;
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object[] args) {
        return null;
    }

    public ElementHandle $(String selector) {
        return null;
    }

    public List<ElementHandle> $x(String expression) {
        return null;
    }

    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) {
        return null;
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) {
        return null;
    }

    public ElementHandle $$(String selector) {
        return null;
    }

    public void setContent(String html, PageNavigateOptions options) {

    }

    public ElementHandle addScriptTag(ScriptTagOptions options) {
        return null;
    }

    public ElementHandle addStyleTag(StyleTagOptions options) {
        return null;
    }

    public void click(String selector, ClickOptions options) {
    }

    public void focus(String selector) {
    }

    public void hover(String selector) {
    }

    public List<String> select(String selector, String[] values) {
        return null;
    }

    public void tap(String selector) {
    }

    public void type(String selector, String text, int delay) {
    }

    public ElementHandle waitForSelector(String selector, WaitForOptions options) {
        return null;
    }

    public ElementHandle waitForXPath(String xpath, WaitForOptions options) {
        return null;
    }

    public String title() {
        return null;
    }

    public JSHandle waitForFunction(String pageFunction, WaitForOptions options, Object[] args) {
        return null;
    }

    public void detach() {
    }


//
//    public boolean hasContext() {
//        return false;
//    }
//
//    contextResolveCallback
}
