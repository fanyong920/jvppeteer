package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.AddPreloadScriptOptions;
import com.ruiyun.jvppeteer.bidi.events.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.RealmInfo;
import com.ruiyun.jvppeteer.bidi.entities.RealmType;
import com.ruiyun.jvppeteer.bidi.events.ContextCreatedEvent;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BrowserCore extends EventEmitter<BrowserCore.BrowserCoreEvent> {
    private final Session session;
    private final Map<String, SharedWorkerRealm> sharedWorkers = new HashMap<>();
    private final Map<String, UserContext> userContexts = new ConcurrentHashMap<>();
    private volatile boolean closed;
    private String reason;
    private final List<DisposableStack<?>> disposables = new ArrayList<>();

    public BrowserCore(Session session) {
        super();
        this.session = session;
    }

    public static BrowserCore from(Session session) {
        BrowserCore browser = new BrowserCore(session);
        browser.initialize();
        return browser;
    }

    private void initialize() {
        Consumer<ClosedEvent> closedEventConsumer = event -> {
            this.dispose(event.getReason(), false);
        };
        this.session.on(ConnectionEvents.ended, closedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session, ConnectionEvents.ended, closedEventConsumer));
        Consumer<RealmInfo> realmCreatedEventConsumer = info -> {
            if (!RealmType.SharedWorker.equals(info.getType())) {
                return;
            }
            this.sharedWorkers.put(info.getRealm(), SharedWorkerRealm.from(this, info.getRealm(), info.getOrigin()));
        };
        session.on(ConnectionEvents.script_realmCreated, realmCreatedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session, ConnectionEvents.script_realmCreated, realmCreatedEventConsumer));
        this.syncUserContexts();
        this.syncBrowsingContexts();
    }

    private void syncBrowsingContexts() {
        Set<String> contextIds = new HashSet<>();
        List<ContextCreatedEvent> contexts = new ArrayList<>();
        Consumer<ContextCreatedEvent> contextCreatedConsumer = info -> {
            contextIds.add(info.getContext());
        };
        try {
            this.session.on(ConnectionEvents.browsingContext_contextCreated, contextCreatedConsumer);
            JsonNode res = this.session.send("browsingContext.getTree", new HashMap<>());
            res.at("/result/contexts").elements().forEachRemaining(ele -> {
                try {
                    ContextCreatedEvent info = Constant.OBJECTMAPPER.treeToValue(ele, ContextCreatedEvent.class);
                    if (!contextIds.contains(info.getContext())) {
                        this.session.emit(ConnectionEvents.browsingContext_contextCreated, info);
                    }
                    if (ValidateUtil.isNotEmpty(info.getChildren())) {
                        contexts.addAll(info.getChildren());
                    }
                } catch (JsonProcessingException e) {
                    throw new JvppeteerException(e);
                }
            });
        } finally {
            this.session.off(ConnectionEvents.browsingContext_contextCreated, contextCreatedConsumer);
        }

        // Simulating events so contexts are created naturally.
        Iterator<ContextCreatedEvent> iterator = contexts.iterator();
        while (iterator.hasNext()) {
            ContextCreatedEvent info = iterator.next();
            if (!contextIds.contains(info.getContext())) {
                this.session.emit(ConnectionEvents.browsingContext_contextCreated, info);
            }
            if (ValidateUtil.isNotEmpty(info.getChildren())) {
                contexts.addAll(info.getChildren());
            }
        }
    }

    private void syncUserContexts() {
        JsonNode res = this.session.send("browser.getUserContexts", new HashMap<>());
        JsonNode userContexts = res.at("/result/userContexts");
        Iterator<JsonNode> elements = userContexts.elements();
        while (elements.hasNext()) {
            JsonNode context = elements.next();
            this.createUserContext(context.get("userContext"));
        }
    }

    private UserContext createUserContext(JsonNode id) {
        UserContext userContext = UserContext.create(this, id.asText());
        this.userContexts.put(id.asText(), userContext);
        Consumer<ClosedEvent> closeListener = (ignore) -> {
            userContext.removeAllListeners(null);
            this.userContexts.remove(id.asText());
        };
        userContext.once(UserContext.UserContextEvent.closed, closeListener);
        this.disposables.add(new DisposableStack<>(userContext, UserContext.UserContextEvent.closed, closeListener));
        return userContext;
    }

    public boolean closed() {
        return this.closed;
    }

    public UserContext defaultUserContext() {
        return this.userContexts.get(UserContext.DEFAULT);
    }

    public boolean disconnected() {
        return this.reason != null;
    }

    public boolean disposed() {
        return this.disconnected();
    }

    public List<UserContext> userContexts() {
        return new ArrayList<>(this.userContexts.values());
    }

    public void dispose(String reason, boolean closed) {
        this.closed = closed;
        this.reason = reason;
        this.disposeSymbol();
    }

    public void disposeSymbol() {
        if (StringUtil.isEmpty(this.reason)) {
            this.reason = "Browser was disconnected, probably because the session ended.";
        }
        ClosedEvent closedEvent = new ClosedEvent(this.reason);
        if (this.closed) {
            this.emit(BrowserCoreEvent.closed, closedEvent);
        }
        this.emit(BrowserCoreEvent.disconnected, closedEvent);
        for (DisposableStack stack : this.disposables) {
            stack.getEmitter().off(stack.getType(), stack.getConsumer());
        }
        super.disposeSymbol();
    }

    public void close() {
        ValidateUtil.assertArg(Objects.isNull(this.reason), "Browser already disposed");
        try {
            this.session.send("browser.close", new HashMap<>());
        } finally {
            this.dispose("Browser already closed.", true);
        }
    }

    public String addPreloadScript(String functionDeclaration, AddPreloadScriptOptions options) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("functionDeclaration", functionDeclaration);
        params.put("arguments", options.getArguments());
        params.put("sandbox", options.getSandbox());
        if(ValidateUtil.isNotEmpty(options.getContexts())){
            params.put("contexts", (options.getContexts()));
        }
        JsonNode res = this.session().send("script.addPreloadScript", params);
        return res.at("/result/script").asText();
    }

    public void removeIntercept(String intercept) {
        ValidateUtil.assertArg(Objects.isNull(this.reason), "Browser has been disposed");
        this.session.send("script.removeIntercept", new HashMap<String, Object>() {
            {
                put("intercept", intercept);
            }
        });
    }

    public void removePreloadScript(String script) {
        ValidateUtil.assertArg(Objects.isNull(this.reason), "Browser has been disposed");
        this.session.send("script.removePreloadScript", new HashMap<String, Object>() {
            {
                put("script", script);
            }
        });
    }

    public UserContext createUserContext() {
        ValidateUtil.assertArg(Objects.isNull(this.reason), "Browser has been disposed");
        JsonNode res = this.session.send("browser.createUserContext", Collections.EMPTY_MAP);
        return this.createUserContext(res.at("/result/userContext"));
    }

    Session session() {
        return this.session;
    }

    public enum BrowserCoreEvent {
        /**
         * ClosedEvent
         */
        closed,
        /**
         * Object
         */
        disconnected,
        sharedworker
    }
}
