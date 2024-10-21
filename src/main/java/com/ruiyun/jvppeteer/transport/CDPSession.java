package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.common.Constant.*;
import static com.ruiyun.jvppeteer.transport.Connection.resolveCallback;

/**
 * The CDPSession instances are used to talk raw Chrome Devtools Protocol:
 * <p>
 * protocol methods can be called with session.send method.
 * protocol events can be subscribed to with session.on method.
 * Useful links:
 * <p>
 * Documentation on DevTools Protocol can be found here: DevTools Protocol Viewer.
 * Getting Started with : <a href="https://github.com/aslushnikov/getting-started-with-cdp/blob/master/README.md">DevTools Protocol</a>
 */
public class CDPSession extends EventEmitter<CDPSession.CDPSessionEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CDPSession.class);
    private final String targetType;
    private final String sessionId;
    private Connection connection;
    private final String parentSessionId;
    private Target target;
    private List<String> events = null;

    public CDPSession(Connection connection, String targetType, String sessionId, String parentSessionId) {
        super();
        this.targetType = targetType;
        this.sessionId = sessionId;
        this.connection = connection;
        this.parentSessionId = parentSessionId;
    }

    public CDPSession parentSession() {
        if (StringUtil.isEmpty(this.parentSessionId)) {
            // To make it work in Firefox that does not have parent (tab) sessions.
            return this;
        }
        if (this.connection != null) {
            return this.connection.session(this.parentSessionId);
        } else {
            return null;
        }
    }

    public void setTarget(Target target) {
        this.target = target;
    }


    public void onClosed() {
        this.connection = null;
        this.emit(CDPSessionEvent.CDPSession_Disconnected, true);
    }

    public JsonNode send(String method) {
        return this.send(method, null);
    }

    public JsonNode send(String method, Map<String, Object> params) {
        return this.send(method, params, null, true);
    }

    public JsonNode send(String method, Map<String, Object> params, Integer timeout, boolean isBlocking) {
        if (this.connection == null || this.connection.closed) {
            throw new JvppeteerException("Protocol error (" + method + "): Session closed. Most likely the" + this.targetType + "has been closed.");
        }
        return this.connection.rawSend(method, params, this.sessionId, timeout, isBlocking);
    }

    /**
     * 页面分离浏览器
     */
    public void detach() {
        if (this.connection == null) {
            throw new JvppeteerException("Session already detached. Most likely the" + this.targetType + "has been closed.");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put(SESSION_ID, this.sessionId);
        this.connection.send("Target.detachFromTarget", params);
    }

    /**
     * receivedNode的结构
     * <blockquote><pre>
     *  {
     *    id?: number;
     *    method: keyof CDPEvents;
     *    params: CDPEvents[keyof CDPEvents];
     *    error: {message: string; data: any; code: number};
     *    result?: any;
     *   }
     * </pre></blockquote>
     *
     * @param response  接受到的返回值
     * @param callbacks 回调
     * @return boolean
     */
    public boolean onMessage(JsonNode response, CallbackRegistry callbacks) {
        JsonNode paramsNode = response.get(PARAMS);
        JsonNode methodNode = response.get(METHOD);
        try {
            if (response.hasNonNull(ID)) {//long类型的id,说明属于这次发送消息后接受的回应
                long id = response.get(ID).asLong();
                resolveCallback(callbacks, response, id, false);
            } else {//发射数据，执行事件的监听方法
                ValidateUtil.assertArg(!response.hasNonNull(ID), "Should not contain id, " + response);
                String method = methodNode.asText();
                if (events == null) {
                    events = Arrays.stream(CDPSession.CDPSessionEvent.values()).map(CDPSession.CDPSessionEvent::getEventName).collect(Collectors.toList());
                }
                boolean match = events.contains(method);
                if (!match) {//不匹配就是没有监听该事件
                    return false;
                }
                if (connection == null) {
                    return false;
                }
                connection.getEventQueue().offer(() -> {
                    try {
                        this.emit(CDPSessionEvent.valueOf(method.replace(".", "_")), LISTENNER_CLASSES.get(method) == null ? true : OBJECTMAPPER.treeToValue(paramsNode, LISTENNER_CLASSES.get(method)));
                    } catch (IllegalArgumentException | JsonProcessingException e) {
                        LOGGER.error("emit error", e);
                    }
                });
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("emit error", e);
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public String id() {
        return this.sessionId;
    }

    public Target getTarget() {
        Objects.requireNonNull(this.target, "Target must exist");
        return this.target;
    }

    public enum CDPSessionEvent {
        CDPSession_Disconnected("CDPSession.Disconnected"),
        CDPSession_Swapped("CDPSession.Swapped"),
        CDPSession_Ready("CDPSession.Ready"),
        sessionAttached("sessionattached"),
        sessionDetached("sessiondetached"),

        Page_domContentEventFired("Page.domContentEventFired"),
        Page_loadEventFired("Page.loadEventFired"),
        Page_javascriptDialogOpening("Page.javascriptDialogOpening"),
        Page_fileChooserOpened("Page.fileChooserOpened"),
        Page_frameStartedLoading("Page.frameStartedLoading"),
        Page_frameAttached("Page.frameAttached"),
        Page_frameNavigated("Page.frameNavigated"),
        Page_navigatedWithinDocument("Page.navigatedWithinDocument"),
        Page_frameDetached("Page.frameDetached"),
        Page_frameStoppedLoading("Page.frameStoppedLoading"),
        Page_lifecycleEvent("Page.lifecycleEvent"),
        Page_screencastFrame("Page.screencastFrame"),

        Runtime_executionContextCreated("Runtime.executionContextCreated"),
        Runtime_executionContextDestroyed("Runtime.executionContextDestroyed"),
        Runtime_executionContextsCleared("Runtime.executionContextsCleared"),
        Runtime_exceptionThrown("Runtime.exceptionThrown"),
        Runtime_consoleAPICalled("Runtime.consoleAPICalled"),
        Runtime_bindingCalled("Runtime.bindingCalled"),

        Inspector_targetCrashed("Inspector.targetCrashed"),
        Performance_metrics("Performance.metrics"),
        Log_entryAdded("Log.entryAdded"),

        Target_targetCreated("Target.targetCreated"),
        Target_targetDestroyed("Target.targetDestroyed"),
        Target_targetInfoChanged("Target.targetInfoChanged"),
        Target_attachedToTarget("Target.attachedToTarget"),
        Target_detachedFromTarget("Target.detachedFromTarget"),
        Debugger_scriptParsed("Debugger.scriptParsed"),

        CSS_styleSheetAdded("CSS.styleSheetAdded"),
        DeviceAccess_deviceRequestPrompted("DeviceAccess.deviceRequestPrompted"),

        targetcreated("targetcreated"),
        targetdestroyed("targetdestroyed"),
        targetchanged("targetchanged"),
        disconnected("disconnected"),

        Fetch_requestPaused("Fetch.requestPaused"),
        Fetch_authRequired("Fetch.authRequired"),

        Network_requestWillBeSent("Network.requestWillBeSent"),
        Network_requestServedFromCache("Network.requestServedFromCache"),
        Network_responseReceived("Network.responseReceived"),
        Network_loadingFinished("Network.loadingFinished"),
        Network_loadingFailed("Network.loadingFailed"),
        Network_responseReceivedExtraInfo("Network.responseReceivedExtraInfo"),

        Tracing_tracingComplete("Tracing.tracingComplete"),
        Input_dragIntercepted("Input.dragIntercepted");

        private String eventName;

        CDPSessionEvent(String eventName) {
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
