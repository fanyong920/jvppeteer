package com.ruiyun.jvppeteer.core;


import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.events.*;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.options.FilterEntry;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChromeTargetManager extends TargetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeTargetManager.class);
    private final Connection connection;
    private final Map<String, TargetInfo> discoveredTargetsByTargetId = new HashMap<>();
    private final Map<String, Target> attachedTargetsByTargetId = new HashMap<>();
    private final Map<String, Target> attachedTargetsBySessionId = new HashMap<>();
    private final Set<String> ignoredTargets = new HashSet<>();
    private final Function<Target, Boolean> targetFilterCallback;
    private final TargetFactory targetFactory;
    private final Map<CDPSession, Consumer<AttachedToTargetEvent>> attachedToTargetListenersBySession = new WeakHashMap<>();
    private final Map<Connection, Consumer<AttachedToTargetEvent>> attachedToTargetListenersByConnection = new WeakHashMap<>();
    private final Map<CDPSession, Consumer<DetachedFromTargetEvent>> detachedFromTargetListenersBySession = new WeakHashMap<>();
    private final Map<Connection, Consumer<DetachedFromTargetEvent>> detachedFromTargetListenersByConnection = new WeakHashMap<>();
    private final SingleSubject<Boolean> initializeSubject= SingleSubject.create();
    private final Set<String> targetsIdsForInit = new HashSet<>();
    private boolean waitForInitiallyDiscoveredTargets = true;
    private final List<FilterEntry> discoveryFilter = new ArrayList<FilterEntry>(){{add(new FilterEntry());}};
    private final List<Disposable> disposables = new ArrayList<>();
    public ChromeTargetManager(Connection connection, TargetFactory targetFactory, Function<Target, Boolean> targetFilterCallback, boolean waitForInitiallyDiscoveredTargets) {
        super();
        this.connection = connection;
        this.targetFilterCallback = targetFilterCallback;
        this.targetFactory = targetFactory;
        this.waitForInitiallyDiscoveredTargets = waitForInitiallyDiscoveredTargets;
        disposables.add(Helper.<TargetCreatedEvent, CDPSession.CDPSessionEvent>fromEmitterEvent(this.connection, CDPSession.CDPSessionEvent.Target_targetCreated).subscribe(this::onTargetCreated));
        disposables.add(Helper.<TargetDestroyedEvent, CDPSession.CDPSessionEvent>fromEmitterEvent(this.connection, CDPSession.CDPSessionEvent.Target_targetDestroyed).subscribe(this::onTargetDestroyed));
        disposables.add(Helper.<TargetInfoChangedEvent, CDPSession.CDPSessionEvent>fromEmitterEvent(this.connection, CDPSession.CDPSessionEvent.Target_targetInfoChanged).subscribe(this::onTargetInfoChanged));
        disposables.add(Helper.<CDPSession, CDPSession.CDPSessionEvent>fromEmitterEvent(this.connection, CDPSession.CDPSessionEvent.sessionDetached).subscribe(this::onSessionDetached));
        this.setupAttachmentListeners(this.connection);
    }
    public void storeExistingTargetsForInit(){
        if (!this.waitForInitiallyDiscoveredTargets) {
            return;
        }
        this.discoveredTargetsByTargetId.forEach((targetId, targetInfo) -> {
            boolean isPageOrFrame = "page".equals(targetInfo.getType()) ||  "iframe".equals(targetInfo.getType());
            boolean isExtension = targetInfo.getUrl().startsWith("chrome-extension://");
            Target targetForFilter = new Target(targetInfo, null, null, this, null);
            if((this.targetFilterCallback == null || this.targetFilterCallback.apply(targetForFilter)) && isPageOrFrame && !isExtension){
                this.targetsIdsForInit.add(targetInfo.getTargetId());
            }
        });
    }
    public void initialize(){
        Map<String, Object> params = new HashMap<>();
        params.put("discover",true);
        params.put("filter",this.discoveryFilter);
        this.connection.send("Target.setDiscoverTargets",params);
        this.storeExistingTargetsForInit();
        params.clear();
        params.put("waitForDebuggerOnStart",true);
        params.put("flatten",true);
        params.put("autoAttach",true);
        List<FilterEntry> filter = new ArrayList<>();
        filter.add(new FilterEntry(true,"page"));
        filter.addAll(this.discoveryFilter);
        params.put("filter",filter);
        params.put("waitForDebuggerOnStart",true);
        this.connection.send("Target.setAutoAttach",params);
        this.finishInitializationIfReady(null);
        this.initializeSubject.blockingSubscribe();
    }
    public void dispose() {
        disposables.forEach(Disposable::dispose);
        this.removeAttachmentListeners(this.connection);
    }
    @Override
    public Map<String,Target> getAvailableTargets() {
        return this.attachedTargetsByTargetId;
    }
    private void setupAttachmentListeners(Connection connection) {
        Consumer<AttachedToTargetEvent> listener = (event) -> {
            this.onAttachedToTarget(connection,event);
        };
        ValidateUtil.assertArg(!this.attachedToTargetListenersByConnection.containsKey(connection), "Already attached to connection");
        this.attachedToTargetListenersByConnection.put(connection, listener);
        connection.on(CDPSession.CDPSessionEvent.Target_attachedToTarget, listener);
        Consumer<DetachedFromTargetEvent> detachedListener = this::onDetachedFromTarget;
        ValidateUtil.assertArg(!this.detachedFromTargetListenersByConnection.containsKey(connection), "Already attached to connection");
        this.detachedFromTargetListenersByConnection.put(connection, detachedListener);
        connection.on(CDPSession.CDPSessionEvent.Target_detachedFromTarget, detachedListener);
    }

    private void onDetachedFromTarget(DetachedFromTargetEvent event) {
        Target target = this.attachedTargetsBySessionId.get(event.getSessionId());
        this.attachedTargetsBySessionId.remove(event.getSessionId());
        if (target == null) {
            return;
        }
        this.attachedTargetsByTargetId.remove(target.getTargetId());
        this.emit(TargetManagerEvent.TargetGone, target);
    }
    private void onDetachedFromTarget(CDPSession parentSession, DetachedFromTargetEvent event) {
        Target target = this.attachedTargetsBySessionId.get(event.getSessionId());
        this.attachedTargetsBySessionId.remove(event.getSessionId());
        if (target == null) {
            return;
        }
        parentSession.getTarget().removeChildTarget(target);
        this.attachedTargetsByTargetId.remove(target.getTargetId());
        this.emit(TargetManagerEvent.TargetGone, target);
    }

    private void onAttachedToTarget(Connection parentConnection, AttachedToTargetEvent event) {
        TargetInfo targetInfo = event.getTargetInfo();
        CDPSession session = this.connection.session(event.getSessionId());
        if(session == null){
            throw new JvppeteerException("Session " + event.getSessionId() + " was not created.");
        }
        if(!this.connection.isAutoAttached(targetInfo.getTargetId())){
            return;
        }
        if("service_worker".equals(targetInfo.getType())){
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentConnection, session);
            if (this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId())) {
                return;
            }
            Target target = this.targetFactory.create(targetInfo, null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.emit(TargetManagerEvent.TargetAvailable, target);
            return;
        }
        boolean isExistingTarget = this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId());
        Target target = isExistingTarget ? this.attachedTargetsByTargetId.get(targetInfo.getTargetId()) : this.targetFactory.create(targetInfo,session,null);
        if(this.targetFilterCallback != null && !this.targetFilterCallback.apply(target)){
            this.ignoredTargets.add(targetInfo.getTargetId());
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentConnection, session);
            return;
        }
        this.setupAttachmentListeners(session);
        if(isExistingTarget) {
            session.setTarget(target);
            this.attachedTargetsBySessionId.put(session.id(), this.attachedTargetsByTargetId.get(targetInfo.getTargetId()));
        } else {
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.attachedTargetsBySessionId.put(session.id(), target);
        }
        parentConnection.emit(CDPSession.CDPSessionEvent.CDPSession_Ready, session);
        this.targetsIdsForInit.remove(target.getTargetId());
        if (!isExistingTarget) {
            this.emit(TargetManagerEvent.TargetAvailable, target);
        }
        this.finishInitializationIfReady(null);
        Map<String, Object> params = new HashMap<>();
        params.put("waitForDebuggerOnStart", true);
        params.put("flatten", true);
        params.put("autoAttach", true);
        params.put("filter", this.discoveryFilter);
        try {
            //阻塞了，可咋办啊todo
            session.send("Target.setAutoAttach",params,null,false);
            session.send("Runtime.runIfWaitingForDebugger",null,null,false);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ",e);
        }
    }

    private void onAttachedToTarget(CDPSession parentSession, AttachedToTargetEvent event) {
        TargetInfo targetInfo = event.getTargetInfo();
        CDPSession session = this.connection.session(event.getSessionId());
        if(session == null){
            throw new JvppeteerException("Session " + event.getSessionId() + " was not created.");
        }
        if(!this.connection.isAutoAttached(targetInfo.getTargetId())){
            return;
        }
        if("service_worker".equals(targetInfo.getType())){
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentSession, session);
            if (this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId())) {
                return;
            }
            Target target = this.targetFactory.create(targetInfo, null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.emit(TargetManagerEvent.TargetAvailable, target);
            return;
        }
        boolean isExistingTarget = this.attachedTargetsByTargetId.containsKey(targetInfo.getTargetId());
        Target target = isExistingTarget ? this.attachedTargetsByTargetId.get(targetInfo.getTargetId()) : this.targetFactory.create(targetInfo,session,parentSession);
        if(this.targetFilterCallback != null && !this.targetFilterCallback.apply(target)){
            this.ignoredTargets.add(targetInfo.getTargetId());
            this.finishInitializationIfReady(targetInfo.getTargetId());
            silentDetach(parentSession, session);
            return;
        }
        this.setupAttachmentListeners(session);
        if(isExistingTarget) {
            session.setTarget(target);
            this.attachedTargetsBySessionId.put(session.id(), this.attachedTargetsByTargetId.get(targetInfo.getTargetId()));
        } else {
            target.initialize();
            this.attachedTargetsByTargetId.put(targetInfo.getTargetId(), target);
            this.attachedTargetsBySessionId.put(session.id(), target);
        }
        Target parentTarget = parentSession.getTarget();
        parentTarget.addChildTarget(target);
        parentSession.emit(CDPSession.CDPSessionEvent.CDPSession_Ready, session);
        this.targetsIdsForInit.remove(target.getTargetId());
        if (!isExistingTarget) {
            this.emit(TargetManagerEvent.TargetAvailable, target);
        }
        this.finishInitializationIfReady(null);
        Map<String, Object> params = new HashMap<>();
        params.put("waitForDebuggerOnStart", true);
        params.put("flatten", true);
        params.put("autoAttach", true);
        params.put("filter", this.discoveryFilter);
        try {
            session.send("Target.setAutoAttach",params,null,false);
            session.send("Runtime.runIfWaitingForDebugger",null,null,false);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ",e);
        }
    }
    //WebSocketConnectReadThread
    private void silentDetach(Connection parentConnection, CDPSession session) {
        try {
            session.send("Runtime.runIfWaitingForDebugger",null,null,false);
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", session.id());
            parentConnection.send("Target.detachFromTarget",params,null,false);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ",e);
        }
    }
    //WebSocketConnectReadThread
    private void silentDetach(CDPSession parentSession, CDPSession session) {
        try {
            session.send("Runtime.runIfWaitingForDebugger",null,null,false);
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", session.id());
            parentSession.send("Target.detachFromTarget",params,null,false);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ",e);
        }
    }



    private void setupAttachmentListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> listener = (event) -> {
            this.onAttachedToTarget(session,event);
        };
        ValidateUtil.assertArg(!this.attachedToTargetListenersBySession.containsKey(session), "Already attached to connection");
        this.attachedToTargetListenersBySession.put(session, listener);
        session.on(CDPSession.CDPSessionEvent.Target_attachedToTarget, listener);
        Consumer<DetachedFromTargetEvent> detachedListener = (event) -> {
            this.onDetachedFromTarget(session,event);
        };
        ValidateUtil.assertArg(!this.detachedFromTargetListenersBySession.containsKey(session), "Already attached to connection");
        this.detachedFromTargetListenersBySession.put(session, detachedListener);
        session.on(CDPSession.CDPSessionEvent.Target_detachedFromTarget, detachedListener);
    }

    private void onTargetCreated(TargetCreatedEvent event) {
        this.discoveredTargetsByTargetId.put(event.getTargetInfo().getTargetId(), event.getTargetInfo());
        this.emit(TargetManagerEvent.TargetDiscovered, event.getTargetInfo());
        if("browser".equals(event.getTargetInfo().getType()) && event.getTargetInfo().getAttached()){
            if(this.attachedTargetsByTargetId.containsKey(event.getTargetInfo().getTargetId())){
                return;
            }
            Target target = this.targetFactory.create(event.getTargetInfo(), null, null);
            target.initialize();
            this.attachedTargetsByTargetId.put(event.getTargetInfo().getTargetId(), target);
        }
    }
    private void onTargetDestroyed(TargetDestroyedEvent event) {
        TargetInfo targetInfo = this.discoveredTargetsByTargetId.get(event.getTargetId());
        this.discoveredTargetsByTargetId.remove(event.getTargetId());
        this.finishInitializationIfReady(event.getTargetId());
        if(targetInfo != null){
            if("service_worker".equals(targetInfo.getType()) && this.attachedTargetsByTargetId.containsKey(event.getTargetId())){
                Target target = this.attachedTargetsByTargetId.get(event.getTargetId());
                if(target != null){
                    this.emit(TargetManagerEvent.TargetGone, target);
                    this.attachedTargetsByTargetId.remove(event.getTargetId());
                }
            }
        }
    }
    private void onTargetInfoChanged(TargetInfoChangedEvent event) {
        this.discoveredTargetsByTargetId.put(event.getTargetInfo().getTargetId(), event.getTargetInfo());
        if(this.ignoredTargets.contains(event.getTargetInfo().getTargetId()) || !this.attachedTargetsByTargetId.containsKey(event.getTargetInfo().getTargetId()) || !event.getTargetInfo().getAttached()){
            return;
        }
        Target target = this.attachedTargetsByTargetId.get(event.getTargetInfo().getTargetId());
        if(target == null){
            return;
        }
        String previousURL = target.url();
        boolean wasInitialized = target.initializedSubject.hasValue() && Target.InitializationStatus.SUCCESS.equals(target.initializedSubject.getValue());
        if(isPageTargetBecomingPrimary(target, event.getTargetInfo())){
            CDPSession session = target.session();
            ValidateUtil.notNull(session, "Target that is being activated is missing a CDPSession.");
            if(session.parentSession() != null){
                session.parentSession().emit(CDPSession.CDPSessionEvent.CDPSession_Swapped,session);
            }
        }
        target.targetInfoChanged(event.getTargetInfo());
        if(wasInitialized && !previousURL.equals(target.url())){
            this.emit(TargetManagerEvent.TargetChanged, target);
        }
    }

    private boolean isPageTargetBecomingPrimary(Target target, TargetInfo targetInfo) {
        return StringUtil.isNotEmpty(target.subtype()) && StringUtil.isEmpty(targetInfo.getSubtype());
    }

    public void onSessionDetached(CDPSession session){
        this.removeAttachmentListeners(session);
    }

    private void removeAttachmentListeners(CDPSession session) {
        Consumer<AttachedToTargetEvent> listener = this.attachedToTargetListenersBySession.get(session);
        if(listener != null){
            session.off(CDPSession.CDPSessionEvent.Target_attachedToTarget, listener);
            this.attachedToTargetListenersBySession.remove(session);
        }
        if(this.detachedFromTargetListenersBySession.containsKey(session)){
            session.off(CDPSession.CDPSessionEvent.Target_detachedFromTarget, this.detachedFromTargetListenersBySession.get(session));
            this.detachedFromTargetListenersBySession.remove(session);
        }
    }
    private void removeAttachmentListeners(Connection connection) {
        Consumer<AttachedToTargetEvent> listener = this.attachedToTargetListenersByConnection.get(connection);
        if(listener != null){
            connection.off(CDPSession.CDPSessionEvent.Target_attachedToTarget, listener);
            this.attachedToTargetListenersByConnection.remove(connection);
        }
        if(this.detachedFromTargetListenersByConnection.containsKey(connection)){
            connection.off(CDPSession.CDPSessionEvent.Target_detachedFromTarget, this.detachedFromTargetListenersByConnection.get(connection));
            this.detachedFromTargetListenersByConnection.remove(connection);
        }
    }

    private void finishInitializationIfReady(String targetId) {
        if(StringUtil.isNotEmpty(targetId)){
            this.targetsIdsForInit.remove(targetId);
        }
        if(this.targetsIdsForInit.isEmpty()){
            this.initializeSubject.onSuccess(true);
        }
    }

}