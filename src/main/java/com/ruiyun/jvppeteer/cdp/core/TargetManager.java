package com.ruiyun.jvppeteer.cdp.core;


import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.FilterEntry;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.events.AttachedToTargetEvent;
import com.ruiyun.jvppeteer.cdp.events.DetachedFromTargetEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetDestroyedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetInfoChangedEvent;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
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

public class TargetManager extends EventEmitter<TargetManager.TargetManagerEvent> {
    private final Connection connection;
    private final Map<String, TargetInfo> discoveredTargetsByTargetId = new HashMap<>();
    private final Map<String, CdpTarget> attachedTargetsByTargetId = new ConcurrentHashMap<>();
    private final Map<String, CdpTarget> attachedTargetsBySessionId = new HashMap<>();
    private final Set<String> ignoredTargets = new HashSet<>();
    private final Function<Target, Boolean> targetFilterCallback;
    private final TargetFactory targetFactory;
    private final Map<CDPSession, Consumer<AttachedToTargetEvent>> attachedToTargetListenersBySession = new WeakHashMap<>();
    private final Map<Connection, Consumer<AttachedToTargetEvent>> attachedToTargetListenersByConnection = new WeakHashMap<>();
    private final Map<CDPSession, Consumer<DetachedFromTargetEvent>> detachedFromTargetListenersBySession = new WeakHashMap<>();
    private final Map<Connection, Consumer<DetachedFromTargetEvent>> detachedFromTargetListenersByConnection = new WeakHashMap<>();
    private final AwaitableResult<Boolean> initializeResult = AwaitableResult.create();
    private final Set<String> targetsIdsForInit = Collections.synchronizedSet(new HashSet<>());
    private final boolean waitForInitiallyDiscoveredTargets;
    private final List<FilterEntry> discoveryFilter = new ArrayList<FilterEntry>() {{
        add(new FilterEntry());
    }};
    private final Map<ConnectionEvents, Consumer<?>> listeners = new HashMap<>();

    public TargetManager(Connection connection, TargetFactory targetFactory, Function<Target, Boolean> targetFilterCallback, boolean waitForInitiallyDiscoveredTargets) {
        super();
        this.connection = connection;
        this.targetFilterCallback = targetFilterCallback;
        this.targetFactory = targetFactory;
        this.waitForInitiallyDiscoveredTargets = waitForInitiallyDiscoveredTargets;
        Consumer<TargetCreatedEvent> onTargetCreatedListener = this::onTargetCreated;
        this.connection.on(ConnectionEvents.Target_targetCreated, onTargetCreatedListener);
        this.listeners.put(ConnectionEvents.Target_targetCreated, onTargetCreatedListener);

        Consumer<TargetDestroyedEvent> onTargetDestroyedListener = this::onTargetDestroyed;
        this.connection.on(ConnectionEvents.Target_targetDestroyed, onTargetDestroyedListener);
        this.listeners.put(ConnectionEvents.Target_targetDestroyed, onTargetDestroyedListener);

        Consumer<TargetInfoChangedEvent> onTargetInfoChangedListener = this::onTargetInfoChanged;
        this.connection.on(ConnectionEvents.Target_targetInfoChanged, onTargetInfoChangedListener);
        this.listeners.put(ConnectionEvents.Target_targetInfoChanged, onTargetInfoChangedListener);

        Consumer<CdpCDPSession> onSessionDetachedListener = this::onSessionDetached;
        this.connection.on(ConnectionEvents.sessionDetached, onSessionDetachedListener);
        this.listeners.put(ConnectionEvents.sessionDetached, onSessionDetachedListener);

        this.setupAttachmentListeners(this.connection);
    }

    public void storeExistingTargetsForInit() {
        if (!this.waitForInitiallyDiscoveredTargets) {
            return;
        }
        this.discoveredTargetsByTargetId.forEach((targetId, targetInfo) -> {
            boolean isPageOrFrame = "page".equals(targetInfo.getType()) || "iframe".equals(targetInfo.getType());
            boolean isExtension = targetInfo.getUrl().startsWith("chrome-extension://");
            CdpTarget targetForFilter = new CdpTarget(targetInfo, null, null, this, null);
            if ((this.targetFilterCallback == null || this.targetFilterCallback.apply(targetForFilter)) && isPageOrFrame && !isExtension) {
                this.targetsIdsForInit.add(targetInfo.getTargetId());
            }
        });
    }

    public void initialize() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("discover", true);
        params.put("filter", this.discoveryFilter);
        this.connection.send("Target.setDiscoverTargets", params);
        this.storeExistingTargetsForInit();
        params.clear();
        params.put("waitForDebuggerOnStart", true);
        params.put("flatten", true);
        params.put("autoAttach", true);
        List<FilterEntry> filter = new ArrayList<>();
        filter.add(new FilterEntry(true, "page"));
        filter.addAll(this.discoveryFilter);
        params.put("filter", filter);
        params.put("waitForDebuggerOnStart", true);
        this.connection.send("Target.setAutoAttach", params);
        this.finishInitializationIfReady(null);
        this.initializeResult.waitingGetResult();
    }

    public void dispose() {
        listeners.forEach(this.connection::off);
        this.removeAttachmentListeners(this.connection);
    }

    public Map<String, CdpTarget> getAvailableTargets() {
        return this.attachedTargetsByTargetId;
    }

    public List<CdpTarget> getChildTargets(CdpTarget target) {
        return target.childTargets();
    }

    private void setupAttachmentListeners(Connection connection) {
        Consumer<AttachedToTargetEvent> listener = (event) -> this.onAttachedToTarget(connection, event);
        ValidateUtil.assertArg(!this.attachedToTargetListenersByConnection.containsKey(connection), "Already attached to connection");
        this.attachedToTargetListenersByConnection.put(connection, listener);
        connection.on(ConnectionEvents.Target_attachedToTarget, listener);
        Consumer<DetachedFromTargetEvent> detachedListener = this::onDetachedFromTarget;
        ValidateUtil.assertArg(!this.detachedFromTargetListenersByConnection.containsKey(connection), "Already attached to connection");
        this.detachedFromTargetListenersByConnection.put(connection, detachedListener);
        connection.on(ConnectionEvents.Target_detachedFromTarget, detachedListener);
    }

    private void onDetachedFromTarget(DetachedFromTargetEvent event) {
        CdpTarget target = this.attachedTargetsBySessionId.remove(event.getSessionId());
        if (target == null) {
            return;
        }
        this.attachedTargetsByTargetId.remove(target.getTargetId());
        this.emit(TargetManagerEvent.TargetGone, target);
    }

    private void onDetachedFromTarget(CDPSession parentSession, DetachedFromTargetEvent event) {
        CdpTarget target = this.attachedTargetsBySessionId.get(event.getSessionId());
        this.attachedTargetsBySessionId.remove(event.getSessionId());
        if (target == null) {
            return;
        }
        ((CdpCDPSession) parentSession).getTarget().removeChildTarget(target);
        this.attachedTargetsByTargetId.remove(target.getTargetId());
        this.emit(TargetManagerEvent.TargetGone, target);
    }

    private void onAttachedToTarget(Connection parentConnection, AttachedToTargetEvent event) {
        TargetInfo targetInfo = event.getTargetInfo();
        CDPSession session = this.connection.session(event.getSessionId());
        if (session == null) {
            throw new JvppeteerException("Session " + event.getSessionId() + " was not created.");
        }
        if (!this.connection.isAutoAttached(targetInfo.getTargetId())) {
            return;
        }
        if ("service_worker".equals(targetInfo.getType())) {
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentConnection, session);
            if (this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId())) {
                return;
            }
            CdpTarget target = this.targetFactory.create(targetInfo, null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.emit(TargetManagerEvent.TargetAvailable, target);
            return;
        }
        boolean isExistingTarget = this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId());
        CdpTarget target = isExistingTarget ? this.attachedTargetsByTargetId.get(targetInfo.getTargetId()) : this.targetFactory.create(targetInfo, session, null);
        if (this.targetFilterCallback != null && !this.targetFilterCallback.apply(target)) {
            this.ignoredTargets.add(targetInfo.getTargetId());
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentConnection, session);
            return;
        }
        this.setupAttachmentListeners(session);
        if (isExistingTarget) {
            ((CdpCDPSession) session).setTarget(target);
            this.attachedTargetsBySessionId.put(session.id(), this.attachedTargetsByTargetId.get(targetInfo.getTargetId()));
        } else {
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.attachedTargetsBySessionId.put(session.id(), target);
        }
        parentConnection.emit(ConnectionEvents.CDPSession_Ready, session);
        this.targetsIdsForInit.remove(target.getTargetId());
        if (!isExistingTarget) {
            this.emit(TargetManagerEvent.TargetAvailable, target);
        }
        this.finishInitializationIfReady(null);
        Map<String, Object> params = ParamsFactory.create();
        params.put("waitForDebuggerOnStart", true);
        params.put("flatten", true);
        params.put("autoAttach", true);
        params.put("filter", this.discoveryFilter);
        try {
            //容易出错，问题不大
            session.send("Target.setAutoAttach", params);
            runIfWaitingForDebugger(session);
        } catch (Exception ignored) {
            //  LOGGER.error("jvppeteer error: ", e);
        }
    }

    private void onAttachedToTarget(CDPSession parentSession, AttachedToTargetEvent event) {
        TargetInfo targetInfo = event.getTargetInfo();
        CDPSession session = this.connection.session(event.getSessionId());
        if (session == null) {
            throw new JvppeteerException("Session " + event.getSessionId() + " was not created.");
        }
        if (!this.connection.isAutoAttached(targetInfo.getTargetId())) {
            return;
        }
        if ("service_worker".equals(targetInfo.getType())) {
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentSession, session);
            if (this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId())) {
                return;
            }
            CdpTarget target = this.targetFactory.create(targetInfo, null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.emit(TargetManagerEvent.TargetAvailable, target);
            return;
        }
        boolean isExistingTarget = this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId());
        CdpTarget target = isExistingTarget ? this.attachedTargetsByTargetId.get(targetInfo.getTargetId()) : this.targetFactory.create(targetInfo, session, parentSession);
        if (this.targetFilterCallback != null && !this.targetFilterCallback.apply(target)) {
            this.ignoredTargets.add(targetInfo.getTargetId());
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentSession, session);
            return;
        }
        this.setupAttachmentListeners(session);
        if (isExistingTarget) {
            ((CdpCDPSession) session).setTarget(target);
            this.attachedTargetsBySessionId.put(session.id(), this.attachedTargetsByTargetId.get(targetInfo.getTargetId()));
        } else {
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.attachedTargetsBySessionId.put(session.id(), target);
        }
        CdpTarget parentTarget = ((CdpCDPSession) parentSession).getTarget();
        parentTarget.addChildTarget(target);
        parentSession.emit(ConnectionEvents.CDPSession_Ready, session);
        this.targetsIdsForInit.remove(target.getTargetId());
        if (!isExistingTarget) {
            this.emit(TargetManagerEvent.TargetAvailable, target);
        }
        this.finishInitializationIfReady(null);
        Map<String, Object> params = ParamsFactory.create();
        params.put("waitForDebuggerOnStart", true);
        params.put("flatten", true);
        params.put("autoAttach", true);
        params.put("filter", this.discoveryFilter);
        try {
            //容易出错，问题不大
            session.send("Target.setAutoAttach", params);
            runIfWaitingForDebugger(session);
        } catch (Exception ignored) {
            // LOGGER.error("jvppeteer error: ", e);
        }
    }

    /**
     * Runtime.runIfWaitingForDebugger 容易出现异常，只发送，不等待
     *
     * @param session client
     */
    private static void runIfWaitingForDebugger(CDPSession session) {
        session.send("Runtime.runIfWaitingForDebugger", null, null, false);
    }

    private void silentDetach(Connection parentConnection, CDPSession session) {
        try {
            runIfWaitingForDebugger(session);
        } catch (Exception ignore) {

        }

        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put(Constant.SESSION_ID, session.id());
            parentConnection.send("Target.detachFromTarget", params, null, true);
        } catch (Exception ignore) {
        }
    }

    //WebSocketConnectReadThread
    private void silentDetach(CDPSession parentSession, CDPSession session) {
        try {
            runIfWaitingForDebugger(session);
        } catch (Exception ignore) {
        }
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put(Constant.SESSION_ID, session.id());
            parentSession.send("Target.detachFromTarget", params, null, true);
        } catch (Exception ignore) {
        }
    }

    private void setupAttachmentListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> listener = (event) -> this.onAttachedToTarget(session, event);
        ValidateUtil.assertArg(!this.attachedToTargetListenersBySession.containsKey(session), "Already attached to connection");
        this.attachedToTargetListenersBySession.put(session, listener);
        session.on(ConnectionEvents.Target_attachedToTarget, listener);
        Consumer<DetachedFromTargetEvent> detachedListener = (event) -> this.onDetachedFromTarget(session, event);
        ValidateUtil.assertArg(!this.detachedFromTargetListenersBySession.containsKey(session), "Already attached to connection");
        this.detachedFromTargetListenersBySession.put(session, detachedListener);
        session.on(ConnectionEvents.Target_detachedFromTarget, detachedListener);
    }

    private void onTargetCreated(TargetCreatedEvent event) {
        this.discoveredTargetsByTargetId.put(event.getTargetInfo().getTargetId(), event.getTargetInfo());
        this.emit(TargetManagerEvent.TargetDiscovered, event.getTargetInfo());
        if ("browser".equals(event.getTargetInfo().getType()) && event.getTargetInfo().getAttached()) {
            if (this.attachedTargetsByTargetId.containsKey(event.getTargetInfo().getTargetId())) {
                return;
            }
            CdpTarget target = this.targetFactory.create(event.getTargetInfo(), null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(event.getTargetInfo().getTargetId(), target);
        }
    }

    private void onTargetDestroyed(TargetDestroyedEvent event) {
        TargetInfo targetInfo = this.discoveredTargetsByTargetId.get(event.getTargetId());
        this.discoveredTargetsByTargetId.remove(event.getTargetId());
        this.finishInitializationIfReady(event.getTargetId());
        if (targetInfo != null) {
            if ("service_worker".equals(targetInfo.getType()) && this.attachedTargetsByTargetId.containsKey(event.getTargetId())) {
                CdpTarget target = this.attachedTargetsByTargetId.get(event.getTargetId());
                if (target != null) {
                    this.emit(TargetManagerEvent.TargetGone, target);
                    this.attachedTargetsByTargetId.remove(event.getTargetId());
                }
            }
        }
    }

    private void onTargetInfoChanged(TargetInfoChangedEvent event) {
        this.discoveredTargetsByTargetId.put(event.getTargetInfo().getTargetId(), event.getTargetInfo());
        if (this.ignoredTargets.contains(event.getTargetInfo().getTargetId()) || !this.attachedTargetsByTargetId.containsKey(event.getTargetInfo().getTargetId()) || !event.getTargetInfo().getAttached()) {
            return;
        }
        CdpTarget target = this.attachedTargetsByTargetId.get(event.getTargetInfo().getTargetId());
        if (target == null) {
            return;
        }
        String previousURL = target.url();
        boolean wasInitialized = target.initializedResult.isDone() && CdpTarget.InitializationStatus.SUCCESS.equals(target.initializedResult.get());
        if (isPageTargetBecomingPrimary(target, event.getTargetInfo())) {
            CDPSession session = target.session();
            Objects.requireNonNull(session, "Target that is being activated is missing a CDPSession.");
            if (session.parentSession() != null) {
                session.parentSession().emit(ConnectionEvents.CDPSession_Swapped, session);
            }
        }
        target.targetInfoChanged(event.getTargetInfo());
        if (wasInitialized && !previousURL.equals(target.url())) {
            this.emit(TargetManagerEvent.TargetChanged, target);
        }
    }

    private boolean isPageTargetBecomingPrimary(CdpTarget target, TargetInfo targetInfo) {
        return StringUtil.isNotEmpty(target.subtype()) && StringUtil.isEmpty(targetInfo.getSubtype());
    }

    public void onSessionDetached(CdpCDPSession session) {
        this.removeAttachmentListeners(session);
    }

    private void removeAttachmentListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> listener = this.attachedToTargetListenersBySession.get(session);
        if (listener != null) {
            session.off(ConnectionEvents.Target_attachedToTarget, listener);
            this.attachedToTargetListenersBySession.remove(session);
        }
        if (this.detachedFromTargetListenersBySession.containsKey(session)) {
            session.off(ConnectionEvents.Target_detachedFromTarget, this.detachedFromTargetListenersBySession.get(session));
            this.detachedFromTargetListenersBySession.remove(session);
        }
    }

    private void removeAttachmentListeners(Connection connection) {
        Consumer<AttachedToTargetEvent> listener = this.attachedToTargetListenersByConnection.get(connection);
        if (listener != null) {
            connection.off(ConnectionEvents.Target_attachedToTarget, listener);
            this.attachedToTargetListenersByConnection.remove(connection);
        }
        if (this.detachedFromTargetListenersByConnection.containsKey(connection)) {
            connection.off(ConnectionEvents.Target_detachedFromTarget, this.detachedFromTargetListenersByConnection.get(connection));
            this.detachedFromTargetListenersByConnection.remove(connection);
        }
    }

    private void finishInitializationIfReady(String targetId) {
        if (StringUtil.isNotEmpty(targetId)) {
            this.targetsIdsForInit.remove(targetId);
        }
        if (this.targetsIdsForInit.isEmpty()) {
            this.initializeResult.onSuccess(true);
        }
    }

    @FunctionalInterface
    public interface TargetFactory {
        CdpTarget create(TargetInfo targetInfo, CDPSession session, CDPSession parentSession);
    }

    public enum TargetManagerEvent {

        TargetDiscovered("targetDiscovered"),
        TargetAvailable("targetAvailable"),
        TargetGone("targetGone"),
        /**
         * Emitted after a target has been initialized and whenever its URL changes.
         */
        TargetChanged("targetChanged");
        private String eventName;

        TargetManagerEvent(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }

}
