package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.FilterEntry;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.events.AttachedToTargetEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetDestroyedEvent;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class FirefoxTargetManager extends TargetManager {
    private final Connection connection;
    private final Map<String, TargetInfo> discoveredTargetsByTargetId = new HashMap<>();
    private final Map<String, CdpTarget> availableTargetsByTargetId = new ConcurrentHashMap<>();
    private final Function<Target, Boolean> targetFilterCallback;
    private final TargetFactory targetFactory;
    private final Map<ConnectionEvents, Consumer<?>> listeners = new HashMap<>();
    private final Map<String, CdpTarget> availableTargetsBySessionId = new HashMap<>();
    private Set<String> targetsIdsForInit = new HashSet<>();
    private final AwaitableResult<Boolean> initializeResult = AwaitableResult.create();
    private final Map<CDPSession, Consumer<AttachedToTargetEvent>> attachedToTargetListenersBySession = new WeakHashMap<>();
    private final Map<Connection, Consumer<AttachedToTargetEvent>> attachedToTargetListenersByConnection = new WeakHashMap<>();

    public FirefoxTargetManager(Connection connection, TargetFactory targetFactory, Function<Target, Boolean> targetFilterCallback) {
        super();
        this.connection = connection;
        this.targetFilterCallback = targetFilterCallback;
        this.targetFactory = targetFactory;

        Consumer<TargetCreatedEvent> onTargetCreatedListener = this::onTargetCreated;
        this.connection.on(ConnectionEvents.Target_targetCreated, onTargetCreatedListener);
        this.listeners.put(ConnectionEvents.Target_targetCreated, onTargetCreatedListener);
        Consumer<TargetDestroyedEvent> onTargetDestroyedListener = this::onTargetDestroyed;
        this.connection.on(ConnectionEvents.Target_targetDestroyed, onTargetDestroyedListener);
        this.listeners.put(ConnectionEvents.Target_targetDestroyed, onTargetDestroyedListener);
        Consumer<CdpCDPSession> onSessionDetachedListener = this::onSessionDetached;
        this.connection.on(ConnectionEvents.sessionDetached, onSessionDetachedListener);
        this.setupAttachmentListeners(this.connection);
    }

    @Override
    public Map<String, CdpTarget> getAvailableTargets() {
        return this.availableTargetsByTargetId;
    }

    @Override
    public List<CdpTarget> getChildTargets(CdpTarget target) {
        return new ArrayList<>();
    }

    @Override
    public void initialize() {
        Map<String, Object> params = ParamsFactory.create();
        List<FilterEntry> discoveryFilter = new ArrayList<FilterEntry>() {{
            add(new FilterEntry());
        }};
        params.put("discover", true);
        params.put("filter", discoveryFilter);
        this.connection.send("Target.setDiscoverTargets", params);
        this.targetsIdsForInit = new HashSet<>(this.discoveredTargetsByTargetId.keySet());
        this.initializeResult.waitingGetResult();
    }

    @Override
    public void dispose() {
        listeners.forEach(this.connection::off);
    }

    private void setupAttachmentListeners(Connection session) {
        Consumer<AttachedToTargetEvent> listener = (event) -> this.onAttachedToTarget(session, event);
        assert (!this.attachedToTargetListenersByConnection.containsKey(session));
        this.attachedToTargetListenersByConnection.put(session, listener);
        session.on(ConnectionEvents.Target_attachedToTarget, listener);
    }

    private void setupAttachmentListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> listener = (event) -> this.onAttachedToTarget(session, event);
        assert (!this.attachedToTargetListenersBySession.containsKey(session));
        this.attachedToTargetListenersBySession.put(session, listener);
        session.on(ConnectionEvents.Target_attachedToTarget, listener);
    }

    private void onTargetCreated(TargetCreatedEvent event) {
        if (this.discoveredTargetsByTargetId.containsKey(event.getTargetInfo().getTargetId())) {
            return;
        }
        this.discoveredTargetsByTargetId.put(event.getTargetInfo().getTargetId(), event.getTargetInfo());
        if ("browser".equals(event.getTargetInfo().getType()) && event.getTargetInfo().getAttached()) {
            CdpTarget target = this.targetFactory.create(event.getTargetInfo(), null, null);
            target.initialize();
            this.availableTargetsByTargetId.put(event.getTargetInfo().getTargetId(), target);
            this.finishInitializationIfReady(target.getTargetId());
            return;
        }
        CdpTarget target = this.targetFactory.create(event.getTargetInfo(), null, null);
        if (Objects.nonNull(this.targetFilterCallback) && !this.targetFilterCallback.apply(target)) {
            this.finishInitializationIfReady(event.getTargetInfo().getTargetId());
            return;
        }
        target.initialize();
        this.availableTargetsByTargetId.put(event.getTargetInfo().getTargetId(), target);
        this.emit(TargetManagerEvent.TargetAvailable, target);
        this.finishInitializationIfReady(target.getTargetId());
    }

    private void finishInitializationIfReady(String targetId) {
        this.targetsIdsForInit.remove(targetId);
        if (this.targetsIdsForInit.isEmpty()) {
            this.initializeResult.onSuccess(true);
        }
    }

    private void onTargetDestroyed(TargetDestroyedEvent event) {
        this.discoveredTargetsByTargetId.remove(event.getTargetId());
        this.finishInitializationIfReady(event.getTargetId());
        CdpTarget target = this.availableTargetsByTargetId.get(event.getTargetId());
        if (Objects.nonNull(target)) {
            this.emit(TargetManagerEvent.TargetGone, target);
            this.availableTargetsByTargetId.remove(event.getTargetId());
        }
    }

    public void onSessionDetached(CDPSession session) {
        this.removeSessionListeners(session);
        this.availableTargetsBySessionId.remove(session.id());
    }

    private void removeSessionListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> consumer = this.attachedToTargetListenersBySession.remove(session);
        if (Objects.nonNull(consumer)) {
            session.off(ConnectionEvents.Target_attachedToTarget, consumer);
        }
    }

    private void onAttachedToTarget(CDPSession parentSession, AttachedToTargetEvent event) {
        CDPSession session = handleAttached(event);
        parentSession.emit(ConnectionEvents.CDPSession_Ready, session);
    }

    private void onAttachedToTarget(Connection parentSession, AttachedToTargetEvent event) {
        CDPSession session = handleAttached(event);
        parentSession.emit(ConnectionEvents.CDPSession_Ready, session);
    }

    private CDPSession handleAttached(AttachedToTargetEvent event) {
        TargetInfo targetInfo = event.getTargetInfo();
        CDPSession session = this.connection.session(event.getSessionId());
        Objects.requireNonNull(session, "Session " + event.getSessionId() + " was not created.");
        CdpTarget target = this.availableTargetsByTargetId.get(targetInfo.getTargetId());
        Objects.requireNonNull(target, "Target " + targetInfo.getTargetId() + " is missing");
        ((CdpCDPSession) session).setTarget(target);
        this.setupAttachmentListeners(session);
        this.availableTargetsBySessionId.put(session.id(), this.availableTargetsByTargetId.get(targetInfo.getTargetId()));
        return session;
    }
}
