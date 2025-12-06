package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Request;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.bidi.entities.AuthCredentials;
import com.ruiyun.jvppeteer.bidi.entities.FetchTimingInfo;
import com.ruiyun.jvppeteer.bidi.entities.Header;
import com.ruiyun.jvppeteer.bidi.entities.ResponseData;
import com.ruiyun.jvppeteer.bidi.entities.Value;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.Credentials;
import com.ruiyun.jvppeteer.cdp.entities.ErrorReasons;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.Initiator;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import com.ruiyun.jvppeteer.cdp.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BidiRequest extends Request {
    public static Map<RequestCore, BidiRequest> requests = new WeakHashMap<>();
    private final List<BidiRequest> redirectChain;
    private volatile BidiResponse response;
    private final String id;
    private final BidiFrame frame;
    private final RequestCore request;
    private volatile boolean authenticationHandled;

    public BidiRequest(RequestCore request, BidiFrame frame, BidiRequest redirect) {
        super();
        requests.put(request, this);
        this.interception.setEnabled(request.isBlocked());
        this.request = request;
        this.frame = frame;
        this.redirectChain = Objects.nonNull(redirect) ? redirect.redirectChain() : new ArrayList<>();
        this.id = request.id();
    }

    public static BidiRequest from(RequestCore bidiRequest, BidiFrame frame, BidiRequest redirect) {
        BidiRequest request = new BidiRequest(bidiRequest, frame, redirect);
        request.initialize();
        return request;
    }


    @Override
    public CDPSession client() {
        return this.frame.client();
    }

    private void initialize() {
        this.request.on(RequestCore.RequestCoreEvents.redirect, (Consumer<RequestCore>) request -> {
            BidiRequest httpRequest = BidiRequest.from(request, this.frame, this);
            this.redirectChain.add(this);
            request.once(RequestCore.RequestCoreEvents.success, (ignored) -> {
                this.frame.page().trustedEmitter().emit(PageEvents.RequestFinished, httpRequest);
            });

            request.once(RequestCore.RequestCoreEvents.error, (ignored) -> {
                this.frame.page().trustedEmitter().emit(PageEvents.RequestFailed, httpRequest);
            });
            httpRequest.finalizeInterceptions();
        });

        this.request.once(RequestCore.RequestCoreEvents.success, (Consumer<ResponseData>) data -> {
            this.response = BidiResponse.from(data, this, this.frame.page().browser().cdpSupported());
        });
        this.request.on(RequestCore.RequestCoreEvents.authenticate, this.handleAuthentication());

        this.frame.page().trustedEmitter().emit(PageEvents.Request, this);

        if (this.hasInternalHeaderOverwrite()) {
            this.interception.getHandlers().add(() -> {
                ContinueRequestOverrides continueRequestOverrides = new ContinueRequestOverrides();
                continueRequestOverrides.setHeaders(this.headers());
                this.continueRequest(continueRequestOverrides, 0);
            });
        }
    }

    @Override
    public String url() {
        return this.request.url();
    }

    @Override
    public ResourceType resourceType() {
        if (!this.frame.page().browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        return StringUtil.isEmpty(this.request.resourceType()) ? ResourceType.Other : ResourceType.valueOf(this.request.resourceType().toLowerCase());
    }

   public String getResponseContent() {
        return this.request.getResponseContent();
    }

    @Override
    public String method() {
        return this.request.method();
    }

    @Override
    public String postData() {
        if (!this.frame.page().browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        return this.request.postData();
    }

    @Override
    public boolean hasPostData() {
        if (!this.frame.page().browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        return this.request.hasPostData();
    }

    @Override
    public String fetchPostData() {
        throw new UnsupportedOperationException();
    }

    private boolean hasInternalHeaderOverwrite() {
        return !this.extraHTTPHeaders().isEmpty();
    }

    private List<HeaderEntry> extraHTTPHeaders() {
        if (Objects.nonNull(this.frame)) {
            if (Objects.nonNull(this.frame.page().extraHTTPHeaders)) {
                return this.frame.page().extraHTTPHeaders;
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<HeaderEntry> headers() {
        List<HeaderEntry> headers = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(this.request.headers())) {
            headers.addAll(this.request.headers().stream().map(header -> new HeaderEntry(header.getName().toLowerCase(), header.getValue().getValue())).collect(Collectors.toList()));
        }
        headers.addAll(this.extraHTTPHeaders());
        return headers;
    }

    @Override
    public BidiResponse response() {
        return this.response;
    }

    @Override
    public String failure() {
        return this.request.error();
    }

    @Override
    public boolean isNavigationRequest() {
        return Objects.nonNull(this.request.navigation());
    }

    @Override
    public Initiator initiator() {
        return Constant.OBJECTMAPPER.convertValue(this.request.initiator(), Initiator.class);
    }

    @Override
    public List<BidiRequest> redirectChain() {
        return new ArrayList<>(this.redirectChain);
    }

    @Override
    public BidiFrame frame() {
        return this.frame;
    }

    @Override
    public void continueRequest(ContinueRequestOverrides overrides, Integer priority) {
        ContinueRequestOverrides newOverrides = new ContinueRequestOverrides();
        if (this.hasInternalHeaderOverwrite()) {
            newOverrides.setHeaders(this.headers());
        } else {
            newOverrides.setHeaders(null);
        }
        super.continueRequest(newOverrides, priority);
    }

    @Override
    public void _continue(ContinueRequestOverrides overrides) {
        List<Header> bidiHeaders = getBidiHeaders(overrides.getHeaders());
        this.interception.setHandled(true);
        try {
            this.request.continueRequest(overrides.getUrl(), overrides.getMethod(), ValidateUtil.isEmpty(bidiHeaders) ? null : bidiHeaders, null, StringUtil.isEmpty(overrides.getPostData()) ? null : Base64Util.encode(overrides.getPostData().getBytes()));
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }

    @Override
    protected void _abort(ErrorReasons errorCode) {
        this.interception.setHandled(true);
        try {
            this.request.failRequest();
        } catch (Exception e) {
            this.interception.setHandled(false);
            throw e;
        }
    }

    @Override
    public void _respond(ResponseForRequest response) {
        this.interception.setHandled(true);
        String base64Body = null;
        int contentLength = 0;
        if (StringUtil.isNotEmpty(response.getBody())) {
            byte[] byteBody = response.getBody().getBytes(StandardCharsets.UTF_8);
            base64Body = Base64Util.encode(byteBody);
            contentLength = byteBody.length;
        }
        List<Header> headers = getBidiHeaders(response.getHeaders());
        boolean hasContentLength = false;
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase("content-length")) {
                hasContentLength = true;
                break;
            }
        }
        if (StringUtil.isNotEmpty(response.getContentType())) {
            headers.add(new Header("content-type", new Value("string", response.getContentType())));
        }
        if (contentLength > 0 && !hasContentLength) {
            headers.add(new Header("content-length", new Value("string", String.valueOf(contentLength))));
        }

        try {
            this.request.provideResponse(response.getStatus(), STATUS_TEXTS.get(response.getStatus()), headers.isEmpty() ? null : headers, base64Body);
        } catch (Exception e) {
            this.interception.setHandled(false);
            handleError(e);
        }
    }

    private Consumer<Boolean> handleAuthentication() {
        return authentication -> {
            if (Objects.isNull(this.frame)) {
                return;
            }
            Credentials credentials = this.frame.page().credentials;
            if (Objects.nonNull(credentials) && !this.authenticationHandled) {
                this.authenticationHandled = true;
                this.request.continueWithAuth("provideCredentials", new AuthCredentials("password", credentials.getUsername(), credentials.getPassword()));
            } else {
                this.request.continueWithAuth("cancel", null);
            }
        };
    }

    public FetchTimingInfo timing() {
        return this.request.timing();
    }

    private List<Header> getBidiHeaders(List<HeaderEntry> headers) {
        List<Header> bidiHeaders = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(headers)) {
            for (HeaderEntry header : headers) {
                if (StringUtil.isNotEmpty(header.getValue())) {
                    Header bidiHeader = new Header();
                    bidiHeader.setName(header.getName());
                    bidiHeader.setValue(new Value("string", header.getValue()));
                    bidiHeaders.add(bidiHeader);
                }
            }
        }
        return bidiHeaders;
    }

    @Override
    public String id() {
        return this.id;
    }
}
