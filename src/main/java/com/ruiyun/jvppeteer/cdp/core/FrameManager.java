package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.api.events.FrameEvents;
import com.ruiyun.jvppeteer.cdp.entities.Binding;
import com.ruiyun.jvppeteer.cdp.entities.ExecutionContextDescription;
import com.ruiyun.jvppeteer.cdp.entities.FramePayload;
import com.ruiyun.jvppeteer.cdp.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.cdp.entities.PreloadScript;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameAttachedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameDetachedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameNavigatedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameStartedLoadingEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameStoppedLoadingEvent;
import com.ruiyun.jvppeteer.cdp.events.LifecycleEvent;
import com.ruiyun.jvppeteer.cdp.events.NavigatedWithinDocumentEvent;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DeviceRequestPromptManager;
import com.ruiyun.jvppeteer.common.FrameProvider;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.INTERNAL_URL;
import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.PUPPETEER_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.TIME_FOR_WAITING_FOR_SWAP;
import static com.ruiyun.jvppeteer.common.Constant.UTILITY_WORLD_NAME;
import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class FrameManager extends EventEmitter<FrameManager.FrameManagerEvent> implements FrameProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameManager.class);

    private final CdpPage page;
    private final NetworkManager networkManager;
    private final TimeoutSettings timeoutSettings;
    private final Set<String> isolatedWorlds = new HashSet<>();
    private volatile CDPSession client;
    private final Map<String, PreloadScript> scriptsToEvaluateOnNewDocument = new HashMap<>();
    private final Set<Binding> bindings = new HashSet<>();
    private final FrameTree<CdpFrame> frameTree = new FrameTree<>();
    private final Set<String> frameNavigatedReceived = new HashSet<>();
    private final Map<CDPSession, DeviceRequestPromptManager> deviceRequestPromptManagerMap = new WeakHashMap<>();
    private volatile AwaitableResult<Boolean> frameTreeHandled;


    public FrameManager(CDPSession client, CdpPage page, TimeoutSettings timeoutSettings) {
        super();
        this.client = client;
        this.page = page;
        this.networkManager = new NetworkManager(this);
        this.timeoutSettings = timeoutSettings;
        setupEventListeners(this.client);
        client.once(ConnectionEvents.CDPSession_Disconnected, (ignored) -> {
            try {
                this.onClientDisconnect();
            } catch (Exception e) {
                LOGGER.error("onClientDisconnect error", e);
            }
        });
    }

    public CdpPage page() {
        return this.page;
    }

    private void onClientDisconnect() {
        CdpFrame mainFrame = this.frameTree.getMainFrame();
        if (mainFrame == null) {
            return;
        }
        if(!this.page().browser().connected()){
            // If the browser is not connected we know
            // that activation will not happen
            this.removeFramesRecursively(mainFrame);
            return;
        }
        mainFrame.childFrames().forEach(this::removeFramesRecursively);
        AwaitableResult<Boolean> swappedSubject = AwaitableResult.create();
        Consumer<Object> onSwapped = (ignored) -> swappedSubject.onSuccess(true);
        try {
            mainFrame.once(FrameEvents.FrameSwappedByActivation, onSwapped);
            swappedSubject.waiting(TIME_FOR_WAITING_FOR_SWAP, TimeUnit.MILLISECONDS);
        } catch (Exception err) {
            this.removeFramesRecursively(mainFrame);
        }
    }

    public void swapFrameTree(CDPSession client) {
        this.client = client;
        CdpFrame frame = this.frameTree.getMainFrame();
        if (frame != null) {
            this.frameNavigatedReceived.add(((CdpCDPSession) this.client).getTarget().getTargetId());
            this.frameTree.removeFrame(frame);
            frame.updateId(((CdpCDPSession) this.client).getTarget().getTargetId());
            this.frameTree.addFrame(frame);
            frame.updateClient(client);
        }
        this.setupEventListeners(client);
        client.once(ConnectionEvents.CDPSession_Disconnected, (ignored) -> {
            try {
                this.onClientDisconnect();
            } catch (Exception e) {
                LOGGER.error("onClientDisconnect error", e);
            }
        });
        this.initialize(client, frame);
        this.networkManager.addClient(client);
        if (frame != null) {
            frame.emit(FrameEvents.FrameSwappedByActivation, true);
        }
    }

    public void registerSpeculativeSession(CdpCDPSession client) {
        this.networkManager.addClient(client);
    }

    private void setupEventListeners(CDPSession session) {
        session.on(ConnectionEvents.Page_frameAttached, (Consumer<FrameAttachedEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameAttached(session, event.getFrameId(), event.getParentFrameId());
        });
        session.on(ConnectionEvents.Page_frameNavigated, (Consumer<FrameNavigatedEvent>) event -> {
            this.frameNavigatedReceived.add(event.getFrame().getId());
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameNavigated(event.getFrame(), event.getType());
        });
        session.on(ConnectionEvents.Page_navigatedWithinDocument, (Consumer<NavigatedWithinDocumentEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameNavigatedWithinDocument(event.getFrameId(), event.getUrl());
        });
        session.on(ConnectionEvents.Page_frameDetached, (Consumer<FrameDetachedEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameDetached(event.getFrameId(), event.getReason());
        });
        session.on(ConnectionEvents.Page_frameStartedLoading, (Consumer<FrameStartedLoadingEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameStartedLoading(event.getFrameId());
        });
        session.on(ConnectionEvents.Page_frameStoppedLoading, (Consumer<FrameStoppedLoadingEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onFrameStoppedLoading(event.getFrameId());
        });
        session.on(ConnectionEvents.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onExecutionContextCreated(event.getContext(), session);
        });

        session.on(ConnectionEvents.Page_lifecycleEvent, (Consumer<LifecycleEvent>) event -> {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(AwaitableResult::waitingGetResult);
            this.onLifecycleEvent(event);
        });

    }

    public CDPSession client() {
        return client;
    }

    public TimeoutSettings timeoutSettings() {
        return timeoutSettings;
    }

    public NetworkManager networkManager() {
        return this.networkManager;
    }

    public void initialize(CDPSession client, CdpFrame frame) {
        try {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(handle -> handle.onSuccess(true));
            this.frameTreeHandled = AwaitableResult.create();
            this.networkManager.addClient(client);
            client.send("Page.enable", null, null, false);
            /* @type Protocol.Page.getFrameTreeReturnValue*/
            JsonNode result = client.send("Page.getFrameTree");
            FrameTreeEvent frameTree = Constant.OBJECTMAPPER.treeToValue(result.get("frameTree"), FrameTreeEvent.class);
            this.handleFrameTree(client, frameTree);
            Optional.ofNullable(this.frameTreeHandled).ifPresent(handle -> handle.onSuccess(true));
            Map<String, Object> params = ParamsFactory.create();
            params.put("enabled", true);
            client.send("Page.setLifecycleEventsEnabled", params, null, false);
            client.send("Runtime.enable");
            this.createIsolatedWorld(client, UTILITY_WORLD_NAME);
            if (frame != null) {
                this.scriptsToEvaluateOnNewDocument.values().forEach(frame::addPreloadScript);
                for (Binding binding : this.bindings) {
                    frame.addExposedFunctionBinding(binding);
                }
            }
        } catch (Exception e) {
            Optional.ofNullable(this.frameTreeHandled).ifPresent(handle -> handle.onSuccess(true));
            if (e instanceof TargetCloseException) return;
            throwError(e);
        }

    }

    public CdpFrame mainFrame() {
        CdpFrame mainFrame = this.frameTree.getMainFrame();
        Objects.requireNonNull(mainFrame, "Requesting main frame too early!");
        return mainFrame;
    }

    public List<CdpFrame> frames() {
        return new ArrayList<>(this.frameTree.frames());
    }

    @Override
    public CdpFrame frame(String frameId) {
        return this.frameTree.getById(frameId);
    }

    public void addExposedFunctionBinding(Binding binding) throws JsonProcessingException, EvaluateException {
        this.bindings.add(binding);
        for (CdpFrame frame : this.frames()) {
            frame.addExposedFunctionBinding(binding);
        }
    }

    public void removeExposedFunctionBinding(Binding binding) throws JsonProcessingException {
        this.bindings.remove(binding);
        for (CdpFrame frame : this.frames()) {
            frame.removeExposedFunctionBinding(binding);
        }
    }

    public NewDocumentScriptEvaluation evaluateOnNewDocument(String source) {
        JsonNode response = this.mainFrame().client().send("Page.addScriptToEvaluateOnNewDocument", new HashMap<String, Object>() {
            {
                put("source", source);
            }
        });
        String identifier = response.get("identifier").asText();
        PreloadScript preloadScript = new PreloadScript(this.mainFrame(), identifier, source);
        this.scriptsToEvaluateOnNewDocument.put(identifier, preloadScript);
        for (CdpFrame frame : this.frames()) {
            frame.addPreloadScript(preloadScript);
        }
        return new NewDocumentScriptEvaluation(identifier);
    }

    public void removeScriptToEvaluateOnNewDocument(String identifier) {
        PreloadScript preloadScript = this.scriptsToEvaluateOnNewDocument.get(identifier);
        if (preloadScript == null) {
            throw new JvppeteerException("Script to evaluate on new document with id " + identifier + " not found");
        }
        this.scriptsToEvaluateOnNewDocument.remove(identifier);
        for (CdpFrame frame : this.frames()) {
            String identifier2 = preloadScript.getIdForFrame(frame);
            if (StringUtil.isEmpty(identifier2)) {
                return;
            }
            try {
                frame.client().send("Page.removeScriptToEvaluateOnNewDocument", new HashMap<String, Object>() {
                    {
                        put("identifier", identifier2);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Page.removeScriptToEvaluateOnNewDocument error", e);
            }
        }
    }

    public void onAttachedToTarget(CdpTarget target) {
        if (!"iframe".equals(target.getTargetInfo().getType())) {
            return;
        }
        CdpFrame frame = this.frame(target.getTargetInfo().getTargetId());
        if (frame != null) {
            frame.updateClient(target.session());
        }
        this.setupEventListeners(target.session());
        this.initialize(target.session(), frame);
    }

    public DeviceRequestPromptManager deviceRequestPromptManager(CDPSession client) {
        DeviceRequestPromptManager manager = this.deviceRequestPromptManagerMap.get(client);
        if (manager == null) {
            manager = new DeviceRequestPromptManager(client, this.timeoutSettings);
            this.deviceRequestPromptManagerMap.put(client, manager);
        }
        return manager;
    }

    private void onLifecycleEvent(LifecycleEvent event) {
        CdpFrame frame = this.frame(event.getFrameId());
        if (frame == null)
            return;
        frame.onLifecycleEvent(event.getLoaderId(), event.getName());
        this.emit(FrameManagerEvent.LifecycleEvent, frame);
        frame.emit(FrameEvents.LifecycleEvent, true);
    }

    private void onFrameStoppedLoading(String frameId) {
        CdpFrame frame = this.frame(frameId);
        if (frame == null)
            return;
        frame.onLoadingStopped();
        this.emit(FrameManagerEvent.LifecycleEvent, frame);
        frame.emit(FrameEvents.LifecycleEvent, true);
    }

    private void onFrameStartedLoading(String frameId) {
        CdpFrame frame = this.frame(frameId);
        if (frame == null) {
            return;
        }
        frame.onLoadingStarted();
    }

    private void handleFrameTree(CDPSession session, FrameTreeEvent frameTree) {
        if (StringUtil.isNotEmpty(frameTree.getFrame().getParentId())) {
            this.onFrameAttached(session, frameTree.getFrame().getId(), frameTree.getFrame().getParentId());
        }
        if (!this.frameNavigatedReceived.contains(frameTree.getFrame().getId())) {
            this.onFrameNavigated(frameTree.getFrame(), "Navigation");
        } else {
            this.frameNavigatedReceived.remove(frameTree.getFrame().getId());
        }
        if (ValidateUtil.isEmpty(frameTree.getChildFrames())) {
            return;
        }
        for (FrameTreeEvent child : frameTree.getChildFrames()) {
            this.handleFrameTree(session, child);
        }
    }

    private void onFrameAttached(CDPSession session, String frameId, String parentFrameId) {
        CdpFrame frame = this.frame(frameId);
        if (frame != null) {
            if (session != null && frame.client() != this.client) {
                frame.updateClient(session);
            }
            return;
        }
        frame = new CdpFrame(this, frameId, parentFrameId, session);
        this.frameTree.addFrame(frame);
        this.emit(FrameManagerEvent.FrameAttached, frame);
    }

    private void onFrameNavigated(FramePayload framePayload, String navigationType) {
        String frameId = framePayload.getId();
        boolean isMainFrame = StringUtil.isEmpty(framePayload.getParentId());
        CdpFrame frame = this.frameTree.getById(frameId);
        // Detach all child frames first.
        if (frame != null) {
            if (ValidateUtil.isNotEmpty(frame.childFrames())) {
                for (CdpFrame childFrame : frame.childFrames()) {
                    this.removeFramesRecursively(childFrame);
                }
            }
        }
        // Update or create main frame.
        if (isMainFrame) {
            if (frame != null) {
                // Update frame id to retain frame identity on cross-process navigation.
                this.frameTree.removeFrame(frame);
                frame.setId(frameId);
            } else {
                // Initial main frame navigation.
                frame = new CdpFrame(this, frameId, null, this.client);
            }
            this.frameTree.addFrame(frame);
        }

        // Update frame payload.
        frame = this.frameTree.waitForFrame(frameId);
        frame.navigated(framePayload);
        this.emit(FrameManagerEvent.FrameNavigated, frame);
        frame.emit(FrameEvents.FrameNavigated, navigationType);
    }

    private void createIsolatedWorld(CDPSession session, String name) {
        String key = session.id() + ":" + name;
        if (this.isolatedWorlds.contains(key))
            return;
        this.isolatedWorlds.add(name);
        Map<String, Object> params = ParamsFactory.create();
        params.put("source", "//# sourceURL=" + INTERNAL_URL);
        params.put("worldName", name);
        session.send("Page.addScriptToEvaluateOnNewDocument", params);
        this.frames().stream().filter(frame -> frame.client() == session).forEach(frame -> {
            // Frames might be removed before we send this, so we don't want to
            // throw an error.
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("frameId", frame.id());
                param.put("grantUniveralAccess", true);
                param.put("worldName", name);
                session.send("Page.createIsolatedWorld", param, null, false);
            } catch (Exception e) {
                LOGGER.error("Page.createIsolatedWorld error: ", e);
            }
        });
    }

    private void onFrameNavigatedWithinDocument(String frameId, String url) {
        CdpFrame frame = this.frame(frameId);
        if (Objects.isNull(frame)) {
            return;
        }
        frame.navigatedWithinDocument(url);
        this.emit(FrameManagerEvent.FrameNavigatedWithinDocument, frame);
        frame.emit(FrameEvents.FrameNavigatedWithinDocument, true);
        this.emit(FrameManagerEvent.FrameNavigated, frame);
        frame.emit(FrameEvents.FrameNavigated, "Navigation");
    }

    private void onFrameDetached(String frameId, String reason) {
        CdpFrame frame = this.frame(frameId);
        if (Objects.isNull(frame)) return;
        if (StringUtil.isEmpty(reason)) {
            return;
        }
        switch (reason) {
            case "remove":
                // Only remove the frame if the reason for the detached event is
                // an actual removement of the frame.
                // For frames that become OOP iframes, the reason would be 'swap'.
                this.removeFramesRecursively(frame);
                break;
            case "swap":
                this.emit(FrameManagerEvent.FrameSwapped, frame);
                frame.emit(FrameEvents.FrameSwapped, true);
                break;
        }
    }

    private void onExecutionContextCreated(ExecutionContextDescription contextPayload, CDPSession session) {
        String frameId = contextPayload.getAuxData() != null ? contextPayload.getAuxData().getFrameId() : null;
        CdpFrame frame = this.frame(frameId);
        IsolatedWorld world = null;
        if (Objects.nonNull(frame)) {
            if (frame.client() != session) {
                return;
            }
            if (contextPayload.getAuxData() != null && contextPayload.getAuxData().getIsDefault()) {
                world = frame.worlds().get(MAIN_WORLD);
            } else if (contextPayload.getName().startsWith(UTILITY_WORLD_NAME)) {
                // In case of multiple sessions to the same target, there's a race between
                // connections so we might end up creating multiple isolated worlds.
                // We can use either.
                world = frame.worlds().get(PUPPETEER_WORLD);
            }
        }
        if (Objects.isNull(world)) {
            return;
        }
        CDPSession client;
        if (Objects.nonNull(frame.client())) {
            client = frame.client();
        } else {
            client = this.client;
        }
        ExecutionContext context = new ExecutionContext(client, contextPayload, world);
        world.setContext(context);
    }

    private void removeFramesRecursively(CdpFrame childFrame) {
        if (ValidateUtil.isNotEmpty(childFrame.childFrames())) {
            for (CdpFrame frame : childFrame.childFrames()) {
                this.removeFramesRecursively(frame);
            }
        }
        childFrame.dispose();
        this.frameTree.removeFrame(childFrame);
        this.emit(FrameManagerEvent.FrameDetached, childFrame);
        childFrame.emit(FrameEvents.FrameDetached, childFrame);
    }

    public FrameTree<CdpFrame> frameTree() {
        return this.frameTree;
    }

    public enum FrameManagerEvent {
        FrameAttached("FrameManager.FrameAttached"),
        FrameNavigated("FrameManager.FrameNavigated"),
        FrameDetached("FrameManager.FrameDetached"),
        FrameSwapped("FrameManager.FrameSwapped"),
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
