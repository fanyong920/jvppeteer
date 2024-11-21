package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.ResourceType;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.entities.InterceptResolutionAction;
import com.ruiyun.jvppeteer.entities.InterceptResolutionState;
import com.ruiyun.jvppeteer.entities.Interception;
import com.ruiyun.jvppeteer.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.entities.HeaderEntry;
import com.ruiyun.jvppeteer.entities.ErrorReasons;
import com.ruiyun.jvppeteer.entities.Initiator;
import com.ruiyun.jvppeteer.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Whenever the page sends a request, such as for a network resource, the following events are emitted by puppeteer's page:
 * <p>
 * 'request' emitted when the request is issued by the page.
 * 'response' emitted when/if the response is received for the request.
 * 'requestfinished' emitted when the response body is downloaded and the request is complete.
 * If request fails at some point, then instead of 'requestfinished' event (and possibly instead of 'response' event), the 'requestfailed' event is emitted.
 * <p>
 * NOTE HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete with 'requestfinished' event.
 * <p>
 * If request gets a 'redirect' response, the request is successfully finished with the 'requestfinished' event, and a new request is issued to a redirected url.
 */
public class Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    private volatile String id;
    private List<Request> redirectChain;
    private Response response;
    private CDPSession client;
    private volatile boolean isNavigationRequest;
    private volatile String url;
    private volatile String interceptionId;
    private volatile ResourceType resourceType;
    private volatile String method;
    private volatile boolean hasPostData;
    private volatile String postData;
    private volatile Map<String, String> headers = new HashMap<>();
    private Frame frame;
    private volatile Initiator initiator;
    private final Interception interception = new Interception();

    private volatile String failureText;
    private static final Map<Integer, String> STATUS_TEXTS = new HashMap<>();
    private volatile boolean fromMemoryCache;

    static {
        // List taken from https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml with extra 306 and 418 codes.
        STATUS_TEXTS.put(100, "Continue");
        STATUS_TEXTS.put(101, "Switching Protocols");
        STATUS_TEXTS.put(102, "Processing");
        STATUS_TEXTS.put(103, "Early Hints");
        STATUS_TEXTS.put(200, "OK");
        STATUS_TEXTS.put(201, "Created");
        STATUS_TEXTS.put(202, "Accepted");
        STATUS_TEXTS.put(203, "Non-Authoritative Information");
        STATUS_TEXTS.put(204, "No Content");
        STATUS_TEXTS.put(205, "Reset Content");
        STATUS_TEXTS.put(206, "Partial Content");
        STATUS_TEXTS.put(207, "Multi-Status");
        STATUS_TEXTS.put(208, "Already Reported");
        STATUS_TEXTS.put(226, "IM Used");
        STATUS_TEXTS.put(300, "Multiple Choices");
        STATUS_TEXTS.put(301, "Moved Permanently");
        STATUS_TEXTS.put(302, "Found");
        STATUS_TEXTS.put(303, "See Other");
        STATUS_TEXTS.put(304, "Not Modified");
        STATUS_TEXTS.put(305, "Use Proxy");
        STATUS_TEXTS.put(306, "Switch Proxy");
        STATUS_TEXTS.put(307, "Temporary Redirect");
        STATUS_TEXTS.put(308, "Permanent Redirect");
        STATUS_TEXTS.put(400, "Bad Request");
        STATUS_TEXTS.put(401, "Unauthorized");
        STATUS_TEXTS.put(402, "Payment Required");
        STATUS_TEXTS.put(403, "Forbidden");
        STATUS_TEXTS.put(404, "Not Found");
        STATUS_TEXTS.put(405, "Method Not Allowed");
        STATUS_TEXTS.put(406, "Not Acceptable");
        STATUS_TEXTS.put(407, "Proxy Authentication Required");
        STATUS_TEXTS.put(408, "Request Timeout");
        STATUS_TEXTS.put(409, "Conflict");
        STATUS_TEXTS.put(410, "Gone");
        STATUS_TEXTS.put(411, "Length Required");
        STATUS_TEXTS.put(412, "Precondition Failed");
        STATUS_TEXTS.put(413, "Payload Too Large");
        STATUS_TEXTS.put(414, "URI Too Long");
        STATUS_TEXTS.put(415, "Unsupported Media Type");
        STATUS_TEXTS.put(416, "Range Not Satisfiable");
        STATUS_TEXTS.put(417, "Expectation Failed");
        STATUS_TEXTS.put(418, "I'm a teapot");
        STATUS_TEXTS.put(421, "Misdirected Request");
        STATUS_TEXTS.put(422, "Unprocessable Entity");
        STATUS_TEXTS.put(423, "Locked");
        STATUS_TEXTS.put(424, "Failed Dependency");
        STATUS_TEXTS.put(425, "Too Early");
        STATUS_TEXTS.put(426, "Upgrade Required");
        STATUS_TEXTS.put(428, "Precondition Required");
        STATUS_TEXTS.put(429, "Too Many Requests");
        STATUS_TEXTS.put(431, "Request Header Fields Too Large");
        STATUS_TEXTS.put(451, "Unavailable For Legal Reasons");
        STATUS_TEXTS.put(500, "Internal Server Error");
        STATUS_TEXTS.put(501, "Not Implemented");
        STATUS_TEXTS.put(502, "Bad Gateway");
        STATUS_TEXTS.put(503, "Service Unavailable");
        STATUS_TEXTS.put(504, "Gateway Timeout");
        STATUS_TEXTS.put(505, "HTTP Version Not Supported");
        STATUS_TEXTS.put(506, "Variant Also Negotiates");
        STATUS_TEXTS.put(507, "Insufficient Storage");
        STATUS_TEXTS.put(508, "Loop Detected");
        STATUS_TEXTS.put(510, "Not Extended");
        STATUS_TEXTS.put(511, "Network Authentication Required");
    }

    public Request() {
        super();
    }

    public Request(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentEvent event, List<Request> redirectChain) {
        super();
        this.client = client;
        this.id = event.getRequestId();
        if (event.getRequestId() != null) {
            if (event.getRequestId().equals(event.getLoaderId()) && "Document".equals(event.getType())) {
                this.isNavigationRequest = true;
            }
        } else {
            this.isNavigationRequest = false;
        }
        this.interceptionId = interceptionId;
        this.url = event.getRequest().getUrl();
        this.resourceType = StringUtil.isEmpty(event.getType()) ? ResourceType.Other : ResourceType.valueOf(event.getType());
        this.method = event.getRequest().getMethod();
        this.postData = event.getRequest().getPostData();
        this.hasPostData = event.getRequest().getHasPostData();
        this.frame = frame;
        this.redirectChain = redirectChain;
        this.initiator = event.getInitiator();
        this.interception.setEnabled(allowInterception);
        for (Map.Entry<String, String> entry : event.getRequest().getHeaders().entrySet()) {
            this.headers.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    public CDPSession client() {
        return this.client;
    }

    public String url() {
        return this.url;
    }

    public ResourceType resourceType() {
        return this.resourceType;
    }

    /**
     * 使用的方法（GET、POST 等）
     *
     * @return 方法
     */
    public String method() {
        return this.method;
    }

    /**
     * 请求体数据
     *
     * @return 请求体
     */
    public String postData() {
        return this.postData;
    }

    /**
     * 是否有请求体
     * <p>
     * 当请求有 POST 数据时为 true。
     * <p>
     * 请注意，当数据太长或不易以解码形式提供时，当此标志为真时，HTTPRequest.postData() 可能仍然未定义。
     * <p>
     * 在这种情况下，请使用 HTTPRequest.fetchPostData()。
     *
     * @return 是否有请求体
     */
    public boolean hasPostData() {
        return this.hasPostData;
    }

    /**
     * 从浏览器获取请求的 POST 数据。
     *
     * @return POST 数据
     */
    public String fetchPostData() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.id);
        JsonNode response = this.client.send("Network.getRequestPostData", params);
        return response.get("postData").asText();
    }

    /**
     * 具有与请求关联的 HTTP 标头的对象。所有标头名称均为小写。
     *
     * @return Map
     */
    public Map<String, String> headers() {
        return this.headers;
    }

    /**
     * 请求对应的响应
     *
     * @return Response
     */
    public Response response() {
        return this.response;
    }

    public Frame frame() {
        return frame;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * 如果请求是当前帧导航的驱动程序，则为 True。
     *
     * @return boolean
     */
    public boolean isNavigationRequest() {
        return isNavigationRequest;
    }

    /**
     * 请求的发起者。
     *
     * @return Initiator
     */
    public Initiator initiator() {
        return this.initiator;
    }

    /**
     * 重定向链
     * <p>
     * redirectChain 是为获取资源而发起的请求链。
     * </p>
     * 请求链 - 如果服务器至少响应一个重定向，则该链将包含所有重定向的请求。
     *
     * @return List
     */
    public List<Request> redirectChain() {
        return this.redirectChain;
    }

    /**
     * 访问有关请求失败的信息。
     *
     * @return 如果请求失败，则可以返回一个带有 errorText 的对象，其中包含人类可读的错误消息，例如 net::ERR_FAILED，不保证会有失败文本。<p>请求成功，返回null
     */
    public String failure() {
        return this.failureText;
    }

    /**
     * 拦截请求时，该方法表示放行请求
     *
     * @param overrides 重写请求的信息。
     */
    public void continueRequest(ContinueRequestOverrides overrides) {
        this.continueRequest(overrides, null);
    }

    /**
     * 拦截请求时，该方法表示放行请求
     *
     * @param priority  如果提供，则使用协作处理规则来解决拦截。否则，拦截将立即解决。
     * @param overrides 重写请求的信息。
     */
    public void continueRequest(ContinueRequestOverrides overrides, Integer priority) {
        if (!this.canBeIntercepted()) {
            return;
        }
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        ValidateUtil.assertArg(!this.interception.getHandled(), "Request is already handled!");
        if (priority == null) {
            this._continue(overrides);
            return;
        }
        this.interception.setRequestOverrides(overrides);
        if (this.interception.getResolutionState().getPriority() == null || priority > this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.CONTINUE, priority));
            return;
        }
        if (priority.equals(this.interception.getResolutionState().getPriority())) {
            if (this.interception.getResolutionState().getAction() == InterceptResolutionAction.ABORT || this.interception.getResolutionState().getAction() == InterceptResolutionAction.RESPOND) {
                return;
            }
            this.interception.getResolutionState().setAction(InterceptResolutionAction.CONTINUE);
        }
    }

    private void _continue(ContinueRequestOverrides overrides) {
        this.interception.setHandled(true);
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.interceptionId), "HTTPRequest is missing _interceptionId needed for Fetch.continueRequest");
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.interceptionId);
        params.put("url", overrides.getUrl());
        params.put("method", overrides.getMethod());
        if (StringUtil.isNotEmpty(overrides.getPostData())) {
            params.put("postData", new String(Base64.getEncoder().encode(overrides.getPostData().getBytes()), StandardCharsets.UTF_8));
        } else {
            params.put("postData", "");
        }
        params.put("headers", headersArray(overrides.getHeaders()));
        try {
            this.client.send("Fetch.continueRequest", params);
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        if (e instanceof ProtocolException) {
            boolean flag = e.getMessage().contains("Invalid header") || e.getMessage().contains("Expected \"header\"") || e.getMessage().contains("invalid argument");
            if (flag) {
                throw (ProtocolException) e;
            }
        }
        LOGGER.error("request error:", e);
    }

    /**
     * 使用给定的响应来满足请求。
     *
     * @param response 响应
     */
    public void respond(ResponseForRequest response) {
        this.respond(response, null);
    }

    public void respond(ResponseForRequest response, Integer priority) {
        if (!this.canBeIntercepted()) {
            return;
        }
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        ValidateUtil.assertArg(!this.interception.getHandled(), "Request is already handled!");
        if (priority == null) {
            this._respond(response);
            return;
        }
        this.interception.setResponse(response);
        if (this.interception.getResolutionState().getPriority() == null || priority > this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.RESPOND, priority));
            return;
        }
        if (priority.equals(this.interception.getResolutionState().getPriority())) {
            if (this.interception.getResolutionState().getAction() == InterceptResolutionAction.ABORT) {
                return;
            }
            this.interception.getResolutionState().setAction(InterceptResolutionAction.RESPOND);
        }
    }

    private void _respond(ResponseForRequest response) {
        this.interception.setHandled(true);
        String base64Body = null;
        int contentLength = 0;
        if (StringUtil.isNotEmpty(response.getBody())) {
            byte[] byteBody = response.getBody().getBytes(StandardCharsets.UTF_8);
            base64Body = Base64Util.encode(byteBody);
            contentLength = byteBody.length;
        }
        Map<String, String> responseHeaders = headers(response, base64Body, contentLength);
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.interceptionId), "HTTPRequest is missing _interceptionId needed for Fetch.fulfillRequest");
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.interceptionId);
        params.put("responseCode", response.getStatus());
        params.put("responsePhrase", STATUS_TEXTS.get(response.getStatus()));
        params.put("responseHeaders", headersArray(responseHeaders));
        if (base64Body != null) {
            params.put("body", base64Body);
        }
        try {
            this.client.send("Fetch.fulfillRequest", params);
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }

    private static Map<String, String> headers(ResponseForRequest response, String base64Body, int contentLength) {
        Map<String, String> responseHeaders = new HashMap<>();
        if (ValidateUtil.isNotEmpty(response.getHeaders())) {
            for (HeaderEntry header : response.getHeaders()) {
                responseHeaders.put(header.getName().toLowerCase(), header.getValue());
            }
        }
        if (StringUtil.isNotEmpty(response.getContentType())) {
            responseHeaders.put("content-type", response.getContentType());
        }
        if (base64Body != null && !responseHeaders.containsKey("content-length")) {
            responseHeaders.put("content-length", String.valueOf(contentLength));
        }
        return responseHeaders;
    }

    public void abort(ErrorReasons errorCode, Integer priority) {
        if (!this.canBeIntercepted()) {
            return;
        }
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        ValidateUtil.assertArg(!this.interception.getHandled(), "Request is already handled!");
        if (priority == null) {
            this._abort(errorCode);
            return;
        }
        this.interception.setAbortReason(errorCode);
        if (this.interception.getResolutionState().getPriority() == null || priority >= this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.ABORT, priority));
        }
    }

    private void _abort(ErrorReasons errorCode) {
        this.interception.setHandled(true);
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.interceptionId), "HTTPRequest is missing _interceptionId needed for Fetch.fulfillRequest");
        String errorReason = errorCode.getName();
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.interceptionId);
        params.put("errorReason", errorReason);
        try {
            this.client.send("Fetch.failRequest", params);
        } catch (Exception e) {
            handleError(e);
        }

    }

    public String interceptionId() {
        return this.interceptionId;
    }

    public void continueRequest() {
        this.continueRequest(new ContinueRequestOverrides(), null);
    }


    private List<HeaderEntry> headersArray(Map<String, String> headers) {
        List<HeaderEntry> result = new ArrayList<>();
        if (headers == null) {
            return result;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String value = entry.getValue();
            if (StringUtil.isNotEmpty(value)) {
                result.add(new HeaderEntry(entry.getKey(), value));
            }
        }
        return result;
    }

    public void abort() {
        this.abort(ErrorReasons.FAILED, null);
    }


    protected void setResponse(Response response) {
        this.response = response;
    }

    public String id() {
        return id;
    }


    protected void setFailureText(String failureText) {
        this.failureText = failureText;
    }


    public boolean fromMemoryCache() {
        return fromMemoryCache;
    }

    protected void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
    }

    //不能阻塞 WebSocketConnectReadThread
    public void finalizeInterceptions() {
        this.interception.getHandlers().forEach(Runnable::run);
        this.interception.setHandlers(new ArrayList<>());
        InterceptResolutionAction action = this.interceptResolutionState().getAction();
        switch (action) {
            case ABORT:
                this.abort(this.interception.getAbortReason(), null);
                break;
            case CONTINUE:
                this.continueRequest(this.interception.getRequestOverrides(), null);
                break;
            case RESPOND:
                if (this.interception.getResponse() == null) {
                    throw new JvppeteerException("Response is missing for the interception");
                }
                this.respond(this.interception.getResponse(), null);
                break;
        }
    }

    private boolean canBeIntercepted() {
        return !this.url().startsWith("data:") && !this.fromMemoryCache;
    }


    private InterceptResolutionState interceptResolutionState() {
        InterceptResolutionState state = new InterceptResolutionState();
        if (!this.interception.getEnabled()) {
            state.setAction(InterceptResolutionAction.DISABLED);
            return state;
        }
        if (this.interception.getHandled()) {
            state.setAction(InterceptResolutionAction.ALREADY_HANDLED);
            return state;
        }
        return this.interception.getResolutionState();
    }

    public void enqueueInterceptAction(Runnable pendingHandler) {
        this.interception.getHandlers().add(pendingHandler);
    }

    public ContinueRequestOverrides continueRequestOverrides() {
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        return this.interception.getRequestOverrides();
    }

    public ErrorReasons abortErrorReason() {
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        return this.interception.getAbortReason();
    }

    public ResponseForRequest responseForRequest() {
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        return this.interception.getResponse();
    }

    public boolean isInterceptResolutionHandled() {
        return this.interception.getHandled();
    }
}
