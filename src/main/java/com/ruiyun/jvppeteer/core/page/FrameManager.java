package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.FrameAttachedEvent;
import com.ruiyun.jvppeteer.events.FrameDetachedEvent;
import com.ruiyun.jvppeteer.events.FrameNavigatedEvent;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.options.GoToOptions;
import com.ruiyun.jvppeteer.options.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.options.WaitForOptions;
import com.ruiyun.jvppeteer.protocol.page.FramePayload;
import com.ruiyun.jvppeteer.protocol.page.FrameStoppedLoadingEvent;
import com.ruiyun.jvppeteer.protocol.page.LifecycleEvent;
import com.ruiyun.jvppeteer.protocol.page.NavigatedWithinDocumentEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FrameManager extends EventEmitter<FrameManager.FrameManagerEvent> {

    private static final String UTILITY_WORLD_NAME = "__puppeteer_utility_world__";

    private CDPSession client;

    private Page page;

    private final TimeoutSettings timeoutSettings;

    private final NetworkManager networkManager;

    private final Map<String, Frame> frames;

    private final Map<Integer, ExecutionContext> contextIdToContext;

    private final Set<String> isolatedWorlds;

    private Frame mainFrame;


    public FrameManager(CDPSession client, Page page , TimeoutSettings timeoutSettings) {
        super();
        this.client = client;
        this.page = page;
        this.networkManager = new NetworkManager(client, this);
        this.timeoutSettings = timeoutSettings;
        this.frames = new HashMap<>();
        this.contextIdToContext = new HashMap<>();
        this.isolatedWorlds = new HashSet<>();
        this.client.on(CDPSession.CDPSessionEvent.Page_frameAttached, (Consumer<FrameAttachedEvent>) event -> this.onFrameAttached(event.getFrameId(),event.getParentFrameId()));
        this.client.on(CDPSession.CDPSessionEvent.Page_frameNavigated, (Consumer<FrameNavigatedEvent>) event -> this.onFrameNavigated(event.getFrame(),event.getType()));
        this.client.on(CDPSession.CDPSessionEvent.Page_navigatedWithinDocument, (Consumer<NavigatedWithinDocumentEvent>) event -> this.onFrameNavigatedWithinDocument(event.getFrameId(),event.getUrl()));
        this.client.on(CDPSession.CDPSessionEvent.Page_frameDetached, (Consumer<FrameDetachedEvent>) event -> this.onFrameDetached(event.getFrameId(),event.getReason()));
        this.client.on(CDPSession.CDPSessionEvent.Page_frameStoppedLoading, (Consumer<FrameStoppedLoadingEvent>) event -> this.onFrameStoppedLoading(event.getFrameId()));
        this.client.on(CDPSession.CDPSessionEvent.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) event -> this.onExecutionContextCreated(event.getContext()));
        this.client.on(CDPSession.CDPSessionEvent.Runtime_executionContextDestroyed, (Consumer<ExecutionContextDestroyedEvent>) event -> this.onExecutionContextDestroyed(event.getExecutionContextId()));
        this.client.on(CDPSession.CDPSessionEvent.Runtime_executionContextsCleared, ignore -> this.onExecutionContextsCleared());
        this.client.on(CDPSession.CDPSessionEvent.Page_lifecycleEvent, (Consumer<LifecycleEvent>) this::onLifecycleEvent);
    }

    private void onLifecycleEvent(LifecycleEvent event) {
        Frame frame = this.frames.get(event.getFrameId());
        if (frame == null)
            return;
        frame.
                onLifecycleEvent(event.getLoaderId(), event.getName());
        this.emit(FrameManagerEvent.LifecycleEvent, frame);
        frame.emit(Frame.FrameEvent.LifecycleEvent,null);
    }

    private void onExecutionContextsCleared() {
        for (ExecutionContext context : this.contextIdToContext.values()) {
            if (context.getWorld() != null) {
                context.getWorld().setContext(null);
            }
        }
        this.contextIdToContext.clear();
    }

    private void onExecutionContextDestroyed(int executionContextId) {
        ExecutionContext context = this.contextIdToContext.get(executionContextId);
        if (context == null)
            return;
        this.contextIdToContext.remove(executionContextId);
        if (context.getWorld() != null) {
            context.getWorld().setContext(null);
        }
    }

    public ExecutionContext executionContextById(int contextId) {
        ExecutionContext context = this.contextIdToContext.get(contextId);
        ValidateUtil.assertArg(context != null, "INTERNAL ERROR: missing context with id = " + contextId);
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
     * @param frameId frame id
     */
    private void onFrameStoppedLoading(String frameId) {
        Frame frame = this.frames.get(frameId);
        if (frame == null)
            return;
        frame.onLoadingStopped();
        this.emit(FrameManagerEvent.LifecycleEvent, frame);
        frame.emit(Frame.FrameEvent.LifecycleEvent, null);
    }

    /**
     * @param frameId frame id
     */
    private void onFrameDetached(String frameId,String reason) {
        Frame frame = this.frames.get(frameId);
        if(frame == null)return;
        switch (reason) {
            case "remove":
                // Only remove the frame if the reason for the detached event is
                // an actual removement of the frame.
                // For frames that become OOP iframes, the reason would be 'swap'.
                this.removeFramesRecursively(frame);
                break;
            case "swap":
                this.emit(FrameManagerEvent.FrameSwapped, frame);
                frame.emit(Frame.FrameEvent.FrameSwapped, null);
                break;
        }
    }

    /**
     * @param frameId frame id
     * @param url url
     */
    private void onFrameNavigatedWithinDocument(String frameId, String url) {
        Frame frame = this.frames.get(frameId);
        if (frame == null) {
            return;
        }
        frame.navigatedWithinDocument(url);
        this.emit(FrameManagerEvent.FrameNavigatedWithinDocument, frame);
        frame.emit(Frame.FrameEvent.FrameNavigatedWithinDocument, null);
        this.emit(FrameManagerEvent.FrameNavigated, frame);
        frame.emit(Frame.FrameEvent.FrameNavigated, "Navigation");
    }


    public void initialize() {
        this.client.send("Page.enable");
        /* @type Protocol.Page.getFrameTreeReturnValue*/
        JsonNode result = this.client.send("Page.getFrameTree");

        FrameTree frameTree;
        try {
            frameTree = Constant.OBJECTMAPPER.treeToValue(result.get("frameTree"), FrameTree.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.handleFrameTree(frameTree);

        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        this.client.send("Page.setLifecycleEventsEnabled", params);
        this.client.send("Runtime.enable");
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
        this.client.send("Page.addScriptToEvaluateOnNewDocument", params);
        this.frames().forEach(frame -> {
            Map<String, Object> param = new HashMap<>();
            param.put("frameId", frame.getId());
            param.put("grantUniveralAccess", true);
            param.put("worldName", name);
            this.client.send("Page.createIsolatedWorld", param);
        });
    }

    private void handleFrameTree(FrameTree frameTree) {
        if (StringUtil.isNotEmpty(frameTree.getFrame().getParentId())) {
            this.onFrameAttached(frameTree.getFrame().getId(), frameTree.getFrame().getParentId());
        }
        this.onFrameNavigated(frameTree.getFrame(),"Navigation");
        if (ValidateUtil.isEmpty(frameTree.getChildFrames()))
            return;
        for (FrameTree child : frameTree.getChildFrames()) {
            this.handleFrameTree(child);
        }
    }

    /**
     * @param  frameId frame id
     * @param  parentFrameId parent frame id
     */
    private void onFrameAttached(String frameId, String parentFrameId) {
        if (this.frames.get(frameId) != null)
            return;
        ValidateUtil.assertArg(StringUtil.isNotEmpty(parentFrameId), "parentFrameId is null");
        Frame parentFrame = this.frames.get(parentFrameId);
        Frame frame = new Frame(this, this.client, parentFrame, frameId);
        this.frames.put(frame.getId(), frame);
        this.emit(FrameManagerEvent.FrameAttached, frame);
    }

    /**
     * @param framePayload frame荷载
     */
    private void onFrameNavigated(FramePayload framePayload,String navigationType) {
        boolean isMainFrame = StringUtil.isEmpty(framePayload.getParentId());
        Frame frame = isMainFrame ? this.mainFrame : this.frames.get(framePayload.getId());
        ValidateUtil.assertArg(isMainFrame || frame != null, "We either navigate top level or have old version of the navigated frame");

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
        this.emit(FrameManagerEvent.FrameNavigated, frame);
        frame.emit(Frame.FrameEvent.FrameNavigated, navigationType);
    }

    public List<Frame> frames() {
        if (this.frames.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.frames.values());
    }

    /**
     * @param childFrame 子frame
     */
    private void removeFramesRecursively(Frame childFrame) {
        if (ValidateUtil.isNotEmpty(childFrame.getChildFrames())) {
            for (Frame frame : childFrame.getChildFrames()) {
                this.removeFramesRecursively(frame);
            }
        }
        childFrame.detach();
        this.frames.remove(childFrame.getId());
        this.emit(FrameManagerEvent.FrameDetached, childFrame);
        childFrame.emit(Frame.FrameEvent.FrameDetached, childFrame);
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


    public Response navigateFrame(Frame frame, String url, GoToOptions options, boolean isBlock) {
        String referrer;
        String refererPolicy;
        List<PuppeteerLifeCycle> waitUntil;
        Integer timeout;
        if (options == null) {
            referrer = frame.getFrameManager().getNetworkManager().extraHTTPHeaders().get("referer");
            refererPolicy = frame.getFrameManager().getNetworkManager().extraHTTPHeaders().get("referer_policy");
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.LOAD);
            timeout = frame.getFrameManager().getTimeoutSettings().navigationTimeout();
        } else {
            if (StringUtil.isEmpty(referrer = options.getReferer())) {
                referrer = frame.getFrameManager().getNetworkManager().extraHTTPHeaders().get("referer");
            }
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.LOAD);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = frame.getFrameManager().getTimeoutSettings().navigationTimeout();
            }
            if (StringUtil.isEmpty(refererPolicy = options.getReferrerPolicy())) {
                refererPolicy = frame.getFrameManager().getNetworkManager().extraHTTPHeaders().get("referer");
            }
        }
        if(!isBlock){
            Map<String, Object> params = new HashMap<>();
            params.put("url", url);
            // jackJson 不序列化null值对 HashMap里面的 null值不起作用
            if(referrer != null){
                params.put("referrer", referrer);
            }
            params.put("frameId", frame.getId());
            this.client.send("Page.navigate", params, null,false);
            return null;
        }
        AtomicBoolean ensureNewDocumentNavigation = new AtomicBoolean(false);
        LifecycleWatcher watcher = new LifecycleWatcher(frame.getFrameManager().getNetworkManager(),frame, waitUntil, timeout);
        try {
            String finalReferrer = referrer;
            String finalRefererPolicy = refererPolicy;
            CompletableFuture<Void> navigateFuture = CompletableFuture.runAsync(() -> {
                navigate(this.client, url, finalReferrer, finalRefererPolicy, frame.getId(),ensureNewDocumentNavigation);
            });
            CompletableFuture<Void> terminationFuture = CompletableFuture.runAsync(watcher::waitForTermination);
            CompletableFuture<Object> anyOfFuture1 = CompletableFuture.anyOf(navigateFuture, terminationFuture);
            anyOfFuture1.whenComplete((ignore, throwable1) -> {
                if (throwable1 == null) {//没有出错就是LifecycleWatcher没有接收到termination事件,那就看看是newDocumentNavigation还是sameDocumentNavigation,并等待它完成
                    CompletableFuture<Void> documentNavigationFuture = CompletableFuture.runAsync(() -> {
                        if (ensureNewDocumentNavigation.get()) {
                            watcher.waitForNewDocumentNavigation();
                        } else {
                            watcher.waitForSameDocumentNavigation();
                        }
                    });
                    CompletableFuture<Object> anyOfFuture2 = CompletableFuture.anyOf(terminationFuture,documentNavigationFuture);
                    anyOfFuture2.whenComplete((ignore1, throwable2) -> {
                        if (throwable2 != null) {
                            throw new JvppeteerException(throwable2);
                        }
                    });
                    anyOfFuture2.join();
                }
            });
            //等待页面导航事件或者是页面termination事件完成
            anyOfFuture1.join();
            return watcher.navigationResponse();
        } finally {
            watcher.dispose();
        }
    }

    private void navigate(CDPSession client, String url, String referrer, String referrerPolicy, String frameId,AtomicBoolean ensureNewDocumentNavigation) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("referrer", referrer);
        params.put("frameId", frameId);
        params.put("referrerPolicy", referrerPolicy);
        JsonNode response = client.send("Page.navigate", params);
        if (response == null) {
            return ;
        }
        if (StringUtil.isNotEmpty(response.get("loaderId").asText())) {
            ensureNewDocumentNavigation.set(true);
        }
        String errorText = null;
        if (response.get("errorText") != null && StringUtil.isNotEmpty(errorText = response.get("errorText").asText()) && "net::ERR_HTTP_RESPONSE_CODE_FAILURE".equals(response.get("errorText").asText())) {
            return ;
        }
        if(StringUtil.isNotEmpty(errorText) ) throw new JvppeteerException(errorText + " at " + url) ;
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

    public Response waitForFrameNavigation(Frame frame, WaitForOptions options, boolean reload) {
        Integer timeout;
        List<PuppeteerLifeCycle> waitUntil;
        boolean ignoreSameDocumentNavigation;
        if (options == null) {
            ignoreSameDocumentNavigation = false;
            waitUntil = new ArrayList<>();
            waitUntil.add(PuppeteerLifeCycle.LOAD);
            timeout = this.getTimeoutSettings().navigationTimeout();
        } else {
            if (ValidateUtil.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add(PuppeteerLifeCycle.LOAD);
            }
            if ((timeout = options.getTimeout()) == null) {
                timeout = this.timeoutSettings.navigationTimeout();
            }
            ignoreSameDocumentNavigation = options.getIgnoreSameDocumentNavigation();
        }
        LifecycleWatcher watcher = new LifecycleWatcher(frame.getFrameManager().getNetworkManager(),frame, waitUntil, timeout);
        AtomicReference<Response> result = new AtomicReference<>();
        try {
            CompletableFuture<Void> terminationFuture = CompletableFuture.runAsync(() -> {
                // 如果是reload页面，需要在等待之前发送刷新命令
                if(reload){
                    this. client.send("Page.reload", null, null,false);
                }
                watcher.waitForTermination();});
            CompletableFuture<Void> sameDocumentNavigationFuture = null;
            if(!ignoreSameDocumentNavigation){
                sameDocumentNavigationFuture = CompletableFuture.runAsync(() -> {
                    watcher.waitForSameDocumentNavigation();
                });
            }
            CompletableFuture<Void> newDocumentNavigationFuture = CompletableFuture.runAsync(() -> {
                watcher.waitForNewDocumentNavigation();
            });
            CompletableFuture<Object> anyOfFutrue1 = sameDocumentNavigationFuture == null ? CompletableFuture.anyOf(terminationFuture, newDocumentNavigationFuture) : CompletableFuture.anyOf(terminationFuture, newDocumentNavigationFuture,sameDocumentNavigationFuture);
            anyOfFutrue1.whenComplete(
                    (ignore, throwable) -> {
                        if (throwable != null){
                            return;
                        }
                        CompletableFuture<Response> responseFuture = CompletableFuture.supplyAsync(watcher::navigationResponse);
                        CompletableFuture<Object> anyOfFuture2 = CompletableFuture.anyOf(terminationFuture, responseFuture);
                        anyOfFuture2.whenComplete((ignore1, throwable1) -> {
                            result.set((Response) ignore1);
                        });
                        anyOfFuture2.join();
                    }
            );
            anyOfFutrue1.join();
            return result.get();
        } finally {
            watcher.dispose();
        }
    }

    private void assertNoLegacyNavigationOptions(List<PuppeteerLifeCycle> waitUtil) {
        ValidateUtil.assertArg(!PuppeteerLifeCycle.NETWORKIDLE.equals(waitUtil.get(0)), "ERROR: \"networkidle\" option is no longer supported. Use \"networkidle2\" instead");
    }

    public Frame mainFrame() {
        return this.mainFrame;
    }

    public NetworkManager networkManager() {
        return this.networkManager;
    }


    public enum FrameManagerEvent{
        FrameAttached ("FrameManager.FrameAttached"),
        FrameNavigated( "FrameManager.FrameNavigated"),
        FrameDetached( "FrameManager.FrameDetached"),
        FrameSwapped( "FrameManager.FrameSwapped"),
        LifecycleEvent("FrameManager.LifecycleEvent"),
        FrameNavigatedWithinDocument("FrameManager.FrameNavigatedWithinDocument"),
        ConsoleApiCalled("FrameManager.ConsoleApiCalled"),
        BindingCalled("FrameManager.BindingCalled");
        private String eventName;
        FrameManagerEvent(String eventName) {
            this.eventName = eventName;
        }
        public String getEventName() {
            return eventName;
        }
        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }
}
