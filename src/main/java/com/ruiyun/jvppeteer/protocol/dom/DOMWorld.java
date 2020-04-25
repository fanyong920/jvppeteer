package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.exception.NavigateException;
import com.ruiyun.jvppeteer.exception.TimeOutException;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.protocol.page.LifecycleWatcher;
import com.ruiyun.jvppeteer.protocol.page.frame.Frame;
import com.ruiyun.jvppeteer.protocol.page.frame.FrameManager;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        this.documentPromise = null;
        this.contextPromise = null;
        this.setContext(null);
        this.waitTasks = new HashSet<>();
        this.detached = false;
        this.hasContext = false;
    }

    /**
     * @return {!Puppeteer.Frame}
     */
    public Frame frame() {
        return this.frame;
    }

    public String content() {
        return  (String)this.evaluate("() => {\n" +
                "      let retVal = '';\n" +
                "      if (document.doctype)\n" +
                "        retVal = new XMLSerializer().serializeToString(document.doctype);\n" +
                "      if (document.documentElement)\n" +
                "        retVal += document.documentElement.outerHTML;\n" +
                "      return retVal;\n" +
                "    }",PageEvaluateType.FUNCTION,null);
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

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context =  this.executionContext();
        return context.evaluateHandle(pageFunction, type,args);
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.executionContext();
        return context.evaluate(pageFunction,type,args);
    }

    public ElementHandle $(String selector) {
        ElementHandle document =  this.document();
        ElementHandle value =  document.$(selector);
        return value;
    }

    private ElementHandle document() {
        if (this.documentPromise != null)
            return this.documentPromise;
        ExecutionContext context = this.executionContext();
        JSHandle document =  context.evaluateHandle("document",PageEvaluateType.STRING,null);
        this.documentPromise = document.asElement();
        return this.documentPromise;
    }

    public List<ElementHandle> $x(String expression) {
        ElementHandle document =  this.document();
        return  document.$x(expression);
    }

    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        ElementHandle document = this.document();
        return document.$eval(selector, pageFunction, type,args);
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        ElementHandle document = this.document();
        return  document.$$eval(selector, pageFunction, type,args);
    }

    public ElementHandle $$(String selector) {
        ElementHandle document = this.document();
        return document.$$(selector);
    }

    public void setContent(String html, PageNavigateOptions options) {
        List<String> waitUntil;
        int timeout;
        if (options == null) {
            waitUntil = new ArrayList<>();
            waitUntil.add("load");
            timeout = this.timeoutSettings.navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add("load");
            }
            if ((timeout = options.getTimeout()) <= 0) {
                timeout = this.timeoutSettings.navigationTimeout();
            }
        }

        this.evaluate("() => {\n" +
                "      document.open();\n" +
                "      document.write("+html+");\n" +
                "      document.close();\n" +
                "    }", PageEvaluateType.FUNCTION, html);
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager,this.frame,waitUntil,timeout);
        CountDownLatch latch = new CountDownLatch(1);
        this.frameManager.setLatch(latch);
        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if(await){
                if ("success".equals(this.frameManager.getNavigateResult())) {
                    watcher.dispose();
                } else if ("timeout".equals(this.frameManager.getNavigateResult())) {
                    throw new TimeOutException("setContent timeout :"+html);
                } else if ("termination".equals(this.frameManager.getNavigateResult())) {
                    throw new NavigateException("Navigating frame was detached");
                } else {
                    throw new NavigateException("UnNokwn result " + this.frameManager.getNavigateResult());
                }
            }else {
                throw new TimeOutException("setContent timeout "+html);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        this.detached = true;
        for (WaitTask waitTask : this.waitTasks)
        waitTask.terminate(new RuntimeException("waitForFunction failed: frame got detached."));
    }


//
//    public boolean hasContext() {
//        return false;
//    }
//
//    contextResolveCallback
}
