package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.protocol.fetch.AuthRequiredEvent;
import com.ruiyun.jvppeteer.protocol.fetch.RequestPausedEvent;
import com.ruiyun.jvppeteer.protocol.network.LoadingFailedEvent;
import com.ruiyun.jvppeteer.protocol.network.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.protocol.network.RequestServedFromCacheEvent;
import com.ruiyun.jvppeteer.protocol.network.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.protocol.network.ResponsePayload;
import com.ruiyun.jvppeteer.protocol.network.ResponseReceivedEvent;
import com.ruiyun.jvppeteer.protocol.webAuthn.Credentials;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class NetworkManager extends EventEmitter<NetworkManager.NetworkManagerEvent> {

    /**
     * cdpsession
     */
    private final CDPSession client;

    private final boolean ignoreHTTPSErrors;

    private final FrameManager frameManager;

    private Map<String, String> extraHTTPHeaders;

    private final Map<String, Request> requestIdToRequest;

    private final Map<String, RequestWillBeSentEvent> requestIdToRequestWillBeSentEvent;

    private boolean offline;

    private Credentials credentials;

    private final Set<String> attemptedAuthentications;

    private boolean userRequestInterceptionEnabled;

    private boolean protocolRequestInterceptionEnabled;

    private boolean userCacheDisabled;

    private final Map<String, String> requestIdToInterceptionId;

    public NetworkManager(CDPSession client, boolean ignoreHTTPSErrors, FrameManager frameManager) {
        this.client = client;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.frameManager = frameManager;
        this.requestIdToRequest = new HashMap<>();
        this.requestIdToRequestWillBeSentEvent = new HashMap<>();
        this.extraHTTPHeaders = new HashMap<>();
        this.offline = false;
        this.credentials = null;
        this.attemptedAuthentications = new HashSet<>();
        this.userRequestInterceptionEnabled = false;
        this.protocolRequestInterceptionEnabled = false;
        this.userCacheDisabled = false;
        this.requestIdToInterceptionId = new HashMap<>();
        this.client.on(CDPSession.CDPSessionEvent.Fetch_requestPaused,(Consumer<RequestPausedEvent>) this::onRequestPaused);
        this.client.on(CDPSession.CDPSessionEvent.Fetch_authRequired, (Consumer<AuthRequiredEvent>) this::onAuthRequired);
        this.client.on(CDPSession.CDPSessionEvent.Network_requestWillBeSent, (Consumer<RequestWillBeSentEvent>) this::onRequestWillBeSent);
        this.client.on(CDPSession.CDPSessionEvent.Network_requestServedFromCache, (Consumer<RequestServedFromCacheEvent>) this::onRequestServedFromCache);
        this.client.on(CDPSession.CDPSessionEvent.Network_responseReceived, (Consumer<ResponseReceivedEvent>) this::onResponseReceived);
        this.client.on(CDPSession.CDPSessionEvent.Network_loadingFinished, (Consumer<LoadingFinishedEvent>) this::onLoadingFinished);
        this.client.on(CDPSession.CDPSessionEvent.Network_loadingFailed, (Consumer<LoadingFailedEvent>) this::onLoadingFailed);
    }

    public void setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
        this.extraHTTPHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : extraHTTPHeaders.entrySet()) {
            String value = entry.getValue();
            ValidateUtil.assertArg(Helper.isString(value), "Expected value of header " + entry.getKey() + " to be String, but " + value.getClass().getCanonicalName() + " is found.");
            this.extraHTTPHeaders.put(entry.getKey(), value);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("headers", this.extraHTTPHeaders);
        this.client.send("Network.setExtraHTTPHeaders", params);
    }

    public void initialize() {
        this.client.send("Network.enable");
        if (this.ignoreHTTPSErrors) {
            Map<String, Object> params = new HashMap<>();
            params.put("ignore", true);
            this.client.send("Security.setIgnoreCertificateErrors", params);
        }

    }

    public void authenticate(Credentials credentials) {
        this.credentials = credentials;
        this.updateProtocolRequestInterception();
    }

    public Map<String, String> extraHTTPHeaders() {
        return new HashMap<>(this.extraHTTPHeaders);
    }

    public void setOfflineMode(boolean value) {
        if (this.offline == value)
            return;
        this.offline = value;
        Map<String, Object> params = new HashMap<>();
        params.put("offline", this.offline);
        // values of 0 remove any active throttling. crbug.com/456324#c9
        params.put("latency", 0);
        params.put("downloadThroughput", -1);
        params.put("uploadThroughput", -1);
        this.client.send("Network.emulateNetworkConditions", params);
    }

    public void setUserAgent(String userAgent) {
        Map<String, Object> params = new HashMap<>();
        params.put("userAgent", userAgent);
        this.client.send("Network.setUserAgentOverride", params);
    }

    public void setCacheEnabled(boolean enabled) {
        this.userCacheDisabled = !enabled;
        this.updateProtocolCacheDisabled();
    }

    public void setRequestInterception(boolean value) {
        this.userRequestInterceptionEnabled = value;
        this.updateProtocolRequestInterception();
    }

    private void updateProtocolCacheDisabled() {
        Map<String, Object> params = new HashMap<>();
        boolean cacheDisabled = this.userCacheDisabled || this.protocolRequestInterceptionEnabled;
        params.put("cacheDisabled", cacheDisabled);
        this.client.send("Network.setCacheDisabled", params);
    }

    public void updateProtocolRequestInterception() {
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.updateProtocolCacheDisabled();
        if (enabled) {
            Map<String, Object> params = new HashMap<>();
            params.put("handleAuthRequests", true);
            List<Object> patterns = new ArrayList<>();
            patterns.add(Constant.OBJECTMAPPER.createObjectNode().put("urlPattern", "*"));
            params.put("patterns", patterns);
            this.client.send("Fetch.enable", params);
        } else {
            this.client.send("Fetch.disable");
        }
    }

    public void onRequestWillBeSent(RequestWillBeSentEvent event) {
        // Request interception doesn't happen for data URLs with Network Service.
        if (this.protocolRequestInterceptionEnabled && !event.getRequest().url().startsWith("data:")) {
            String requestId = event.getRequestId();
            String interceptionId = this.requestIdToInterceptionId.get(requestId);
            if (StringUtil.isNotEmpty(interceptionId)) {
                this.onRequest(event, interceptionId);
                this.requestIdToInterceptionId.remove(requestId);
            } else {
                this.requestIdToRequestWillBeSentEvent.put(event.getRequestId(), event);
            }
            return;
        }
        this.onRequest(event, null);
    }

    public void onAuthRequired(AuthRequiredEvent event) {
        /* @type {"Default"|"CancelAuth"|"ProvideCredentials"} */
        String response = "Default";
        if (this.attemptedAuthentications.contains(event.getRequestId())) {
            response = "CancelAuth";
        } else if (this.credentials != null) {
            response = "ProvideCredentials";
            this.attemptedAuthentications.add(event.getRequestId());
        }
        String username, password;
        ObjectNode respParams = Constant.OBJECTMAPPER.createObjectNode();
        respParams.put("response",response);
        if (this.credentials != null) {
            if (StringUtil.isNotEmpty(username = credentials.getUsername())) {
                respParams.put("username", username);
            }
            if (StringUtil.isNotEmpty(password = credentials.getPassword())) {
                respParams.put("password", password);
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("response", "Default");
        params.put("requestId", event.getRequestId());
        params.put("authChallengeResponse", respParams);
        this.client.send("Fetch.continueWithAuth", params, null,false);
    }

    public void onRequestPaused(RequestPausedEvent event) {
        if (!this.userRequestInterceptionEnabled && this.protocolRequestInterceptionEnabled) {
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", event.getRequestId());
            this.client.send("Fetch.continueRequest", params, null,false);
        }

        String requestId = event.getNetworkId();
        String interceptionId = event.getRequestId();
        if (StringUtil.isNotEmpty(requestId) && this.requestIdToRequestWillBeSentEvent.containsKey(requestId)) {
            RequestWillBeSentEvent requestWillBeSentEvent = this.requestIdToRequestWillBeSentEvent.get(requestId);
            this.onRequest(requestWillBeSentEvent, interceptionId);
            this.requestIdToRequestWillBeSentEvent.remove(requestId);
        } else {
            this.requestIdToInterceptionId.put(requestId, interceptionId);
        }
    }

    public void onRequest(RequestWillBeSentEvent event, String interceptionId) {
        List<Request> redirectChain = new ArrayList<>();
        if (event.getRedirectResponse() != null) {
            Request request = this.requestIdToRequest.get(event.getRequestId());
            // If we connect late to the target, we could have missed the requestWillBeSent event.
            if (request != null) {
                this.handleRequestRedirect(request, event.getRedirectResponse());
                redirectChain = request.redirectChain();
            }
        }
        Frame frame = StringUtil.isNotEmpty(event.getFrameId()) ? this.frameManager.getFrame(event.getFrameId()) : null;
        Request request = new Request(this.client, frame, interceptionId, this.userRequestInterceptionEnabled, event, redirectChain);
        this.requestIdToRequest.put(event.getRequestId(), request);
        this.emit(NetworkManagerEvent.Request, request);
    }

    private void handleRequestRedirect(Request request, ResponsePayload responsePayload) {
        Response response = new Response(this.client, request, responsePayload);
        request.setResponse(response);
        request.redirectChain().add(request);
        response.resolveBody("Response body is unavailable for redirect responses");
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(NetworkManagerEvent.Response, response);
        this.emit(NetworkManagerEvent.RequestFinished, request);
    }

    public void onLoadingFinished(LoadingFinishedEvent event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;

        // Under certain conditions we never get the Network.responseReceived
        // event from protocol. @see https://crbug.com/883475
        if (request.response() != null)
            request.response().bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(NetworkManagerEvent.RequestFailed, request);
    }

    public void onResponseReceived(ResponseReceivedEvent event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // FileUpload sends a response without a matching request.
        if (request == null)
            return;
        Response response = new Response(this.client, request, event.getResponse());
        request.setResponse(response);
        this.emit(NetworkManagerEvent.Response, response);
    }

    public void onLoadingFailed(LoadingFailedEvent event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;
        request.setFailureText(event.getErrorText());
        Response response = request.response();
        if (response != null)
            response.bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(NetworkManagerEvent.RequestFailed, request);
    }

    public void onRequestServedFromCache(RequestServedFromCacheEvent event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request != null)
            request.setFromMemoryCache(true);
    }


    public enum NetworkManagerEvent{
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
