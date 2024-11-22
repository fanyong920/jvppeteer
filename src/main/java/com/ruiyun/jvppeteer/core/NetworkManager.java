package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.FrameProvider;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.AuthChallengeResponse;
import com.ruiyun.jvppeteer.entities.Credentials;
import com.ruiyun.jvppeteer.entities.InternalNetworkConditions;
import com.ruiyun.jvppeteer.entities.NetworkConditions;
import com.ruiyun.jvppeteer.entities.QueuedEventGroup;
import com.ruiyun.jvppeteer.entities.RedirectInfo;
import com.ruiyun.jvppeteer.entities.ResponsePayload;
import com.ruiyun.jvppeteer.entities.UserAgentMetadata;
import com.ruiyun.jvppeteer.events.AuthRequiredEvent;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.LoadingFailedEvent;
import com.ruiyun.jvppeteer.events.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.events.RequestPausedEvent;
import com.ruiyun.jvppeteer.events.RequestServedFromCacheEvent;
import com.ruiyun.jvppeteer.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.events.ResponseReceivedEvent;
import com.ruiyun.jvppeteer.events.ResponseReceivedExtraInfoEvent;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager extends EventEmitter<NetworkManager.NetworkManagerEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private final FrameProvider frameManager;
    private final NetworkEventManager networkEventManager = new NetworkEventManager();
    private Map<String, String> extraHTTPHeaders;
    private volatile Credentials credentials;
    private final Set<String> attemptedAuthentications = new HashSet<>();
    private volatile boolean protocolRequestInterceptionEnabled;
    private volatile Boolean userCacheDisabled;
    private InternalNetworkConditions emulatedNetworkConditions;
    private volatile String userAgent;
    private volatile UserAgentMetadata userAgentMetadata;
    private final Map<CDPSession, Map<CDPSession.CDPSessionEvent, Consumer<?>>> clients = new HashMap<>();
    private volatile boolean userRequestInterceptionEnabled = false;

    public NetworkManager(FrameProvider frameManager) {
        super();
        this.frameManager = frameManager;
    }

    public void addClient(CDPSession client) {
        if (this.clients.containsKey(client)) {
            return;
        }
        Map<CDPSession.CDPSessionEvent, Consumer<?>> listeners = new HashMap<>();
        Consumer<RequestPausedEvent> requestPaused = event -> this.onRequestPaused(client, event);
        client.on(CDPSession.CDPSessionEvent.Fetch_requestPaused, requestPaused);
        listeners.put(CDPSession.CDPSessionEvent.Fetch_requestPaused, requestPaused);

        Consumer<AuthRequiredEvent> authRequired = event -> this.onAuthRequired(client, event);
        client.on(CDPSession.CDPSessionEvent.Fetch_authRequired, authRequired);
        listeners.put(CDPSession.CDPSessionEvent.Fetch_authRequired, authRequired);

        Consumer<RequestWillBeSentEvent> requestWillBeSent = event -> this.onRequestWillBeSent(client, event);
        client.on(CDPSession.CDPSessionEvent.Network_requestWillBeSent, requestWillBeSent);
        listeners.put(CDPSession.CDPSessionEvent.Network_requestWillBeSent, requestWillBeSent);

        Consumer<RequestServedFromCacheEvent> requestServedFromCache = event -> this.onRequestServedFromCache(client, event);
        client.on(CDPSession.CDPSessionEvent.Network_requestServedFromCache, requestServedFromCache);
        listeners.put(CDPSession.CDPSessionEvent.Network_requestServedFromCache, requestServedFromCache);

        Consumer<ResponseReceivedEvent> responseReceived = event -> this.onResponseReceived(client, event);
        client.on(CDPSession.CDPSessionEvent.Network_responseReceived, responseReceived);
        listeners.put(CDPSession.CDPSessionEvent.Network_responseReceived, responseReceived);

        Consumer<LoadingFinishedEvent> loadingFinished = event -> this.onLoadingFinished(client, event);
        client.on(CDPSession.CDPSessionEvent.Network_loadingFinished, loadingFinished);
        listeners.put(CDPSession.CDPSessionEvent.Network_loadingFinished, loadingFinished);

        Consumer<LoadingFailedEvent> loadingFailed = event -> this.onLoadingFailed(client,event);
        client.on(CDPSession.CDPSessionEvent.Network_loadingFailed, loadingFailed);
        listeners.put(CDPSession.CDPSessionEvent.Network_loadingFailed, loadingFailed);

        Consumer<ResponseReceivedExtraInfoEvent> responseReceivedExtraInfo = event -> this.onResponseReceivedExtraInfo(client, event);
        client.on(CDPSession.CDPSessionEvent.Network_responseReceivedExtraInfo, responseReceivedExtraInfo);
        listeners.put(CDPSession.CDPSessionEvent.Network_responseReceivedExtraInfo, responseReceivedExtraInfo);

        Consumer<Object> disconnected = (ignore) -> this.removeClient(client);
        client.on(CDPSession.CDPSessionEvent.CDPSession_Disconnected, disconnected);
        listeners.put(CDPSession.CDPSessionEvent.CDPSession_Disconnected, disconnected);


        this.clients.put(client, listeners);
        client.send("Network.enable");
        this.applyExtraHTTPHeaders(client);
        this.applyNetworkConditions(client);
        this.applyProtocolCacheDisabled(client);
        this.applyProtocolRequestInterception(client);
        this.applyUserAgent(client);
    }

    public void removeClient(CDPSession client) {
        Map<CDPSession.CDPSessionEvent, Consumer<?>> listeners = this.clients.remove(client);
        if (listeners != null) {//取消监听
            listeners.forEach(client::off);
        }
    }

    public void authenticate(Credentials credentials) {
        this.credentials = credentials;
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.clients.forEach((client1, disposables) -> this.applyProtocolRequestInterception(client1));
    }

    public void setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
        this.extraHTTPHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : extraHTTPHeaders.entrySet()) {
            String value = entry.getValue();
            this.extraHTTPHeaders.put(entry.getKey().toLowerCase(), value);
        }
        this.clients.forEach((client1, disposables) -> this.applyExtraHTTPHeaders(client1));
    }

    private void applyProtocolRequestInterception(CDPSession client) {
        if (this.userCacheDisabled == null) {
            this.userCacheDisabled = false;
        }
        if (this.protocolRequestInterceptionEnabled) {
            this.applyProtocolCacheDisabled(client);
            Map<String, Object> params = ParamsFactory.create();
            params.put("handleAuthRequests", true);
            List<Object> patterns = new ArrayList<>();
            patterns.add(Constant.OBJECTMAPPER.createObjectNode().put("urlPattern", "*"));
            params.put("patterns", patterns);
            client.send("Fetch.enable", params);
        } else {
            this.applyProtocolCacheDisabled(client);
            client.send("Fetch.disable");
        }
    }

    public Map<String, String> extraHTTPHeaders() {
        return Objects.isNull(this.extraHTTPHeaders) ? new HashMap<>() : this.extraHTTPHeaders;
    }

    private void applyProtocolCacheDisabled(CDPSession client) {
        if (this.userCacheDisabled == null) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("cacheDisabled", this.userCacheDisabled);
        client.send("Network.setCacheDisabled", params);
    }

    private void applyExtraHTTPHeaders(CDPSession client) {
        if (this.extraHTTPHeaders == null) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("headers", this.extraHTTPHeaders);
        client.send("Network.setExtraHTTPHeaders", params);
    }

    private void applyNetworkConditions(CDPSession client) {
        if (this.emulatedNetworkConditions == null) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("offline", this.emulatedNetworkConditions.getOffline());
        params.put("latency", this.emulatedNetworkConditions.getLatency());
        params.put("uploadThroughput", this.emulatedNetworkConditions.getUpload());
        params.put("downloadThroughput", this.emulatedNetworkConditions.getDownload());
        client.send("Network.emulateNetworkConditions", params);
    }

    private void applyUserAgent(CDPSession client) {
        if (this.userAgent == null) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("userAgent", this.userAgent);
        params.put("userAgentMetadata", this.userAgentMetadata);
        client.send("Network.setUserAgentOverride", params);
    }

    public int inFlightRequestsCount() {
        return this.networkEventManager.inFlightRequestsCount();
    }

    public void setOfflineMode(boolean value) {
        if (this.emulatedNetworkConditions == null) {
            this.emulatedNetworkConditions = new InternalNetworkConditions(false, -1, -1, 0);
        }
        this.emulatedNetworkConditions.setOffline(value);
        this.clients.forEach((client1, disposables) -> this.applyNetworkConditions(client1));
    }

    public void emulateNetworkConditions(NetworkConditions networkConditions) {
        if (this.emulatedNetworkConditions == null) {
            this.emulatedNetworkConditions = new InternalNetworkConditions(false, -1, -1, 0);
        }
        this.emulatedNetworkConditions.setUpload(networkConditions.getUpload());
        this.emulatedNetworkConditions.setDownload(networkConditions.getDownload());
        this.emulatedNetworkConditions.setLatency(networkConditions.getLatency());
        this.clients.forEach((client1, disposables) -> this.applyNetworkConditions(client1));
    }

    public void setUserAgent(String userAgent, UserAgentMetadata userAgentMetadata) {
        this.userAgent = userAgent;
        this.userAgentMetadata = userAgentMetadata;
        this.clients.forEach((client1, disposables) -> this.applyUserAgent(client1));
    }

    public void setCacheEnabled(boolean enabled) {
        this.userCacheDisabled = !enabled;
        this.clients.forEach((client1, disposables) -> this.applyProtocolCacheDisabled(client1));
    }

    public void setRequestInterception(boolean value) {
        this.userRequestInterceptionEnabled = value;
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.clients.forEach((client1, disposables) -> this.applyProtocolRequestInterception(client1));
    }

    public void onRequestWillBeSent(CDPSession client, RequestWillBeSentEvent event) {
        // Request interception doesn't happen for data URLs with Network Service.
        if (this.protocolRequestInterceptionEnabled && !event.getRequest().getUrl().startsWith("data:")) {
            String networkRequestId = event.getRequestId();
            this.networkEventManager.storeRequestWillBeSent(networkRequestId, event);
            RequestPausedEvent requestPausedEvent = this.networkEventManager.getRequestPaused(networkRequestId);
            if (requestPausedEvent != null) {
                String fetchRequestId = requestPausedEvent.getRequestId();
                this.patchRequestEventHeaders(event, requestPausedEvent);
                this.onRequest(client, event, fetchRequestId, false);
                this.networkEventManager.forgetRequestPaused(networkRequestId);
            }
            return;
        }
        this.onRequest(client, event, null, false);
    }

    public void onAuthRequired(CDPSession client, AuthRequiredEvent event) {
        /* @type {"Default"|"CancelAuth"|"ProvideCredentials"} */
        AuthChallengeResponse authChallengeResponse = new AuthChallengeResponse();
        if (this.attemptedAuthentications.contains(event.getRequestId())) {
            authChallengeResponse.setResponse("CancelAuth");
        } else if (this.credentials != null) {
            authChallengeResponse.setResponse("ProvideCredentials");
            this.attemptedAuthentications.add(event.getRequestId());
        }
        String username, password;
        if (this.credentials != null) {
            if (StringUtil.isNotEmpty(username = credentials.getUsername())) {
                authChallengeResponse.setUsername(username);
            }
            if (StringUtil.isNotEmpty(password = credentials.getPassword())) {
                authChallengeResponse.setPassword(password);
            }
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", event.getRequestId());
        params.put("authChallengeResponse", authChallengeResponse);
        try {
            client.send("Fetch.continueWithAuth", params);
        } catch (Exception e) {
            LOGGER.error("onAuthRequired error", e);
        }
    }

    public void onRequestPaused(CDPSession client, RequestPausedEvent event) {
        if (!this.userRequestInterceptionEnabled && this.protocolRequestInterceptionEnabled) {
            Map<String, Object> params = ParamsFactory.create();
            params.put("requestId", event.getRequestId());
            client.send("Fetch.continueRequest", params);
        }
        String networkId = event.getNetworkId();
        String requestId = event.getRequestId();
        if (StringUtil.isEmpty(networkId)) {
            this.onRequestWithoutNetworkInstrumentation(client, event);
            return;
        }
        RequestWillBeSentEvent requestWillBeSentEvent = this.networkEventManager.getRequestWillBeSent(networkId);
        if (requestWillBeSentEvent != null && (!requestWillBeSentEvent.getRequest().getUrl().equals(event.getRequest().getUrl()) || !requestWillBeSentEvent.getRequest().getMethod().equals(event.getRequest().getMethod()))) {
            this.networkEventManager.forgetRequestWillBeSent(networkId);
            return;
        }
        if (requestWillBeSentEvent != null) {
            this.patchRequestEventHeaders(requestWillBeSentEvent, event);
            this.onRequest(client, requestWillBeSentEvent, requestId, false);
        } else {
            this.networkEventManager.storeRequestPaused(networkId, event);
        }
    }

    private void onRequestWithoutNetworkInstrumentation(CDPSession client, RequestPausedEvent event) {
        String frameId = event.getFrameId();
        Frame frame = null;
        if (!StringUtil.isEmpty(frameId)) {
            frame = this.frameManager.frame(frameId);
        }
        RequestWillBeSentEvent requestWillBeSent = new RequestWillBeSentEvent();
        requestWillBeSent.setRequestId(event.getRequestId());
        requestWillBeSent.setRequest(event.getRequest());
        Request request = new Request(client, frame, event.getRequestId(), this.userRequestInterceptionEnabled, requestWillBeSent, new ArrayList<>());
        this.emit(NetworkManagerEvent.Request, request);
        request.finalizeInterceptions();
    }

    private void patchRequestEventHeaders(RequestWillBeSentEvent requestWillBeSentEvent, RequestPausedEvent requestPausedEvent) {
        Map<String, String> headers = new HashMap<>();
        if (requestWillBeSentEvent.getRequest().getHeaders() != null) {
            headers.putAll(requestWillBeSentEvent.getRequest().getHeaders());
        }
        if (requestPausedEvent.getRequest().getHeaders() != null) {
            headers.putAll(requestPausedEvent.getRequest().getHeaders());
        }
        requestWillBeSentEvent.getRequest().setHeaders(headers);
    }

    private void onResponseReceivedExtraInfo(CDPSession client, ResponseReceivedExtraInfoEvent event) {
        RedirectInfo redirectInfo = this.networkEventManager.takeQueuedRedirectInfo(event.getRequestId());
        if (redirectInfo != null) {
            this.networkEventManager.responseExtraInfo(event.getRequestId()).offer(event);
            this.onRequest(client, redirectInfo.getEvent(), redirectInfo.getFetchRequestId(), false);
            return;
        }
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (queuedEvents != null) {
            this.networkEventManager.forgetQueuedEventGroup(event.getRequestId());
            this.emitResponseEvent(client, queuedEvents.getResponseReceivedEvent(), event);
            if (queuedEvents.getLoadingFinishedEvent() != null) {
                this.emitLoadingFinished(client,queuedEvents.getLoadingFinishedEvent());
            }
            if (queuedEvents.getLoadingFailedEvent() != null) {
                this.emitLoadingFailed(client,queuedEvents.getLoadingFailedEvent());
            }
            return;
        }
        this.networkEventManager.responseExtraInfo(event.getRequestId()).offer(event);
    }

    private void emitLoadingFailed(CDPSession client, LoadingFailedEvent event) {
        Request request = this.networkEventManager.getRequest(event.getRequestId());
        if (request == null) {
            return;
        }
        this.maybeReassignOOPIFRequestClient(client, request);
        request.setFailureText(event.getErrorText());
        Response response = request.response();
        if (response != null) {
            response.resolveBody(null);
        }
        this.forgetRequest(request, true);
        this.emit(NetworkManagerEvent.RequestFailed, request);
    }

    private void emitLoadingFinished(CDPSession client,LoadingFinishedEvent event) {
        Request request = this.networkEventManager.getRequest(event.getRequestId());
        if (request == null) {
            return;
        }
        this.maybeReassignOOPIFRequestClient(client, request);
        if (request.response() != null) {
            request.response().resolveBody(null);
        }
        this.forgetRequest(request, true);
        this.emit(NetworkManagerEvent.RequestFinished, request);
    }

    private void maybeReassignOOPIFRequestClient(CDPSession client, Request request) {
        // Document requests for OOPIFs start in the parent frame but are adopted by their
        // child frame, meaning their loadingFinished and loadingFailed events are fired on
        // the child session. In this case we reassign the request CDPSession to ensure all
        // subsequent actions use the correct session (e.g. retrieving response body in
        // HTTPResponse).
        if (client != request.client() && request.isNavigationRequest()) {
            request.setClient(client);
        }
    }


    private void onRequest(CDPSession client, RequestWillBeSentEvent event, String fetchRequestId, boolean fromMemoryCache) {
        List<Request> redirectChain = new ArrayList<>();
        if (event.getRedirectResponse() != null) {
            ResponseReceivedExtraInfoEvent redirectResponseExtraInfo = null;
            if (event.getRedirectHasExtraInfo()) {
                redirectResponseExtraInfo = this.networkEventManager.responseExtraInfo(event.getRequestId()).poll();
                if (redirectResponseExtraInfo == null) {
                    RedirectInfo redirectInfo = new RedirectInfo();
                    redirectInfo.setEvent(event);
                    redirectInfo.setFetchRequestId(fetchRequestId);
                    this.networkEventManager.queueRedirectInfo(event.getRequestId(), redirectInfo);
                    return;
                }
            }
            Request request = this.networkEventManager.getRequest(event.getRequestId());
            if (request != null) {
                this.handleRequestRedirect(client, request, event.getRedirectResponse(), redirectResponseExtraInfo);
                redirectChain = request.redirectChain();
            }
        }
        String frameId = event.getFrameId();
        Frame frame = null;
        if (StringUtil.isNotEmpty(frameId)) {
            frame = this.frameManager.frame(frameId);
        }
        Request request = new Request(client, frame, fetchRequestId, this.userRequestInterceptionEnabled, event, redirectChain);
        request.setFromMemoryCache(fromMemoryCache);
        this.networkEventManager.storeRequest(event.getRequestId(), request);
        this.emit(NetworkManagerEvent.Request, request);
        request.finalizeInterceptions();
    }

    //不能阻塞 WebSocketConnectReadThread
    private void handleRequestRedirect(CDPSession _client, Request request, ResponsePayload responsePayload, ResponseReceivedExtraInfoEvent extraInfo) {
        Response response = new Response(request, responsePayload, extraInfo);
        request.setResponse(response);
        request.redirectChain().add(request);
        response.resolveBody("Response body is unavailable for redirect responses，request url: " + request.url());
        this.forgetRequest(request, false);
        this.emit(NetworkManagerEvent.Response, response);
        this.emit(NetworkManagerEvent.RequestFinished, request);
    }

    // WebSocketConnectReadThread
    private void forgetRequest(Request request, boolean events) {
        String requestId = request.id();
        String interceptionId = request.interceptionId();
        this.networkEventManager.forgetRequest(requestId);
        if (interceptionId != null) {
            this.attemptedAuthentications.remove(interceptionId);
        }
        if (events) {
            this.networkEventManager.forget(requestId);
        }
    }

    public void onLoadingFinished(CDPSession client, LoadingFinishedEvent event) {
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (queuedEvents != null) {
            queuedEvents.setLoadingFinishedEvent(event);
        } else {
            this.emitLoadingFinished(client,event);
        }
    }

    private void onResponseReceived(CDPSession client, ResponseReceivedEvent event) {
        Request request = this.networkEventManager.getRequest(event.getRequestId());
        ResponseReceivedExtraInfoEvent extraInfo = null;
        if (request != null && !request.fromMemoryCache() && event.getHasExtraInfo()) {
            extraInfo = this.networkEventManager.responseExtraInfo(event.getRequestId()).poll();
            if (extraInfo == null) {
                QueuedEventGroup group = new QueuedEventGroup();
                group.setResponseReceivedEvent(event);
                this.networkEventManager.queueEventGroup(event.getRequestId(), group);
                return;
            }
        }
        this.emitResponseEvent(client, event, extraInfo);
    }

    public void onLoadingFailed(CDPSession client,LoadingFailedEvent event) {
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (queuedEvents != null) {
            queuedEvents.setLoadingFailedEvent(event);
        } else {
            this.emitLoadingFailed(client,event);
        }
    }

    public void onRequestServedFromCache(CDPSession client, RequestServedFromCacheEvent event) {
        RequestWillBeSentEvent requestWillBeSentEvent = this.networkEventManager.getRequestWillBeSent(event.getRequestId());
        Request request = this.networkEventManager.getRequest(event.getRequestId());
        if (Objects.nonNull(request)) {
            request.setFromMemoryCache(true);
        }
        if (Objects.isNull(request) && Objects.nonNull(requestWillBeSentEvent)) {
            this.onRequest(client, requestWillBeSentEvent, null, true);
            request = this.networkEventManager.getRequest(event.getRequestId());
        }
        if (Objects.isNull(request)) {
            LOGGER.error("Request {} was served from cache but we could not find the corresponding request object", event.getRequestId(), new JvppeteerException());
            return;
        }
        this.emit(NetworkManagerEvent.RequestServedFromCache, request);
    }

    public void emitResponseEvent(CDPSession _client, ResponseReceivedEvent responseReceived, ResponseReceivedExtraInfoEvent extraInfo) {
        Request request = this.networkEventManager.getRequest(responseReceived.getRequestId());
        if (request == null) {
            return;
        }
        List<ResponseReceivedExtraInfoEvent> extraInfos = this.networkEventManager.responseExtraInfo(responseReceived.getRequestId());
        if (ValidateUtil.isNotEmpty(extraInfos)) {
            LOGGER.error("Unexpected extraInfo events for request {}", responseReceived.getRequestId());
        }
        if (responseReceived.getResponse().getFromDiskCache()) {
            extraInfo = null;
        }
        Response response = new Response(request, responseReceived.getResponse(), extraInfo);
        request.setResponse(response);
        this.emit(NetworkManagerEvent.Response, response);
    }

    public enum NetworkManagerEvent {
        Request("NetworkManager.Request"),
        RequestServedFromCache("NetworkManager.RequestServedFromCache"),
        Response("NetworkManager.Response"),
        RequestFailed("NetworkManager.RequestFailed"),
        RequestFinished("NetworkManager.RequestFinished");
        private String eventName;

        NetworkManagerEvent(String eventName) {
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
