package com.ruiyun.jvppeteer.bidi.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.BluetoothEmulation;
import com.ruiyun.jvppeteer.api.core.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.AddInterceptOptions;
import com.ruiyun.jvppeteer.bidi.entities.AddPreloadScriptOptions;
import com.ruiyun.jvppeteer.bidi.entities.BaseParameters;
import com.ruiyun.jvppeteer.bidi.entities.BeforeRequestSentParameters;
import com.ruiyun.jvppeteer.bidi.entities.CaptureScreenshotOptions;
import com.ruiyun.jvppeteer.bidi.entities.CookieFilter;
import com.ruiyun.jvppeteer.bidi.entities.GetCookiesOptions;
import com.ruiyun.jvppeteer.bidi.entities.HandleUserPromptOptions;
import com.ruiyun.jvppeteer.bidi.entities.LogEntry;
import com.ruiyun.jvppeteer.bidi.entities.NavigationInfo;
import com.ruiyun.jvppeteer.bidi.entities.PartialCookie;
import com.ruiyun.jvppeteer.bidi.entities.PartitionDescriptor;
import com.ruiyun.jvppeteer.bidi.entities.PrintOptions;
import com.ruiyun.jvppeteer.bidi.entities.ReloadParameters;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.bidi.entities.SetGeoLocationOverrideOptions;
import com.ruiyun.jvppeteer.bidi.entities.SetViewportParameters;
import com.ruiyun.jvppeteer.bidi.entities.SharedReference;
import com.ruiyun.jvppeteer.bidi.entities.SourceActions;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptOpenedParameters;
import com.ruiyun.jvppeteer.bidi.events.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.events.ContextCreatedEvent;
import com.ruiyun.jvppeteer.bidi.events.FileDialogInfo;
import com.ruiyun.jvppeteer.bidi.events.NavigationInfoEvent;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;

public class BrowsingContext extends EventEmitter<BrowsingContext.BrowsingContextEvents> {
    private volatile String url;
    UserContext userContext;
    private final String originalOpener;
    private final String id;
    private final WindowRealm defaultRealm;
    private volatile String reason;
    private final BrowsingContext parent;
    private final Map<String, BrowsingContext> children = new ConcurrentHashMap<>();
    private final List<DisposableStack<?>> disposables = new ArrayList<>();
    private final Map<String, RequestCore> requests = new LinkedHashMap<>();
    private volatile Navigation navigation;
    private BluetoothEmulation bluetoothEmulation;
    private BidiDeviceRequestPromptManager deviceRequestPromptManager;

    private BrowsingContext(UserContext userContext, BrowsingContext parent, String id, String url, String originalOpener) {
        super();
        this.url = url;
        this.id = id;
        this.parent = parent;
        this.userContext = userContext;
        this.originalOpener = originalOpener;
        this.defaultRealm = this.createWindowRealm(null);
        this.bluetoothEmulation = new BidiBluetoothEmulation(this.id, this.session());
        this.deviceRequestPromptManager = new BidiDeviceRequestPromptManager(this.session(), this.id);
    }

    public static BrowsingContext from(UserContext userContext, BrowsingContext parent, String id, String url, String originalOpener) {
        BrowsingContext browsingContext = new BrowsingContext(userContext, parent, id, url, originalOpener);
        browsingContext.initialize();
        return browsingContext;
    }

    private void initialize() {
        Consumer<ClosedEvent> closedEventConsumer = event -> {
            this.dispose("Browsing context already closed:" + event.getReason());
        };
        this.userContext.on(UserContext.UserContextEvent.closed, closedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.userContext, UserContext.UserContextEvent.closed, closedEventConsumer));

        Consumer<FileDialogInfo> fileDialogOpenedConsumer = info -> {
            if (!Objects.equals(this.id, info.getContext())) {
                return;
            }
            this.emit(BrowsingContextEvents.filedialogopened, info);
        };
        this.session().on(ConnectionEvents.input_fileDialogOpened, fileDialogOpenedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.input_fileDialogOpened, fileDialogOpenedConsumer));

        Consumer<ContextCreatedEvent> contextCreatedEventConsumer = info -> {
            if (!Objects.equals(info.getParent(), this.id)) {
                return;
            }
            BrowsingContext browsingContext = BrowsingContext.from(this.userContext, this, info.getContext(), info.getUrl(), info.getOriginalOpener());
            if (Objects.isNull(info.getContext())) {
                this.children.put("null", browsingContext);
            } else {
                this.children.put(info.getContext(), browsingContext);
            }
            Consumer<Object> closedConsumer = ignored -> {
                browsingContext.removeAllListeners(null);
                if (Objects.isNull(browsingContext.id)) {
                    this.children.remove("null");
                } else {
                    this.children.remove(browsingContext.id);
                }
            };
            browsingContext.once(BrowsingContextEvents.closed, closedConsumer);
            this.disposables.add(new DisposableStack<>(browsingContext, BrowsingContextEvents.closed, closedConsumer));
            this.emit(BrowsingContextEvents.browsingcontext, browsingContext);
        };
        this.session().on(ConnectionEvents.browsingContext_contextCreated, contextCreatedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_contextCreated, contextCreatedEventConsumer));

        Consumer<ContextCreatedEvent> contextDestroyedEventConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.id)) {
                return;
            }
            this.dispose("Browsing context already closed.");
        };
        this.session().on(ConnectionEvents.browsingContext_contextDestroyed, contextDestroyedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_contextDestroyed, contextDestroyedEventConsumer));

        Consumer<NavigationInfoEvent> domContentLoadedEventConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.id)) {
                return;
            }
            this.url = info.getUrl();
            this.emit(BrowsingContextEvents.DOMContentLoaded, true);
        };
        this.session().on(ConnectionEvents.browsingContext_domContentLoaded, domContentLoadedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_domContentLoaded, domContentLoadedEventConsumer));

        Consumer<NavigationInfoEvent> loadEventConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.id)) {
                return;
            }
            this.url = info.getUrl();
            this.emit(BrowsingContextEvents.load, true);
        };
        this.session().on(ConnectionEvents.browsingContext_load, loadEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_load, loadEventConsumer));

        Consumer<NavigationInfoEvent> navigationStartedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.id)) {
                return;
            }
            // Note: we should not update this.#url at this point since the context
            // has not finished navigating to the info.url yet.
            Iterator<Map.Entry<String, RequestCore>> iterator = this.requests.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, RequestCore> entry = iterator.next();
                RequestCore request = entry.getValue();
                if (request.disposed()) {
                    iterator.remove();
                }
            }

            // If the navigation hasn't finished, then this is nested navigation. The
            // current navigation will handle this.
            if (Objects.nonNull(this.navigation) && !this.navigation.disposed()) {
                return;
            }

            // Note the navigation ID is null for this event.
            this.navigation = Navigation.from(this);

            Consumer<NavigationInfo> fragmentConsumer = event -> {
                this.navigation.disposeSymbol();
                this.url = event.getUrl();
            };
            this.navigation.once(Navigation.NavigationEvents.fragment, fragmentConsumer);
            this.disposables.add(new DisposableStack<>(this.navigation, Navigation.NavigationEvents.fragment, fragmentConsumer));

            Consumer<NavigationInfo> failedConsumer = event -> {
                this.navigation.disposeSymbol();
                this.url = event.getUrl();
            };
            this.navigation.once(Navigation.NavigationEvents.failed, failedConsumer);
            this.disposables.add(new DisposableStack<>(this.navigation, Navigation.NavigationEvents.failed, failedConsumer));

            Consumer<NavigationInfo> abortedConsumer = event -> {
                this.navigation.disposeSymbol();
                this.url = event.getUrl();
            };
            this.navigation.once(Navigation.NavigationEvents.aborted, abortedConsumer);
            this.disposables.add(new DisposableStack<>(this.navigation, Navigation.NavigationEvents.aborted, abortedConsumer));
            this.emit(BrowsingContextEvents.navigation, this.navigation);
        };
        this.session().on(ConnectionEvents.browsingContext_navigationStarted, navigationStartedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_navigationStarted, navigationStartedConsumer));

        Consumer<BaseParameters> beforeRequestSentConsumer = event -> {
            if (!Objects.equals(event.getContext(), this.id)) {
                return;
            }
            if (this.requests.containsKey(event.getRequest().getRequest())) {
                // Means the request is a redirect. This is handled in Request.
                // Or an Auth event was issued
                return;
            }

            RequestCore request = RequestCore.from(this, Constant.OBJECTMAPPER.convertValue(event, BeforeRequestSentParameters.class));
            this.requests.put(request.id(), request);
            this.emit(BrowsingContextEvents.request, request);
        };
        this.session().on(ConnectionEvents.network_beforeRequestSent, beforeRequestSentConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.network_beforeRequestSent, beforeRequestSentConsumer));

        Consumer<LogEntry> logEntryConsumer = entry -> {
            if (!Objects.equals(entry.getSource().getContext(), this.id)) {
                return;
            }
            this.emit(BrowsingContextEvents.log, entry);
        };
        this.session().on(ConnectionEvents.log_entryAdded, logEntryConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.log_entryAdded, logEntryConsumer));

        Consumer<UserPromptOpenedParameters> userPromptOpenedConsumer = info -> {
            if (!Objects.equals(info.getContext(), this.id)) {
                return;
            }
            UserPrompt userPrompt = UserPrompt.from(this, info);
            this.emit(BrowsingContextEvents.userprompt, userPrompt);
        };
        this.session().on(ConnectionEvents.browsingContext_userPromptOpened, userPromptOpenedConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.browsingContext_userPromptOpened, userPromptOpenedConsumer));
    }

    public Session session() {
        return this.userContext.browser.session();
    }

    public List<BrowsingContext> children() {
        return Collections.unmodifiableList(new ArrayList<>(this.children.values()));

    }

    public boolean closed() {
        return StringUtil.isNotEmpty(this.reason);
    }

    public boolean disposed() {
        return this.closed();
    }

    public String url() {
        return this.url;
    }

    private void dispose(String reason) {
        this.reason = reason;
        for (BrowsingContext context : this.children.values()) {
            context.dispose("Parent browsing context was disposed");
        }
        this.disposeSymbol();
    }

    public void disposeSymbol() {
        if (StringUtil.isEmpty(this.reason)) {
            this.reason = "Browsing context already closed, probably because the user context closed.";
        }
        this.emit(BrowsingContextEvents.closed, new ClosedEvent(this.reason));
        for (DisposableStack disposable : this.disposables) {
            disposable.getEmitter().off(disposable.getType(), disposable.getConsumer());
        }
        super.disposeSymbol();
    }

    public void activate() {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        this.session().send("browsingContext.activate", params);
    }

    public JsonNode captureScreenshot(CaptureScreenshotOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("origin", options.getOrigin());
        params.put("format", options.getFormat());
        params.put("clip", options.getClip());
        return this.session().send("browsingContext.captureScreenshot", params).at("/result/data");
    }

    public void close(boolean promptUnload) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        this.children.values().parallelStream().forEach(child -> child.close(promptUnload));
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("promptUnload", promptUnload);
        this.session().send("browsingContext.close", params);
    }

    public void traverseHistory(int delta) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("delta", delta);
        this.session().send("browsingContext.traverseHistory", params, null, false);
    }

    public void navigate(String url, ReadinessState wait, boolean waitForResult) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), "Attempted to use detached BrowsingContext: " + this.id);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("url", url);
        params.put("wait", wait);
        this.session().send("browsingContext.navigate", params, null, waitForResult);
    }

    public void reload(ReloadParameters options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        options.setContext(this.id);
        this.session().send("browsingContext.reload", options);
    }

    public void setCacheBehavior(String cacheBehavior) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        if (Objects.equals(cacheBehavior, "default") || Objects.equals(cacheBehavior, "bypass")) {
            throw new IllegalArgumentException("cacheBehavior cannot be set to '" + cacheBehavior + "'.");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", Collections.singletonList(this.id));
        params.put("cacheBehavior", cacheBehavior);
        this.session().send("network.setCacheBehavior", params);
    }


    public JsonNode print(PrintOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("background", options.getBackground());
        params.put("margin", options.getMargin());
        params.put("orientation", options.getOrientation());
        params.put("page", options.getPage());
        params.put("pageRanges", options.getPageRanges());
        params.put("scale", options.getScale());
        params.put("shrinkToFit", options.getShrinkToFit());
        return this.session().send("browsingContext.print", params).at("/result/data");
    }

    public void handleUserPrompt(HandleUserPromptOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("accept", options.getAccept());
        params.put("userText", options.getUserText());
        this.session().send("browsingContext.handleUserPrompt", params);

    }

    public void setViewport(SetViewportParameters options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("viewport", options.getViewport());
        params.put("devicePixelRatio", options.getDevicePixelRatio());
        this.session().send("browsingContext.setViewport", params);
    }

    public void performActions(List<SourceActions> actions) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("actions", actions);
        this.session().send("input.performActions", params);
    }

    public void releaseActions() {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        this.session().send("input.releaseActions", params);
    }

    public WindowRealm createWindowRealm(String sandbox) {
        return this._createWindowRealm(sandbox);
    }

    private WindowRealm _createWindowRealm(String sandbox) {
        WindowRealm realm = WindowRealm.from(this, sandbox);
        realm.on(BidiRealmCore.RealmCoreEvents.worker, (Consumer<DedicatedWorkerRealm>) workRealm -> {
            this.emit(BrowsingContextEvents.worker, workRealm);
        });
        return realm;
    }

    public String addPreloadScript(String functionDeclaration, AddPreloadScriptOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        return this.userContext.browser.addPreloadScript(functionDeclaration, options);
    }

    public String addIntercept(AddInterceptOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", Collections.singletonList(this.id));
        params.put("phases", options.getPhases());
        params.put("urlPatterns", options.getUrlPatterns());
        return this.userContext.browser.session().send("network.addIntercept", params).at("/result/intercept").asText();
    }

    public void removePreloadScript(String script) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        this.userContext.browser.removePreloadScript(script);
    }

    public JsonNode getCookies(GetCookiesOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("partition", new PartitionDescriptor("context", this.id, null, null));
        params.put("filter", options.getFilter());
        return this.session().send("storage.getCookies", params).at("/result/cookies");
    }

    public void setCookie(PartialCookie cookie) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("cookie", cookie);
        params.put("partition", new PartitionDescriptor("context", this.id, null, null));
        this.session().send("storage.setCookie", params);
    }

    public void setFiles(SharedReference element, List<String> files) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("element", element);
        params.put("files", files);
        this.session().send("input.setFiles", params);
    }


    public void subscribe(List<String> events) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        this.session().subscribe(events, Collections.singletonList(this.id));
    }

    public void addInterception(List<String> events) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        this.session().subscribe(events, Collections.singletonList(this.id));
    }

    public void deleteCookie(List<CookieFilter> cookieFilters) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        if (ValidateUtil.isNotEmpty(cookieFilters)) {
            for (CookieFilter filter : cookieFilters) {
                Map<String, Object> params = ParamsFactory.create();
                params.put("filter", filter);
                params.put("partition", new PartitionDescriptor("context", this.id, null, null));
                this.session().send("storage.deleteCookies", params);
            }
        }
    }

    public List<RemoteValue> locateNodes(ObjectNode locator, List<SharedReference> startNodes) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.id);
        params.put("locator", locator);
        params.put("startNodes", ValidateUtil.isNotEmpty(startNodes) ? startNodes.size() : null);
        JsonNode nodes = this.session().send("browsingContext.locateNodes", params).at("/result/nodes");
        Iterator<JsonNode> elements = nodes.elements();
        List<RemoteValue> result = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            result.add(Constant.OBJECTMAPPER.convertValue(next, RemoteValue.class));
        }
        return result;
    }

    public void setGeolocationOverride(SetGeoLocationOverrideOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("contexts", Collections.singletonList(this.id));
        params.put("coordinates", options.getCoordinates());
        this.userContext.browser.session().send("emulation.setGeolocationOverride", params);
    }

    public void setTimezoneOverride(String timezoneId) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        if (StringUtil.isNotEmpty(timezoneId) && timezoneId.startsWith("GMT")) {
            // CDP requires `GMT` prefix before timezone offset, while BiDi does not. Remove the
            // `GMT` for interop between CDP and BiDi.
            timezoneId = timezoneId.replace("GMT", "");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("timezoneId", timezoneId);
        params.put("contexts", Collections.singletonList(this.id));
        this.userContext.browser.session().send("emulation.setTimezoneOverride", params);
    }

    String originalOpener() {
        return this.originalOpener;
    }

    String id() {
        return this.id;
    }

    public WindowRealm defaultRealm() {
        return this.defaultRealm;
    }

    public BluetoothEmulation bluetooth() {
        return this.bluetoothEmulation;
    }

    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        return this.deviceRequestPromptManager.waitForDevicePrompt(timeout);
    }

    public void setOfflineMode(boolean enabled){
        Map<String, Object> params = ParamsFactory.create();
        if (enabled) {
            ObjectNode offlineConditions = OBJECTMAPPER.createObjectNode();
            offlineConditions.put("type", "offline");
            params.put("networkConditions", offlineConditions);
        }
        params.put("contexts", Collections.singletonList(this.id));
        this.session().send("Network.setBypassServiceWorker", params);
    }
    public enum BrowsingContextEvents {
        /**
         * ClosedEvent.class
         */
        closed,
        /**
         * Browsingcontext.class
         */
        browsingcontext,
        /**
         * Navigation.class
         */
        navigation,
        /**
         * FileDialogInfo.class
         */
        filedialogopened,
        /**
         * RequestCore
         */
        request,
        /**
         * LogEntry.class
         */
        log,
        /**
         * UserPrompt.class
         */
        userprompt,
        /**
         * void
         */
        DOMContentLoaded,
        /**
         * void
         */
        load,
        /**
         * DedicatedWorkerRealm.class
         */
        worker
    }
}
