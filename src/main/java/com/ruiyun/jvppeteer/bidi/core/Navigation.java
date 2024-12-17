package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.NavigationInfo;
import com.ruiyun.jvppeteer.bidi.events.NavigationInfoEvent;
import com.ruiyun.jvppeteer.common.DisposableStack;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Navigation extends EventEmitter<Navigation.NavigationEvents> {
    private final BrowsingContext browsingContext;
    private volatile RequestCore request;
    private volatile Navigation navigation;
    private final List<DisposableStack<?>> disposables = new ArrayList<>();
    private String id;
    private volatile boolean disposed;

    public Navigation(BrowsingContext context) {
        super();
        this.browsingContext = context;
    }

    public static Navigation from(BrowsingContext context) {
        Navigation navigation = new Navigation(context);
        navigation.initialize();
        return navigation;
    }

    private void initialize() {
        Consumer<Object> closeConsumer = (ignored) -> {
            NavigationInfo info = new NavigationInfo();
            info.setUrl(this.browsingContext.url());
            info.setTimestamp(new Date());
            this.emit(NavigationEvents.failed, info);
            this.disposed();
        };
        this.browsingContext.once(BrowsingContext.BrowsingContextEvents.closed, closeConsumer);
        this.disposables.add(new DisposableStack<>(this.browsingContext, BrowsingContext.BrowsingContextEvents.closed, closeConsumer));

        Consumer<RequestCore> requestCoreConsumer = request -> {
            if (Objects.isNull(request.navigation()) || !this.matches(request.navigation())) {
                return;
            }
            this.request = request;
            this.emit(NavigationEvents.request, request);
            Consumer<RequestCore> reqConsumer = req -> {
                this.request = req;
            };
            this.request.on(RequestCore.RequestCoreEvents.redirect, reqConsumer);
            this.disposables.add(new DisposableStack<>(this.request, RequestCore.RequestCoreEvents.redirect, reqConsumer));
        };
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.request, requestCoreConsumer);
        this.disposables.add(new DisposableStack<>(this.browsingContext, BrowsingContext.BrowsingContextEvents.request, requestCoreConsumer));

        Consumer<NavigationInfoEvent> navigationInfoEventConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || Objects.nonNull(this.navigation)) {
                return;
            }
            this.navigation = Navigation.from(this.browsingContext);
        };
        this.session().on(ConnectionEvents.browsingContext_navigationStarted, navigationInfoEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_navigationStarted, navigationInfoEventConsumer));

        Consumer<NavigationInfoEvent> domContentLoadedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || Objects.isNull(info.getNavigation()) || !this.matches(info.getNavigation())) {
                return;
            }
            this.dispose();
        };
        this.session().on(ConnectionEvents.browsingContext_domContentLoaded, domContentLoadedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_domContentLoaded, domContentLoadedConsumer));

        Consumer<NavigationInfoEvent> domConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || Objects.isNull(info.getNavigation()) || !this.matches(info.getNavigation())) {
                return;
            }
            this.dispose();
        };
        this.session().on(ConnectionEvents.browsingContext_load, domConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_load, domConsumer));

        Consumer<NavigationInfoEvent> fragmentNavigatedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || !this.matches(info.getNavigation())) {
                return;
            }
            NavigationInfo navigationInfo = new NavigationInfo();
            navigationInfo.setUrl(info.getUrl());
            navigationInfo.setTimestamp(new Date());
            this.emit(NavigationEvents.fragment, navigationInfo);
            this.dispose();
        };
        this.session().on(ConnectionEvents.browsingContext_fragmentNavigated, fragmentNavigatedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_fragmentNavigated, fragmentNavigatedConsumer));

        Consumer<NavigationInfoEvent> navigationFailedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || !this.matches(info.getNavigation())) {
                return;
            }
            NavigationInfo navigationInfo = new NavigationInfo();
            navigationInfo.setUrl(info.getUrl());
            navigationInfo.setTimestamp(new Date());
            this.emit(NavigationEvents.failed, navigationInfo);
            this.dispose();
        };
        this.session().on(ConnectionEvents.browsingContext_navigationFailed, navigationFailedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_navigationFailed, navigationFailedConsumer));

        Consumer<NavigationInfoEvent> navigationAbortedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.browsingContext.id()) || !this.matches(info.getNavigation())) {
                return;
            }
            NavigationInfo navigationInfo = new NavigationInfo();
            navigationInfo.setUrl(info.getUrl());
            navigationInfo.setTimestamp(new Date());
            this.emit(NavigationEvents.aborted, navigationInfo);
            this.dispose();
        };
        this.session().on(ConnectionEvents.browsingContext_navigationAborted, navigationAbortedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_navigationAborted, navigationAbortedConsumer));
    }

    private boolean matches(String navigation) {
        if (Objects.nonNull(this.navigation) && !this.navigation.disposed) {
            return false;
        }
        if (Objects.isNull(this.id)) {
            this.id = navigation;
            return true;
        }
        return Objects.equals(this.id, navigation);
    }

    public Session session() {
        return this.browsingContext.userContext.browser.session();
    }

    public boolean disposed() {
        return this.disposed;
    }

    public RequestCore request() {
        return this.request;
    }

    public Navigation navigation() {
        return this.navigation;
    }

    public void dispose() {
        this.disposeSymbol();
    }

    public void disposeSymbol() {
        this.disposed = true;
        for (DisposableStack stack : this.disposables) {
            stack.getEmitter().off(stack.getType(), stack.getConsumer());
        }
        super.disposeSymbol();
    }

    public enum NavigationEvents {
        /**
         * Emitted when navigation has a request associated with it.
         */
        request,
        /** Emitted when fragment navigation occurred. */
        /**
         * NavigationInfo.class
         */
        fragment,
        /** Emitted when navigation failed. */
        /**
         * NavigationInfo.class
         */
        failed,
        /** Emitted when navigation was aborted. */
        /**
         * NavigationInfo.class
         */
        aborted
    }
}
