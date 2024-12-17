package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.Capabilities;
import com.ruiyun.jvppeteer.bidi.entities.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.NewResult;
import com.ruiyun.jvppeteer.bidi.events.NavigationInfoEvent;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class Session extends EventEmitter<ConnectionEvents> {
    private final BidiConnection connection;
    private final NewResult info;
    BrowserCore browser;
    private String reason;
    private final List<DisposableStack<?>> disposables = new ArrayList<>();

    public Session(BidiConnection connection, NewResult info) {
        super();
        this.info = info;
        this.connection = connection;
        this.connection.pipeTo(this);
    }

    public static Session from(BidiConnection connection, ObjectNode capabilities) throws JsonProcessingException {
        Map<String, Object> params = ParamsFactory.create();
        params.put("capabilities", capabilities);
        JsonNode res = connection.send("session.new", params);
        Session session = new Session(connection, Constant.OBJECTMAPPER.treeToValue(res.get(Constant.RESULT), NewResult.class));
        session.initialize();
        return session;
    }

    private void initialize() {
        this.browser = BrowserCore.from(this);
        Consumer<ClosedEvent> closedEventConsumer = event -> {
            this.dispose(event.getReason());
        };
        this.browser.once(BrowserCore.BrowserCoreEvent.closed, closedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.browser, BrowserCore.BrowserCoreEvent.closed, closedEventConsumer));
        Set<WeakReference<NavigationInfoEvent>> seen = new HashSet<>();
        this.on(ConnectionEvents.browsingContext_fragmentNavigated, (Consumer<NavigationInfoEvent>) event -> {
            WeakReference<NavigationInfoEvent> eventRef = new WeakReference<>(event);
            for (WeakReference<NavigationInfoEvent> weakReference : seen) {
                if (weakReference.get() != null) {
                    if (Objects.equals(weakReference.get(), event)) {
                        return;
                    }
                }
            }
            seen.add(eventRef);
            this.emit(ConnectionEvents.browsingContext_navigationStarted, event);
            this.emit(ConnectionEvents.browsingContext_fragmentNavigated, event);
        });
    }

    private void dispose(String reason) {
        this.reason = reason;
        this.disposeSymbol();
    }

    public void disposeSymbol() {
        if (StringUtil.isEmpty(this.reason)) {
            this.reason = "Session already destroyed, probably because the connection broke.";
        }
        ClosedEvent event = new ClosedEvent(this.reason);
        this.emit(ConnectionEvents.ended, event);
        for (DisposableStack stack : this.disposables) {
            stack.getEmitter().off(stack.getType(), stack.getConsumer());
        }
        super.disposeSymbol();
    }

    public Capabilities capabilities() {
        return this.info.getCapabilities();
    }

    public boolean disposed() {
        return this.ended();
    }

    private boolean ended() {
        return this.reason != null;
    }

    public String id() {
        return this.info.getSessionId();
    }

    public JsonNode send(String method, Object params) {
        return this.send(method, params, null, true);
    }

    public JsonNode send(String method, Object params, Integer timeout, boolean isBlocking) {
        ValidateUtil.assertArg(!this.disposed(), "Session already disposed");
        return this.connection.send(method, params, timeout, isBlocking);
    }

    public void subscribe(List<String> events, List<String> contexts) {
        ValidateUtil.assertArg(!this.disposed(), "Session already disposed");
        Map<String, Object> params = ParamsFactory.create();
        params.put("events", events);
        params.put("contexts", contexts);
        this.send("session.subscribe", params);
    }

    public void addIntercepts(List<String> events, List<String> contexts) {
        ValidateUtil.assertArg(!this.disposed(), "Session already disposed");
        Map<String, Object> params = ParamsFactory.create();
        params.put("events", events);
        params.put("contexts", contexts);
        this.send("session.subscribe", params);
    }

    public void end() {
        try {
            this.send("session.end", new HashMap<>());
        } finally {
            this.dispose("Session already ended.");
        }
    }

    public Connection connection() {
        return this.connection;
    }
}