package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.Events;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * web socket client 浏览器级别的连接
 *
 * @author fff
 */
public class Connection extends EventEmitter implements Consumer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    /**
     * websoket url
     */
    private final String url;

    private final ConnectionTransport transport;
    /**
     * The unit is millisecond
     */
    private final int delay;

    private static final AtomicLong lastId = new AtomicLong(0);

    private final Map<Long, SendMsg> callbacks = new ConcurrentHashMap<>();//并发

    private final Map<String, CDPSession> sessions = new ConcurrentHashMap<>();

    private boolean closed;

    public Connection(String url, ConnectionTransport transport, int delay) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        if (this.transport instanceof WebSocketTransport) {
            ((WebSocketTransport) this.transport).addMessageConsumer(this);
        }
    }

    public JsonNode send(String method, Map<String, Object> params, boolean isWait) {
        SendMsg message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        try {
            if (isWait) {
                message.setCountDownLatch(new CountDownLatch(1));
                long id = rawSend(message,true,this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringUtil.isNotEmpty(message.getErrorText())) {
                    throw new ProtocolException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
               rawSend(message,false,this.callbacks);
                return null;
            }
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public JsonNode send(String method, Map<String, Object> params, boolean isWait, CountDownLatch outLatch) {
        SendMsg message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        try {
            if (isWait) {
                if (outLatch != null) {
                    message.setCountDownLatch(outLatch);
                } else {
                    message.setCountDownLatch(new CountDownLatch(1));
                }
                long id = this.rawSend(message, true,this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringUtil.isNotEmpty(message.getErrorText())) {
                    throw new ProtocolException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                if (outLatch != null) {
                    message.setNeedRemove(true);
                    message.setCountDownLatch(outLatch);
                    this.rawSend(message, true,this.callbacks);
                } else {
                    this.rawSend(message, false,this.callbacks);
                }
            }


        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
        return null;
    }

    /**
     *
     * @param message 发送的消息内容
     * @param putCallback 是否应该放进callbacks里面
     * @param callbacks 对应的callbacks
     * @return 发送消息的id
     */
    public long rawSend(SendMsg message, boolean putCallback,Map<Long, SendMsg> callbacks) {
        long id = lastId.incrementAndGet();
        message.setId(id);
        try {
            if (putCallback) {
                callbacks.put(id, message);
            }
            String sendMsg = Constant.OBJECTMAPPER.writeValueAsString(message);
            transport.send(sendMsg);
            LOGGER.trace("SEND -> " + sendMsg);
            return id;
        } catch (JsonProcessingException e) {
            LOGGER.error("parse message fail:", e);
        }
        return -1;
    }

    /**
     * recevie message from browser by websocket
     *
     * @param message 从浏览器接受到的消息
     */
    public void onMessage(String message) {

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOGGER.error("slowMo browser Fail:", e);
            }
        }
        LOGGER.trace("<- RECV " + message);
        try {
            if (StringUtil.isNotEmpty(message)) {
                JsonNode readTree = Constant.OBJECTMAPPER.readTree(message);
                JsonNode methodNode = readTree.get(Constant.RECV_MESSAGE_METHOD_PROPERTY);
                String method = null;
                if (methodNode != null) {
                    method = methodNode.asText();
                }
                if ("Target.attachedToTarget".equals(method)) {//attached to target -> page attached to browser
                    JsonNode paramsNode = readTree.get(Constant.RECV_MESSAGE_PARAMS_PROPERTY);
                    JsonNode sessionId = paramsNode.get(Constant.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    JsonNode typeNode = paramsNode.get(Constant.RECV_MESSAGE_TARGETINFO_PROPERTY).get(Constant.RECV_MESSAGE_TYPE_PROPERTY);
                    CDPSession cdpSession = new CDPSession(this, typeNode.asText(), sessionId.asText());
                    sessions.put(sessionId.asText(), cdpSession);
                } else if ("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
                    JsonNode paramsNode = readTree.get(Constant.RECV_MESSAGE_PARAMS_PROPERTY);
                    JsonNode sessionId = paramsNode.get(Constant.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    String sessionIdString = sessionId.asText();
                    CDPSession cdpSession = sessions.get(sessionIdString);
                    if (cdpSession != null) {
                        cdpSession.onClosed();
                        sessions.remove(sessionIdString);
                    }
                }
                JsonNode objectSessionId = readTree.get(Constant.RECV_MESSAGE_SESSION_ID_PROPERTY);
                JsonNode objectId = readTree.get(Constant.RECV_MESSAGE_ID_PROPERTY);
                if (objectSessionId != null) {//cdpsession消息，当然cdpsession来处理
                    String objectSessionIdString = objectSessionId.asText();
                    CDPSession cdpSession = this.sessions.get(objectSessionIdString);
                    if (cdpSession != null) {
                        cdpSession.onMessage(readTree);
                    }
                } else if (objectId != null) {//long类型的id,说明属于这次发送消息后接受的回应
                    long id = objectId.asLong();
                    SendMsg callback = this.callbacks.get(id);
                    if (callback != null) {
                        try {
                            JsonNode error = readTree.get(Constant.RECV_MESSAGE_ERROR_PROPERTY);
                            if (error != null) {
                                if (callback.getCountDownLatch() != null) {
                                    callback.setErrorText(Helper.createProtocolError(readTree));
                                }
                            } else {
                                JsonNode result = readTree.get(Constant.RECV_MESSAGE_RESULT_PROPERTY);
                                callback.setResult(result);
                            }
                        } finally {

                            //最后把callback都移除掉，免得关闭页面后打印错误
                            if (callback.getNeedRemove()) {
                                this.callbacks.remove(id);
                            }

                            //放行等待的线程
                            if (callback.getCountDownLatch() != null) {
                                callback.getCountDownLatch().countDown();
                                callback.setCountDownLatch(null);
                            }
                        }
                    }
                } else {//是我们监听的事件，把它事件
                    JsonNode paramsNode = readTree.get(Constant.RECV_MESSAGE_PARAMS_PROPERTY);
                    this.emit(method, paramsNode);
                }
            }
        } catch (Exception e) {
            ProtocolException protocolException =  new ProtocolException();
            protocolException.initCause(e);
            throw protocolException;
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
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetInfo.getTargetId());
        params.put("flatten", true);
        JsonNode result = this.send("Target.attachToTarget", params, true);
        return this.sessions.get(result.get(Constant.RECV_MESSAGE_SESSION_ID_PROPERTY).asText());
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
        this.onClose();
        this.transport.close();
    }

    private void onClose() {
        if (this.closed)
            return;
        this.closed = true;
        for (SendMsg callback : this.callbacks.values()) {
            LOGGER.error("Protocol error " + callback.getMethod() + " Target closed.");
        }
        this.callbacks.clear();
        for (CDPSession session : this.sessions.values())
            session.onClosed();
        this.sessions.clear();
        this.emit(Events.CONNECTION_DISCONNECTED.getName(), null);
    }


    public boolean getClosed() {
        return closed;
    }

}

