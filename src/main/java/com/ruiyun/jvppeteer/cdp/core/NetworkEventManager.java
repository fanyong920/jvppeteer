package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.cdp.entities.QueuedEventGroup;
import com.ruiyun.jvppeteer.cdp.entities.RedirectInfo;
import com.ruiyun.jvppeteer.cdp.entities.RequestWillBeSentExtraInfoEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestPausedEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedExtraInfoEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NetworkEventManager {

    /**
     * There are four possible orders of events:
     * A. `_onRequestWillBeSent`
     * B. `_onRequestWillBeSent`, `_onRequestPaused`
     * C. `_onRequestPaused`, `_onRequestWillBeSent`
     * D. `_onRequestPaused`, `_onRequestWillBeSent`, `_onRequestPaused`,
     * `_onRequestWillBeSent`, `_onRequestPaused`, `_onRequestPaused`
     * (see crbug.com/1196004)
     * <p>
     * For `_onRequest` we need the event from `_onRequestWillBeSent` and
     * optionally the `interceptionId` from `_onRequestPaused`.
     * <p>
     * If request interception is disabled, call `_onRequest` once per call to
     * `_onRequestWillBeSent`.
     * If request interception is enabled, call `_onRequest` once per call to
     * `_onRequestPaused` (once per `interceptionId`).
     * <p>
     * Events are stored to allow for subsequent events to call `_onRequest`.
     * <p>
     * Note that (chains of) redirect requests have the same `requestId` (!) as
     * the original request. We have to anticipate series of events like these:
     * A. `_onRequestWillBeSent`,
     * `_onRequestWillBeSent`, ...
     * B. `_onRequestWillBeSent`, `_onRequestPaused`,
     * `_onRequestWillBeSent`, `_onRequestPaused`, ...
     * C. `_onRequestWillBeSent`, `_onRequestPaused`,
     * `_onRequestPaused`, `_onRequestWillBeSent`, ...
     * D. `_onRequestPaused`, `_onRequestWillBeSent`,
     * `_onRequestPaused`, `_onRequestWillBeSent`, `_onRequestPaused`,
     * `_onRequestWillBeSent`, `_onRequestPaused`, `_onRequestPaused`, ...
     * (see crbug.com/1196004)
     */
    private final Map<String, RequestWillBeSentEvent> requestWillBeSentMap = new HashMap<>();
    private final Map<String, RequestPausedEvent> requestPausedMap = new HashMap<>();
    private final Map<String, CdpRequest> httpRequestsMap = new HashMap<>();
    private final Map<String, LinkedList<RequestWillBeSentExtraInfoEvent>> requestWillBeSentExtraInfoMap = new HashMap<>();

    /**
     * The below maps are used to reconcile Network.responseReceivedExtraInfo
     * events with their corresponding request. Each response and redirect
     * response gets an ExtraInfo event, and we don't know which will come first.
     * This means that we have to store a Response or an ExtraInfo for each
     * response, and emit the event when we get both of them. In addition, to
     * handle redirects, we have to make them Arrays to represent the chain of
     * events.
     */
    private final Map<String, LinkedList<ResponseReceivedExtraInfoEvent>> responseReceivedExtraInfoMap = new HashMap<>();
    private final Map<String, LinkedList<RedirectInfo>> queuedRedirectInfoMap = new HashMap<>();
    private final Map<String, QueuedEventGroup> queuedEventGroupMap = new HashMap<>();

    public void forget(String networkRequestId) {
        this.requestWillBeSentMap.remove(networkRequestId);
        this.requestPausedMap.remove(networkRequestId);
        this.requestWillBeSentExtraInfoMap.remove(networkRequestId);
        this.queuedEventGroupMap.remove(networkRequestId);
        this.queuedRedirectInfoMap.remove(networkRequestId);
        this.responseReceivedExtraInfoMap.remove(networkRequestId);
    }

    public LinkedList<RequestWillBeSentExtraInfoEvent> requestExtraInfo(String networkRequestId) {
        if (!this.requestWillBeSentExtraInfoMap.containsKey(networkRequestId)) {
            this.requestWillBeSentExtraInfoMap.put(networkRequestId, new LinkedList<>());
        }
        return this.requestWillBeSentExtraInfoMap.get(networkRequestId);
    }

    public LinkedList<ResponseReceivedExtraInfoEvent> responseExtraInfo(String networkRequestId) {
        if (!this.responseReceivedExtraInfoMap.containsKey(networkRequestId)) {
            this.responseReceivedExtraInfoMap.put(networkRequestId, new LinkedList<>());
        }
        return this.responseReceivedExtraInfoMap.get(networkRequestId);
    }

    private LinkedList<RedirectInfo> queuedRedirectInfo(String fetchRequestId) {
        if (!this.queuedRedirectInfoMap.containsKey(fetchRequestId)) {
            this.queuedRedirectInfoMap.put(fetchRequestId, new LinkedList<>());
        }
        return this.queuedRedirectInfoMap.get(fetchRequestId);
    }

    public void queueRedirectInfo(String fetchRequestId, RedirectInfo redirectInfo) {
        this.queuedRedirectInfo(fetchRequestId).add(redirectInfo);
    }

    public RedirectInfo takeQueuedRedirectInfo(String fetchRequestId) {
        return this.queuedRedirectInfo(fetchRequestId).poll();
    }

    public int inFlightRequestsCount() {
        int inFlightRequestCounter = 0;
        for (CdpRequest request : this.httpRequestsMap.values()) {
            if (request.response() == null) {
                inFlightRequestCounter++;
            }
        }
        return inFlightRequestCounter;
    }

    public void storeRequestWillBeSent(String networkRequestId, RequestWillBeSentEvent event) {
        this.requestWillBeSentMap.put(networkRequestId, event);
    }

    public RequestWillBeSentEvent getRequestWillBeSent(String networkRequestId) {
        return this.requestWillBeSentMap.get(networkRequestId);
    }

    public void forgetRequestWillBeSent(String networkRequestId) {
        this.requestWillBeSentMap.remove(networkRequestId);
    }

    public RequestPausedEvent getRequestPaused(String networkRequestId) {
        return this.requestPausedMap.get(networkRequestId);
    }

    public void forgetRequestPaused(String networkRequestId) {
        this.requestPausedMap.remove(networkRequestId);
    }

    public void storeRequestPaused(String networkRequestId, RequestPausedEvent event) {
        this.requestPausedMap.put(networkRequestId, event);
    }

    public CdpRequest getRequest(String networkRequestId) {
        return this.httpRequestsMap.get(networkRequestId);
    }

    public void storeRequest(String networkRequestId, CdpRequest request) {
        this.httpRequestsMap.put(networkRequestId, request);
    }

    public void forgetRequest(String networkRequestId) {
        this.httpRequestsMap.remove(networkRequestId);
    }

    public QueuedEventGroup getQueuedEventGroup(String networkRequestId) {
        return this.queuedEventGroupMap.get(networkRequestId);
    }

    public void queueEventGroup(String networkRequestId, QueuedEventGroup event) {
        this.queuedEventGroupMap.put(networkRequestId, event);
    }

    public void forgetQueuedEventGroup(String networkRequestId) {
        this.queuedEventGroupMap.remove(networkRequestId);
    }
}
