package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.fetch.HeaderEntry;
import com.ruiyun.jvppeteer.protocol.network.ErrorCode;
import com.ruiyun.jvppeteer.protocol.network.RequestWillBeSentPayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Whenever the page sends a request, such as for a network resource, the following events are emitted by puppeteer's page:
 *
 * 'request' emitted when the request is issued by the page.
 * 'response' emitted when/if the response is received for the request.
 * 'requestfinished' emitted when the response body is downloaded and the request is complete.
 * If request fails at some point, then instead of 'requestfinished' event (and possibly instead of 'response' event), the 'requestfailed' event is emitted.
 *
 * NOTE HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete with 'requestfinished' event.
 *
 * If request gets a 'redirect' response, the request is successfully finished with the 'requestfinished' event, and a new request is issued to a redirected url.
 */
public class Request {

    private static final Map<Integer,String> STATUS_TEXTS = new HashMap<>();

    private CDPSession client;

    private String requestId;

    private boolean isNavigationRequest;

    private String interceptionId;

    private boolean allowInterception;

    private boolean interceptionHandled;

    private Response response;

    private String failureText;

    private String url;

    private String  resourceType;

    private String method;

    private String postData;

    private Map<String,String> headers;

    private Frame frame;

    private List<Request> redirectChain;

    private boolean fromMemoryCache;
    static {
        // List taken from https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml with extra 306 and 418 codes.
        STATUS_TEXTS.put(100,"Continue");
        STATUS_TEXTS.put(101,"Switching Protocols");
        STATUS_TEXTS.put(102,"Processing");
        STATUS_TEXTS.put(103,"Early Hints");
        STATUS_TEXTS.put(200,"OK");
        STATUS_TEXTS.put(201,"Created");
        STATUS_TEXTS.put(202,"Accepted");
        STATUS_TEXTS.put(203,"Non-Authoritative Information");
        STATUS_TEXTS.put(204,"No Content");
        STATUS_TEXTS.put(205,"Reset Content");
        STATUS_TEXTS.put(206,"Partial Content");
        STATUS_TEXTS.put(207,"Multi-Status");
        STATUS_TEXTS.put(208,"Already Reported");
        STATUS_TEXTS.put(226,"IM Used");
        STATUS_TEXTS.put(300,"Multiple Choices");
        STATUS_TEXTS.put(301,"Moved Permanently");
        STATUS_TEXTS.put(302,"Found");
        STATUS_TEXTS.put(303,"See Other");
        STATUS_TEXTS.put(304,"Not Modified");
        STATUS_TEXTS.put(305,"Use Proxy");
        STATUS_TEXTS.put(306,"Switch Proxy");
        STATUS_TEXTS.put(307,"Temporary Redirect");
        STATUS_TEXTS.put(308,"Permanent Redirect");
        STATUS_TEXTS.put(400,"Bad Request");
        STATUS_TEXTS.put(401,"Unauthorized");
        STATUS_TEXTS.put(402,"Payment Required");
        STATUS_TEXTS.put(403,"Forbidden");
        STATUS_TEXTS.put(404,"Not Found");
        STATUS_TEXTS.put(405,"Method Not Allowed");
        STATUS_TEXTS.put(406,"Not Acceptable");
        STATUS_TEXTS.put(407,"Proxy Authentication Required");
        STATUS_TEXTS.put(408,"Request Timeout");
        STATUS_TEXTS.put(409,"Conflict");
        STATUS_TEXTS.put(410,"Gone");
        STATUS_TEXTS.put(411,"Length Required");
        STATUS_TEXTS.put(412,"Precondition Failed");
        STATUS_TEXTS.put(413,"Payload Too Large");
        STATUS_TEXTS.put(414,"URI Too Long");
        STATUS_TEXTS.put(415,"Unsupported Media Type");
        STATUS_TEXTS.put(416,"Range Not Satisfiable");
        STATUS_TEXTS.put(417,"Expectation Failed");
        STATUS_TEXTS.put(418,"I'm a teapot");
        STATUS_TEXTS.put(421,"Misdirected Request");
        STATUS_TEXTS.put(422,"Unprocessable Entity");
        STATUS_TEXTS.put(423,"Locked");
        STATUS_TEXTS.put(424,"Failed Dependency");
        STATUS_TEXTS.put(425,"Too Early");
        STATUS_TEXTS.put(426,"Upgrade Required");
        STATUS_TEXTS.put(428,"Precondition Required");
        STATUS_TEXTS.put(429,"Too Many Requests");
        STATUS_TEXTS.put(431,"Request Header Fields Too Large");
        STATUS_TEXTS.put(451,"Unavailable For Legal Reasons");
        STATUS_TEXTS.put(500,"Internal Server Error");
        STATUS_TEXTS.put(501,"Not Implemented");
        STATUS_TEXTS.put(502,"Bad Gateway");
        STATUS_TEXTS.put(503,"Service Unavailable");
        STATUS_TEXTS.put(504,"Gateway Timeout");
        STATUS_TEXTS.put(505,"HTTP Version Not Supported");
        STATUS_TEXTS.put(506,"Variant Also Negotiates");
        STATUS_TEXTS.put(507,"Insufficient Storage");
        STATUS_TEXTS.put(508,"Loop Detected");
        STATUS_TEXTS.put(510,"Not Extended");
        STATUS_TEXTS.put(511,"Network Authentication Required");
    }
    public Request() {
        super();
    }

    public Request(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentPayload event, List<Request> redirectChain) {
        super();
        this.client = client;
        this.requestId = event.getRequestId();
        if(event.getRequestId() != null){
            if(event.getRequestId().equals(event.getLoaderId()))
            this.isNavigationRequest = true;
        }else{
            if(event.getLoaderId() == null)
                this.isNavigationRequest = true;
        }
        this.interceptionId = interceptionId;
        this.allowInterception = allowInterception;
        this.url = event.getRequest().getUrl();
        this.resourceType = event.getType().toLowerCase();
        this.method = event.getRequest().getMethod();
        this.postData = event.getRequest().getPostData();
        this.headers = new HashMap<>();
        this.frame = frame;
        this.redirectChain = redirectChain;
        this.interceptionHandled = false;
        this.fromMemoryCache = false;
        for(Map.Entry<String,String> entry : event.getRequest().headers.entrySet()){
            this.headers.put(entry.getKey().toLowerCase(),entry.getValue());

        }
        this.response = null;
        this.failureText = null;

    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public String getInterceptionId() {
        return interceptionId;
    }

    public void setInterceptionId(String interceptionId) {
        this.interceptionId = interceptionId;
    }

    public boolean isAllowInterception() {
        return allowInterception;
    }

    public void setAllowInterception(boolean allowInterception) {
        this.allowInterception = allowInterception;
    }

    /**
     * 失败信息
     * @return errorText
     */
    public String failure(){
        if (StringUtil.isEmpty(this.failureText))
            return null;
        return this.failureText;
    }

    /**
     * continue()方法，但是continue是java关键字，所以改成了continueRequest
     * @param url
     * @param method
     * @param postData
     * @param headers
     */
    public void continueRequest(String url,String method ,String postData ,Map<String,String> headers) {
        // Request interception is not supported for data: urls.
        if (this.url.startsWith("data:"))
            return;
        ValidateUtil.assertBoolean(this.allowInterception, "Request Interception is not enabled!");
        ValidateUtil.assertBoolean(!this.interceptionHandled, "Request is already handled!");

        this.interceptionHandled = true;
        Map<String,Object> params = new HashMap<>();
        params.put("requestId",this.interceptionId);
        params.put("url",url);
        params.put("method",method);
        params.put("postData",postData);
        if(headers != null && headers.size() > 0){
            params.put("headers",headersArray(headers));
        }
         this.client.send("Fetch.continueRequest", params,true);
    }
    public void respond(int status,Map<String,String> headers,String contentType,String body) throws UnsupportedEncodingException {
        // Mocking responses for dataURL requests is not currently supported.
        if (this.url.startsWith("data:"))
            return;
        ValidateUtil.assertBoolean(this.allowInterception, "Request Interception is not enabled!");
        ValidateUtil.assertBoolean(!this.interceptionHandled, "Request is already handled!");
        this.interceptionHandled = true;
        byte[] responseBody = null;
        if(StringUtil.isNotEmpty(body)){
             responseBody = body.getBytes("utf-8");
        }



    Map<String,String> responseHeaders= new HashMap<>();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String,String> entry : headers.entrySet())
            responseHeaders.put(entry.getKey().toLowerCase(),entry.getValue());
        }
        if (StringUtil.isNotEmpty(contentType))
            responseHeaders.put("content-type",contentType);
        if (responseBody != null && !responseHeaders.containsKey("content-length"));
        responseHeaders.put("content-length" ,String.valueOf(responseBody.length));


        Map<String,Object> params = new HashMap<>();
        params.put("requestId",this.interceptionId);
        params.put("responseCode",status);
        params.put("responsePhrase",STATUS_TEXTS.get(status));
        params.put("responseHeaders",headersArray(responseHeaders));
        if(responseBody != null){
            params.put("body", Base64.encode(responseBody));
        }
         this.client.send("Fetch.fulfillRequest", params,true);
    }
    public void abort(ErrorCode errorCode){
        // Request interception is not supported for data: urls.
        if (this.url.startsWith("data:"))
            return;
    String errorReason = errorCode.getName();
        ValidateUtil.assertBoolean(this.allowInterception, "Request Interception is not enabled!");
        ValidateUtil.assertBoolean(!this.interceptionHandled, "Request is already handled!");
        this.interceptionHandled = true;
        Map<String,Object> params = new HashMap<>();
        params.put("requestId",this.interceptionId);
        params.put("errorReason",errorReason);
         this.client.send("Fetch.failRequest", params,true);
    }
    private List<HeaderEntry> headersArray(Map<String, String> headers) {
        List<HeaderEntry> result= new ArrayList<>();
        for(Map.Entry<String,String> entry: headers.entrySet()){
            String value = entry.getValue();
            if(StringUtil.isNotEmpty(value)){
                result.add(new HeaderEntry(entry.getKey(),value));
            }
        }
        return result;
    }

    public List<Request> getRedirectChain() {
        return redirectChain;
    }

    public void setRedirectChain(List<Request> redirectChain) {
        this.redirectChain = redirectChain;
    }

    public Response response() {
        return  this.response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean getIsNavigationRequest() {
        return isNavigationRequest;
    }

    public void setNavigationRequest(boolean navigationRequest) {
        isNavigationRequest = navigationRequest;
    }

    public boolean getIsInterceptionHandled() {
        return interceptionHandled;
    }

    public void setInterceptionHandled(boolean interceptionHandled) {
        this.interceptionHandled = interceptionHandled;
    }

    public String getFailureText() {
        return failureText;
    }

    public void setFailureText(String failureText) {
        this.failureText = failureText;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public boolean getFromMemoryCache() {
        return fromMemoryCache;
    }

    public void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
    }
}
