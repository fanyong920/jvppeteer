package com.ruiyun.jvppeteer.types.page.payload;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.impl.EventEmitter;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.types.page.frame.Frame;
import com.ruiyun.jvppeteer.types.page.frame.FrameManager;
import com.ruiyun.jvppeteer.types.page.frame.Request;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private Map<String, Request> requestIdToRequest;

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
        DefaultBrowserListener<RequestPausedPayload>  requestPausedListener = new DefaultBrowserListener<RequestPausedPayload>() {
            @Override
            public void onBrowserEvent(RequestPausedPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onRequestPaused(event);
            }
        };
        requestPausedListener.setMothod("Fetch.requestPaused");
        requestPausedListener.setTarget(this);
        this.client.on(requestPausedListener.getMothod(),requestPausedListener);

        DefaultBrowserListener<AuthRequiredPayload>  authRequiredListener = new DefaultBrowserListener<AuthRequiredPayload>() {
            @Override
            public void onBrowserEvent(AuthRequiredPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onAuthRequired(event);
            }
        };
        authRequiredListener.setMothod("Fetch.authRequired");
        authRequiredListener.setTarget(this);
        this.client.on(authRequiredListener.getMothod(),authRequiredListener);

        DefaultBrowserListener<RequestWillBeSentPayload>  requestWillBeSentListener = new DefaultBrowserListener<RequestWillBeSentPayload>() {
            @Override
            public void onBrowserEvent(RequestWillBeSentPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onRequestWillBeSent(event);
            }
        };
        requestWillBeSentListener.setMothod("Network.requestWillBeSent");
        requestWillBeSentListener.setTarget(this);
        this.client.on(requestWillBeSentListener.getMothod(),requestWillBeSentListener);

        DefaultBrowserListener<RequestServedFromCachePayload>  requestServedFromCacheListener = new DefaultBrowserListener<RequestServedFromCachePayload>() {
            @Override
            public void onBrowserEvent(RequestServedFromCachePayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onRequestServedFromCache(event);
            }
        };
        requestServedFromCacheListener.setMothod("Network.requestServedFromCache");
        requestServedFromCacheListener.setTarget(this);
        this.client.on(requestServedFromCacheListener.getMothod(),requestServedFromCacheListener);

        DefaultBrowserListener<ResponseReceivedPayload>  responseReceivedListener = new DefaultBrowserListener<ResponseReceivedPayload>() {
            @Override
            public void onBrowserEvent(ResponseReceivedPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onResponseReceived(event);
            }
        };
        responseReceivedListener.setMothod("Network.responseReceived");
        responseReceivedListener.setTarget(this);
        this.client.on(responseReceivedListener.getMothod(),responseReceivedListener);

        DefaultBrowserListener<LoadingFinishedPayload>  loadingFinishedListener = new DefaultBrowserListener<LoadingFinishedPayload>() {
            @Override
            public void onBrowserEvent(LoadingFinishedPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onLoadingFinished(event);
            }
        };
        loadingFinishedListener.setMothod("Network.loadingFinished");
        loadingFinishedListener.setTarget(this);
        this.client.on(loadingFinishedListener.getMothod(),loadingFinishedListener);

        DefaultBrowserListener<LoadingFailedPayload>  loadingFailedListener = new DefaultBrowserListener<LoadingFailedPayload>() {
            @Override
            public void onBrowserEvent(LoadingFailedPayload event) {
                NetworkManager manager =  (NetworkManager)this.getTarget();
                manager.onLoadingFailed(event);
            }
        };
        loadingFailedListener.setMothod("Network.loadingFailed");
        loadingFailedListener.setTarget(this);
        this.client.on(loadingFailedListener.getMothod(),loadingFailedListener);

    }

    public void setExtraHTTPHeaders(Map<String,Object> extraHTTPHeaders) {
        this.extraHTTPHeaders = new HashMap<>();
        for(Map.Entry<String,Object> entry:extraHTTPHeaders.entrySet()){

            Object value = entry.getValue();
            ValidateUtil.assertBoolean(Helper.isString(value), "Expected value of header "+entry.getKey()+" to be String, but "+value.getClass().getCanonicalName()+" is found.");
            this.extraHTTPHeaders.put(entry.getKey(),(String)value);
        }
        Map<String,Object> params = new HashMap<>();
        params.put("headers",this.extraHTTPHeaders);
         this.client.send("Network.setExtraHTTPHeaders", params,true);
    }

    public void initialize() {
         this.client.send("Network.enable",null,true);
        if (this.ignoreHTTPSErrors){
            Map<String,Object> params = new HashMap<>();
            params.put("ignore",true);
            this.client.send("Security.setIgnoreCertificateErrors", params,true);
        }

    }

    public void authenticate(Credentials credentials) {
        this.credentials = credentials;
        this.updateProtocolRequestInterception();
    }

    public Map<String,String> extraHTTPHeaders(){
        return new HashMap<>(this.extraHTTPHeaders);
    }

    public void setOfflineMode(boolean value) {
        if (this.offline == value)
            return;
        this.offline = value;
        Map<String,Object> params = new HashMap<>();
        params.put("offline",this.offline);
        // values of 0 remove any active throttling. crbug.com/456324#c9
        params.put("latency",0);
        params.put("downloadThroughput",-1);
        params.put("uploadThroughput",-1);
         this.client.send("Network.emulateNetworkConditions", params,true);
    }
    public void setUserAgent(String userAgent) {
        Map<String,Object> params = new HashMap<>();
        params.put("userAgent",userAgent);
        this.client.send("Network.setUserAgentOverride", params,true);
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
        Map<String,Object> params = new HashMap<>();
        boolean cacheDisabled = this.userCacheDisabled || this.protocolRequestInterceptionEnabled;
        params.put("cacheDisabled",cacheDisabled);
        this.client.send("Network.setCacheDisabled", params,false);
    }

    public void updateProtocolRequestInterception() {
        boolean enabled = false;
        if(this.userRequestInterceptionEnabled || this.credentials != null){
            enabled = true;
        }
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        if (enabled) {
            this.updateProtocolCacheDisabled();
            Map<String,Object> params = new HashMap<>();
            params.put("handleAuthRequests",true);
            List<String> patterns = new ArrayList<>();
            patterns.add("{urlPattern: \"*\"}");
            params.put("patterns",patterns);
            this.client.send("Fetch.enable", params,true);
        } else {
            this.updateProtocolCacheDisabled();
            this.client.send("Fetch.disable",null,true);
        }
    }
   public void  onRequestWillBeSent(RequestWillBeSentPayload event) {
        // Request interception doesn't happen for data URLs with Network Service.
        if (this.protocolRequestInterceptionEnabled && !event.getRequest().getUrl().startsWith("data:")) {
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

    public void onAuthRequired(AuthRequiredPayload event) {
        /* @type {"Default"|"CancelAuth"|"ProvideCredentials"} */
        String  response = "Default";
        if (this.attemptedAuthentications.contains(event.getRequestId())) {
            response = "CancelAuth";
        } else if (this.credentials != null) {
            response = "ProvideCredentials";
            this.attemptedAuthentications.add(event.getRequestId());
        }
        String  username ,password = null;
        ObjectNode respParams = null;
        if(this.credentials != null){
            respParams = Constant.OBJECTMAPPER.createObjectNode();
            if(StringUtil.isNotEmpty(username = credentials.getUsername())){
                respParams.put("username",username);
            }
            if(StringUtil.isNotEmpty(password = credentials.getPassword())){
                respParams.put("password",password);
            }
        }
        Map<String,Object> params = new HashMap<>();
        params.put("response","Default");
        params.put("requestId",event.getRequestId());
        if(respParams != null)
        params.put("authChallengeResponse",respParams.toString());
        this.client.send("Fetch.continueWithAuth",params,false);
    }

    public void onRequestPaused(RequestPausedPayload event) {
        if (!this.userRequestInterceptionEnabled && this.protocolRequestInterceptionEnabled) {
            Map<String,Object> params = new HashMap<>();
            params.put("requestId",event.getRequestId());
            this.client.send("Fetch.continueRequest", params,false);
        }

        String requestId = event.getNetworkId();
        String interceptionId = event.getRequestId();
        if (StringUtil.isNotEmpty(requestId) && this.requestIdToRequestWillBeSentEvent.containsKey(requestId)) {
      RequestWillBeSentPayload requestWillBeSentEvent = this.requestIdToRequestWillBeSentEvent.get(requestId);
            this.onRequest(requestWillBeSentEvent, interceptionId);
            this.requestIdToRequestWillBeSentEvent.remove(requestId);
        } else {
            this.requestIdToInterceptionId.put(requestId, interceptionId);
        }
    }

    public void onRequest(RequestWillBeSentPayload event, String interceptionId) {
        List<Request> redirectChain = new ArrayList<>();
        if (event.getRedirectResponse() != null) {
            Request request = this.requestIdToRequest.get(event.getRequestId());
            // If we connect late to the target, we could have missed the requestWillBeSent event.
            if (request != null) {
                this.handleRequestRedirect(request, event.getRedirectResponse());
                redirectChain = request.getRedirectChain();
            }
        }
        Frame frame = StringUtil.isNotEmpty(event.getFrameId())? this.frameManager.getFrame(event.getFrameId()) : null;
        Request request = new Request(this.client, frame, interceptionId, this.userRequestInterceptionEnabled, event, redirectChain);
        this.requestIdToRequest.put(event.getRequestId(), request);
        this.emit(Events.NETWORK_MANAGER_REQUEST.getName(), request);
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

    public void  onLoadingFinished(LoadingFinishedPayload event) {
    Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;

        // Under certain conditions we never get the Network.responseReceived
        // event from protocol. @see https://crbug.com/883475
        if (request.response() != null)
            request.response().bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.getRequestId());
        this.attemptedAuthentications.remove(request.getInterceptionId());
        this.emit(Events.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

    public void onResponseReceived(ResponseReceivedPayload event) {
    Request request = this.requestIdToRequest.get(event.getRequestId());
        // FileUpload sends a response without a matching request.
        if (request == null)
            return;
        Response response = new Response(this.client, request, event.getResponse());
        request.setResponse(response);
        this.emit(Events.NETWORK_MANAGER_RESPONSE.getName(),response);
    }

    public void onLoadingFailed(LoadingFailedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;
        request.setFailureText(event.getErrorText()) ;
        Response response = request.response();
        if (response != null)
            response.bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.getRequestId());
        this.attemptedAuthentications.remove(request.getInterceptionId());
        this.emit(Events.NETWORK_MANAGER_REQUEST_FAILED.getName(), request);
    }

    public void onRequestServedFromCache(RequestServedFromCachePayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request != null)
            request.setFromMemoryCache(true);
    }
}
