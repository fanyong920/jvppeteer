package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.ConnectionClosedException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static com.ruiyun.jvppeteer.common.Constant.CODE;
import static com.ruiyun.jvppeteer.common.Constant.ERROR;
import static com.ruiyun.jvppeteer.common.Constant.EVENTS;
import static com.ruiyun.jvppeteer.common.Constant.ID;
import static com.ruiyun.jvppeteer.common.Constant.LISTENER_CLASSES;
import static com.ruiyun.jvppeteer.common.Constant.METHOD;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.PARAMS;
import static com.ruiyun.jvppeteer.common.Constant.RESULT;
import static com.ruiyun.jvppeteer.common.Constant.SESSION_ID;
import static com.ruiyun.jvppeteer.util.Helper.createProtocolErrorMessage;
import static com.ruiyun.jvppeteer.util.Helper.removeNull;

/**
 * web socket client 浏览器级别的连接
 *
 * @author fff
 */
public class CdpConnection extends Connection {


    public CdpConnection(String url, ConnectionTransport transport, int delay, int timeout) {
        super(url, transport, delay, timeout);
    }

    protected Runnable handleMessageRunnable(JsonNode response) {
        return () -> {
            try {
                String method;
                if (response.hasNonNull(METHOD)) {
                    method = response.get(METHOD).asText();
                } else {
                    method = null;
                }
                String sessionId = null;
                JsonNode paramsNode;
                if (response.hasNonNull(PARAMS)) {
                    paramsNode = response.get(PARAMS);
                    if (paramsNode.hasNonNull(SESSION_ID)) {
                        sessionId = paramsNode.get(SESSION_ID).asText();
                    }
                } else {
                    paramsNode = null;
                }
                String parentSessionId = "";
                if (response.hasNonNull(SESSION_ID)) {
                    parentSessionId = response.get(SESSION_ID).asText();
                }
                if ("Target.attachedToTarget".equals(method)) {//attached to target -> page attached to browser
                    assert paramsNode != null;
                    JsonNode typeNode = paramsNode.get(Constant.TARGET_INFO).get(Constant.TYPE);
                    CdpCDPSession cdpSession = new CdpCDPSession(this, typeNode.asText(), sessionId, parentSessionId);
                    this.sessions.put(sessionId, cdpSession);
                    this.emit(ConnectionEvents.sessionAttached, cdpSession);
                    CDPSession parentSession = this.sessions.get(parentSessionId);
                    if (Objects.nonNull(parentSession)) {
                        parentSession.emit(ConnectionEvents.sessionAttached, cdpSession);
                    }
                } else if ("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
                    CDPSession cdpSession = this.sessions.get(sessionId);
                    if (Objects.nonNull(cdpSession)) {
                        cdpSession.onClosed();
                        this.sessions.remove(sessionId);
                        this.emit(ConnectionEvents.sessionDetached, cdpSession);
                        CDPSession parentSession = this.sessions.get(parentSessionId);
                        if (Objects.nonNull(parentSession)) {
                            parentSession.emit(ConnectionEvents.sessionDetached, cdpSession);
                        }
                    }
                }
                if (StringUtil.isNotEmpty(parentSessionId)) {
                    CdpCDPSession parentSession = this.sessions.get(parentSessionId);
                    if (Objects.nonNull(parentSession)) {
                        parentSession.onMessage(response, callbacks);
                    }
                } else if (response.hasNonNull(ID)) {//long类型的id,说明属于这次发送消息后接受的回应
                    long id = response.get(ID).asLong();
                    handleCdpCallback(this.callbacks, response, id, false);
                } else {//是一个事件，那么响应监听器
                    boolean match = EVENTS.contains(method);
                    if (match) {//匹配就是有监听该事件
                        assert method != null;
                        this.emit(ConnectionEvents.valueOf(method.replace(".", "_")), LISTENER_CLASSES.get(method) == null ? true : OBJECTMAPPER.treeToValue(paramsNode, LISTENER_CLASSES.get(method)));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Handle message error: ", e);
            }
        };
    }

    public boolean isAutoAttached(String targetId) {
        return !this.manuallyAttached.remove(targetId);
    }

    public JsonNode rawSend(String method, Object params, String sessionId, Integer timeout,
                            boolean isBlocking) {
        if (this.closed) {
            throw new ConnectionClosedException("Connection closed.");
        };
        if (timeout == null) {
            timeout = this.timeout;
        }
        Callback callback = new Callback(this.id.incrementAndGet(), method, timeout);
        return this.callbacks.create(callback, (id) -> {
            ObjectNode objectNode = OBJECTMAPPER.createObjectNode();
            objectNode.put(METHOD, method);
            if (Objects.nonNull(params)) {
                removeNull(params);
                objectNode.putPOJO(PARAMS, params);
            }
            objectNode.put(ID, id);
            if (StringUtil.isNotEmpty(sessionId)) {
                objectNode.put(SESSION_ID, sessionId);
            }
            String stringifiedMessage;
            try {
                stringifiedMessage = OBJECTMAPPER.writeValueAsString(objectNode);
            } catch (JsonProcessingException e) {
                throw new JvppeteerException(e);
            }
//            LOGGER.info("jvppeteer:protocol:SEND ► {}", stringifiedMessage);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("jvppeteer:protocol:SEND ► {}", stringifiedMessage);
            }
            this.transport.send(stringifiedMessage);
        }, isBlocking);
    }

    @Override
    public void onMessage(String message) {
        try {
            if (StringUtil.isEmpty(message)) {
                return;
            }
            if (this.delay > 0) {
                Helper.justWait(this.delay);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("jvppeteer:protocol:RECV ◀ {}", message);
            }
            JsonNode readTree = OBJECTMAPPER.readTree(message);
            if (readTree.hasNonNull(ID)) {//long类型的id,说明属于发送请求后接收到的消息
                long id = readTree.get(ID).asLong();
                handleCdpCallback(this.callbacks, readTree, id, true);
            }
            this.handleMessageExecutorService.submit(handleMessageRunnable(readTree));
        } catch (Exception e) {
            LOGGER.error("jvppeteer error:", e);
        }
    }

    /**
     * 解析回调并根据响应结果进行处理
     * <p>
     * 此方法主要用于根据服务器返回的响应数据来解决回调操作它检查响应中是否包含错误信息，
     * 如果包含，则调用reject方法回传错误信息和错误代码；否则，调用resolve方法回传响应结果
     *
     * @param callbacks            回调注册表，用于管理所有的回调操作
     * @param response             浏览器返回的JSON响应数据
     * @param id                   请求的唯一标识符，用于匹配对应的回调操作
     * @param handleListenerThread 是否在监听线程中处理回调，默认为false
     */
    static void handleCdpCallback(CallbackRegistry callbacks, JsonNode response, long id, boolean handleListenerThread) {
        if (response.hasNonNull(ERROR)) {
            callbacks.reject(id, createProtocolErrorMessage(response), response.get(ERROR).hasNonNull(CODE) ? response.get(ERROR).get(CODE).asInt() : 0, handleListenerThread);
        } else {
            callbacks.resolve(id, response.get(RESULT), handleListenerThread);
        }
    }

    /**
     * 从{@link CdpCDPSession}中拿到对应的{@link CdpConnection}
     *
     * @param client cdpsession
     * @return Connection
     */
    public static Connection fromSession(CdpCDPSession client) {
        return client.connection();
    }

    /**
     * 根据给定的 Target info 创建一个{@link CdpCDPSession}
     *
     * @param targetInfo 给定的 Target info
     * @return CDPSession 创建的
     */
    public CDPSession createSession(TargetInfo targetInfo) {
        return this._createSession(targetInfo, false);
    }

    public CDPSession _createSession(TargetInfo targetInfo, boolean isAutoAttachEmulated) {
        if (!isAutoAttachEmulated) {
            this.manuallyAttached.add(targetInfo.getTargetId());
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("targetId", targetInfo.getTargetId());
        params.put("flatten", true);
        JsonNode response = this.send("Target.attachToTarget", params, null, true);
        if (response.hasNonNull(SESSION_ID)) {
            String sessionId = response.get(SESSION_ID).asText();
            CDPSession cdpSession = this.sessions.get(sessionId);
            if (cdpSession == null) {
                throw new JvppeteerException("CDPSession creation failed.");
            }
            return cdpSession;
        } else {
            throw new JvppeteerException("CDPSession creation failed.");
        }
    }

    public String url() {
        return this.url;
    }

    public CDPSession session(String sessionId) {
        return this.sessions.get(sessionId);
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
        this.handleMessageExecutorService.shutdown();
//        waitForHandleMessageThreadFinish();
        this.callbacks.clear();
        for (CDPSession session : this.sessions.values())
            session.onClosed();
        this.sessions.clear();
        this.emit(ConnectionEvents.CDPSession_Disconnected, true);
    }


    public List<ProtocolException> getPendingProtocolErrors() {
        return new ArrayList<>(this.callbacks.getPendingProtocolErrors());
    }

    public boolean closed() {
        return this.closed;
    }
}

