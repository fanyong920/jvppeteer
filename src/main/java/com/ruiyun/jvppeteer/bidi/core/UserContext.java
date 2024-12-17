package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.CreateBrowsingContextOptions;
import com.ruiyun.jvppeteer.bidi.entities.CreateType;
import com.ruiyun.jvppeteer.bidi.entities.GetCookiesOptions;
import com.ruiyun.jvppeteer.bidi.entities.PartialCookie;
import com.ruiyun.jvppeteer.bidi.entities.PermissionState;
import com.ruiyun.jvppeteer.bidi.events.ContextCreatedEvent;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UserContext extends EventEmitter<UserContext.UserContextEvent> {
    static final String DEFAULT = "default";
    private final String id;
    BrowserCore browser;
    private final Map<String, BrowsingContext> browsingContexts = new ConcurrentHashMap<>();
    private String reason;
    private final Map<EventEmitter<?>, Consumer<?>> disposables = new HashMap<>();

    public UserContext(BrowserCore browserCore, String id) {
        super();
        this.id = id;
        this.browser = browserCore;
    }

    public static UserContext create(BrowserCore browserCore, String id) {
        UserContext context = new UserContext(browserCore, id);
        context.initialize();
        return context;
    }

    private void initialize() {
        Consumer<ClosedEvent> closedListener = event -> this.dispose("User context was closed: " + event.getReason());
        this.browser.on(BrowserCore.BrowserCoreEvent.closed, closedListener);
        this.disposables.put(this.browser, closedListener);
        Consumer<ClosedEvent> disconnectListener = event -> this.dispose("User context was closed: " + event.getReason());
        this.browser.on(BrowserCore.BrowserCoreEvent.disconnected, disconnectListener);
        this.disposables.put(this.browser, disconnectListener);
        Consumer<ContextCreatedEvent> contextCreatedListener = info -> {
            if (Objects.nonNull(info.getParent())) {
                return;
            }
            if (!Objects.equals(info.getUserContext(), this.id)) {
                return;
            }
            BrowsingContext browsingContext = BrowsingContext.from(this, null, info.getContext(), info.getUrl(), info.getOriginalOpener());
            if (Objects.isNull(info.getContext())) {
                this.browsingContexts.put("null", browsingContext);
            } else {
                this.browsingContexts.put(info.getContext(), browsingContext);
            }
            Consumer<Object> closedConsumer = ignored -> {
                browsingContext.removeAllListeners(null);
                if (Objects.isNull(info.getContext())) {
                    this.browsingContexts.remove("null");
                } else {
                    this.browsingContexts.remove(info.getContext());
                }
            };
            browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, closedConsumer);
            this.emit(UserContextEvent.browsingcontext, browsingContext);
            this.disposables.put(browsingContext, closedConsumer);
        };
        this.session().on(ConnectionEvents.browsingContext_contextCreated, contextCreatedListener);
        this.disposables.put(this.session(), contextCreatedListener);
    }

    public Session session() {
        return this.browser.session();
    }

    public List<BrowsingContext> browsingContexts() {
        return new ArrayList<>(this.browsingContexts.values());
    }

    public boolean closed() {
        return this.reason != null;
    }

    public boolean disposed() {
        return this.closed();
    }

    public String id() {
        return this.id;
    }

    public void dispose(String reason) {
        this.reason = reason;
        this.disposeSymbol();
    }

    public void disposeSymbol() {
        if (StringUtil.isEmpty(this.reason)) {
            this.reason = "User context already closed, probably because the browser disconnected/closed.";
        }
        ClosedEvent closedEvent = new ClosedEvent(this.reason);
        this.emit(UserContextEvent.closed, closedEvent);
        for (Map.Entry<EventEmitter<?>, Consumer<?>> entry : this.disposables.entrySet()) {
            EventEmitter<?> emitter = entry.getKey();
            Consumer<?> consumer = entry.getValue();
            emitter.off(null, consumer);
        }
        super.disposeSymbol();
    }


    public BrowsingContext createBrowsingContext(CreateType type, CreateBrowsingContextOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), "User context already closed, probably because the browser disconnected/closed.");
        Map<String, Object> params = ParamsFactory.create();
        params.put("type", type.name().toLowerCase());
        params.put("background", options.getBackground());
        params.put("userContext", this.id);
        params.put("referenceContext", options.getReferenceContext());
        JsonNode res = this.session().send("browsingContext.create", params);
        String contextId = res.at("/result/context").asText();
        Supplier<BrowsingContext> conditionChecker = () -> this.browsingContexts.getOrDefault(contextId, null);
        return Helper.waitForCondition(conditionChecker, Constant.DEFAULT_TIMEOUT, "The WebDriver BiDi implementation is failing to create a browsing context correctly.");
    }

    public void remove() {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), "User context already closed, probably because the browser disconnected/closed.");
        Map<String, Object> params = ParamsFactory.create();
        params.put("userContext", this.id);
        try {
            this.session().send("browser.removeUserContext", params);
        } finally {
            this.dispose("User context already closed.");
        }

    }

    public List<JsonNode> getCookies(GetCookiesOptions options, String sourceOrigin) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), "User context already closed, probably because the browser disconnected/closed.");
        Map<String, Object> params = ParamsFactory.create();
        params.put("filter", options.getFilter());
        ObjectNode partition = Constant.OBJECTMAPPER.createObjectNode();
        partition.put("type", "storageKey");
        partition.put("userContext", this.id);
        partition.put("sourceOrigin", sourceOrigin);
        params.put("partition", partition);
        JsonNode res = this.session().send("storage.getCookies", params);
        JsonNode cookies = res.at("/result/cookies");
        List<JsonNode> cookiesList = new ArrayList<>();
        cookies.elements().forEachRemaining(cookiesList::add);
        return cookiesList;
    }

    public void setCookie(PartialCookie cookie, String sourceOrigin) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("cookie", cookie);
        ObjectNode partition = Constant.OBJECTMAPPER.createObjectNode();
        partition.put("type", "storageKey");
        partition.put("userContext", this.id);
        partition.put("sourceOrigin", sourceOrigin);
        params.put("partition", partition);
        this.session().send("storage.setCookie", params);
    }

    public void setPermissions(String origin, String name, PermissionState state) {
        Map<String, Object> params = ParamsFactory.create();
        ObjectNode descriptor = Constant.OBJECTMAPPER.createObjectNode();
        descriptor.put("name", name);
        params.put("origin", origin);
        params.put("descriptor", descriptor);
        params.put("state", state);
        params.put("userContext", this.id);
        this.session().send("permissions.setPermission", params);
    }

    public enum UserContextEvent {
        /**
         * {@link BrowsingContext}
         */
        browsingcontext,
        /**
         * CloseEvent.class
         */
        closed
    }
}
