package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.AuthChallengeResponse;
import com.ruiyun.jvppeteer.cdp.entities.Credentials;
import com.ruiyun.jvppeteer.cdp.entities.InternalNetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.NetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.QueuedEventGroup;
import com.ruiyun.jvppeteer.cdp.entities.RedirectInfo;
import com.ruiyun.jvppeteer.cdp.entities.RequestWillBeSentExtraInfoEvent;
import com.ruiyun.jvppeteer.cdp.entities.ResponsePayload;
import com.ruiyun.jvppeteer.cdp.entities.UserAgentMetadata;
import com.ruiyun.jvppeteer.cdp.events.AuthRequiredEvent;
import com.ruiyun.jvppeteer.cdp.events.LoadingFailedEvent;
import com.ruiyun.jvppeteer.cdp.events.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestPausedEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestServedFromCacheEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedExtraInfoEvent;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.FrameProvider;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.UserAgentOptions;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
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


import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class NetworkManager extends EventEmitter<NetworkManager.NetworkManagerEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private final FrameProvider frameManager;
    private final NetworkEventManager networkEventManager = new NetworkEventManager();
    private Map<String, String> extraHTTPHeaders;
    private volatile Credentials credentials;
    private final Set<String> attemptedAuthentications = new HashSet<>();
    private volatile Boolean protocolRequestInterceptionEnabled;
    private volatile Boolean userCacheDisabled;
    private InternalNetworkConditions emulatedNetworkConditions;
    private volatile String userAgent;
    private volatile UserAgentMetadata userAgentMetadata;
    private final Map<CDPSession, Map<ConnectionEvents, Consumer<?>>> clients = new HashMap<>();
    private volatile boolean userRequestInterceptionEnabled = false;
    private volatile boolean networkEnabled;
    private String platform;

    public NetworkManager(FrameProvider frameManager, boolean networkEnabled) {
        super();
        this.frameManager = frameManager;
        this.networkEnabled = networkEnabled;
    }

    private boolean canIgnoreError(Exception error) {
        return error instanceof TargetCloseException || (StringUtil.isNotEmpty(error.getMessage()) && (error.getMessage().contains("Not supported") || error.getMessage().contains("wasn't found")));
    }

    public void addClient(CDPSession client) {
        if (!this.networkEnabled || this.clients.containsKey(client)) {
            return;
        }
        Map<ConnectionEvents, Consumer<?>> listeners = new HashMap<>();
        Consumer<RequestPausedEvent> requestPaused = event -> this.onRequestPaused(client, event);
        client.on(ConnectionEvents.Fetch_requestPaused, requestPaused);
        listeners.put(ConnectionEvents.Fetch_requestPaused, requestPaused);

        Consumer<AuthRequiredEvent> authRequired = event -> this.onAuthRequired(client, event);
        client.on(ConnectionEvents.Fetch_authRequired, authRequired);
        listeners.put(ConnectionEvents.Fetch_authRequired, authRequired);

        Consumer<RequestWillBeSentEvent> requestWillBeSent = event -> this.onRequestWillBeSent(client, event);
        client.on(ConnectionEvents.Network_requestWillBeSent, requestWillBeSent);
        listeners.put(ConnectionEvents.Network_requestWillBeSent, requestWillBeSent);

        Consumer<RequestWillBeSentExtraInfoEvent> requestWillBeSentExtraInfo = event -> this.onRequestWillBeSentExtraInfo(client, event);
        client.on(ConnectionEvents.Network_requestWillBeSentExtraInfo, requestWillBeSentExtraInfo);
        listeners.put(ConnectionEvents.Network_requestWillBeSentExtraInfo, requestWillBeSentExtraInfo);

        Consumer<RequestServedFromCacheEvent> requestServedFromCache = event -> this.onRequestServedFromCache(client, event);
        client.on(ConnectionEvents.Network_requestServedFromCache, requestServedFromCache);
        listeners.put(ConnectionEvents.Network_requestServedFromCache, requestServedFromCache);

        Consumer<ResponseReceivedEvent> responseReceived = event -> this.onResponseReceived(client, event);
        client.on(ConnectionEvents.Network_responseReceived, responseReceived);
        listeners.put(ConnectionEvents.Network_responseReceived, responseReceived);

        Consumer<LoadingFinishedEvent> loadingFinished = event -> this.onLoadingFinished(client, event);
        client.on(ConnectionEvents.Network_loadingFinished, loadingFinished);
        listeners.put(ConnectionEvents.Network_loadingFinished, loadingFinished);

        Consumer<LoadingFailedEvent> loadingFailed = event -> this.onLoadingFailed(client, event);
        client.on(ConnectionEvents.Network_loadingFailed, loadingFailed);
        listeners.put(ConnectionEvents.Network_loadingFailed, loadingFailed);

        Consumer<ResponseReceivedExtraInfoEvent> responseReceivedExtraInfo = event -> this.onResponseReceivedExtraInfo(client, event);
        client.on(ConnectionEvents.Network_responseReceivedExtraInfo, responseReceivedExtraInfo);
        listeners.put(ConnectionEvents.Network_responseReceivedExtraInfo, responseReceivedExtraInfo);

        Consumer<Object> disconnected = (ignore) -> this.removeClient(client);
        client.on(ConnectionEvents.CDPSession_Disconnected, disconnected);
        listeners.put(ConnectionEvents.CDPSession_Disconnected, disconnected);

        try {
            this.clients.put(client, listeners);
            client.send("Network.enable");
            this.applyExtraHTTPHeaders(client);
            this.applyNetworkConditions(client);
            this.applyProtocolCacheDisabled(client);
            this.applyProtocolRequestInterception(client);
            this.applyUserAgent(client);
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }

    }

    public void removeClient(CDPSession client) {
        Map<ConnectionEvents, Consumer<?>> listeners = this.clients.remove(client);
        if (Objects.nonNull(listeners)) {//取消监听
            listeners.forEach(client::off);
        }
    }

    public void authenticate(Credentials credentials) {
        this.credentials = credentials;
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (Objects.equals(enabled, this.protocolRequestInterceptionEnabled))
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
        if (Objects.isNull(this.protocolRequestInterceptionEnabled)) {
            return;
        }
        if (this.userCacheDisabled == null) {
            this.userCacheDisabled = false;
        }
        try {
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
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }
    }

    public Map<String, String> extraHTTPHeaders() {
        return Objects.isNull(this.extraHTTPHeaders) ? new HashMap<>() : this.extraHTTPHeaders;
    }

    private void applyProtocolCacheDisabled(CDPSession client) {
        if (this.userCacheDisabled == null) {
            return;
        }
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("cacheDisabled", this.userCacheDisabled);
            client.send("Network.setCacheDisabled", params);
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }
    }

    private void applyExtraHTTPHeaders(CDPSession client) {
        if (this.extraHTTPHeaders == null) {
            return;
        }
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("headers", this.extraHTTPHeaders);
            client.send("Network.setExtraHTTPHeaders", params);
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }
    }

    private void applyNetworkConditions(CDPSession client) {
        if (this.emulatedNetworkConditions == null) {
            return;
        }
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("offline", this.emulatedNetworkConditions.getOffline());
            params.put("latency", this.emulatedNetworkConditions.getLatency());
            params.put("uploadThroughput", this.emulatedNetworkConditions.getUpload());
            params.put("downloadThroughput", this.emulatedNetworkConditions.getDownload());
            client.send("Network.emulateNetworkConditions", params);
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }
    }

    private void applyUserAgent(CDPSession client) {
        if (this.userAgent == null) {
            return;
        }
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("userAgent", this.userAgent);
            params.put("userAgentMetadata", this.userAgentMetadata);
            params.put("platform", this.platform);
            client.send("Network.setUserAgentOverride", params);
        } catch (Exception e) {
            if (canIgnoreError(e)) {
                return;
            }
            throwError(e);
        }
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

    public void setUserAgent(UserAgentOptions options) {
        this.userAgent = options.getUserAgent();
        this.userAgentMetadata = options.getUserAgentMetadata();
        this.platform = options.getPlatform();
        this.clients.forEach((client1, disposables) -> this.applyUserAgent(client1));
    }

    public void setCacheEnabled(boolean enabled) {
        this.userCacheDisabled = !enabled;
        this.clients.forEach((client1, disposables) -> this.applyProtocolCacheDisabled(client1));
    }

    public void setRequestInterception(boolean value) {
        this.userRequestInterceptionEnabled = value;
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (Objects.equals(enabled, this.protocolRequestInterceptionEnabled))
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.clients.forEach((client1, disposables) -> this.applyProtocolRequestInterception(client1));
    }

    public void onRequestWillBeSent(CDPSession client, RequestWillBeSentEvent event) {
        // Request interception doesn't happen for data URLs with Network Service.
        if (Objects.nonNull(this.protocolRequestInterceptionEnabled) && this.protocolRequestInterceptionEnabled && !event.getRequest().getUrl().startsWith("data:")) {
            String networkRequestId = event.getRequestId();
            this.networkEventManager.storeRequestWillBeSent(networkRequestId, event);
            RequestPausedEvent requestPausedEvent = this.networkEventManager.getRequestPaused(networkRequestId);
            if (Objects.nonNull(requestPausedEvent)) {
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
        if (!this.userRequestInterceptionEnabled && Objects.nonNull(this.protocolRequestInterceptionEnabled) && this.protocolRequestInterceptionEnabled) {
            try {
                Map<String, Object> params = ParamsFactory.create();
                params.put("requestId", event.getRequestId());
                client.send("Fetch.continueRequest", params);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error");
            }
        }
        String networkRequestId = event.getNetworkId();
        String fetchRequestId = event.getRequestId();
        if (StringUtil.isEmpty(networkRequestId)) {
            this.onRequestWithoutNetworkInstrumentation(client, event);
            return;
        }
        RequestWillBeSentEvent requestWillBeSentEvent = this.networkEventManager.getRequestWillBeSent(networkRequestId);
        if (Objects.nonNull(requestWillBeSentEvent) && (!Objects.equals(requestWillBeSentEvent.getRequest().getUrl(), event.getRequest().getUrl()) || !Objects.equals(requestWillBeSentEvent.getRequest().getMethod(), event.getRequest().getMethod()))) {
            this.networkEventManager.forgetRequestWillBeSent(networkRequestId);
            requestWillBeSentEvent = null;
        }
        if (requestWillBeSentEvent != null) {
            this.patchRequestEventHeaders(requestWillBeSentEvent, event);
            this.onRequest(client, requestWillBeSentEvent, fetchRequestId, false);
        } else {
            this.networkEventManager.storeRequestPaused(networkRequestId, event);
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
        CdpRequest request = new CdpRequest(client, frame, event.getRequestId(), this.userRequestInterceptionEnabled, requestWillBeSent, new ArrayList<>());
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
        if (Objects.nonNull(redirectInfo)) {
            this.networkEventManager.responseExtraInfo(event.getRequestId()).offer(event);
            this.onRequest(client, redirectInfo.getEvent(), redirectInfo.getFetchRequestId(), false);
            return;
        }
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (Objects.nonNull(queuedEvents)) {
            this.networkEventManager.forgetQueuedEventGroup(event.getRequestId());
            this.emitResponseEvent(client, queuedEvents.getResponseReceivedEvent(), event);
            if (Objects.nonNull(queuedEvents.getLoadingFinishedEvent())) {
                this.emitLoadingFinished(client, queuedEvents.getLoadingFinishedEvent());
            }
            if (Objects.nonNull(queuedEvents.getLoadingFailedEvent())) {
                this.emitLoadingFailed(client, queuedEvents.getLoadingFailedEvent());
            }
            return;
        }
        this.networkEventManager.responseExtraInfo(event.getRequestId()).offer(event);
    }

    private void emitLoadingFailed(CDPSession client, LoadingFailedEvent event) {
        CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
        if (request == null) {
            return;
        }
        this.adoptCdpSessionIfNeeded(client, request);
        request.setFailureText(event.getErrorText());
        CdpResponse response = request.response();
        if (Objects.nonNull(response)) {
            response.resolveBody(null);
        }
        this.forgetRequest(request, true);
        this.emit(NetworkManagerEvent.RequestFailed, request);
    }

    private void emitLoadingFinished(CDPSession client, LoadingFinishedEvent event) {
        CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
        if (request == null) {
            return;
        }
        this.adoptCdpSessionIfNeeded(client, request);
        if (Objects.nonNull(request.response())) {
            request.response().resolveBody(null);
        }
        this.forgetRequest(request, true);
        this.emit(NetworkManagerEvent.RequestFinished, request);
    }

    private void adoptCdpSessionIfNeeded(CDPSession client, CdpRequest request) {
        // Document requests for OOPIFs start in the parent frame but are
        // adopted by their child frame, meaning their loadingFinished and
        // loadingFailed events are fired on the child session. In this case
        // we reassign the request CDPSession to ensure all subsequent
        // actions use the correct session (e.g. retrieving response body in
        // HTTPResponse). The same applies to main worker script requests.
        if (client != request.client()) {
            request.setClient(client);
        }
    }


    private void onRequest(CDPSession client, RequestWillBeSentEvent event, String fetchRequestId, boolean fromMemoryCache) {
        List<Request> redirectChain = new ArrayList<>();
        if (Objects.nonNull(event.getRedirectResponse())) {
            // We want to emit a response and requestfinished for the
            // redirectResponse, but we can't do so unless we have a
            // responseExtraInfo ready to pair it up with. If we don't have any
            // responseExtraInfos saved in our queue, they we have to wait until
            // the next one to emit response and requestfinished, *and* we should
            // also wait to emit this Request too because it should come after the
            // response/requestfinished.
            ResponseReceivedExtraInfoEvent redirectResponseExtraInfo = null;
            if (event.getRedirectHasExtraInfo()) {
                redirectResponseExtraInfo = this.networkEventManager.responseExtraInfo(event.getRequestId()).poll();
                if (Objects.isNull(redirectResponseExtraInfo)) {
                    RedirectInfo redirectInfo = new RedirectInfo();
                    redirectInfo.setEvent(event);
                    redirectInfo.setFetchRequestId(fetchRequestId);
                    this.networkEventManager.queueRedirectInfo(event.getRequestId(), redirectInfo);
                    return;
                }
            }
            CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
            if (Objects.nonNull(request)) {
                this.handleRequestRedirect(client, request, event.getRedirectResponse(), redirectResponseExtraInfo);
                redirectChain = request.redirectChain();
                RequestWillBeSentExtraInfoEvent extraInfo = this.networkEventManager.requestExtraInfo(event.getRequestId()).poll();
                if (Objects.nonNull(extraInfo)) {
                    request.updateHeaders(extraInfo.getHeaders());
                }
            }
        }
        String frameId = event.getFrameId();
        Frame frame = null;
        if (StringUtil.isNotEmpty(frameId)) {
            frame = this.frameManager.frame(frameId);
        }
        CdpRequest request = new CdpRequest(client, frame, fetchRequestId, this.userRequestInterceptionEnabled, event, redirectChain);
        request.setFromMemoryCache(fromMemoryCache);
        this.networkEventManager.storeRequest(event.getRequestId(), request);
        this.emit(NetworkManagerEvent.Request, request);
        request.finalizeInterceptions();
    }

    public void onRequestWillBeSentExtraInfo(CDPSession client, RequestWillBeSentExtraInfoEvent event) {
        CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
        if (Objects.nonNull(request)) {
            request.updateHeaders(event.getHeaders());
        } else {
            this.networkEventManager.requestExtraInfo(event.getRequestId()).add(event);
        }
    }

    private void handleRequestRedirect(CDPSession _client, CdpRequest request, ResponsePayload responsePayload, ResponseReceivedExtraInfoEvent extraInfo) {
        CdpResponse response = new CdpResponse(request, responsePayload, extraInfo);
        request.setResponse(response);
        request.redirectChain().add(request);
        response.resolveBody("Response body is unavailable for redirect responses，request url: " + request.url());
        this.forgetRequest(request, false);
        this.emit(NetworkManagerEvent.Response, response);
        this.emit(NetworkManagerEvent.RequestFinished, request);
    }

    private void forgetRequest(CdpRequest request, boolean events) {
        String requestId = request.id();
        String interceptionId = request.interceptionId();
        this.networkEventManager.forgetRequest(requestId);
        if (StringUtil.isNotEmpty(interceptionId)) {
            this.attemptedAuthentications.remove(interceptionId);
        }
        if (events) {
            this.networkEventManager.forget(requestId);
        }
    }

    public void onLoadingFinished(CDPSession client, LoadingFinishedEvent event) {
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (Objects.nonNull(queuedEvents)) {
            queuedEvents.setLoadingFinishedEvent(event);
        } else {
            this.emitLoadingFinished(client, event);
        }
    }

    private void onResponseReceived(CDPSession client, ResponseReceivedEvent event) {
        CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
        ResponseReceivedExtraInfoEvent extraInfo = null;
        if (request != null && !request.fromMemoryCache() && event.getHasExtraInfo()) {
            extraInfo = this.networkEventManager.responseExtraInfo(event.getRequestId()).poll();
            if (Objects.isNull(extraInfo)) {
                QueuedEventGroup group = new QueuedEventGroup();
                group.setResponseReceivedEvent(event);
                this.networkEventManager.queueEventGroup(event.getRequestId(), group);
                return;
            }
        }
        this.emitResponseEvent(client, event, extraInfo);
    }

    public void onLoadingFailed(CDPSession client, LoadingFailedEvent event) {
        QueuedEventGroup queuedEvents = this.networkEventManager.getQueuedEventGroup(event.getRequestId());
        if (queuedEvents != null) {
            queuedEvents.setLoadingFailedEvent(event);
        } else {
            this.emitLoadingFailed(client, event);
        }
    }

    public void onRequestServedFromCache(CDPSession client, RequestServedFromCacheEvent event) {
        RequestWillBeSentEvent requestWillBeSentEvent = this.networkEventManager.getRequestWillBeSent(event.getRequestId());
        CdpRequest request = this.networkEventManager.getRequest(event.getRequestId());
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
        CdpRequest request = this.networkEventManager.getRequest(responseReceived.getRequestId());
        if (Objects.isNull(request)) {
            return;
        }
        List<ResponseReceivedExtraInfoEvent> extraInfos = this.networkEventManager.responseExtraInfo(responseReceived.getRequestId());
        if (ValidateUtil.isNotEmpty(extraInfos)) {
            LOGGER.warn("Unexpected extraInfo events for request {}", responseReceived.getRequestId());
        }
        if (responseReceived.getResponse().getFromDiskCache()) {
            extraInfo = null;
        }
        CdpResponse response = new CdpResponse(request, responseReceived.getResponse(), extraInfo);
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
