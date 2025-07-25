package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.bidi.entities.FetchTimingInfo;
import com.ruiyun.jvppeteer.bidi.entities.Header;
import com.ruiyun.jvppeteer.bidi.entities.ResponseData;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.RemoteAddress;
import com.ruiyun.jvppeteer.cdp.entities.ResourceTiming;
import com.ruiyun.jvppeteer.cdp.entities.ResponseSecurityDetails;
import com.ruiyun.jvppeteer.cdp.entities.SecurityDetails;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BidiResponse extends Response {
    private final ResponseData data;
    private final BidiRequest request;
    private ResponseSecurityDetails  securityDetails;
    private final boolean cdpSupported;
    public BidiResponse(ResponseData data, BidiRequest request, boolean cdpSupported) {
        super();
        this.data = data;
        this.request = request;
        this.cdpSupported = cdpSupported;
        SecurityDetails securityDetails1 = data.getSecurityDetails();
        if(this.cdpSupported && Objects.nonNull(securityDetails1)) {
            this.securityDetails = Constant.OBJECTMAPPER.convertValue(securityDetails1, ResponseSecurityDetails.class);
        }
    }

    public static BidiResponse from(ResponseData data, BidiRequest request, boolean cdpSupported) {
        BidiResponse response = new BidiResponse(data, request, cdpSupported);
        response.initialize();
        return response;
    }

    private void initialize() {
        BidiFrame frame = this.request.frame();
        if(this.data.getFromCache()){
            this.request.setFromMemoryCache(true);

            if (Objects.nonNull(frame)) {
                frame.page().trustedEmitter().emit(PageEvents.RequestServedFromCache,this.request);
            }
        }
        frame.page().trustedEmitter().emit(PageEvents.Response, this);
    }

    @Override
    public RemoteAddress remoteAddress() {
        return new RemoteAddress("",-1);
    }

    @Override
    public String url() {
        return this.data.getUrl();
    }

    @Override
    public int status() {
        return this.data.getStatus();
    }

    @Override
    public String statusText() {
        return this.data.getStatusText();
    }

    @Override
    public List<HeaderEntry> headers() {
        List<HeaderEntry> headers = new ArrayList<HeaderEntry>();
        if(ValidateUtil.isNotEmpty(this.data.getHeaders())) {
            for (Header header : this.data.getHeaders()) {
                if(Objects.equals("string",header.getValue().getType())){
                    headers.add(new HeaderEntry(header.getName().toLowerCase(),header.getValue().getValue()));
                }
            }
        }
        return headers;
    }

    @Override
    public Request request() {
        return this.request;
    }

    @Override
    public boolean fromCache() {
        return this.data.getFromCache();
    }

    @Override
    public ResourceTiming timing() {
        FetchTimingInfo bidiTiming = this.request.timing();
        ResourceTiming timing = new ResourceTiming();
        timing.setRequestTime(bidiTiming.getRequestTime().longValue());
        timing.setProxyStart(-1);
        timing.setProxyEnd(-1);
        timing.setDnsStart(bidiTiming.getDnsStart().longValue());
        timing.setDnsEnd(bidiTiming.getDnsEnd().longValue());
        timing.setConnectStart(bidiTiming.getConnectStart().longValue());
        timing.setConnectEnd(bidiTiming.getConnectEnd().longValue());
        timing.setSslStart(bidiTiming.getTlsStart().longValue());
        timing.setSslEnd(-1);
        timing.setWorkerStart(-1);
        timing.setWorkerReady(-1);
        timing.setWorkerFetchStart(-1);
        timing.setWorkerRespondWithSettled(-1);
        timing.setWorkerRouterEvaluationStart(-1);
        timing.setWorkerCacheLookupStart(-1);
        timing.setSendStart(bidiTiming.getRedirectStart().longValue());
        timing.setSendEnd(-1);
        timing.setPushStart(-1);
        timing.setPushEnd(-1);
        timing.setReceiveHeadersStart(bidiTiming.getResponseStart().longValue());
        timing.setReceiveHeadersEnd(bidiTiming.getResponseEnd().longValue());
        return timing;
    }

    @Override
    public Frame frame() {
        return this.request.frame();
    }
    @Override
    public boolean fromServiceWorker() {
        return false;
    }

    @Override
    public ResponseSecurityDetails securityDetails() {
        if(!this.cdpSupported) {
            throw new UnsupportedOperationException();
        }
        return this.securityDetails;
    }

    @Override
    public byte[] content() {
        return Base64Util.decode(this.request.getResponseContent().getBytes());
    }

}
