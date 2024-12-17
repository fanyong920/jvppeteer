package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.events.FrameEvents;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class LifecycleWatcher {

    private final List<String> expectedLifecycle = new ArrayList<>();
    private final Map<FrameEvents, Consumer<?>> frameListeners = new HashMap<>();
    private final Map<NetworkManager.NetworkManagerEvent, Consumer<?>> networkListeners = new HashMap<>();
    private final Map<FrameManager.FrameManagerEvent, Consumer<?>> frameManagerListeners = new HashMap<>();
    private final CdpFrame frame;
    private CdpRequest navigationRequest;
    private final String initialLoaderId;
    private boolean hasSameDocumentNavigation;
    private final AwaitableResult<Boolean> lifecycleResult = AwaitableResult.create();
    private final AwaitableResult<Boolean> sameDocumentNavigationResult = AwaitableResult.create();
    private final AwaitableResult<Boolean> newDocumentNavigationResult = AwaitableResult.create();
    public final AwaitableResult<Exception> terminationResult = AwaitableResult.create();
    public AwaitableResult<Boolean> navigationResponseReceived;
    private boolean swapped = false;
    private final NetworkManager networkManager;

    public LifecycleWatcher(NetworkManager networkManager, CdpFrame frame, List<PuppeteerLifeCycle> waitUntil) {
        super();
        this.frame = frame;
        this.initialLoaderId = frame.loaderId();
        this.networkManager = networkManager;
        waitUntil.forEach(value -> {
            this.expectedLifecycle.add(value.getValue());
        });
        Consumer<Object> lifecycleListener = (ignore) -> this.checkLifecycleComplete();
        this.frame.frameManager().on(FrameManager.FrameManagerEvent.LifecycleEvent, lifecycleListener);
        this.frameManagerListeners.put(FrameManager.FrameManagerEvent.LifecycleEvent, lifecycleListener);

        Consumer<Object> frameNavigatedWithinDocumentListener = (ignore) -> this.navigatedWithinDocument();
        this.frame.on(FrameEvents.FrameNavigatedWithinDocument, frameNavigatedWithinDocumentListener);
        this.frameListeners.put(FrameEvents.FrameNavigatedWithinDocument, frameNavigatedWithinDocumentListener);

        Consumer<String> frameNavigatedListener = this::navigated;
        this.frame.on(FrameEvents.FrameNavigated, frameNavigatedListener);
        this.frameListeners.put(FrameEvents.FrameNavigated, frameNavigatedListener);

        Consumer<Object> frameSwappedListener = (ignore) -> this.frameSwapped();
        frame.on(FrameEvents.FrameSwapped, frameSwappedListener);
        this.frameListeners.put(FrameEvents.FrameSwapped, frameSwappedListener);


        Consumer<Object> frameSwappedByActivationListener = (ignore) -> this.frameSwapped();
        this.frame.on(FrameEvents.FrameSwappedByActivation, frameSwappedByActivationListener);
        this.frameListeners.put(FrameEvents.FrameSwappedByActivation, frameSwappedByActivationListener);


        Consumer<CdpFrame> frameDetachedListener = this::frameDetached;
        this.frame.on(FrameEvents.FrameDetached, frameDetachedListener);
        this.frameListeners.put(FrameEvents.FrameDetached, frameDetachedListener);

        Consumer<CdpRequest> requestListener = this::onRequest;
        this.networkManager.on(NetworkManager.NetworkManagerEvent.Request, requestListener);
        this.networkListeners.put(NetworkManager.NetworkManagerEvent.Request, requestListener);

        Consumer<CdpRequest> requestFailedListener = this::onRequestFailed;
        this.networkManager.on(NetworkManager.NetworkManagerEvent.RequestFailed, requestFailedListener);
        this.networkListeners.put(NetworkManager.NetworkManagerEvent.RequestFailed, requestFailedListener);

        Consumer<CdpResponse> responseListener = this::onResponse;
        this.networkManager.on(NetworkManager.NetworkManagerEvent.Response, responseListener);
        this.networkListeners.put(NetworkManager.NetworkManagerEvent.Response, responseListener);
        this.checkLifecycleComplete();
    }

    private void onRequestFailed(CdpRequest request) {
        if (this.navigationRequest != null) {
            if (!this.navigationRequest.id().equals(request.id())) {
                return;
            }
        }
        if (this.navigationResponseReceived != null) {
            this.navigationResponseReceived.onSuccess(true);
        }
    }

    private void onResponse(CdpResponse response) {
        if (this.navigationRequest != null) {
            if (!this.navigationRequest.id().equals(response.request().id())) {
                return;
            }
        }
        if (this.navigationResponseReceived != null) {
            this.navigationResponseReceived.onSuccess(true);
        }
    }

    private void frameDetached(CdpFrame frame) {
        if (this.frame.equals(frame)) {
            terminationResult.onSuccess(new JvppeteerException("Navigating frame was detached'"));
            return;
        }
        this.checkLifecycleComplete();
    }

    /**
     * @param navigationType ('Navigation' | 'BackForwardCacheRestore');
     */
    private void navigated(String navigationType) {
        if ("BackForwardCacheRestore".equals(navigationType)) {
            this.frameSwapped();
            return;
        }
        this.checkLifecycleComplete();
    }

    private void frameSwapped() {
        this.swapped = true;
        this.checkLifecycleComplete();
    }

    public boolean sameDocumentNavigationIsDone() {
        return this.sameDocumentNavigationResult.isDone();
    }

    public boolean newDocumentNavigationIsDone() {
        return this.newDocumentNavigationResult.isDone();
    }

    private void onRequest(CdpRequest request) {
        if (!Objects.equals(request.frame(),this.frame) || !request.isNavigationRequest()) {
            return;
        }
        this.navigationRequest = request;
        if (this.navigationResponseReceived != null) {
            this.navigationResponseReceived.onSuccess(true);
        }
        this.navigationResponseReceived = AwaitableResult.create();
        if (request.response() != null) {
            if (this.navigationResponseReceived != null) {
                this.navigationResponseReceived.onSuccess(true);
            }
        }
    }

    public void navigatedWithinDocument() {
        this.hasSameDocumentNavigation = true;
        this.checkLifecycleComplete();
    }

    private void checkLifecycleComplete() {
        // We expect navigation to commit.
        if (!checkLifecycle(this.frame, this.expectedLifecycle)) return;
        this.lifecycleResult.onSuccess(true);
        if (this.hasSameDocumentNavigation) {
            this.sameDocumentNavigationResult.onSuccess(true);
        }
        if (this.swapped || !this.frame.loaderId().equals(this.initialLoaderId)) {
            this.newDocumentNavigationResult.onSuccess(true);
        }
    }

    /**
     * @param frame             frame
     * @param expectedLifecycle 生命周期集合
     * @return boolean 结果
     */
    private boolean checkLifecycle(Frame frame, List<String> expectedLifecycle) {
        if (ValidateUtil.isNotEmpty(expectedLifecycle)) {
            for (String event : expectedLifecycle) {
                if (!((CdpFrame)frame).lifecycleEvents().contains(event)) return false;
            }
        }
        if (ValidateUtil.isNotEmpty(frame.childFrames())) {
            for (Frame child : frame.childFrames()) {
                if (!checkLifecycle(child, expectedLifecycle)) return false;
            }
        }
        return true;
    }

    public boolean lifecycleIsDone() {
        return this.lifecycleResult.isDone();
    }

    public boolean terminationIsDone() {
        return this.terminationResult.isDone();
    }

    public void dispose() {
        this.frameListeners.forEach(this.frame::off);
        this.networkListeners.forEach(this.networkManager::off);
        this.frameManagerListeners.forEach(this.frame.frameManager()::off);
        this.terminationResult.onSuccess(new JvppeteerException("LifecycleWatcher disposed"));
    }

    public boolean navigationResponseIsDone() {
        if (this.navigationResponseReceived != null) {
            return this.navigationResponseReceived.isDone();
        }
        return true;
    }

    public CdpResponse navigationResponse() {
        return this.navigationRequest != null ? this.navigationRequest.response() : null;
    }

    public enum NavigationType {
        Navigation,
        BackForwardCacheRestore
    }

}

