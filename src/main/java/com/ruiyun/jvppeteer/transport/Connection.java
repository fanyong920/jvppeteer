package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.events.*;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.protocol.debugger.ScriptParsedEvent;
import com.ruiyun.jvppeteer.protocol.fetch.AuthRequiredEvent;
import com.ruiyun.jvppeteer.protocol.fetch.RequestPausedEvent;
import com.ruiyun.jvppeteer.protocol.log.EntryAddedEvent;
import com.ruiyun.jvppeteer.protocol.network.*;
import com.ruiyun.jvppeteer.protocol.page.*;
import com.ruiyun.jvppeteer.protocol.performance.MetricsEvent;
import com.ruiyun.jvppeteer.protocol.runtime.BindingCalledEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.ruiyun.jvppeteer.core.Constant.*;
import static com.ruiyun.jvppeteer.util.Helper.createProtocolErrorMessage;

/**
 * web socket client 浏览器级别的连接
 *
 * @author fff
 */
public class Connection extends EventEmitter<CDPSession.CDPSessionEvent> implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackRegistry.class);
    private final String url;
    private final ConnectionTransport transport;
    private final int delay;
    private final int timeout;
    private final Map<String, CDPSession> sessions = new HashMap<>();
    public boolean closed;
    Set<String> manuallyAttached = new HashSet<>();
    private final CallbackRegistry callbacks = new CallbackRegistry();//并发
    public static final Map<String, Class<?>> classes = new HashMap<String, Class<?>>(){
        {
            for(CDPSession.CDPSessionEvent event : CDPSession.CDPSessionEvent.values()){
                if(event.getEventName().equals("CDPSession.Disconnected")){
                    put(event.getEventName(), null);
                }else if(event.getEventName().equals("CDPSession.Swapped")){
                    //todo
                } else if(event.getEventName().equals("CDPSession.Ready")) {
                    //todo
                } else if (event.getEventName().equals("sessionattached")) {
                    put(event.getEventName(), CDPSession.class);
                } else if (event.getEventName().equals("sessionDetached") ) {
                    put(event.getEventName(), CDPSession.class);
                } else if (event.getEventName().equals("Target.targetCreated")) {
                    put(event.getEventName(), TargetCreatedEvent.class);
                } else if (event.getEventName().equals("Target.targetDestroyed")) {
                    put(event.getEventName(), TargetDestroyedEvent.class);
                }else if (event.getEventName().equals("Target.targetInfoChanged")) {
                    put(event.getEventName(), TargetInfoChangedEvent.class);
                }else if (event.getEventName().equals("Target.attachedToTarget")) {
                    put(event.getEventName(), AttachedToTargetEvent.class);
                }else if (event.getEventName().equals("Target.detachedFromTarget")) {
                    put(event.getEventName(), DetachedFromTargetEvent.class);
                }else if (event.getEventName().equals("Page.javascriptDialogOpening")) {
                    put(event.getEventName(), JavascriptDialogOpeningEvent.class);
                }else if (event.getEventName().equals("Runtime.exceptionThrown")) {
                    put(event.getEventName(), ExceptionThrownEvent.class);
                }else if (event.getEventName().equals("Performance.metrics")) {
                    put(event.getEventName(), MetricsEvent.class);
                }else if (event.getEventName().equals("Log.entryAdded")) {
                    put(event.getEventName(), EntryAddedEvent.class);
                }else if (event.getEventName().equals("Page.fileChooserOpened")) {
                    put(event.getEventName(), FileChooserOpenedEvent.class);
                }else if (event.getEventName().equals("Debugger.scriptParsed")) {
                    put(event.getEventName(), ScriptParsedEvent.class);
                }else if (event.getEventName().equals("Runtime.executionContextCreated")) {
                    put(event.getEventName(), ExecutionContextCreatedEvent.class);
                }else if (event.getEventName().equals("Runtime.executionContextDestroyed")) {
                    put(event.getEventName(), ExecutionContextDestroyedEvent.class);
                }else if (event.getEventName().equals("CSS.styleSheetAdded")) {
                    put(event.getEventName(), StyleSheetAddedEvent.class);
                }else if (event.getEventName().equals("Page.frameAttached")) {
                    put(event.getEventName(), FrameAttachedEvent.class);
                }else if (event.getEventName().equals("Page.frameNavigated")) {
                    put(event.getEventName(), FrameNavigatedEvent.class);
                }else if (event.getEventName().equals("Page.navigatedWithinDocument")) {
                    put(event.getEventName(), NavigatedWithinDocumentEvent.class);
                }else if (event.getEventName().equals("Page.frameDetached")) {
                    put(event.getEventName(), FrameDetachedEvent.class);
                }else if (event.getEventName().equals("Page.frameStoppedLoading")) {
                    put(event.getEventName(), FrameStoppedLoadingEvent.class);
                }else if (event.getEventName().equals("Page.lifecycleEvent")) {
                    put(event.getEventName(), LifecycleEvent.class);
                }else if (event.getEventName().equals("Fetch.requestPaused")) {
                    put(event.getEventName(), RequestPausedEvent.class);
                }else if (event.getEventName().equals("Fetch.authRequired")) {
                    put(event.getEventName(), AuthRequiredEvent.class);
                }else if (event.getEventName().equals("Network.requestWillBeSent")) {
                    put(event.getEventName(), RequestWillBeSentEvent.class);
                }else if (event.getEventName().equals("Network.requestServedFromCache")) {
                    put(event.getEventName(), RequestServedFromCacheEvent.class);
                }else if (event.getEventName().equals("Network.responseReceived")) {
                    put(event.getEventName(), ResponseReceivedEvent.class);
                }else if (event.getEventName().equals("Network.loadingFinished")) {
                    put(event.getEventName(), LoadingFinishedEvent.class);
                }else if (event.getEventName().equals("Network.loadingFailed")) {
                    put(event.getEventName(), LoadingFailedEvent.class);
                }else if (event.getEventName().equals("Runtime.consoleAPICalled")) {
                    put(event.getEventName(), ConsoleAPICalledEvent.class);
                }else if (event.getEventName().equals("Runtime.bindingCalled")) {
                    put(event.getEventName(), BindingCalledEvent.class);
                }else if (event.getEventName().equals("Tracing.tracingComplete")) {
                    put(event.getEventName(), TracingCompleteEvent.class);
                }
            }
        }
    };
    public Connection(String url, ConnectionTransport transport, int delay, int timeout) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        this.timeout = timeout;
        this.transport.setConnection(this);
    }
    public JsonNode send(String method) {
        return this.rawSend(this.callbacks, method, null, null, this.timeout, true);
    }
    public JsonNode send(String method, Map<String, Object> params) {
        return this.rawSend(this.callbacks, method, params, null, this.timeout, true);
    }
    public JsonNode send(String method, Map<String, Object> params, Integer timeout, boolean isBlocking) {
        return this.rawSend(this.callbacks, method, params, null, timeout, isBlocking);
    }

    public JsonNode rawSend(CallbackRegistry callbacks, String method, Map<String, Object> params, String sessionId, Integer timeout, boolean isBlocking) {
        ValidateUtil.assertArg(!this.closed, "Protocol error: Connection closed.");
        if(timeout == null){
            timeout = this.timeout;
        }
        return callbacks.create(method, timeout, (id) -> {
            ObjectNode objectNode = OBJECTMAPPER.createObjectNode();
            objectNode.put(MESSAGE_METHOD_PROPERTY, method);
            if(params != null) {
                objectNode.set(MESSAGE_PARAMS_PROPERTY, OBJECTMAPPER.valueToTree(params));
            }
            objectNode.put(MESSAGE_ID_PROPERTY, id);
            if(StringUtil.isNotEmpty(sessionId)){
                objectNode.put(MESSAGE_SESSION_ID_PROPERTY, sessionId);
            }
            String stringifiedMessage = objectNode.toString();
            LOGGER.trace("jvppeteer:protocol:SEND ► {}", stringifiedMessage);
            this.transport.send(stringifiedMessage);
        }, isBlocking);
    }

    /**
     * recevie message from browser by websocket
     * @param message 从浏览器接受到的消息
     */
    public void onMessage(String message) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // 恢复中断状态
                Thread.currentThread().interrupt();
                LOGGER.error("slowMo browser Fail:", e);
            }
        }
        LOGGER.trace("jvppeteer:protocol:RECV ◀ {}", message);
        try {
            if (StringUtil.isEmpty(message)) {
                return;
            }
            JsonNode readTree = OBJECTMAPPER.readTree(message);
            String method = null;
            if (readTree.hasNonNull(MESSAGE_METHOD_PROPERTY)) {
                method = readTree.get(MESSAGE_METHOD_PROPERTY).asText();
            }
            String sessionId = null;
            JsonNode paramsNode = null;
            if(readTree.hasNonNull(MESSAGE_PARAMS_PROPERTY)){
                paramsNode = readTree.get(MESSAGE_PARAMS_PROPERTY);
                if(paramsNode.hasNonNull(MESSAGE_SESSION_ID_PROPERTY)){
                    sessionId = paramsNode.get(MESSAGE_SESSION_ID_PROPERTY).asText();
                }
            }
            String parentSessionId = null;
            if(readTree.hasNonNull(MESSAGE_SESSION_ID_PROPERTY)){
                parentSessionId = readTree.get(MESSAGE_SESSION_ID_PROPERTY).asText();
            }
            if ("Target.attachedToTarget".equals(method)) {//attached to target -> page attached to browser
                JsonNode typeNode = paramsNode.get(Constant.MESSAGE_TARGETINFO_PROPERTY).get(Constant.MESSAGE_TYPE_PROPERTY);
                CDPSession cdpSession = new CDPSession(this, typeNode.asText(), sessionId,parentSessionId);
                this.sessions.put(sessionId, cdpSession);
                this.emit(CDPSession.CDPSessionEvent.sessionAttached, cdpSession);
                CDPSession parentSession = this.sessions.get(parentSessionId);
                if(parentSession != null){
                    parentSession.emit(CDPSession.CDPSessionEvent.sessionAttached, cdpSession);
                }
            } else if ("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
                CDPSession cdpSession = this.sessions.get(sessionId);
                if (cdpSession != null) {
                    cdpSession.onClosed();
                    this.sessions.remove(sessionId);
                    this.emit(CDPSession.CDPSessionEvent.sessionDetached, cdpSession);
                    CDPSession parentSession = this.sessions.get(parentSessionId);
                    if(parentSession != null){
                        parentSession.emit(CDPSession.CDPSessionEvent.sessionDetached, cdpSession);
                    }
                }
            }
            if(StringUtil.isNotEmpty(parentSessionId)){
                CDPSession parentSession = this.sessions.get(parentSessionId);
                if (parentSession != null) {
                    parentSession.onMessage(readTree);
                }
            } else if (readTree.hasNonNull(MESSAGE_ID_PROPERTY)) {//long类型的id,说明属于这次发送消息后接受的回应
                int id = readTree.get(MESSAGE_ID_PROPERTY).asInt();
                if(readTree.hasNonNull(MESSAGE_ERROR_PROPERTY)){
                    this.callbacks.reject(id,createProtocolErrorMessage(readTree), readTree.get(MESSAGE_ERROR_PROPERTY).get(MESSAGE_MESSAGE_PROPERTY).asText());
                }else {
                    this.callbacks.resolve(id, readTree.get(MESSAGE_RESULT_PROPERTY));
                }
            }else{//是一个事件，那么响应监听器
                String finalMethod = method;
                boolean match = CDPSession.eventStream.anyMatch((CDPSession.CDPSessionEvent event) -> event.getEventName().equals(finalMethod));
                if(!match){//不匹配就是没有监听该事件
                    return;
                }
                this.emit(CDPSession.CDPSessionEvent.valueOf(method.replace(".","_")), classes.get(method) == null ? null : OBJECTMAPPER.treeToValue(paramsNode, classes.get(method)));
            }
        } catch (Exception e) {
            LOGGER.error("onMessage error:", e);
        }
    }
    /**
     * 从{@link CDPSession}中拿到对应的{@link Connection}
     *
     * @param client cdpsession
     * @return Connection
     */
    public static Connection fromSession(CDPSession client) {
        return client.getConnection();
    }

    /**
     * 创建一个{@link CDPSession}
     *
     * @param targetInfo target info
     * @return CDPSession client
     */
    public CDPSession createSession(TargetInfo targetInfo) {
        return this._createSession(targetInfo,false);
    }
    public CDPSession _createSession(TargetInfo targetInfo,boolean isAutoAttachEmulated) {
        if (!isAutoAttachEmulated) {
            this.manuallyAttached.add(targetInfo.getTargetId());
        }
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetInfo.getTargetId());
        params.put("flatten", true);
        JsonNode receivedNode = this.send("Target.attachToTarget", params, null,true);
        this.manuallyAttached.remove(targetInfo.getTargetId());
        if(receivedNode.hasNonNull(MESSAGE_SESSION_ID_PROPERTY)){
            CDPSession session = this.sessions.get(receivedNode.get(MESSAGE_SESSION_ID_PROPERTY).asText());
            if(session == null){
                throw new JvppeteerException("CDPSession creation failed.");
            }
            return session;
        }else {
            throw new JvppeteerException("CDPSession creation failed.");
        }
    }

    public String url() {
        return this.url;
    }

    public String getUrl() {
        return url;
    }

    public CDPSession session(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void accept(String t) {
        onMessage(t);
    }

    public void dispose() {
        this.onClose();//清理Connection资源
        this.transport.close();//关闭websocket
    }

    public void onClose() {
        if (this.closed)
            return;
        this.closed = true;
        this.transport.setConnection(null);
        this.callbacks.clear();
        for (CDPSession session : this.sessions.values())
            session.onClosed();
        this.sessions.clear();
        this.emit(CDPSession.CDPSessionEvent.CDPSession_Disconnected, null);
    }

}

