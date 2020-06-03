package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.network.RemoteAddress;
import com.ruiyun.jvppeteer.protocol.network.ResponsePayload;
import com.ruiyun.jvppeteer.transport.CDPSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Response {

    private CDPSession client;

    private Request request;

    private byte[] contentPromise;

    private CountDownLatch contentPromiseLatch ;

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
        this.contentPromiseLatch = new CountDownLatch(1);
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

    protected void bodyLoadedPromiseFulfill(RuntimeException e) {
        if (e != null) {
            throw e;
        }
        if(this.contentPromiseLatch != null){
            this.contentPromiseLatch.countDown();
        }
//        if (this.contentPromise == null) {
//            return Helper.commonExecutor().submit(
//                return contentPromise;
//            });
//        }
//        return new CompletedFuture<>(this.contentPromise, null);

    }

    public boolean ok() {
        return this.status == 0 || (this.status >= 200 && this.status <= 299);
    }

    public byte[] buffer() {
        if (this.contentPromise == null) {
           this.contentPromiseLatch.countDown();
        }
        return this.contentPromise;
    }

    public String text() {
        byte[] content = this.buffer();
        return new String(content, StandardCharsets.UTF_8);
    }

    public <T> T json(Class<T> clazz) throws IOException {
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
