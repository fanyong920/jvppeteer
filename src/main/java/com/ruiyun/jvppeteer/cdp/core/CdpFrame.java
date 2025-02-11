package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.events.FrameEvents;
import com.ruiyun.jvppeteer.cdp.entities.Binding;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.FramePayload;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.PreloadScript;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.cdp.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.DeviceRequestPromptManager;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.PUPPETEER_WORLD;

public class CdpFrame extends Frame {
    private volatile String url;
    private boolean detached;
    private CDPSession client;
    private final FrameManager frameManager;
    private volatile String loaderId;

    private final Set<String> lifecycleEvents = new HashSet<>();

    private final Map<String, IsolatedWorld> worlds = new HashMap<>();

    public Map<String, IsolatedWorld> worlds() {
        return worlds;
    }

    public CdpFrame(FrameManager frameManager, String frameId, String parentFrameId, CDPSession client) {
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
        this.accessibility = new Accessibility(this.worlds.get(MAIN_WORLD), frameId);
        this.on(FrameEvents.FrameSwappedByActivation, (ignore) -> {
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

    public CdpResponse goTo(String url, GoToOptions options) {
        String referrer;
        String refererPolicy;
        List<PuppeteerLifeCycle> waitUntil;
        Integer timeout;
        if (options == null) {
            referrer = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            refererPolicy = this.frameManager.networkManager().extraHTTPHeaders().get("referer_policy");
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.load);
            timeout = this.frameManager().timeoutSettings().navigationTimeout();
        } else {
            if (StringUtil.isEmpty(referrer = options.getReferer())) {
                referrer = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            }
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.load);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
            if (StringUtil.isEmpty(refererPolicy = options.getReferrerPolicy())) {
                refererPolicy = this.frameManager.networkManager().extraHTTPHeaders().get("referer");
            }
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

    @Override
    public CdpResponse waitForNavigation(WaitForOptions options, Runnable navigateRunner) {
        Integer timeout;
        List<PuppeteerLifeCycle> waitUntil;
        boolean ignoreSameDocumentNavigation;
        if (options == null) {
            ignoreSameDocumentNavigation = false;
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.load);
            timeout = this.frameManager.timeoutSettings().navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.load);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
            ignoreSameDocumentNavigation = options.getIgnoreSameDocumentNavigation();
        }
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager.networkManager(), this, waitUntil);
        // 如果是reload页面，需要在等待之前发送刷新命令
        Optional.ofNullable(navigateRunner).ifPresent(Runnable::run);
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
            waitUntil.add(PuppeteerLifeCycle.load);
            timeout = this.frameManager.timeoutSettings().navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.load);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.frameManager.timeoutSettings().navigationTimeout();
            }
        }
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager.networkManager(), this, waitUntil);
        this.setFrameContent(html);
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

    public String url() {
        return this.url;
    }

    public Frame parentFrame() {
        return this.frameManager.frameTree().parentFrame(this.id);
    }

    public List<CdpFrame> childFrames() {
        return this.frameManager.frameTree().childFrames(this.id);
    }

    public DeviceRequestPromptManager deviceRequestPromptManager() {
        return this.frameManager.deviceRequestPromptManager(this.client);
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

    public void addExposedFunctionBinding(Binding binding) throws JsonProcessingException, EvaluateException {
        if (this != this.frameManager.mainFrame() && !this.hasStartedLoading) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        this.client.send("Runtime.addBinding", params);
        this.evaluate(binding.initSource(), EvaluateType.STRING, null);
    }

    public void removeExposedFunctionBinding(Binding binding) throws JsonProcessingException {
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        this.client.send("Runtime.removeBinding", params);
        this.evaluate("name => {\n" +
                "        // Removes the dangling Puppeteer binding wrapper.\n" +
                "        // @ts-expect-error: In a different context.\n" +
                "        globalThis[name] = undefined;\n" +
                "      }", Collections.singletonList(binding.name()));
    }

    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        return this.deviceRequestPromptManager().waitForDevicePrompt(timeout);
    }

    public void navigated(FramePayload framePayload) {
        this.name = framePayload.getName();
        this.url = framePayload.getUrl() + (framePayload.getUrlFragment() == null ? "" : framePayload.getUrlFragment());
    }

    public void navigatedWithinDocument(String url) {
        this.url = url;
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

    public void onLoadingStarted() {
        this.hasStartedLoading = true;
    }

    public boolean detached() {
        return this.detached;
    }

    public void dispose() {
        if (this.detached) {
            return;
        }
        this.detached = true;
        this.worlds.get(MAIN_WORLD).dispose();
        this.worlds.get(PUPPETEER_WORLD).dispose();
    }

    public ElementHandle frameElement() throws JsonProcessingException, EvaluateException {
        CdpTarget target = (CdpTarget) this.page().target();
        boolean isFirefox = target.targetManager() instanceof FirefoxTargetManager;
        if (isFirefox) {
            return super.frameElement();
        }
        Frame parentFrame = this.parentFrame();
        if (parentFrame == null) {
            return null;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("frameId", this.id);
        JsonNode response = parentFrame.client().send("DOM.getFrameOwner", params);
        return parentFrame.mainRealm().adoptBackendNode(response.get("backendNodeId").asInt()).asElement();
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

    public Set<String> lifecycleEvents() {
        return lifecycleEvents;
    }


    public void setId(String frameId) {
        this.id = frameId;
    }
}
