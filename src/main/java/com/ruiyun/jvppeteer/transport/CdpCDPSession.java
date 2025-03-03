package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.core.CdpTarget;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.EVENTS;
import static com.ruiyun.jvppeteer.common.Constant.ID;
import static com.ruiyun.jvppeteer.common.Constant.LISTENER_CLASSES;
import static com.ruiyun.jvppeteer.common.Constant.METHOD;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.PARAMS;
import static com.ruiyun.jvppeteer.common.Constant.SESSION_ID;
import static com.ruiyun.jvppeteer.transport.CdpConnection.handleCdpCallback;

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
public class CdpCDPSession extends CDPSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(CdpCDPSession.class);
    private final String targetType;
    private final String sessionId;
    private final CdpConnection connection;
    private final String parentSessionId;
    private CdpTarget target;
    private volatile boolean detached;

    public CdpCDPSession(CdpConnection connection, String targetType, String sessionId, String parentSessionId) {
        super();
        this.targetType = targetType;
        this.sessionId = sessionId;
        this.connection = connection;
        this.parentSessionId = parentSessionId;
    }

    public CDPSession parentSession() {
        if (StringUtil.isEmpty(this.parentSessionId)) {
            // In some cases, e.g., DevTools pages there is no parent session. In this
            // case, we treat the current session as the parent session.
            return this;
        }
        if (Objects.nonNull(this.connection)) {
            return this.connection.session(this.parentSessionId);
        } else {
            return null;
        }
    }

    public void setTarget(CdpTarget target) {
        this.target = target;
    }

    public void onClosed() {
        this.detached = true;
        this.emit(ConnectionEvents.CDPSession_Disconnected, true);
    }

    public JsonNode send(String method) {
        return this.send(method, null);
    }

    public JsonNode send(String method, Map<String, Object> params) {
        return this.send(method, params, null, true);
    }

    public JsonNode send(String method, Object params, Integer timeout, boolean isBlocking) {
        if (this.detached()) {
            throw new JvppeteerException("Protocol error (" + method + "): Session closed. Most likely the" + this.targetType + "has been closed.");
        }
        return this.connection.rawSend(method, params, this.sessionId, timeout, isBlocking);
    }

    /**
     * 页面分离浏览器
     */
    public void detach() {
        if (this.detached()) {
            throw new JvppeteerException("Session already detached. Most likely the " + this.targetType + "has been closed.");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put(SESSION_ID, this.sessionId);
        this.connection.send("Target.detachFromTarget", params);
        this.detached = true;
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
     */
    public void onMessage(JsonNode response, CallbackRegistry callbacks) {
        JsonNode paramsNode = response.get(PARAMS);
        JsonNode methodNode = response.get(METHOD);
        try {
            if (response.hasNonNull(ID)) {//long类型的id,说明属于这次发送消息后接受的回应
                long id = response.get(ID).asLong();
                handleCdpCallback(callbacks, response, id, false);
            } else {//发射数据，执行事件的监听方法
                ValidateUtil.assertArg(!response.hasNonNull(ID), "Should not contain id, " + response);
                if (Objects.isNull(this.connection)) {
                    return;
                }
                String method = methodNode.asText();
                boolean match = EVENTS.contains(method);
                if (match) {//不匹配就是没有监听该事件
                    this.emit(ConnectionEvents.valueOf(method.replace(".", "_")), LISTENER_CLASSES.get(method) == null ? true : OBJECTMAPPER.treeToValue(paramsNode, LISTENER_CLASSES.get(method)));
                }
            }
        } catch (Exception e) {
            LOGGER.error("emit error", e);
        }
    }

    public Connection connection() {
        return this.connection;
    }

    @Override
    public boolean detached() {
        return  this.connection.closed() || this.detached;
    }

    public String id() {
        return this.sessionId;
    }

    public CdpTarget getTarget() {
        Objects.requireNonNull(this.target, "Target must exist");
        return this.target;
    }

}
