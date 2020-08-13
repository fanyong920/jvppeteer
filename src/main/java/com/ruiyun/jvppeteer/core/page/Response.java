package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.network.RemoteAddress;
import com.ruiyun.jvppeteer.protocol.network.ResponsePayload;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    private String  bodyLoadedErrorMsg;

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

    /**
     * 处理请求体响应
     * @param errorMsg 错误信息
     */
    protected void resolveBody(String errorMsg){
        this.bodyLoadedPromiseFulfill(errorMsg);
    }

    protected void bodyLoadedPromiseFulfill(String errorMsg) {
        if (StringUtil.isNotEmpty(errorMsg)) {
            bodyLoadedErrorMsg = errorMsg;
        }
        if(this.contentPromiseLatch != null){
            this.contentPromiseLatch.countDown();
        }
    }

    public boolean ok() {
        return this.status == 0 || (this.status >= 200 && this.status <= 299);
    }

    /**
     * 获取该响应的byte数组
     * @return 字节数组
     * @throws InterruptedException 被打断异常
     */
    public byte[] buffer() throws InterruptedException {
        if (this.contentPromise == null) {
           this.contentPromiseLatch.await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

           if(StringUtil.isNotEmpty(this.bodyLoadedErrorMsg)){
               throw new RuntimeException(this.bodyLoadedErrorMsg);
           }
           Map<String,Object> params = new HashMap<>();
           params.put("requestId",this.request.requestId());
            JsonNode response = this.client.send("Network.getResponseBody", params, true);
            if(response != null){
                if(response.get("base64Encoded").asBoolean()){
                    contentPromise = Base64.decode(response.get("body").asText());
                }else{
                    contentPromise =  response.get("body").asText().getBytes(Charset.forName("utf-8"));
                }
            }
        }

        return this.contentPromise;
    }

    public String text() throws InterruptedException {
        byte[] content = this.buffer();
        return new String(content, StandardCharsets.UTF_8);
    }

    public <T> T json(Class<T> clazz) throws IOException, InterruptedException {
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
