package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.DeviceRequestPromptManager;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.QueryHandler;
import com.ruiyun.jvppeteer.common.QuerySelector;
import com.ruiyun.jvppeteer.entities.Binding;
import com.ruiyun.jvppeteer.entities.ClickOptions;
import com.ruiyun.jvppeteer.entities.EvaluateType;
import com.ruiyun.jvppeteer.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.entities.FramePayload;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.PreloadScript;
import com.ruiyun.jvppeteer.entities.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.entities.WaitForOptions;
import com.ruiyun.jvppeteer.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.QueryHandlerUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.DEFAULT_BATCH_SIZE;
import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.PUPPETEER_WORLD;
import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public class Frame extends EventEmitter<Frame.FrameEvent> {
    private String url;
    private boolean detached;
    private CDPSession client;
    private final FrameManager frameManager;
    private String loaderId;

    public Set<String> lifecycleEvents() {
        return lifecycleEvents;
    }

    private final Set<String> lifecycleEvents = new HashSet<>();
    private String id;
    private final String parentId;
    private final Accessibility accessibility;
    private final Map<String, IsolatedWorld> worlds = new HashMap<>();
    private boolean hasStartedLoading;
    private String name;
    private ElementHandle document;

    public Map<String, IsolatedWorld> worlds() {
        return worlds;
    }

    public Frame(FrameManager frameManager, String frameId, String parentFrameId, CDPSession client) {
        super();
        this.frameManager = frameManager;
        this.url = "";
        this.id = frameId;
        this.parentId = parentFrameId;
        this.client = client;
        this.detached = false;
        this.loaderId = "";
        this.worlds.put(MAIN_WORLD, new IsolatedWorld(this, null, this.frameManager.timeoutSettings()));
        this.worlds.put(PUPPETEER_WORLD, new IsolatedWorld(this, null, this.frameManager.timeoutSettings()));
        this.accessibility = new Accessibility(this.worlds.get(MAIN_WORLD));
        this.on(FrameEvent.FrameSwappedByActivation, (ignore) -> {
            this.onLoadingStarted();
            this.onLoadingStopped();
        });
        this.worlds.get(MAIN_WORLD).emitter().on(IsolatedWorldEmitter.IsolatedWorldEventType.Consoleapicalled, (event) -> this.onMainWorldConsoleApiCalled((ConsoleAPICalledEvent) event));
        this.worlds.get(MAIN_WORLD).emitter().on(IsolatedWorldEmitter.IsolatedWorldEventType.Bindingcalled, (event) -> this.onMainWorldBindingCalled((BindingCalledEvent) event));
    }

    private void onMainWorldBindingCalled(BindingCalledEvent event) {
        List<Object> args = new ArrayList<>();
        args.add(this.worlds.get(MAIN_WORLD));
        args.add(event);
        this.frameManager.emit(FrameManager.FrameManagerEvent.BindingCalled, args);
    }

    private void onMainWorldConsoleApiCalled(ConsoleAPICalledEvent event) {
        this.frameManager.emit(FrameManager.FrameManagerEvent.ConsoleApiCalled, new Object[]{this.worlds.get(MAIN_WORLD), event});
    }

    public void onLoadingStarted() {
        this.hasStartedLoading = true;
    }

    public CDPSession client() {
        return this.client;
    }

    public void updateId(String id) {
        this.id = id;
    }

    public void updateClient(CDPSession client) {
        this.client = client;
    }

    public Page page() {
        return this.frameManager.page();
    }

    public Response goTo(String url, GoToOptions options, boolean isBlocking) {
        String referrer;
        String refererPolicy;
        List<PuppeteerLifeCycle> waitUntil;
        Integer timeout;
        if (options == null) {
            referrer = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            refererPolicy = this.frameManager.networkManager().extraHTTPHeaders().get("referer_policy");
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.LOAD);
            timeout = this.frameManager().timeoutSettings().navigationTimeout();
        } else {
            if (StringUtil.isEmpty(referrer = options.getReferer())) {
                referrer = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            }
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.LOAD);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
            if (StringUtil.isEmpty(refererPolicy = options.getReferrerPolicy())) {
                refererPolicy = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            }
        }
        if (!isBlocking) {//不等待结果返回，只是发送指令
            Map<String, Object> params = ParamsFactory.create();
            params.put("url", url);
            params.put("frameId", this.id());
            params.put("referrer", referrer);
            params.put("referrerPolicy", refererPolicy);
            this.client.send("Page.navigate", params, null, false);
            return null;
        }
        AtomicBoolean ensureNewDocumentNavigation = new AtomicBoolean(false);
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager.networkManager(), this, waitUntil);
        try {
            this.navigate(this.client, url, referrer, refererPolicy, this.id(), ensureNewDocumentNavigation);
            String timeoutMessage = "Navigation timeout of " + timeout + " ms exceeded";
            Supplier<Boolean> conditionChecker = () -> {
                if (watcher.terminationIsDone()) {
                    throw new TimeoutException(timeoutMessage);
                }
                if (ensureNewDocumentNavigation.get()) {
                    if (watcher.newDocumentNavigationIsDone()) {
                        return true;
                    }
                } else {
                    if (watcher.sameDocumentNavigationIsDone()) {
                        return true;
                    }
                }
                return null;
            };
            Helper.waitForCondition(conditionChecker, timeout, timeoutMessage);
            return watcher.navigationResponse();
        } finally {
            watcher.dispose();
        }
    }

    private void navigate(CDPSession client, String url, String referrer, String referrerPolicy, String frameId, AtomicBoolean ensureNewDocumentNavigation) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("url", url);
        params.put("referrer", referrer);
        params.put("frameId", frameId);
        params.put("referrerPolicy", referrerPolicy);
        JsonNode response = client.send("Page.navigate", params);
        if (response == null) {
            return;
        }
        if (StringUtil.isNotEmpty(response.get("loaderId").asText())) {
            ensureNewDocumentNavigation.set(true);
        }
        String errorText = null;
        if (response.get("errorText") != null && StringUtil.isNotEmpty(errorText = response.get("errorText").asText()) && "net::ERR_HTTP_RESPONSE_CODE_FAILURE".equals(response.get("errorText").asText())) {
            return;
        }
        if (StringUtil.isNotEmpty(errorText)) throw new JvppeteerException(errorText + " at " + url);
    }

    public List<Frame> childFrames() {
        return this.frameManager.frameTree().childFrames(this.id);
    }

    public DeviceRequestPromptManager deviceRequestPromptManager() {
        return this.frameManager.deviceRequestPromptManager(this.client);
    }

    public void dispose() {
        if (this.detached) {
            return;
        }
        this.detached = true;
        this.worlds.get(MAIN_WORLD).dispose();
        this.worlds.get(PUPPETEER_WORLD).dispose();
    }

    public void navigatedWithinDocument(String url) {
        this.url = url;
    }

    public Response waitForNavigation(WaitForOptions options, boolean reload) {
        Integer timeout;
        List<PuppeteerLifeCycle> waitUntil;
        boolean ignoreSameDocumentNavigation;
        if (options == null) {
            ignoreSameDocumentNavigation = false;
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.LOAD);
            timeout = this.frameManager.timeoutSettings().navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.LOAD);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
            ignoreSameDocumentNavigation = options.getIgnoreSameDocumentNavigation();
        }
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager.networkManager(), this, waitUntil);
        // 如果是reload页面，需要在等待之前发送刷新命令
        if (reload) {
            this.client.send("Page.reload", null, null, false);
        }
        try {
            long base = System.currentTimeMillis();
            long now = 0;
            while (true) {
                long delay = timeout - now;
                if (delay <= 0) {
                    throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
                }
                if (watcher.terminationIsDone()) {
                    throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
                }
                if (!ignoreSameDocumentNavigation) {//不忽略sameDocumentNavigation
                    if ((watcher.newDocumentNavigationIsDone() || watcher.sameDocumentNavigationIsDone()) && watcher.navigationResponseIsDone()) {
                        break;
                    }
                } else {
                    if (watcher.newDocumentNavigationIsDone() && watcher.navigationResponseIsDone()) {
                        break;
                    }
                }
                now = System.currentTimeMillis() - base;
            }
            return watcher.navigationResponse();
        } finally {
            watcher.dispose();
        }
    }

    public IsolatedWorld mainRealm() {
        return this.worlds.get(MAIN_WORLD);
    }

    public IsolatedWorld isolatedRealm() {
        return this.worlds.get(PUPPETEER_WORLD);
    }

    public void setContent(String html, WaitForOptions options) throws JsonProcessingException, EvaluateException {
        List<PuppeteerLifeCycle> waitUntil;
        Integer timeout;
        if (options == null) {
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.LOAD);
            timeout = this.frameManager.timeoutSettings().navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.LOAD);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
        }
        this.setFrameContent(html);
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager.networkManager(), this, waitUntil);
        try {
            long base = System.currentTimeMillis();
            long now = 0;
            while (true) {
                long delay = timeout - now;
                if (delay <= 0) {
                    throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
                }
                if (watcher.terminationIsDone()) {
                    throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
                }
                if (watcher.lifecycleIsDone()) {
                    break;
                }
                now = System.currentTimeMillis() - base;
            }
        } finally {
            watcher.dispose();
        }
    }

    public void setFrameContent(String content) throws JsonProcessingException, EvaluateException {
        this.evaluate("(content) => {\n" +
                "      document.open();\n" +
                "      document.write(content);\n" +
                "      document.close();\n" +
                "    }", Collections.singletonList(content));
    }

    public String url() {
        return this.url;
    }

    public Frame parentFrame() {
        return this.frameManager.frameTree().parentFrame(this.id);
    }

    public void navigated(FramePayload framePayload) {
        this.name = framePayload.getName();
        this.url = framePayload.getUrl() + (framePayload.getUrlFragment() == null ? "" : framePayload.getUrlFragment());
    }

    public void onLifecycleEvent(String loaderId, String name) {
        if ("init".equals(name)) {
            this.loaderId = loaderId;
            this.lifecycleEvents.clear();
        }
        this.lifecycleEvents.add(name);
    }

    public void onLoadingStopped() {
        this.lifecycleEvents.add("DOMContentLoaded");
        this.lifecycleEvents.add("load");
    }

    public boolean detached() {
        return this.detached;
    }

    public String id() {
        return id;
    }

    public String parentId() {
        return this.parentId;
    }

    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        return this.deviceRequestPromptManager().waitForDevicePrompt(timeout);
    }

    public void removeExposedFunctionBinding(Binding binding) throws JsonProcessingException, EvaluateException {
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        this.client.send("Runtime.removeBinding", params);
        this.evaluate("name => {\n" +
                "        // Removes the dangling Puppeteer binding wrapper.\n" +
                "        // @ts-expect-error: In a different context.\n" +
                "        globalThis[name] = undefined;\n" +
                "      }", Collections.singletonList(binding.name()));
    }

    public void addExposedFunctionBinding(Binding binding) throws JsonProcessingException, EvaluateException {
        if (this != this.frameManager.mainFrame() && !this.hasStartedLoading) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        this.client.send("Runtime.addBinding", params);
        this.evaluate(binding.initSource(), EvaluateType.STRING, null);
    }

    public void addPreloadScript(PreloadScript preloadScript) {
        if (this.client != this.frameManager.client() && this != this.frameManager.mainFrame()) {
            return;
        }
        if (StringUtil.isNotEmpty(preloadScript.getIdForFrame(this))) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("source", preloadScript.getSource());
        JsonNode response = this.client.send("Page.addScriptToEvaluateOnNewDocument", params);
        preloadScript.setIdForFrame(this, response.get("identifier").asText());
    }

    public ElementHandle document() throws JsonProcessingException, EvaluateException {
        if (this.document == null) {
            this.document = this.mainRealm().evaluateHandle("() => {\n" +
                    "        return document;\n" +
                    "      }", null).asElement();
        }
        return this.document;
    }

    public void clearDocumentHandle() {
        this.document = null;
    }

    public ElementHandle frameElement() throws JsonProcessingException, EvaluateException {
        Frame parentFrame = this.parentFrame();
        if (parentFrame == null) {
            return null;
        }
        JSHandle list = parentFrame.isolatedRealm().evaluateHandle("() => {\n" +
                "      return document.querySelectorAll('iframe,frame');\n" +
                "    }");
        ElementHandle result = null;
        List<JSHandle> lists = this.transposeIterableHandle(list);
        try {
            for (JSHandle iframe : lists) {
                Frame frame = iframe.asElement().contentFrame();
                if (frame != null && frame.id().equals(this.id)) {
                    result = iframe.asElement();
                    lists.remove(iframe);
                    break;
                }
            }

        } finally {
            lists.forEach(JSHandle::dispose);
            Optional.of(list).ifPresent(JSHandle::dispose);
        }
        return result;
    }

    private List<JSHandle> transposeIterableHandle(JSHandle list) throws JsonProcessingException {
        JSHandle generatorHandle = null;
        try {
            generatorHandle = list.evaluateHandle("iterable => {\n" +
                    "    return (async function* () {\n" +
                    "      yield* iterable;\n" +
                    "    })();\n" +
                    "  }");
            return transposeIteratorHandle(generatorHandle);
        } finally {
            Optional.ofNullable(generatorHandle).ifPresent(JSHandle::dispose);
        }
    }

    private List<JSHandle> transposeIteratorHandle(JSHandle iterator) throws JsonProcessingException, EvaluateException {
        int size = DEFAULT_BATCH_SIZE;
        List<JSHandle> results = new ArrayList<>();
        List<JSHandle> result;
        while ((result = fastTransposeIteratorHandle(iterator, size)) != null) {
            results.addAll(result);
            size <<= 1;
        }
        return results;
    }

    private List<JSHandle> fastTransposeIteratorHandle(JSHandle iterator, int size) throws JsonProcessingException, EvaluateException {
        JSHandle array = null;
        Collection<JSHandle> handles;
        try {
            array = iterator.evaluateHandle("async (iterator, size) => {\n" +
                    "    const results = [];\n" +
                    "    while (results.length < size) {\n" +
                    "      const result = await iterator.next();\n" +
                    "      if (result.done) {\n" +
                    "        break;\n" +
                    "      }\n" +
                    "      results.push(result.value);\n" +
                    "    }\n" +
                    "    return results;\n" +
                    "  }", Collections.singletonList(size));
            Map<String, JSHandle> properties = array.getProperties();
            handles = properties.values();
            if (properties.isEmpty()) {
                return null;
            }
            return new ArrayList<>(handles);
        } finally {
            Optional.ofNullable(array).ifPresent(JSHandle::dispose);
        }
    }

    public ElementHandle $(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle document = this.document();
        return document.$(selector);
    }

    public List<ElementHandle> $$(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle document = this.document();
        return document.$$(selector);
    }

    public Object $eval(String selector, String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("$eval", pageFunction);
        ElementHandle document = this.document();
        return document.$eval(selector, pageFunction, args);
    }

    public Object $$eval(String selector, String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("$$eval", pageFunction);
        ElementHandle document = this.document();
        return document.$$eval(selector, pageFunction, args);
    }

    /**
     * 等待直到指定的选择器匹配的元素满足某些条件（可见、隐藏或存在）.
     *
     * @param selector 选择器字符串，用于选择目标元素.
     * @param options  包含等待条件的选项，如元素可见或隐藏.
     * @return 返回匹配选择器的目标元素的句柄.
     * @throws JsonProcessingException 如果在处理JSON时发生错误.
     */
    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws JsonProcessingException {
        boolean waitForVisible = options.getVisible();
        boolean waitForHidden = options.getHidden();
        String polling = waitForVisible || waitForHidden ? "raf" : "mutation";
        options.setPolling(polling);
        QuerySelector queryHandlerAndSelector = QueryHandlerUtil.getQueryHandlerAndSelector(selector, "(element, selector) =>\n" +
                "      element.querySelector(selector)");
        QueryHandler queryHandler = queryHandlerAndSelector.getQueryHandler();
        String updatedSelector = queryHandlerAndSelector.getUpdatedSelector();
        String predicate = "function predicate(selectorOrXPath, isXPath, waitForVisible, waitForHidden) {\n" +
                "            const node = isXPath\n" +
                "                ? document.evaluate(selectorOrXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue\n" +
                "                : predicateQueryHandler\n" +
                "                    ? predicateQueryHandler(document, selectorOrXPath)\n" +
                "                    : document.querySelector(selectorOrXPath);\n" +
                "            if (!node)\n" +
                "                return waitForHidden;\n" +
                "            if (!waitForVisible && !waitForHidden)\n" +
                "                return node;\n" +
                "            const element = node.nodeType === Node.TEXT_NODE\n" +
                "                ? node.parentElement\n" +
                "                : node;\n" +
                "            const style = window.getComputedStyle(element);\n" +
                "            const isVisible = style && style.visibility !== 'hidden' && hasVisibleBoundingBox();\n" +
                "            const success = waitForVisible === isVisible || waitForHidden === !isVisible;\n" +
                "            return success ? node : null;\n" +
                "            function hasVisibleBoundingBox() {\n" +
                "                const rect = element.getBoundingClientRect();\n" +
                "                return !!(rect.top || rect.bottom || rect.width || rect.height);\n" +
                "            }\n" +
                "        }";
        List<Object> args = new ArrayList<>(Arrays.asList(updatedSelector, false, waitForVisible, waitForHidden));
        JSHandle handle = this.isolatedRealm().waitForFunction(predicate, queryHandler.queryOne(), options, args);
        try {
            if (handle == null)
                return null;
            JSHandle result = this.mainRealm().transferHandle(handle);
            return result.asElement();
        } finally {
            Optional.ofNullable(handle).ifPresent(JSHandle::dispose);
        }
    }

    public JSHandle waitForFunction(String pageFunction, WaitForSelectorOptions options, List<Object> args) {
        return this.mainRealm().waitForFunction(pageFunction, null, options, args);
    }

    public String content() throws JsonProcessingException, EvaluateException {
        return (String) this.evaluate("() => {\n" +
                "      let content = '';\n" +
                "      for (const node of document.childNodes) {\n" +
                "        switch (node) {\n" +
                "          case document.documentElement:\n" +
                "            content += document.documentElement.outerHTML;\n" +
                "            break;\n" +
                "          default:\n" +
                "            content += new XMLSerializer().serializeToString(node);\n" +
                "            break;\n" +
                "        }\n" +
                "      }\n" +
                "\n" +
                "      return content;\n" +
                "    }");
    }

    public Object evaluate(String pageFunction, EvaluateType type, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluate", pageFunction);
        return this.mainRealm().evaluate(pageFunction, type, args);
    }

    public Object evaluate(String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluate(pageFunction, null, null);
    }

    public Object evaluate(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        return this.evaluate(pageFunction, null, args);
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pageFunction);
        return this.mainRealm().evaluateHandle(pageFunction, args);
    }

    public String name() {
        return this.name == null ? "" : this.name;
    }

    /**
     * 在当前文档中添加一个脚本标签。
     * <p>
     * 此方法使用提供的选项创建一个脚本标签，可以是通过URL、文件路径或直接通过内容来加载脚本。
     * 它支持异步执行，并处理脚本加载的生命周期事件，如'load'和'error'。
     *
     * @param options 脚本标签的选项，包括URL、路径或内容等。
     * @return 返回新创建的脚本元素的句柄。
     * @throws IOException       当读取脚本文件时发生IO错误。
     * @throws EvaluateException 当脚本执行失败时抛出。
     */
    public ElementHandle addScriptTag(FrameAddScriptTagOptions options) throws IOException, EvaluateException {
        if (options == null) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getUrl()) && StringUtil.isEmpty(options.getPath()) && StringUtil.isEmpty(options.getContent())) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getType())) {
            options.setType("text/javascript");
        }
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            options.setContent(String.join("\n", contents) + "//# sourceURL=" + options.getPath().replaceAll("\n", ""));
        }
        return this.mainRealm().evaluateHandle("async ({url, id, type, content}) => {\n" +
                "    return await new Promise((resolve, reject) => {\n" +
                "      const script = document.createElement('script');\n" +
                "      script.type = type;\n" +
                "      script.text = content;\n" +
                "      script.addEventListener(\n" +
                "        'error',\n" +
                "        event => {\n" +
                "          reject(new Error(event.message ?? 'Could not load script'));\n" +
                "        },\n" +
                "        {once: true}\n" +
                "      );\n" +
                "      if (id) {\n" +
                "        script.id = id;\n" +
                "      }\n" +
                "      if (url) {\n" +
                "        script.src = url;\n" +
                "        script.addEventListener(\n" +
                "          'load',\n" +
                "          () => {\n" +
                "            resolve(script);\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        document.head.appendChild(script);\n" +
                "      } else {\n" +
                "        document.head.appendChild(script);\n" +
                "        resolve(script);\n" +
                "      }\n" +
                "    });\n" +
                "  }", Collections.singletonList(options)).asElement();

    }

    /**
     * 向文档头部添加样式标签
     * <p>
     * 该方法用于在HTML文档的头部内插入一个新的样式标签。它支持通过URL链接到外部样式表，
     * 或者直接包含样式内容。当提供样式文件的路径时，将读取该文件的内容并作为内联样式添加。
     *
     * @param options 样式标签的配置选项，包括url、path或content属性
     * @return 插入的样式元素的句柄
     * @throws IOException       当读取样式文件时可能抛出的IO异常
     * @throws EvaluateException 当在页面上执行JavaScript时发生错误时抛出的异常
     */
    public ElementHandle addStyleTag(FrameAddStyleTagOptions options) throws IOException, EvaluateException {
        if (options == null) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getUrl()) && StringUtil.isEmpty(options.getPath()) && StringUtil.isEmpty(options.getContent())) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        String content;
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            content = String.join("\n", contents) + "/*# sourceURL=" + options.getPath().replaceAll("\n", "") + "*/";
            options.setContent(content);
        }
        return this.mainRealm().transferHandle(this.isolatedRealm().evaluateHandle("async ({url, content}) => {\n" +
                "    return await new Promise(\n" +
                "      (resolve, reject) => {\n" +
                "        let element;\n" +
                "        if (!url) {\n" +
                "          element = document.createElement('style');\n" +
                "          element.appendChild(document.createTextNode(content));\n" +
                "        } else {\n" +
                "          const link = document.createElement('link');\n" +
                "          link.rel = 'stylesheet';\n" +
                "          link.href = url;\n" +
                "          element = link;\n" +
                "        }\n" +
                "        element.addEventListener(\n" +
                "          'load',\n" +
                "          () => {\n" +
                "            resolve(element);\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        element.addEventListener(\n" +
                "          'error',\n" +
                "          event => {\n" +
                "            reject(\n" +
                "              new Error(\n" +
                "                (event ).message ?? 'Could not load style'\n" +
                "              )\n" +
                "            );\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        document.head.appendChild(element);\n" +
                "        return element;\n" +
                "      }\n" +
                "    );\n" +
                "  }", Collections.singletonList(options))).asElement();
    }

    public void click(String selector, ClickOptions options) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        Objects.requireNonNull(handle, "No node found for selector: " + selector);
        handle.click(options);
        handle.dispose();
    }

    public void focus(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.focus();
        handle.dispose();
    }

    public void hover(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.hover();
        handle.dispose();
    }

    public List<String> select(String selector, List<String> values) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        List<String> result = handle.select(values);
        handle.dispose();
        return result;
    }

    public void tap(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.tap();
        handle.dispose();
    }

    public void type(String selector, String text, long delay) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.type(text, delay);
        handle.dispose();
    }

    public String title() throws JsonProcessingException, EvaluateException {
        return (String) this.isolatedRealm().evaluate("() => {\n" +
                "      return document.title;\n" +
                "    }");
    }

    public String loaderId() {
        return loaderId;
    }

    public FrameManager frameManager() {
        return this.frameManager;
    }

    public Accessibility accessibility() {
        return accessibility;
    }

    public void setId(String frameId) {
        this.id = frameId;
    }

    public enum FrameEvent {
        FrameNavigated("Frame.FrameNavigated"),
        FrameSwapped("Frame.FrameSwapped"),
        LifecycleEvent("Frame.LifecycleEvent"),
        FrameNavigatedWithinDocument("Frame.FrameNavigatedWithinDocument"),
        FrameDetached("Frame.FrameDetached"),
        FrameSwappedByActivation("Frame.FrameSwappedByActivation");
        private final String eventType;

        FrameEvent(String eventType) {
            this.eventType = eventType;
        }

        public String getEventType() {
            return eventType;
        }
    }
}
