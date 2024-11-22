package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.entities.RemoteAddress;
import com.ruiyun.jvppeteer.entities.ResourceTiming;
import com.ruiyun.jvppeteer.entities.ResponsePayload;
import com.ruiyun.jvppeteer.entities.ResponseSecurityDetails;
import com.ruiyun.jvppeteer.events.ResponseReceivedExtraInfoEvent;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class Response {

    private Request request;
    private final AwaitableResult<byte[]> contentResult = AwaitableResult.create();
    private final AwaitableResult<String> bodyLoadedResult = AwaitableResult.create();
    private volatile RemoteAddress remoteAddress;
    private volatile int status;
    private volatile String statusText;
    private volatile String url;
    private volatile boolean fromDiskCache;
    private volatile boolean fromServiceWorker;
    private volatile Map<String, String> headers;
    private volatile ResponseSecurityDetails securityDetails;
    private ResourceTiming timing;

    public Response() {
    }

    public Response(Request request, ResponsePayload responsePayload, ResponseReceivedExtraInfoEvent extraInfo) {
        this.request = request;
        this.remoteAddress = new RemoteAddress(responsePayload.getRemoteIPAddress(), responsePayload.getRemotePort());
        this.statusText = StringUtil.isNotEmpty(this.parseStatusTextFromExtraInfo(extraInfo)) ? this.parseStatusTextFromExtraInfo(extraInfo) : responsePayload.getStatusText();
        this.url = request.url();
        this.fromDiskCache = responsePayload.getFromDiskCache();
        this.fromServiceWorker = responsePayload.getFromServiceWorker();
        this.status = extraInfo != null ? extraInfo.getStatusCode() : responsePayload.getStatus();
        this.headers = new HashMap<>();
        Map<String, String> headers;
        if (extraInfo != null) {
            headers = extraInfo.getHeaders();
        } else {
            headers = responsePayload.getHeaders();
        }
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        this.securityDetails = responsePayload.getSecurityDetails() != null ? new ResponseSecurityDetails(responsePayload.getSecurityDetails()) : null;
    }

    private String parseStatusTextFromExtraInfo(ResponseReceivedExtraInfoEvent extraInfo) {
        if (extraInfo == null || StringUtil.isEmpty(extraInfo.getHeadersText())) {
            return null;
        }
        String[] split = extraInfo.getHeadersText().split("\r", 1);
        if (split.length > 0) {
            String firstLine = split[0];
            if (StringUtil.isEmpty(firstLine)) {
                return null;
            }
            Pattern pattern = Pattern.compile("[^ ]* [^ ]* (.*)");
            Matcher matcher = pattern.matcher(firstLine);
            if (!matcher.find()) {
                return null;
            }
            String group = matcher.group(1);
            if (StringUtil.isEmpty(group)) {
                return null;
            }
            return group;
        } else {
            return null;
        }
    }

    /**
     * 处理请求体响应
     *
     * @param errorMsg 错误信息
     */
    protected void resolveBody(String errorMsg) {
        if (StringUtil.isNotEmpty(errorMsg)) {
            this.setBodyLoadedResult(errorMsg);
        } else {
            this.setBodyLoadedResult("");
        }
    }

    private void setBodyLoadedResult(String result) {
        this.bodyLoadedResult.onSuccess(result);
    }

    private void getResponseBody() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("requestId", this.request.id());
        try {
            JsonNode response = this.request.client().send("Network.getResponseBody", params);
            if (response != null) {
                if (response.get("base64Encoded").asBoolean()) {
                    this.contentResult.onSuccess(Base64.getDecoder().decode(response.get("body").asText()));
                } else {
                    this.contentResult.onSuccess(response.get("body").asText().getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            if (e instanceof ProtocolException && "No resource with given identifier found".equals(e.getMessage())) {
                throw new ProtocolException("Could not load body for this request. This might happen if the request is a preflight request.", e);
            }
            throwError(e);
        }
    }

    public RemoteAddress remoteAddress() {
        return this.remoteAddress;
    }

    public String url() {
        return this.url;
    }

    public int status() {
        return this.status;
    }

    public boolean ok() {
        return this.status == 0 || (this.status >= 200 && this.status <= 299);
    }

    public String statusText() {
        return this.statusText;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public ResponseSecurityDetails securityDetails() {
        return this.securityDetails;
    }

    public ResourceTiming timing() {
        return this.timing;
    }

    /**
     * 获取该响应的byte数组，当对应的请求已经重定向，会抛出出错误
     *
     * @return 字节数组
     */
    public byte[] content() {
        if (!this.contentResult.isDone()) {
            if (StringUtil.isEmpty(this.bodyLoadedResult.get())) {
                this.getResponseBody();
            }else {
                throw new JvppeteerException(this.bodyLoadedResult.get());
            }
        }
        return this.contentResult.get();
    }

    /**
     * 获取该响应的byte数组，无视重定向，获取响应的内容
     *
     * @return 字节数组
     */
    public byte[] getContentForcibly() {
        if (!this.contentResult.isDone()) {
            this.getResponseBody();
        }
        return this.contentResult.get();
    }

    public Request request() {
        return this.request;
    }

    public boolean fromCache() {
        return this.fromDiskCache || this.request.fromMemoryCache();
    }

    public boolean fromServiceWorker() {
        return this.fromServiceWorker;
    }

    public Frame frame() {
        return this.request.frame();
    }
}
