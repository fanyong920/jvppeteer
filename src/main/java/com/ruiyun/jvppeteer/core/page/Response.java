package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.network.RemoteAddress;
import com.ruiyun.jvppeteer.protocol.network.ResponsePayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.sun.xml.internal.ws.util.CompletedFuture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Response {

    private CDPSession client;

    private Request request;

    private byte[] contentPromise;

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
        this.url = request.url();
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

    protected Future<byte[]> bodyLoadedPromiseFulfill(RuntimeException e) {
        if (e != null) {
            throw e;
        }
        if (this.contentPromise == null) {
            return Helper.commonExecutor().submit(() -> {
                //System.out.println("threadname="+Thread.currentThread().getName()+"this.client send:"+this.client.hashCode());
                Map<String, Object> params = new HashMap<>();
                params.put("requestId", request.requestId());
                JsonNode response = this.client.send("Network.getResponseBody", params, true);
                JsonNode charsetNode = response.get("base64Encoded");
                if (charsetNode != null) {
                     contentPromise = Base64.decode(response.get("data").asText());
                } else {
                   contentPromise = response.get("data").asText().getBytes(StandardCharsets.UTF_8);
                }
                return contentPromise;
            });
        }
        return new CompletedFuture<>(this.contentPromise, null);

    }

    public boolean ok() {
        return this.status == 0 || (this.status >= 200 && this.status <= 299);
    }

    public byte[] buffer() throws ExecutionException, InterruptedException {
        if (this.contentPromise == null) {
            Future<byte[]> future = bodyLoadedPromiseFulfill(null);
            future.get();
        }
        return this.contentPromise;
    }

    public String text() throws IOException, ExecutionException, InterruptedException {
        byte[] content = this.buffer();
        return new String(content, "utf-8");
    }

    public <T> T json(Class<T> clazz) throws IOException, ExecutionException, InterruptedException {
        String content = this.text();
        return Constant.OBJECTMAPPER.readValue(content, clazz);
    }

    public Request request() {
        return this.request;
    }

    public boolean fromCache() {
        return this.fromDiskCache || this.request.fromMemoryCache();
    }

    public String url() {
        return this.url;
    }

    public int status() {
        return this.status;
    }

    public String statusText() {
        return this.statusText;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public SecurityDetails securityDetails() {
        return this.securityDetails;
    }

    public boolean fromServiceWorker() {
        return this.fromServiceWorker;
    }

    public Frame frame() {
        return this.request.frame();
    }

    public RemoteAddress remoteAddress() {
        return remoteAddress;
    }

}
