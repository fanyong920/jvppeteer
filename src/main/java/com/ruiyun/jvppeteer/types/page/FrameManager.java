package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.impl.EventEmitter;
import com.ruiyun.jvppeteer.exception.NavigateException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextCreatedPayload;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDestroyedPayload;
import com.ruiyun.jvppeteer.protocol.page.FrameAttachedPayload;
import com.ruiyun.jvppeteer.protocol.page.FrameDetachedPayload;
import com.ruiyun.jvppeteer.protocol.page.FrameNavigatedPayload;
import com.ruiyun.jvppeteer.protocol.page.FramePayload;
import com.ruiyun.jvppeteer.protocol.page.FrameStoppedLoadingPayload;
import com.ruiyun.jvppeteer.protocol.page.LifecycleEventPayload;
import com.ruiyun.jvppeteer.protocol.page.NavigatedWithinDocumentPayload;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FrameManager extends EventEmitter {

    private static final String UTILITY_WORLD_NAME = "__puppeteer_utility_world__";

    private CDPSession client;

    private Page page;

    private TimeoutSettings timeoutSettings;

    private NetworkManager networkManager;

    private Map<String, Frame> frames;

    private Map<Integer, ExecutionContext> contextIdToContext;

    private Set<String> isolatedWorlds;

    private Frame mainFrame;

    /**
     * 给导航到新的网页用
     */
    private CountDownLatch latch;

    /**
     * 导航到新的网页的结果
     * "success" "timeout" "termination"
     */
    private String navigateResult;

    public FrameManager(CDPSession client, Page page, boolean ignoreHTTPSErrors, TimeoutSettings timeoutSettings) {
        super();
        this.client = client;
        this.page = page;
        this.networkManager = new NetworkManager(client, ignoreHTTPSErrors, this);
        this.timeoutSettings = timeoutSettings;
        this.frames = new HashMap<>();
        this.contextIdToContext = new HashMap<>();
        this.isolatedWorlds = new HashSet<>();
        //1 Page.frameAttached
        DefaultBrowserListener<FrameAttachedPayload> frameAttachedListener = new DefaultBrowserListener<FrameAttachedPayload>() {
            @Override
            public void onBrowserEvent(FrameAttachedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameAttached(event.getFrameId(), event.getParentFrameId());
            }
        };
        frameAttachedListener.setTarget(this);
        frameAttachedListener.setMothod("Page.frameAttached");
        this.client.on(frameAttachedListener.getMothod(), frameAttachedListener);
        //2 Page.frameNavigated
        DefaultBrowserListener<FrameNavigatedPayload> frameNavigatedListener = new DefaultBrowserListener<FrameNavigatedPayload>() {
            @Override
            public void onBrowserEvent(FrameNavigatedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameNavigated(event.getFrame());
            }
        };
        frameNavigatedListener.setTarget(this);
        frameNavigatedListener.setMothod("Page.frameNavigated");
        this.client.on(frameNavigatedListener.getMothod(), frameNavigatedListener);
        //3 Page.navigatedWithinDocument
        DefaultBrowserListener<NavigatedWithinDocumentPayload> navigatedWithinDocumentListener = new DefaultBrowserListener<NavigatedWithinDocumentPayload>() {
            @Override
            public void onBrowserEvent(NavigatedWithinDocumentPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameNavigatedWithinDocument(event.getFrameId(), event.getUrl());
            }
        };
        navigatedWithinDocumentListener.setTarget(this);
        navigatedWithinDocumentListener.setMothod("Page.navigatedWithinDocument");
        this.client.on(navigatedWithinDocumentListener.getMothod(), navigatedWithinDocumentListener);

        //4 Page.frameDetached
        DefaultBrowserListener<FrameDetachedPayload> frameDetachedListener = new DefaultBrowserListener<FrameDetachedPayload>() {
            @Override
            public void onBrowserEvent(FrameDetachedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameDetached(event.getFrameId());
            }
        };
        frameDetachedListener.setTarget(this);
        frameDetachedListener.setMothod("Page.frameDetached");
        this.client.on(frameDetachedListener.getMothod(), frameDetachedListener);

        //5 Page.frameStoppedLoading
        DefaultBrowserListener<FrameStoppedLoadingPayload> frameStoppedLoadingListener = new DefaultBrowserListener<FrameStoppedLoadingPayload>() {
            @Override
            public void onBrowserEvent(FrameStoppedLoadingPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameStoppedLoading(event.getFrameId());
            }
        };
        frameStoppedLoadingListener.setTarget(this);
        frameStoppedLoadingListener.setMothod("Page.frameStoppedLoading");
        this.client.on(frameStoppedLoadingListener.getMothod(), frameStoppedLoadingListener);

        //6 Runtime.executionContextCreated
        DefaultBrowserListener<ExecutionContextCreatedPayload> executionContextCreatedListener = new DefaultBrowserListener<ExecutionContextCreatedPayload>() {
            @Override
            public void onBrowserEvent(ExecutionContextCreatedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextCreated(event.getContext());
            }
        };
        executionContextCreatedListener.setTarget(this);
        executionContextCreatedListener.setMothod("Runtime.executionContextCreated");
        this.client.on(executionContextCreatedListener.getMothod(), executionContextCreatedListener);

        //7 Runtime.executionContextDestroyed
        DefaultBrowserListener<ExecutionContextDestroyedPayload> executionContextDestroyedListener = new DefaultBrowserListener<ExecutionContextDestroyedPayload>() {
            @Override
            public void onBrowserEvent(ExecutionContextDestroyedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextDestroyed(event.getExecutionContextId());
            }
        };
        executionContextDestroyedListener.setTarget(this);
        executionContextDestroyedListener.setMothod("Runtime.executionContextDestroyed");
        this.client.on(executionContextDestroyedListener.getMothod(), executionContextDestroyedListener);

        //8 Runtime.executionContextsCleared
        DefaultBrowserListener<Object> executionContextsClearedListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextsCleared();
            }
        };
        executionContextsClearedListener.setTarget(this);
        executionContextsClearedListener.setMothod("Runtime.executionContextsCleared");
        this.client.on(executionContextsClearedListener.getMothod(), executionContextsClearedListener);

        //9 Page.lifecycleEvent
        DefaultBrowserListener<LifecycleEventPayload> lifecycleEventListener = new DefaultBrowserListener<LifecycleEventPayload>() {
            @Override
            public void onBrowserEvent(LifecycleEventPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onLifecycleEvent(event);
            }
        };
        lifecycleEventListener.setTarget(this);
        lifecycleEventListener.setMothod("Page.lifecycleEvent");
        this.client.on(lifecycleEventListener.getMothod(), lifecycleEventListener);

    }

    private void onLifecycleEvent(LifecycleEventPayload event) {
        Frame frame = this.frames.get(event.getFrameId());
        if (frame == null)
            return;
        frame.onLifecycleEvent(event.getLoaderId(), event.getName());
        this.emit(Events.FRAME_MANAGER_LIFECYCLE_EVENT.getName(), frame);
    }

    private void onExecutionContextsCleared() {
        for (ExecutionContext context : this.contextIdToContext.values()) {
            if (context.getWorld() != null)
                context.getWorld().setContext(null);
        }
        this.contextIdToContext.clear();
    }

    private void onExecutionContextDestroyed(int executionContextId) {
        ExecutionContext context = this.contextIdToContext.get(executionContextId);
        if (context == null)
            return;
        this.contextIdToContext.remove(executionContextId);
        if (context.getWorld() != null)
            context.getWorld().setContext(null);
    }

    public ExecutionContext executionContextById(int contextId) {
        ExecutionContext context = this.contextIdToContext.get(contextId);
        ValidateUtil.assertBoolean(context != null, "INTERNAL ERROR: missing context with id = " + contextId);
        return context;
    }

    private void onExecutionContextCreated(ExecutionContextDescription contextPayload) {
        String frameId = contextPayload.getAuxData() != null ? contextPayload.getAuxData().getFrameId() : null;
        Frame frame = this.frames.get(frameId);
        DOMWorld world = null;
        if (frame != null) {
            if (contextPayload.getAuxData() != null && contextPayload.getAuxData().getIsDefault()) {
                world = frame.getMainWorld();
            } else if (contextPayload.getName().equals(UTILITY_WORLD_NAME) && !frame.getSecondaryWorld().hasContext()) {
                // In case of multiple sessions to the same target, there's a race between
                // connections so we might end up creating multiple isolated worlds.
                // We can use either.
                world = frame.getSecondaryWorld();
            }
        }
        if (contextPayload.getAuxData() != null && "isolated".equals(contextPayload.getAuxData().getType()))
            this.isolatedWorlds.add(contextPayload.getName());
        /*  ${@link ExecutionContext} */
        ExecutionContext context = new ExecutionContext(this.client, contextPayload, world);
        if (world != null)
            world.setContext(context);
        this.contextIdToContext.put(contextPayload.getId(), context);

    }

    /**
     * @param frameId
     */
    private void onFrameStoppedLoading(String frameId) {
        Frame frame = this.frames.get(frameId);
        if (frame == null)
            return;
        frame.onLoadingStopped();
        this.emit(Events.FRAME_MANAGER_LIFECYCLE_EVENT.getName(), frame);
    }

    /**
     * @param frameId
     */
    private void onFrameDetached(String frameId) {
        Frame frame = this.frames.get(frameId);
        if (frame != null)
            this.removeFramesRecursively(frame);
    }

    /**
     * @param frameId
     * @param url
     */
    private void onFrameNavigatedWithinDocument(String frameId, String url) {
        Frame frame = this.frames.get(frameId);
        if (frame == null) {
            return;
        }
        frame.navigatedWithinDocument(url);
        this.emit(Events.FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT.getName(), frame);
        this.emit(Events.FRAME_MANAGER_FRAME_NAVIGATED.getName(), frame);
    }


    public void initialize() throws JsonProcessingException {

        this.client.send("Page.enable", null, false);
        /* @type Protocol.Page.getFrameTreeReturnValue*/
        JsonNode result = this.client.send("Page.getFrameTree", null, true);

        FrameTree frameTree = Constant.OBJECTMAPPER.treeToValue(result.get("frameTree"), FrameTree.class);
        this.handleFrameTree(frameTree);

        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        this.client.send("Page.setLifecycleEventsEnabled", params, false);

        this.client.send("Runtime.enable", null, true);
        this.ensureIsolatedWorld(UTILITY_WORLD_NAME);
        this.networkManager.initialize();

    }

    private void ensureIsolatedWorld(String name) {
        if (this.isolatedWorlds.contains(name))
            return;
        this.isolatedWorlds.add(name);
        Map<String, Object> params = new HashMap<>();
        params.put("source", "//# sourceURL=" + ExecutionContext.EVALUATION_SCRIPT_URL);
        params.put("worldName", name);
        this.client.send("Page.addScriptToEvaluateOnNewDocument", params, true);
        this.frames().parallelStream().map(frame -> {
            Map<String, Object> param = new HashMap<>();
            param.put("frameId", frame.getId());
            param.put("grantUniveralAccess", true);
            param.put("worldName", name);
            JsonNode send = this.client.send("Page.createIsolatedWorld", param, true);
            return send;
        }).count();
    }

    private void handleFrameTree(FrameTree frameTree) {
        if (StringUtil.isNotEmpty(frameTree.getFrame().getParentId())) {
            this.onFrameAttached(frameTree.getFrame().getId(), frameTree.getFrame().getParentId());
        }

        this.onFrameNavigated(frameTree.getFrame());
        if (ValidateUtil.isEmpty(frameTree.getChildFrames()))
            return;

        for (FrameTree child : frameTree.getChildFrames()) {
            this.handleFrameTree(child);
        }
    }

    /**
     * @param {string}  frameId
     * @param {?string} parentFrameId
     */
    private void onFrameAttached(String frameId, String parentFrameId) {
        if (this.frames.get(frameId) != null)
            return;
        ValidateUtil.assertBoolean(StringUtil.isNotEmpty(parentFrameId), "parentFrameId is null");
        Frame parentFrame = this.frames.get(parentFrameId);
        Frame frame = new Frame(this, this.client, parentFrame, frameId);
        this.frames.put(frame.getId(), frame);
        this.emit(Events.FRAME_MANAGER_FRAME_ATTACHED.getName(), frame);
    }

    /**
     * @param {!Protocol.Page.Frame} framePayload
     */
    private void onFrameNavigated(FramePayload framePayload) {
        boolean isMainFrame = StringUtil.isEmpty(framePayload.getParentId());
        Frame frame = isMainFrame ? this.mainFrame : this.frames.get(framePayload.getId());
        ValidateUtil.assertBoolean(isMainFrame || frame != null, "We either navigate top level or have old version of the navigated frame");

        // Detach all child frames first.
        if (frame != null) {
            if (ValidateUtil.isNotEmpty(frame.getChildFrames())) {
                for (Frame childFrame : frame.getChildFrames()) {
                    this.removeFramesRecursively(childFrame);
                }
            }
        }

        // Update or create main frame.
        if (isMainFrame) {
            if (frame != null) {
                // Update frame id to retain frame identity on cross-process navigation.
                this.frames.remove(frame.getId());
                frame.setId(framePayload.getId());
            } else {
                // Initial main frame navigation.
                frame = new Frame(this, this.client, null, framePayload.getId());
            }
            this.frames.put(framePayload.getId(), frame);
            this.mainFrame = frame;
        }

        // Update frame payload.
        frame.navigated(framePayload);

        this.emit(Events.FRAME_MANAGER_FRAME_NAVIGATED.getName(), frame);
    }

    public List<Frame> frames() {
        if (this.frames.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.frames.values());
    }

    /**
     * @param childFrame
     */
    private void removeFramesRecursively(Frame childFrame) {
        if (ValidateUtil.isNotEmpty(childFrame.getChildFrames())) {
            for (Frame frame : childFrame.getChildFrames()) {
                this.removeFramesRecursively(frame);
            }
        }
        childFrame.detach();
        this.frames.remove(childFrame.getId());
        this.emit(Events.FRAME_MANAGER_FRAME_DETACHED.getName(), childFrame);
    }

    public CDPSession getClient() {
        return client;
    }

    public Page getPage() {
        return page;
    }

    public TimeoutSettings getTimeoutSettings() {
        return timeoutSettings;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public Map<String, Frame> getFrames() {
        return frames;
    }

    public Map<Integer, ExecutionContext> getContextIdToContext() {
        return contextIdToContext;
    }

    public Set<String> getIsolatedWorlds() {
        return isolatedWorlds;
    }

    public Frame getMainFrame() {
        return mainFrame;
    }


    public Response navigateFrame(Frame frame, String url, PageNavigateOptions options) {
        String referer;
        List<String> waitUntil;
        int timeout;
        if (options == null) {
            referer = this.networkManager.extraHTTPHeaders().get("referer");
            waitUntil = new ArrayList<>();
            waitUntil.add("load");
            timeout = this.timeoutSettings.navigationTimeout();
        } else {
            if (StringUtil.isEmpty(referer = options.getReferer())) {
                referer = this.networkManager.extraHTTPHeaders().get("referer");
            }
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add("load");
            }
            if ((timeout = options.getTimeout()) <= 0) {
                timeout = this.timeoutSettings.navigationTimeout();
            }
            assertNoLegacyNavigationOptions(options);
        }

        this.latch = new CountDownLatch(1);
        LifecycleWatcher watcher = new LifecycleWatcher(this, frame, waitUntil, timeout);
        navigate(this.client, url, referer, frame.getId(), timeout);
        if ("success".equals(navigateResult)) {
            watcher.dispose();
            return watcher.navigationResponse();
        } else if ("timeout".equals(navigateResult)) {
            throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
        } else if ("termination".equals(navigateResult)) {
            throw new NavigateException("Navigating frame was detached");
        } else {
            throw new NavigateException("UnNokwn result " + navigateResult);
        }
    }

    private boolean navigate(CDPSession client, String url, String referer, String frameId, int timeout) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("referer", referer);
        params.put("frameId", frameId);
        try {
            JsonNode response = client.send("Page.navigate", params, true, latch, timeout);
            if (response != null && response.get("loaderId") != null) {
                this.setNavigateResult("success");
                return true;
            }
            if (response.get("errorText") != null) {
                throw new NavigateException(response.get("errorText").toString());
            }
        } catch (TimeoutException e) {
            this.setNavigateResult("timeout");
        }
        return false;
    }


    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public String getNavigateResult() {
        return navigateResult;
    }

    public void setNavigateResult(String navigateResult) {
        this.navigateResult = navigateResult;
    }

    public Frame getFrame(String frameId) {
        return this.frames.get(frameId);
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Frame frame(String frameId) {
        return this.frames.get(frameId);
    }

    public Response waitForFrameNavigation(Frame frame, PageNavigateOptions options) {
        String referer;
        List<String> waitUntil;
        int timeout;
        if (options == null) {
            referer = this.networkManager.extraHTTPHeaders().get("referer");
            waitUntil = new ArrayList<>();
            waitUntil.add("load");
            timeout = this.timeoutSettings.navigationTimeout();
        } else {
            if (StringUtil.isEmpty(referer = options.getReferer())) {
                referer = this.networkManager.extraHTTPHeaders().get("referer");
            }
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add("load");
            }
            if ((timeout = options.getTimeout()) <= 0) {
                timeout = this.timeoutSettings.navigationTimeout();
            }
            assertNoLegacyNavigationOptions(options);
        }

        this.latch = new CountDownLatch(1);
        LifecycleWatcher watcher = new LifecycleWatcher(this, frame, waitUntil, timeout);
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new NavigateException("UnNokwn result " + e.getMessage());
        }
        if ("success".equals(navigateResult)) {
            watcher.dispose();
            return watcher.navigationResponse();
        } else if ("timeout".equals(navigateResult)) {
            throw new TimeoutException("Navigation timeout of " + timeout + " ms exceeded");
        } else if ("termination".equals(navigateResult)) {
            throw new NavigateException("Navigating frame was detached");
        } else {
            throw new NavigateException("UnNokwn result " + navigateResult);
        }

    }

    private void assertNoLegacyNavigationOptions(PageNavigateOptions options){
        ValidateUtil.assertBoolean(!"networkidle".equals(options.getWaitUntil()),"ERROR: \"networkidle\" option is no longer supported. Use \"networkidle2\" instead");
    }

    public Frame mainFrame() {
        return  this.mainFrame;
    }

    public NetworkManager networkManager() {
        return this.networkManager;
    }
}
