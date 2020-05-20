package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.Events;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
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
    private String url;

    private ConnectionTransport transport;
    /**
     * The unit is millisecond
     */
    private int delay;

    private static final AtomicLong lastId = new AtomicLong(0);

    private final Map<Long, SendMsg> callbacks = new ConcurrentHashMap<>();//并发

    private final Map<String, CDPSession> sessions = new HashMap<>();

    private boolean closed;

    private int port;


    public Connection(String url, ConnectionTransport transport, int delay) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        if (this.transport instanceof WebSocketTransport) {
            ((WebSocketTransport) this.transport).addMessageConsumer(this);
        }
        if (StringUtil.isNotEmpty(url)) {
            int start = url.lastIndexOf(":");
            int end = url.indexOf("/", start);
            this.port = Integer.parseInt(url.substring(start + 1, end));
        }
    }

    public JsonNode send(String method, Map<String, Object> params, boolean isWait) {
        SendMsg message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        try {
            long id = rawSend(message);
            if (isWait) {
                CountDownLatch latch = new CountDownLatch(1);
                message.setCountDownLatch(latch);
                boolean hasResult = message.waitForResult(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!hasResult) {
                    throw new TimeoutException("["+message+"] has send ,But wait result for " + Constant.DEFAULT_TIMEOUT + " MILLISECONDS with no response");
                }
                if(message.getError() != null){
                    throw message.getError();
                }
                return callbacks.remove(id).getResult();
            }else {
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
            long id = rawSend(message);
            if (isWait) {
                if (outLatch != null) {
                    message.setCountDownLatch(outLatch);
                } else {
                    CountDownLatch latch = new CountDownLatch(1);
                    message.setCountDownLatch(latch);
                }
                boolean hasResult = message.waitForResult(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!hasResult) {
                    throw new TimeoutException("Wait "+method+" for " + Constant.DEFAULT_TIMEOUT + " MILLISECONDS with no response");
                }
                if(message.getError() != null){
                    throw message.getError();
                }
                return callbacks.remove(id).getResult();
            } else {
                if (outLatch != null)
                    message.setCountDownLatch(outLatch);
            }

        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
        return null;
    }

    public long rawSend(SendMsg message) {
        long id = lastId.incrementAndGet();
        message.setId(id);
		this.callbacks.put(id, message);
        try {
            String sendMsg = Constant.OBJECTMAPPER.writeValueAsString(message);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("SEND -> " + sendMsg);
//            System.out.println("SEND -> " + sendMsg);
            transport.send(sendMsg);
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
        if (delay >= 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                LOGGER.error("slowMo browser Fail:", e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<- RECV " + message);
        }
//        System.out.println("<- RECV " + message);
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
					SendMsg sendMsg = this.callbacks.get(id);
                    JsonNode error = readTree.get(Constant.RECV_MESSAGE_ERROR_PROPERTY);
                    if (error != null) {
                        if (sendMsg.getCountDownLatch() != null && sendMsg.getCountDownLatch().getCount() > 0) {
                            sendMsg.setError(new ProtocolException(Helper.createProtocolError(readTree)));
                            sendMsg.getCountDownLatch().countDown();
                            sendMsg.setCountDownLatch(null);
                        }
                    } else {
                        JsonNode result = readTree.get(Constant.RECV_MESSAGE_RESULT_PROPERTY);
                        sendMsg.setResult(result);
                        if (sendMsg.getCountDownLatch() != null && sendMsg.getCountDownLatch().getCount() > 0) {
                            sendMsg.getCountDownLatch().countDown();
							sendMsg.setCountDownLatch(null);
                        }
                    }
                } else {//是我们监听的事件，把它事件
                    JsonNode paramsNode = readTree.get(Constant.RECV_MESSAGE_PARAMS_PROPERTY);
                    this.emit(method, paramsNode);
                }
            }
        } catch (JsonProcessingException e) {
            throw new ProtocolException(e);
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


    public int getPort() {
        return port;
    }


    public void dispose() {
        this.onClose();
        this.transport.close();
    }

    private void onClose() {
        if (this.closed)
            return;
        this.closed = true;
        for (SendMsg callback : this.callbacks.values())
            LOGGER.error("Protocol error " + callback.getMethod() + " Target closed.");
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

