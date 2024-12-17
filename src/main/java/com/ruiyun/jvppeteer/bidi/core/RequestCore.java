package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.AuthCredentials;
import com.ruiyun.jvppeteer.bidi.entities.BaseParameters;
import com.ruiyun.jvppeteer.bidi.entities.BeforeRequestSentParameters;
import com.ruiyun.jvppeteer.bidi.entities.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.FetchErrorParameters;
import com.ruiyun.jvppeteer.bidi.entities.FetchTimingInfo;
import com.ruiyun.jvppeteer.bidi.entities.Header;
import com.ruiyun.jvppeteer.bidi.entities.Initiator;
import com.ruiyun.jvppeteer.bidi.entities.ResponseCompletedParameters;
import com.ruiyun.jvppeteer.bidi.entities.ResponseData;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class RequestCore extends EventEmitter<RequestCore.RequestCoreEvents> {
    private volatile String error;
    private volatile RequestCore redirect;
    private volatile ResponseData response;
    private final BrowsingContext browsingContext;
    private final List<DisposableStack<?>> disposables = new ArrayList<>();
    private final BeforeRequestSentParameters event;
    private volatile boolean disposed;

    public RequestCore(BrowsingContext browsingContext, BeforeRequestSentParameters event) {
        super();
        this.browsingContext = browsingContext;
        this.event = event;
    }

    public static RequestCore from(BrowsingContext browsingContext, BeforeRequestSentParameters event) {
        RequestCore request = new RequestCore(browsingContext, event);
        request.initialize();
        return request;
    }

    private void initialize() {
        Consumer<ClosedEvent> closedConsumer = event -> {
            this.error = event.getReason();
            this.emit(RequestCoreEvents.error, this.error);
            this.dispose();
        };
        this.browsingContext.once(BrowsingContext.BrowsingContextEvents.closed, closedConsumer);
        this.disposables.add(new DisposableStack<>(this.browsingContext, BrowsingContext.BrowsingContextEvents.closed, closedConsumer));

        Consumer<BaseParameters> beforeRequestSentConsumer = event -> {
            if (!Objects.equals(event.getContext(), this.browsingContext.id())
                    || !Objects.equals(event.getRequest().getRequest(), this.id())
                    || event.getRedirectCount() != this.event.getRedirectCount() + 1) {
                return;
            }
            this.redirect = RequestCore.from(this.browsingContext, Constant.OBJECTMAPPER.convertValue(event, BeforeRequestSentParameters.class));
            this.emit(RequestCoreEvents.redirect, this.redirect);
            this.dispose();
        };
        this.session().on(ConnectionEvents.network_beforeRequestSent, beforeRequestSentConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.network_beforeRequestSent, beforeRequestSentConsumer));

        Consumer<BaseParameters> authRequiredConsumer = event -> {
            if (!Objects.equals(event.getContext(), this.browsingContext.id())
                    || !Objects.equals(event.getRequest().getRequest(), this.id())
                    ||
                    // Don't try to authenticate for events that are not blocked
                    !event.getIsBlocked()) {
                return;
            }
            this.emit(RequestCoreEvents.authenticate, true);
        };
        this.session().on(ConnectionEvents.network_authRequired, authRequiredConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.network_authRequired, authRequiredConsumer));

        Consumer<FetchErrorParameters> fetchErrorConsumer = event -> {
            if (!Objects.equals(event.getContext(), this.browsingContext.id()) || !Objects.equals(event.getRequest().getRequest(), this.id()) || this.event.getRedirectCount() != event.getRedirectCount()) {
                return;
            }
            this.error = event.getErrorText();
            this.emit(RequestCoreEvents.error, this.error);
            this.dispose();
        };
        this.session().on(ConnectionEvents.network_fetchError, fetchErrorConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.network_fetchError, fetchErrorConsumer));

        Consumer<ResponseCompletedParameters> responseCompletedConsumer = event -> {
            if (!Objects.equals(event.getContext(), this.browsingContext.id())
                    || !Objects.equals(event.getRequest().getRequest(), this.id())
                    || this.event.getRedirectCount() != event.getRedirectCount()) {
                return;
            }
            this.response = event.getResponse();
            this.event.getRequest().setTimings(event.getRequest().getTimings());
            this.emit(RequestCoreEvents.success, this.response);
            // In case this is a redirect.
            if (this.response.getStatus() >= 300 && this.response.getStatus() < 400) {
                return;
            }
            this.dispose();
        };
        this.session().on(ConnectionEvents.network_responseCompleted, responseCompletedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.network_responseCompleted, responseCompletedConsumer));
    }

    private void dispose() {
        this.disposeSymbol();
    }

    public Session session() {
        return this.browsingContext.userContext.browser.session();
    }

    public String error() {
        return this.error;
    }

    List<Header> headers() {
        return this.event.getRequest().getHeaders();
    }

    public String id() {
        return this.event.getRequest().getRequest();
    }

    public Initiator initiator() {
        return this.event.getInitiator();
    }

    public String method() {
        return this.event.getRequest().getMethod();
    }

    public String navigation() {
        return this.event.getNavigation();
    }

    public RequestCore redirect() {
        return this.redirect;
    }

    public RequestCore lastRedirect() {
        RequestCore redirect1 = this.redirect();
        while (Objects.nonNull(redirect1)) {
            if (Objects.isNull(redirect1.redirect())) {
                return redirect1;
            }else {
                redirect1 = redirect1.redirect();
            }
        }
        return redirect1;
    }

    public ResponseData response() {
        return this.response;
    }

    public String url() {
        return this.event.getRequest().getUrl();
    }

    public boolean isBlocked() {
        return this.event.getIsBlocked();
    }

    public String resourceType() {
        return this.event.getRequest().getResourceType();
    }

    public String postData() {
        return this.event.getRequest().getPostData();
    }

    public boolean hasPostData() {
        return this.event.getRequest().getHasPostData();
    }

    /**
     * @param url     连接
     * @param method  方法
     * @param headers 头
     * @param cookies cookies
     * @param body    可能要base64
     */
    public void continueRequest(String url, String method, List<Header> headers, List<HeaderEntry> cookies, String body) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("request", this.id());
        params.put("url", url);
        params.put("method", method);
        if(ValidateUtil.isNotEmpty(headers)){
            params.put("headers", headers);
        }
        params.put("cookies", cookies);
        ObjectNode bodyNode = Constant.OBJECTMAPPER.createObjectNode();
        if(StringUtil.isNotEmpty(body)){
            bodyNode.put("type", "base64");
            bodyNode.put("value", body);
            params.put("body", bodyNode);
        }
        this.session().send("network.continueRequest", params);
    }

    public void failRequest() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("request", this.id());
        this.session().send("network.failRequest", params);
    }

    public void provideResponse(int statusCode, String reasonPhrase, List<Header> headers, String body) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("request", this.id());
        params.put("statusCode", statusCode);
        params.put("reasonPhrase", reasonPhrase);
        params.put("headers", headers);
        if(Objects.nonNull(body)) {
            ObjectNode bodyNode = Constant.OBJECTMAPPER.createObjectNode();
            bodyNode.put("type", "base64");
            bodyNode.put("value", body);
            params.put("body", bodyNode);
        }
        this.session().send("network.provideResponse", params);
    }

    public void continueWithAuth(String action, AuthCredentials credentials) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("request", this.id());
        params.put("action", action);
        this.session().send("network.continueWithAuth", params);
        if (Objects.equals("provideCredentials", action)) {
            params.put("credentials", credentials);
        }
        this.session().send("network.continueWithAuth", params);
    }


    public boolean disposed() {
        return this.disposed;
    }

    public void disposeSymbol() {
        this.disposed = true;
        for (DisposableStack stack : this.disposables) {
            stack.getEmitter().off(stack.getType(), stack.getConsumer());
        }
        super.disposeSymbol();
    }

    public FetchTimingInfo timing() {
        return this.event.getRequest().getTimings();
    }

    public enum RequestCoreEvents {
        /**
         * RequestCore
         */
        redirect,
        /**
         * true
         */
        authenticate,
        /**
         * ResponseData.class
         */
        success,
        /**
         * string
         */
        error
    }
}
