package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.bidi.entities.NavigationInfo;
import com.ruiyun.jvppeteer.bidi.entities.ResponseData;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.common.WaitForOptions;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


import static com.ruiyun.jvppeteer.bidi.core.BidiRequest.requests;

public class BidiLifeCycleWatch {

    private final BidiFrame frame;
    private final WaitForOptions options;
    private final AwaitableResult<Navigation> navigationWaitFor = new AwaitableResult<>();
    private final AwaitableResult<Boolean> loadWaitFor = new AwaitableResult<>();
    private final AwaitableResult<Boolean> domWaitFor = new AwaitableResult<>();
    private final AwaitableResult<Boolean> detachedWaitFor = new AwaitableResult<>();
    private final AwaitableResult<String> navigationFailedWaitFor = new AwaitableResult<>();
    private final AwaitableResult<Navigation> navigationFragmentWaitFor = new AwaitableResult<>();
    private final AwaitableResult<String> navigationErrorWaitFor = new AwaitableResult<>();
    private final AwaitableResult<Boolean> requestWaitFor = new AwaitableResult<>();
    private BidiResponse response;
    private final Consumer<Frame> detachedListener;
    private volatile boolean started;

    public BidiLifeCycleWatch(BidiFrame frame, WaitForOptions options) {
        this.frame = frame;
        this.options = options;
        this.detachedListener = detachedFrame -> {
            if (detachedFrame == this.frame || this.frame.detached()) {
                detachedWaitFor.complete(true);
            }
        };
        this.startWatch();
    }

    public void startWatch() {
        ValidateUtil.assertArg(!this.frame.detached(), "Attempted to use detached Frame " + this.frame.id());
        this.frame.page().trustedEmitter().once(PageEvents.FrameDetached, detachedListener);
        Consumer<Navigation> navigationConsumer = navigation -> {
            this.completeNavigation(navigation);
            navigation.once(Navigation.NavigationEvents.fragment, (Consumer<NavigationInfo>) info -> {
                this.navigationFragmentWaitFor.complete(navigation);
            });
            navigation.once(Navigation.NavigationEvents.failed, (Consumer<NavigationInfo>) info -> {
                this.navigationFailedWaitFor.complete("Navigation fail: " + info.getUrl());
            });
            navigation.once(Navigation.NavigationEvents.aborted, (Consumer<NavigationInfo>) info -> {
                this.navigationErrorWaitFor.complete("Navigation aborted: " + info.getUrl());
            });
        };
        this.frame.browsingContext.once(BrowsingContext.BrowsingContextEvents.navigation, navigationConsumer);

        //waitForLoad
        List<PuppeteerLifeCycle> waitUntil = Objects.nonNull(options.getWaitUntil()) ? options.getWaitUntil() : Collections.singletonList(PuppeteerLifeCycle.load);
        if (ValidateUtil.isEmpty(waitUntil)) {
            waitUntil = Collections.singletonList(PuppeteerLifeCycle.load);
        }
        Consumer<Object> loadListener = event -> this.completeLoad();
        Consumer<Object> domListener = event -> this.completeDom();
        waitUntil.forEach(value -> {
            switch (value) {
                case load:
                    this.frame.browsingContext.once(BrowsingContext.BrowsingContextEvents.load, loadListener);
                    break;
                case domcontentloaded:
                    this.frame.browsingContext.once(BrowsingContext.BrowsingContextEvents.DOMContentLoaded, domListener);
                    break;
            }
        });
        if (!waitUntil.contains(PuppeteerLifeCycle.load)) {
            this.completeLoad();
        }
        if (!waitUntil.contains(PuppeteerLifeCycle.domcontentloaded)) {
            this.completeDom();
        }
    }

    private void completeNavigation(Navigation result) {
        //System.out.println("completeNavigation");
        this.navigationWaitFor.complete(result);
    }

    private void completeLoad() {
       // System.out.println("completeLoad");
        this.loadWaitFor.complete(true);
    }

    private void completeDom() {
       // System.out.println("completeDom");
        this.domWaitFor.complete(true);
    }

    private void completeRequest() {
       // System.out.println("completeRequest");
        this.requestWaitFor.complete(true);
    }

    public boolean loadFinished() {
        return loadWaitFor.isDone() && domWaitFor.isDone();
    }

    public boolean requestFinished() {
        return this.requestWaitFor.isDone();
    }

    private void requestFinished(RequestCore request) {
        // Reduces flakiness if the response events arrive after
        // the load event.
        // Usually, the response or error is already there at this point.
        if (Objects.nonNull(request)) {
            if (Objects.nonNull(request.response()) || Objects.nonNull(request.error())) {
                this.completeRequest();
            }
            if (Objects.nonNull(request.redirect())) {
                requestFinished(request.redirect());
            }
            request.once(RequestCore.RequestCoreEvents.success, (Consumer<ResponseData>) info -> {
                this.completeRequest();
            });
            request.once(RequestCore.RequestCoreEvents.error, (Consumer<String>) errorMsg -> {
                this.completeRequest();
            });
            request.once(RequestCore.RequestCoreEvents.redirect, (Consumer<RequestCore>) requestCore -> {
                this.completeRequest();
            });

        }
        this.completeRequest();
    }

    public boolean navigationFinished() {
        return this.navigationWaitFor.isDone();
    }

    public boolean checkNavigationFinished() {
        if (this.detachedWaitFor.isDone()) {
            throw new JvppeteerException("Frame detached.");
        }
        if (this.navigationFragmentWaitFor.isDone()) {
            RequestCore request = this.navigationFragmentWaitFor.get().request();
            if (Objects.isNull(request)) {
                this.response = null;
            }else {
                RequestCore lastRequest = request.lastRedirect();
                if (Objects.isNull(lastRequest)) {
                    lastRequest = request;
                }
                BidiRequest httpRequest = requests.get(lastRequest);
                this.response = httpRequest.response();
            }
            return true;
        }
        if (this.navigationFailedWaitFor.isDone()) {
            throw new JvppeteerException(this.navigationFailedWaitFor.get());
        }
        if (this.navigationErrorWaitFor.isDone()) {
            throw new JvppeteerException(this.navigationErrorWaitFor.get());
        }
        if (navigationFinished() && loadFinished() && requestFinished()) {
            RequestCore request = this.navigationWaitFor.get().request();
            if (Objects.isNull(request)) {
                this.response = null;
            }else {
                RequestCore lastRequest = request.lastRedirect();
                if (Objects.isNull(lastRequest)) {
                    lastRequest = request;
                }
                BidiRequest httpRequest = requests.get(lastRequest);
                this.response = httpRequest.response();
            }
            return true;
        }
        if (this.navigationWaitFor.isDone()) {
            if (!this.started) {
                requestFinished(this.navigationWaitFor.get().request());
                this.started = true;
            }
        }
        return false;
    }

    public BidiResponse getResponse() {
        return this.response;
    }

    public void dispose() {
        this.frame.page().trustedEmitter().off(PageEvents.FrameDetached, detachedListener);
    }
}
