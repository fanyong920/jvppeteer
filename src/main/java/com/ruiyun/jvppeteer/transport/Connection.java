package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.common.Constant.CODE;
import static com.ruiyun.jvppeteer.common.Constant.JV_EMIT_EVENT_THREAD;
import static com.ruiyun.jvppeteer.common.Constant.JV_HANDLE_MESSAGE_THREAD;
import static com.ruiyun.jvppeteer.common.Constant.LISTENNER_CLASSES;
import static com.ruiyun.jvppeteer.common.Constant.ERROR;
import static com.ruiyun.jvppeteer.common.Constant.ID;
import static com.ruiyun.jvppeteer.common.Constant.METHOD;
import static com.ruiyun.jvppeteer.common.Constant.PARAMS;
import static com.ruiyun.jvppeteer.common.Constant.RESULT;
import static com.ruiyun.jvppeteer.common.Constant.SESSION_ID;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.util.Helper.createProtocolErrorMessage;

/**
 * web socket client 浏览器级别的连接
 *
 * @author fff
 */
public class Connection extends EventEmitter<CDPSession.CDPSessionEvent> implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    private final String url;
    private final ConnectionTransport transport;
    private final int delay;
    private final int timeout;
    private final Map<String, CDPSession> sessions = new ConcurrentHashMap<>();
    protected volatile boolean closed;
    final Set<String> manuallyAttached = new HashSet<>();
    private final CallbackRegistry callbacks = new CallbackRegistry();//并发
    private volatile boolean handleMessageThreadFinish = false;

    public ConcurrentLinkedQueue<Runnable> getEventQueue() {
        return this.eventQueue;
    }

    private final ConcurrentLinkedQueue<Runnable> eventQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<JsonNode> messagesQueue = new ConcurrentLinkedQueue<>();
    private List<String> events = null;
    final AtomicLong id = new AtomicLong(1);

    public Connection(String url, ConnectionTransport transport, int delay, int timeout) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        this.timeout = timeout;
        this.transport.setConnection(this);
        this.startThread();
    }

    /**
     * 专门处理从websocket接收到的消息的线程
     */
    public void startHandleMessageThread() {
        Runnable runnable = () -> {
            do {
                synchronized (this) {
                    try {
                        //如果还有event还没处理完，就等待
                        if (!this.eventQueue.isEmpty()) {
                            this.wait();
                        }
                        JsonNode response = this.messagesQueue.poll();
                        if (response == null) {
                            continue;
                        }
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
                            JsonNode typeNode = paramsNode.get(Constant.TARGET_INFO).get(Constant.TYPE);
                            CDPSession cdpSession = new CDPSession(this, typeNode.asText(), sessionId, parentSessionId);
                            this.sessions.put(sessionId, cdpSession);
                            this.eventQueue.offer(() -> this.emit(CDPSession.CDPSessionEvent.sessionAttached, cdpSession));
                            this.wait();
                            CDPSession parentSession = this.sessions.get(parentSessionId);
                            if (parentSession != null) {
                                this.eventQueue.offer(() -> parentSession.emit(CDPSession.CDPSessionEvent.sessionAttached, cdpSession));
                                this.wait();
                            }
                        } else if ("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
                            CDPSession cdpSession = this.sessions.get(sessionId);
                            if (cdpSession != null) {
                                cdpSession.onClosed();
                                this.sessions.remove(sessionId);
                                this.eventQueue.offer(() -> this.emit(CDPSession.CDPSessionEvent.sessionDetached, cdpSession));
                                this.wait();
                                CDPSession parentSession = this.sessions.get(parentSessionId);
                                if (parentSession != null) {
                                    this.eventQueue.offer(() -> parentSession.emit(CDPSession.CDPSessionEvent.sessionDetached, cdpSession));
                                    this.wait();
                                }
                            }
                        }
                        if (StringUtil.isNotEmpty(parentSessionId)) {
                            CDPSession parentSession = this.sessions.get(parentSessionId);
                            if (parentSession != null) {
                                if (parentSession.onMessage(response, callbacks)) {
                                    this.wait();
                                }
                            }
                        } else if (response.hasNonNull(ID)) {//long类型的id,说明属于这次发送消息后接受的回应
                            long id = response.get(ID).asLong();
                            resolveCallback(this.callbacks, response, id, false);
                        } else {//是一个事件，那么响应监听器
                            if (events == null) {
                                events = Arrays.stream(CDPSession.CDPSessionEvent.values()).map(CDPSession.CDPSessionEvent::getEventName).collect(Collectors.toList());
                            }
                            boolean match = events.contains(method);
                            if (!match) {//不匹配就是没有监听该事件
                                return;
                            }
                            this.eventQueue.offer(() -> {
                                try {
                                    this.emit(CDPSession.CDPSessionEvent.valueOf(method.replace(".", "_")), LISTENNER_CLASSES.get(method) == null ? true : OBJECTMAPPER.treeToValue(paramsNode, LISTENNER_CLASSES.get(method)));
                                } catch (JsonProcessingException e) {
                                    LOGGER.error("onMessage error:", e);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Handle message error: ", e);
                    }
                }
            } while (!this.messagesQueue.isEmpty() || !this.closed);
            this.handleMessageThreadFinish = true;
        };
        Thread.ofVirtual().name(JV_HANDLE_MESSAGE_THREAD, 1).start(runnable);
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
    protected static void resolveCallback(CallbackRegistry callbacks, JsonNode response, long id, boolean handleListenerThread) {
        if (response.hasNonNull(ERROR)) {
            callbacks.reject(id, createProtocolErrorMessage(response), response.get(ERROR).hasNonNull(CODE) ? response.get(ERROR).get(CODE).asInt() : 0, handleListenerThread);
        } else {
            callbacks.resolve(id, response.get(RESULT), handleListenerThread);
        }
    }

    /**
     * 启动用于处理监听器的线程
     */
    public void startEmitEventThread() {
        //先唤醒
        Runnable eventRunnable = () -> {
            do {
                synchronized (this) {
                    try {
                        Runnable runnable = eventQueue.poll();
                        if (runnable == null) {
                            this.notifyAll();//先唤醒
                        } else {
                            runnable.run();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Emit event error: ", e);
                    }
                }
            } while (!this.handleMessageThreadFinish || !this.messagesQueue.isEmpty() || !this.eventQueue.isEmpty() || !this.closed);
        };
        Thread.ofVirtual().name(JV_EMIT_EVENT_THREAD, 1).start(eventRunnable);
    }

    public boolean isAutoAttached(String targetId) {
        return !this.manuallyAttached.remove(targetId);
    }

    public JsonNode send(String method) {
        return this.rawSend(method, null, null, this.timeout, true);
    }

    public JsonNode send(String method, Map<String, Object> params) {
        return this.rawSend(method, params, null, this.timeout, true);
    }

    public JsonNode send(String method, Map<String, Object> params, Integer timeout, boolean isBlocking) {
        return this.rawSend(method, params, null, timeout, isBlocking);
    }

    public JsonNode rawSend(String method, Map<String, Object> params, String sessionId, Integer timeout,
                            boolean isBlocking) {
        ValidateUtil.assertArg(!this.closed, "Protocol error: Connection closed.");
        if (timeout == null) {
            timeout = this.timeout;
        }
        Callback callback = new Callback(this.id.incrementAndGet(), method, timeout);
        return this.callbacks.create(callback, (id) -> {
            ObjectNode objectNode = OBJECTMAPPER.createObjectNode();
            objectNode.put(METHOD, method);
            if (params != null) {
                objectNode.set(PARAMS, OBJECTMAPPER.valueToTree(params));
            }
            objectNode.put(ID, id);
            if (StringUtil.isNotEmpty(sessionId)) {
                objectNode.put(SESSION_ID, sessionId);
            }
            String stringifiedMessage = objectNode.toString();
            LOGGER.trace("jvppeteer:protocol:SEND ► {}", stringifiedMessage);
            this.transport.send(stringifiedMessage);
        }, isBlocking);
    }

    /**
     * recevie message from browser by websocket
     *
     * @param message 从浏览器接受到的消息
     */
    public void onMessage(String message) {
        try {
            if (StringUtil.isEmpty(message)) {
                return;
            }
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
            JsonNode readTree = OBJECTMAPPER.readTree(message);
            if (readTree.hasNonNull(ID)) {//long类型的id,说明属于发送请求后接收到的消息
                long id = readTree.get(ID).asLong();
                resolveCallback(this.callbacks, readTree, id, true);
            }
            this.messagesQueue.offer(readTree);
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

    @Override
    public void accept(String t) {
        onMessage(t);
    }

    public void dispose() {
        this.onClose();//清理Connection资源
        this.transport.close();//关闭websocket
    }

    private void onClose() {
        if (this.closed)
            return;
        this.closed = true;
        this.transport.setConnection(null);
        waitForHandleMessageThreadFinish();
        this.callbacks.clear();
        for (CDPSession session : this.sessions.values())
            session.onClosed();
        this.sessions.clear();
        this.emit(CDPSession.CDPSessionEvent.CDPSession_Disconnected, true);
    }

    private void waitForHandleMessageThreadFinish() {
        while (true) {
            if (this.handleMessageThreadFinish) {
                break;
            }
        }
    }

    public List<ProtocolException> getPendingProtocolErrors() {
        return new ArrayList<>(this.callbacks.getPendingProtocolErrors());
    }

    private void startThread() {
        this.startEmitEventThread();
        this.startHandleMessageThread();
    }

    public boolean closed() {
        return this.closed;
    }
}

