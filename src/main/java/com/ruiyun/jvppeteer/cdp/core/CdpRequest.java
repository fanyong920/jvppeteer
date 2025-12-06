package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.ErrorReasons;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.Initiator;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import com.ruiyun.jvppeteer.cdp.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CdpRequest extends Request {

    private final String id;
    private final String urlFragment;
    private CDPSession client;
    private volatile boolean isNavigationRequest;
    private final String url;
    private final ResourceType resourceType;
    private final String method;
    private final boolean hasPostData;
    private final String postData;
    private volatile List<HeaderEntry> headers = new ArrayList<>();
    private final Frame frame;
    private final Initiator initiator;

    public CdpRequest(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentEvent event, List<Request> redirectChain) {
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
        this.urlFragment = event.getRequest().getUrlFragment();
        this.resourceType = StringUtil.isEmpty(event.getType()) ? ResourceType.Other : ResourceType.valueOf(event.getType());
        this.method = event.getRequest().getMethod();
        this.postData = event.getRequest().getPostData();
        this.hasPostData = event.getRequest().getHasPostData();
        this.frame = frame;
        this.redirectChain = redirectChain;
        this.initiator = event.getInitiator();
        this.interception.setEnabled(allowInterception);
        updateHeaders(event.getRequest().getHeaders());
    }

    public void updateHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.add(new HeaderEntry(entry.getKey().toLowerCase(), entry.getValue()));
        }
    }

    public CDPSession client() {
        return this.client;
    }

    public String url() {
        return this.url(false);
    }

    public String url(boolean withFragment) {
        return withFragment ? this.url + this.urlFragment : this.url;
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
    public List<HeaderEntry> headers() {
        return this.headers;
    }

    /**
     * 请求对应的响应
     *
     * @return Response
     */
    public CdpResponse response() {
        return this.response;
    }

    public Frame frame() {
        return frame;
    }

    public void setHeaders(List<HeaderEntry> headers) {
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


    public void _continue(ContinueRequestOverrides overrides) {
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
        params.put("headers", filterHeaders(overrides.getHeaders()));
        try {
            this.client.send("Fetch.continueRequest", params);
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }




    public void _respond(ResponseForRequest response) {
        this.interception.setHandled(true);
        String base64Body = null;
        int contentLength = 0;
        if (StringUtil.isNotEmpty(response.getBody())) {
            byte[] byteBody = response.getBody().getBytes(StandardCharsets.UTF_8);
            base64Body = Base64Util.encode(byteBody);
            contentLength = byteBody.length;
        }
        List<HeaderEntry> responseHeaders = headers(response, base64Body, contentLength);
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.interceptionId), "HTTPRequest is missing _interceptionId needed for Fetch.fulfillRequest");
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.interceptionId);
        params.put("responseCode", response.getStatus());
        params.put("responsePhrase", STATUS_TEXTS.get(response.getStatus()));
        params.put("responseHeaders", filterHeaders(responseHeaders));
        if (Objects.nonNull(base64Body)) {
            params.put("body", base64Body);
        }
        try {
            this.client.send("Fetch.fulfillRequest", params);
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }

    private static List<HeaderEntry> headers(ResponseForRequest response, String base64Body, int contentLength) {
        List<HeaderEntry> responseHeaders = new ArrayList<>();
        boolean hasContentLength = false;
        if (ValidateUtil.isNotEmpty(response.getHeaders())) {
            for (HeaderEntry header : response.getHeaders()) {
                String name = header.getName().toLowerCase();
                responseHeaders.add(new HeaderEntry(name, header.getValue()));
                if (name.equals("content-length")) {
                    hasContentLength = true;
                }
            }
        }
        if (StringUtil.isNotEmpty(response.getContentType())) {
            responseHeaders.add(new HeaderEntry("content-type", response.getContentType()));
        }
        if (base64Body != null && !hasContentLength) {
            responseHeaders.add(new HeaderEntry("content-length", String.valueOf(contentLength)));
        }
        return responseHeaders;
    }


    public void _abort(ErrorReasons errorCode) {
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

    private List<HeaderEntry> filterHeaders(List<HeaderEntry> headers) {
        if (headers == null) {
            return null;
        }
        Iterator<HeaderEntry> iterator = headers.iterator();
        while (iterator.hasNext()) {
            HeaderEntry next = iterator.next();
            if (StringUtil.isEmpty(next.getValue())) {
                iterator.remove();
            }
        }
        return headers;
    }

    protected void setResponse(CdpResponse response) {
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



    public void setClient(CDPSession client) {
        this.client = client;
    }
}
