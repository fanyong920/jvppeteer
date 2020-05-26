package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.network.RemoteAddress;
import com.ruiyun.jvppeteer.protocol.network.ResponsePayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class Response {

    private CDPSession client;

    private Request request;

    private byte[] contentPromise;

    private boolean bodyLoadedPromise;

    private RemoteAddress remoteAddress;

    private int status;

    private String statusText;

    private String url;

    private boolean fromDiskCache;

    private boolean fromServiceWorker;

    private Map<String, String> headers;

    private SecurityDetails securityDetails;

    public Response() {
    }

    public Response(CDPSession client, Request request, ResponsePayload responsePayload) {
        this.client = client;
        this.request = request;
        this.contentPromise = null;
        this.remoteAddress = new RemoteAddress(responsePayload.getRemoteIPAddress(), responsePayload.getRemotePort());
        this.status = responsePayload.getStatus();
        this.statusText = responsePayload.getStatusText();
        this.url = request.getUrl();
        this.fromDiskCache = responsePayload.getFromDiskCache();
        this.fromServiceWorker = responsePayload.getFromServiceWorker();
        this.headers = new HashMap<>();

        Map<String, String> headers = responsePayload.getHeaders();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        this.securityDetails = responsePayload.getSecurityDetails() != null ? new SecurityDetails(responsePayload.getSecurityDetails()) : null;
    }

    public Future bodyLoadedPromiseFulfill(RuntimeException e) {
        if (e != null) {
            throw e;
        }
        synchronized (Response.class) {
            if (this.contentPromise == null) {
                return Helper.commonExecutor().submit(() -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("requestId", this.request.getRequestId());
                    JsonNode response = this.client.send("Network.getResponseBody", params, true);

                    JsonNode charsetNode = response.get("base64Encoded");
                    if (charsetNode != null) {
                        this.contentPromise = Base64.decode(response.get("data").asText());
                    } else {
                        this.contentPromise = response.get("data").asText().getBytes(StandardCharsets.UTF_8);
                    }
                });
            }
            return null;
        }
    }

    public boolean ok() {
        return this.status == 0 || (this.status >= 200 && this.status <= 299);
    }

    public byte[] buffer() {
        if (this.contentPromise == null) {
            bodyLoadedPromiseFulfill(null);
        }
        return this.contentPromise;
    }

    public String text() throws IOException {
        byte[] content = this.buffer();
        return new String(content, "utf-8");
    }

    public <T> T json(Class<T> clazz) throws IOException {
        String content = this.text();
        return Constant.OBJECTMAPPER.readValue(content, clazz);
    }
    public Request request() {
        return this.request;
    }

   public boolean fromCache() {
        return this.fromDiskCache || this.request.getFromMemoryCache();
    }
    public String url() {
        return this.url;
    }
    public int status() {
        return this.status;
    }

   public String  statusText() {
        return this.statusText;
    }

    public Map<String,String> headers() {
        return this.headers;
    }

    public SecurityDetails securityDetails() {
        return this.securityDetails;
    }
    public boolean fromServiceWorker() {
        return this.fromServiceWorker;
    }

    public Frame frame() {
        return this.request.getFrame();
    }
    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public byte[] getContentPromise() {
        return contentPromise;
    }

    public void setContentPromise(byte[] contentPromise) {
        this.contentPromise = contentPromise;
    }

    public RemoteAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(RemoteAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getIsFromDiskCache() {
        return fromDiskCache;
    }

    public void setFromDiskCache(boolean fromDiskCache) {
        this.fromDiskCache = fromDiskCache;
    }

    public boolean getIsFromServiceWorker() {
        return fromServiceWorker;
    }

    public void setFromServiceWorker(boolean fromServiceWorker) {
        this.fromServiceWorker = fromServiceWorker;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public SecurityDetails getSecurityDetails() {
        return securityDetails;
    }

    public void setSecurityDetails(SecurityDetails securityDetails) {
        this.securityDetails = securityDetails;
    }
}
