package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.protocol.fetch.HeaderEntry;
import com.ruiyun.jvppeteer.protocol.network.ErrorCode;
import com.ruiyun.jvppeteer.protocol.network.RequestWillBeSentPayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

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

    private static final Map<Integer, String> STATUS_TEXTS = new HashMap<>();

    private CDPSession client;

    private String requestId;

    private boolean isNavigationRequest;

    private String interceptionId;

    private boolean allowInterception;

    private boolean interceptionHandled;

    private Response response;

    private String failureText;

    private String url;

    private String resourceType;

    private String method;

    private String postData;

    private Map<String, String> headers;

    private Frame frame;

    private List<Request> redirectChain;

    private boolean fromMemoryCache;

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

    public Request(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentPayload event, List<Request> redirectChain) {
        super();
        this.client = client;
        this.requestId = event.getRequestId();
        if (event.getRequestId() != null) {
            if (event.getRequestId().equals(event.getLoaderId()))
                this.isNavigationRequest = true;
        } else {
            if (event.getLoaderId() == null)
                this.isNavigationRequest = true;
        }
        this.interceptionId = interceptionId;
        this.allowInterception = allowInterception;
        this.url = event.getRequest().url();
        this.resourceType = event.getType().toLowerCase();
        this.method = event.getRequest().method();
        this.postData = event.getRequest().postData();
        this.headers = new HashMap<>();
        this.frame = frame;
        this.redirectChain = redirectChain;
        this.interceptionHandled = false;
        this.fromMemoryCache = false;
        for (Map.Entry<String, String> entry : event.getRequest().headers.entrySet()) {
            this.headers.put(entry.getKey().toLowerCase(), entry.getValue());

        }
        this.response = null;
        this.failureText = null;

    }

    public Frame frame() {
        return frame;
    }

    public String interceptionId() {
        return interceptionId;
    }

    public boolean isAllowInterception() {
        return allowInterception;
    }

    /**
     * 返回请求的失败信息
     *
     * @return errorText
     */
    public String failure() {
        return this.failureText;
    }

    /**
     * continue()方法，但是continue是java关键字，所以改成了continueRequest
     *
     * @param url      url
     * @param method   方法 GET POST
     * @param postData 数据 the post data of request
     * @param headers  请求头
     * @return Future
     */
    public JsonNode continueRequest(String url, String method, String postData, Map<String, String> headers) {
        // Request interception is not supported for data: urls.
        if (url().startsWith("data:"))
            return null;

        ValidateUtil.assertArg(isAllowInterception(), "Request Interception is not enabled!");
        ValidateUtil.assertArg(!isInterceptionHandled(), "Request is already handled!");

        setInterceptionHandled(true);
        Map<String, Object> params = new HashMap<>();
        params.put("requestId", interceptionId());

        if(StringUtil.isNotEmpty(url)) {
            params.put("url", url);
        }
        if(StringUtil.isNotEmpty(method)) {
            params.put("method", method);
        }
        if(StringUtil.isNotEmpty(postData)){
            params.put("postData", new String(Base64.getEncoder().encode(postData.getBytes()),StandardCharsets.UTF_8));
        }

        if (headers != null && headers.size() > 0) {
            params.put("headers", headersArray(headers));
        }
        return client.send("Fetch.continueRequest", params, true);
    }

    /**
     * 请求继续
     */
    public void continueRequest() {
        this.continueRequest(null, null, null, null);
    }

    /**
     * 自定义响应
     * @param status 响应状态
     * @param headers 响应头
     * @param contentType contentType
     * @param body 响应体
     * @return Future
     */
    public JsonNode respond(int status, List<HeaderEntry> headers, String contentType, String body) {
        // Mocking responses for dataURL requests is not currently supported.
        if (url().startsWith("data:")) {
            return null;
        }

        ValidateUtil.assertArg(allowInterception, "Request Interception is not enabled!");
        ValidateUtil.assertArg(!interceptionHandled, "Request is already handled!");

        setInterceptionHandled(true);
        byte[] responseBody = null;
        if (StringUtil.isNotEmpty(body)) {
            responseBody = body.getBytes(StandardCharsets.UTF_8);
        }
        Map<String, String> responseHeaders = new HashMap<>();

        if (ValidateUtil.isNotEmpty(headers)) {
            for (HeaderEntry header : headers) {
                responseHeaders.put(header.getName().toLowerCase(), header.getValue());
            }
        }

        if (StringUtil.isNotEmpty(contentType)){
            responseHeaders.put("content-type", contentType);
        }

        if (responseBody != null && !responseHeaders.containsKey("content-length")){
            responseHeaders.put("content-length", String.valueOf(responseBody.length));
        }

        Map<String, Object> params = new HashMap<>();
        params.put("requestId", interceptionId);
        params.put("responseCode", status);
        params.put("responsePhrase", STATUS_TEXTS.get(status));
        params.put("responseHeaders", headersArray(responseHeaders));
        if (responseBody != null) {
            params.put("body", Base64.getDecoder().decode(responseBody));
        }
        return client.send("Fetch.fulfillRequest", params, true);
    }

    /**
     * 拒绝发请求
     *
     * @param errorCode errorCode错误码
     * @return Future
     */
    public JsonNode abort(ErrorCode errorCode) {
      // Request interception is not supported for data: urls.
      if (url().startsWith("data:"))
          return null;

      String errorReason = errorCode.getName();
      ValidateUtil.assertArg(allowInterception, "Request Interception is not enabled!");
      ValidateUtil.assertArg(!interceptionHandled, "Request is already handled!");

      setInterceptionHandled(true);
      Map<String, Object> params = new HashMap<>();
      params.put("requestId", interceptionId);
      params.put("errorReason", errorReason);
      return client.send("Fetch.failRequest", params, true);

    }

    private List<HeaderEntry> headersArray(Map<String, String> headers) {
        List<HeaderEntry> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String value = entry.getValue();
            if (StringUtil.isNotEmpty(value)) {
                result.add(new HeaderEntry(entry.getKey(), value));
            }
        }
        return result;
    }

    /**
     * 截断请求
     */
    public void abort() {
        this.abort(ErrorCode.FAILED);
    }

    public List<Request> redirectChain() {
        return redirectChain;
    }

    public Response response() {
        return this.response;
    }

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public String postData() {
        return postData;
    }

    public Map<String, String> headers() {
        return headers;
    }

    protected void setResponse(Response response) {
        this.response = response;
    }

    public String requestId() {
        return requestId;
    }

    public boolean isNavigationRequest() {
        return isNavigationRequest;
    }

    public boolean isInterceptionHandled() {
        return interceptionHandled;
    }

    protected void setInterceptionHandled(boolean interceptionHandled) {
        this.interceptionHandled = interceptionHandled;
    }

    protected void setFailureText(String failureText) {
        this.failureText = failureText;
    }

    public String resourceType() {
        return resourceType;
    }

    public boolean fromMemoryCache() {
        return fromMemoryCache;
    }

    protected void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
    }

}
