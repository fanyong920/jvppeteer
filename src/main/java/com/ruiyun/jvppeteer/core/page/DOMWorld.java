package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.exception.NavigateException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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

    private CountDownLatch waitForContext = null;

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
                "    }", PageEvaluateType.FUNCTION);
    }

    public void setContext(ExecutionContext context) {
        if (context != null) {
            this.contextResolveCallback(context);
            hasContext = true;
            for (WaitTask waitTask : this.waitTasks) {
                Helper.commonExecutor().submit(waitTask::rerun);
            }
        } else {
            this.documentPromise = null;
            this.hasContext = false;
        }
    }

    private void contextResolveCallback(ExecutionContext context) {
        this.contextPromise = context;
//        JSHandle document = (JSHandle)context.evaluateHandle("document", PageEvaluateType.STRING, null);
//        this.documentPromise = document.asElement();
        if (this.waitForContext != null) {
            this.waitForContext.countDown();
        }
    }

    public boolean hasContext() {
        return hasContext;
    }

    public ExecutionContext executionContext() {
        if (this.detached)
            throw new RuntimeException(MessageFormat.format("Execution Context is not available in detached frame {0} (are you trying to evaluate?)", this.frame.getUrl()));
        if (this.contextPromise == null) {
            this.waitForContext = new CountDownLatch(1);
            try {
                boolean await = this.waitForContext.await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!await) {
                    throw new TimeoutException("Wait for ExecutionContext time out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.contextPromise;
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.executionContext();
        return (JSHandle) context.evaluateHandle(pageFunction, type, args);
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.executionContext();
        return context.evaluate(pageFunction, type, args);
    }

    public ElementHandle $(String selector) {
        ElementHandle document = this.document();
        return document.$(selector);
    }

    private ElementHandle document() {
        if (this.documentPromise != null)
            return this.documentPromise;
        ExecutionContext context = this.executionContext();
        JSHandle document = (JSHandle) context.evaluateHandle("document", PageEvaluateType.STRING);
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

    public List<ElementHandle> $$(String selector) {
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
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager, this.frame, waitUntil, timeout);
        this.evaluate("(html) => {\n" +
                "      document.open();\n" +
                "      document.write(html);\n" +
                "      document.close();\n" +
                "    }", PageEvaluateType.FUNCTION, html);
        if (watcher.lifecyclePromise() != null) {
            return;
        }
        try {
            CountDownLatch latch = new CountDownLatch(1);
            this.frameManager.setContentLatch(latch);
            this.frameManager.setNavigateResult(null);
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (await) {
                if (NavigateResult.CONTENT_SUCCESS.getResult().equals(this.frameManager.getNavigateResult())) {

                } else if (NavigateResult.TIMEOUT.getResult().equals(this.frameManager.getNavigateResult())) {
                    throw new TimeoutException("setContent timeout :" + html);
                } else if (NavigateResult.TERMINATION.getResult().equals(this.frameManager.getNavigateResult())) {
                    throw new NavigateException("Navigating frame was detached");
                } else {
                    throw new NavigateException("UnNokwn result " + this.frameManager.getNavigateResult());
                }
            } else {
                throw new TimeoutException("setContent timeout " + html);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            watcher.dispose();
        }
    }

    public ElementHandle addScriptTag(ScriptTagOptions options) throws IOException {
        if (StringUtil.isNotEmpty(options.getUrl())) {
            try {
                ExecutionContext context = this.executionContext();
                ElementHandle handle = (ElementHandle) context.evaluateHandle(addScriptUrl(), PageEvaluateType.FUNCTION, options.getUrl(), options.getType());
                return handle.asElement();
            } catch (Exception e) {
                throw new RuntimeException("Loading script from " + options.getUrl() + " failed", e);
            }
        }
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            String content = String.join("\n",contents)+"//# sourceURL=" + options.getPath().replaceAll("\n", "");
            ExecutionContext context = this.executionContext();
            ElementHandle evaluateHandle = (ElementHandle) context.evaluateHandle(addScriptContent(), PageEvaluateType.FUNCTION, content, options.getType());
            return evaluateHandle.asElement();
        }
        if (StringUtil.isNotEmpty(options.getContent())) {
            ExecutionContext context = this.executionContext();
            ElementHandle elementHandle = (ElementHandle) context.evaluateHandle(addScriptContent(), PageEvaluateType.FUNCTION, options.getContent(), options.getType());
            return elementHandle.asElement();
        }
        throw new IllegalArgumentException("Provide an object with a `url`, `path` or `content` property");
    }

    private String addScriptContent() {
        return "function addScriptContent(content, type = 'text/javascript') {\n" +
                "    const script = document.createElement('script');\n" +
                "    script.type = type;\n" +
                "    script.text = content;\n" +
                "    let error = null;\n" +
                "    script.onerror = e => error = e;\n" +
                "    document.head.appendChild(script);\n" +
                "    if (error)\n" +
                "      throw error;\n" +
                "    return script;\n" +
                "  }";
    }

    private String addScriptUrl() {
        return "async function addScriptUrl(url, type) {\n" +
                "      const script = document.createElement('script');\n" +
                "      script.src = url;\n" +
                "      if (type)\n" +
                "        script.type = type;\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        script.onload = res;\n" +
                "        script.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(script);\n" +
                "      await promise;\n" +
                "      return script;\n" +
                "    }";
    }

    public ElementHandle addStyleTag(StyleTagOptions options) throws IOException {
        if (options != null && StringUtil.isNotEmpty(options.getUrl())) {
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleUrl(), PageEvaluateType.FUNCTION, options.getUrl());
            return handle.asElement();
        }

        if (options != null && StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            String content = String.join("\n",contents)+"/*# sourceURL=" + options.getPath().replaceAll("\n", "")+"*/";
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleContent(), PageEvaluateType.FUNCTION, content);
            return handle.asElement();
        }

        if (options != null && StringUtil.isNotEmpty(options.getContent())) {
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleContent(), PageEvaluateType.FUNCTION, options.getContent());
            return handle.asElement();
        }

        throw new IllegalArgumentException("Provide an object with a `url`, `path` or `content` property");
    }

    private String addStyleContent() {
        return "async function addStyleContent(content) {\n" +
                "      const style = document.createElement('style');\n" +
                "      style.type = 'text/css';\n" +
                "      style.appendChild(document.createTextNode(content));\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        style.onload = res;\n" +
                "        style.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(style);\n" +
                "      await promise;\n" +
                "      return style;\n" +
                "    }";
    }

    private String addStyleUrl() {
        return "async function addStyleUrl(url) {\n" +
                "      const link = document.createElement('link');\n" +
                "      link.rel = 'stylesheet';\n" +
                "      link.href = url;\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        link.onload = res;\n" +
                "        link.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(link);\n" +
                "      await promise;\n" +
                "      return link;\n" +
                "    }";
    }

    public void click(String selector, ClickOptions options,boolean isBlock) throws InterruptedException, ExecutionException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        if(isBlock){
            handle.click(options,true);
            handle.dispose();
            return;
        }
        Helper.commonExecutor().submit(() -> {
            try {
                handle.click(options,true);
                handle.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    public void focus(String selector) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.focus();
        handle.dispose();
    }

    public void hover(String selector) throws ExecutionException, InterruptedException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.hover();
        handle.dispose();
    }

    public List<String> select(String selector, List<String> values) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        List<String> result = handle.select(values);
        handle.dispose();
        return result;
    }

    public void tap(String selector,boolean isBlock) {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        if(isBlock){
            handle.tap();
            handle.dispose();
        }else {
            Helper.commonExecutor().submit(() -> {
                handle.tap();
                handle.dispose();
            });
        }
    }

    public void type(String selector, String text, int delay) throws InterruptedException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.type(text, delay);
        handle.dispose();
    }

    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws InterruptedException {
        return this.waitForSelectorOrXPath(selector, false, options);
    }

    private ElementHandle waitForSelectorOrXPath(String selectorOrXPath, boolean isXPath, WaitForSelectorOptions options) throws InterruptedException {
        boolean waitForVisible = false;
        boolean waitForHidden = false;
        int timeout = this.timeoutSettings.timeout();
        if (options != null) {
            waitForVisible = options.getVisible();
            waitForHidden = options.getHidden();
            if (options.getTimeout() > 0) {
                timeout = options.getTimeout();
            }
        }
        String polling = waitForVisible || waitForHidden ? "raf" : "mutation";
        String title = (isXPath ? "XPath" : "selector") + " " + "\"" + selectorOrXPath + "\"" + (waitForHidden ? " to be hidden" : "");

        QuerySelector queryHandlerAndSelector = QueryHandler.getQueryHandlerAndSelector(selectorOrXPath, "(element, selector) =>\n" +
                "      document.querySelector(selector)");
        String queryHandler = queryHandlerAndSelector.getQueryHandler();
        String updatedSelector = queryHandlerAndSelector.getUpdatedSelector();
        String predicate = "function predicate(selectorOrXPath, isXPath, waitForVisible, waitForHidden) {\n" +
                "      const node = isXPath\n" +
                "        ? document.evaluate(selectorOrXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue\n" +
                "        : document.querySelector(selectorOrXPath);\n" +
                "      if (!node)\n" +
                "        return waitForHidden;\n" +
                "      if (!waitForVisible && !waitForHidden)\n" +
                "        return node;\n" +
                "      const element = /** @type {Element} */ (node.nodeType === Node.TEXT_NODE ? node.parentElement : node);\n" +
                "\n" +
                "      const style = window.getComputedStyle(element);\n" +
                "      const isVisible = style && style.visibility !== 'hidden' && hasVisibleBoundingBox();\n" +
                "      const success = (waitForVisible === isVisible || waitForHidden === !isVisible);\n" +
                "      return success ? node : null;\n" +
                "\n" +
                "      /**\n" +
                "       * @return {boolean}\n" +
                "       */\n" +
                "      function hasVisibleBoundingBox() {\n" +
                "        const rect = element.getBoundingClientRect();\n" +
                "        return !!(rect.top || rect.bottom || rect.width || rect.height);\n" +
                "      }\n" +
                "    }";
        WaitTask waitTask = new WaitTask(this, predicate, queryHandler, PageEvaluateType.FUNCTION, title, polling, timeout, updatedSelector, isXPath, waitForVisible, waitForHidden);
        JSHandle handle = waitTask.getPromise();
        if(handle == null){
            return null;
        }
        if (handle.asElement() == null) {
            handle.dispose();
            return null;
        }
        return handle.asElement();
    }

    public ElementHandle waitForXPath(String xpath, WaitForSelectorOptions options) throws InterruptedException {
        return this.waitForSelectorOrXPath(xpath, true, options);
    }

    public String title() {
        return (String) this.evaluate("() => document.title", PageEvaluateType.FUNCTION);
    }

    public JSHandle waitForFunction(String pageFunction, PageEvaluateType type, WaitForSelectorOptions options, Object... args) throws InterruptedException {
        String polling = "raf";
        int timeout = this.timeoutSettings.timeout();
        if (StringUtil.isNotEmpty(options.getPolling())) {
            polling = options.getPolling();
        }
        if (options.getTimeout() > 0) {
            timeout = options.getTimeout();
        }
        return new WaitTask(this, pageFunction, null, type, "function", polling, timeout, args).getPromise();
    }

    public void detach() {
        this.detached = true;
        for (WaitTask waitTask : this.waitTasks)
            waitTask.terminate(new RuntimeException("waitForFunction failed: frame got detached."));
    }

    public Set<WaitTask> getWaitTasks() {
        return waitTasks;
    }

}
