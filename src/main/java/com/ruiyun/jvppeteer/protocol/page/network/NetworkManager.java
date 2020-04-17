package com.ruiyun.jvppeteer.protocol.page.network;

import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.protocol.page.frame.FrameManager;
import com.ruiyun.jvppeteer.protocol.page.frame.Request;
import com.ruiyun.jvppeteer.protocol.page.payload.RequestWillBeSentPayload;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworkManager extends EventEmitter {

    /**
     * cdpsession
     */
    private CDPSession client;

    private boolean ignoreHTTPSErrors;

    private FrameManager frameManager;

    private Map<String,String> extraHTTPHeaders;

    private Map<String,Request> requestIdToRequest;

    private Map<String, RequestWillBeSentPayload> requestIdToRequestWillBeSentEvent;

    private boolean offline;

    private Credentials credentials;

    private Set<String> attemptedAuthentications;

    private boolean userRequestInterceptionEnabled;

    private boolean protocolRequestInterceptionEnabled;

    private boolean userCacheDisabled;

    private Map<String,String> requestIdToInterceptionId;

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

        //TODO
    }

    public Map<String,String> extraHTTPHeaders(){
        return new HashMap<>(this.extraHTTPHeaders);
    }

    private void handleRequestRedirect(Request request, ResponsePayload responsePayload) {
        Response response = new Response(this.client, request, responsePayload);
        request.setResponse(response);
        request.getRedirectChain().add(request);
        response.bodyLoadedPromiseFulfill(new RuntimeException("Response body is unavailable for redirect responses"));
        this.requestIdToRequest.remove(request.getRequestId());
        this.attemptedAuthentications.remove(request.getInterceptionId());
        this.emit(Events.NETWORK_MANAGER_RESPONSE.getName(), response);
        this.emit(Events.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

}
