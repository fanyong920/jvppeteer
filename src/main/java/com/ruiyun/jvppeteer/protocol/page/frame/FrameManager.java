package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.options.PageOptions;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContextDescription;
import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.protocol.page.network.NetworkManager;
import com.ruiyun.jvppeteer.protocol.page.network.Response;
import com.ruiyun.jvppeteer.protocol.page.payload.*;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrameManager extends EventEmitter {

    private CDPSession client;

    private Page page;

    private TimeoutSettings timeoutSettings;

    private NetworkManager networkManager;

    private Map<String, Frame> frames;

    private Map<Integer, ExecutionContext> contextIdToContext;

    private Set<String> isolatedWorlds;

    private Frame mainFrame;

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
    }

    private void onExecutionContextsCleared() {
    }

    private void onExecutionContextDestroyed(int executionContextId) {
    }

    private void onExecutionContextCreated(ExecutionContextDescription context) {
    }

    /**
     *
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


    public void initialize() {

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
    private void onFrameNavigated(Frame framePayload) {
        boolean isMainFrame = StringUtil.isEmpty(framePayload.getParentId());
        Frame frame = isMainFrame ? this.mainFrame : this.frames.get(framePayload.getId());
        ValidateUtil.assertBoolean(!isMainFrame || frame != null, "We either navigate top level or have old version of the navigated frame");

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
        this.frames.remove(childFrame);
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

    public Frame mainFrame() {
        return mainFrame;
    }

    public Response navigateFrame(Frame frame, String url, PageOptions options) {
        return null;
    }
}
