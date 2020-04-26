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
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.sun.deploy.uitoolkit.ToolkitStore.dispose;

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
        return (String) this.evaluate("() => {\n" +
                "      let retVal = '';\n" +
                "      if (document.doctype)\n" +
                "        retVal = new XMLSerializer().serializeToString(document.doctype);\n" +
                "      if (document.documentElement)\n" +
                "        retVal += document.documentElement.outerHTML;\n" +
                "      return retVal;\n" +
                "    }", PageEvaluateType.FUNCTION, null);
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
            throw new RuntimeException(MessageFormat.format("Execution Context is not available in detached frame {0} (are you trying to evaluate?)", this.frame.getUrl()));
        return this.contextPromise;
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.executionContext();
        return context.evaluateHandle(pageFunction, type, args);
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.executionContext();
        return context.evaluate(pageFunction, type, args);
    }

    public ElementHandle $(String selector) {
        ElementHandle document = this.document();
        ElementHandle value = document.$(selector);
        return value;
    }

    private ElementHandle document() {
        if (this.documentPromise != null)
            return this.documentPromise;
        ExecutionContext context = this.executionContext();
        JSHandle document = context.evaluateHandle("document", PageEvaluateType.STRING, null);
        this.documentPromise = document.asElement();
        return this.documentPromise;
    }

    public List<ElementHandle> $x(String expression) {
        ElementHandle document = this.document();
        return document.$x(expression);
    }

    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        ElementHandle document = this.document();
        return document.$eval(selector, pageFunction, type, args);
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        ElementHandle document = this.document();
        return document.$$eval(selector, pageFunction, type, args);
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
                "      document.write(" + html + ");\n" +
                "      document.close();\n" +
                "    }", PageEvaluateType.FUNCTION, html);
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager, this.frame, waitUntil, timeout);
        CountDownLatch latch = new CountDownLatch(1);
        this.frameManager.setLatch(latch);
        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (await) {
                if ("success".equals(this.frameManager.getNavigateResult())) {
                    watcher.dispose();
                } else if ("timeout".equals(this.frameManager.getNavigateResult())) {
                    throw new TimeOutException("setContent timeout :" + html);
                } else if ("termination".equals(this.frameManager.getNavigateResult())) {
                    throw new NavigateException("Navigating frame was detached");
                } else {
                    throw new NavigateException("UnNokwn result " + this.frameManager.getNavigateResult());
                }
            } else {
                throw new TimeOutException("setContent timeout " + html);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ElementHandle addScriptTag(ScriptTagOptions options) throws IOException {
        if (StringUtil.isNotEmpty(options.getUrl())) {
            try {
                ExecutionContext context = this.executionContext();
                return (context.evaluateHandle(addScriptUrl(), PageEvaluateType.FUNCTION, options.getUrl(), options.getType())).asElement();
            } catch (Exception e) {
                throw new RuntimeException("Loading script from ${url} failed");
            }
        }
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);

            contents.add("//# sourceURL=" + options.getPath().replaceAll("\\n", ""));
            ExecutionContext context = this.executionContext();
            return (context.evaluateHandle(addScriptContent(), PageEvaluateType.FUNCTION, contents, options.getType())).asElement();
        }
        if (StringUtil.isNotEmpty(options.getContent())) {
            ExecutionContext context = this.executionContext();
            return (context.evaluateHandle(addScriptContent(), PageEvaluateType.FUNCTION, options.getContent(), options.getType())).asElement();
        }
        throw new IllegalArgumentException("Provide an object with a `url`, `path` or `content` property");
    }

    private String addScriptContent() {
        return null;
    }

    //TODO 貌似这个功能做不了，因为java不能实现document.head.appendChild(script);这样的语法
    private String addScriptUrl() {
        return null;
    }

    public ElementHandle addStyleTag(StyleTagOptions options) {
        return null;
    }

    public void click(String selector, ClickOptions options) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        handle.click(options);
        handle.dispose();
    }

    public void focus(String selector) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        handle.focus();
        handle.dispose();
    }

    public void hover(String selector) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        handle.hover();
        handle.dispose();
    }

    public List<String> select(String selector, String[] values) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        List<String> result = handle.select(values);
        handle.dispose();
        return result;
    }

    public void tap(String selector) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        handle.tap();
        handle.dispose();
    }

    public void type(String selector, String text, int delay) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertBoolean(handle != null, "No node found for selector: " + selector);
        handle.type(text, delay);
        handle.dispose();
    }

    public ElementHandle waitForSelector(String selector, WaitForOptions options) {
        return this.waitForSelectorOrXPath(selector, false, options);
    }
    //TODO
    private ElementHandle waitForSelectorOrXPath(String selector, boolean b, WaitForOptions options) {
        return null;
    }

    public ElementHandle waitForXPath(String xpath, WaitForOptions options) {
        return this.waitForSelectorOrXPath(xpath, true, options);
    }

    public String title() {
        return (String) this.evaluate("() => document.title",PageEvaluateType.FUNCTION,null);
    }

    public JSHandle waitForFunction(String pageFunction,PageEvaluateType type, WaitForOptions options, Object... args) {
        String polling = "raf";
        int timeout = this.timeoutSettings.timeout();
        if(StringUtil.isNotEmpty(options.getPolling())){
            polling = options.getPolling();
        }
        if(options.getTimeout() > 0){
            timeout = options.getTimeout();
        }
        return new WaitTask(this,pageFunction,type,"function",polling,timeout,args).getPromise();
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
