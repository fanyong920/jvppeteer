package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.transport.Callback;
import com.ruiyun.jvppeteer.transport.CallbackRegistry;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import static com.ruiyun.jvppeteer.common.Constant.CODE;
import static com.ruiyun.jvppeteer.common.Constant.ERROR;
import static com.ruiyun.jvppeteer.common.Constant.EVENTS;
import static com.ruiyun.jvppeteer.common.Constant.ID;
import static com.ruiyun.jvppeteer.common.Constant.LISTENER_CLASSES;
import static com.ruiyun.jvppeteer.common.Constant.METHOD;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.PARAMS;
import static com.ruiyun.jvppeteer.common.Constant.SESSION;

public class BidiConnection extends Connection {

    private final List<EventEmitter<ConnectionEvents>> emitters = new ArrayList<>();

    public BidiConnection(String url, ConnectionTransport transport, int delay, int timeout) {
        super(url, transport, delay, timeout);
    }

    public static Connection fromSession(CDPSession session) {
        return session.getConnection();
    }

    @Override
    public JsonNode rawSend(String method, Object params, String sessionId, Integer timeout, boolean isBlocking) {
        ValidateUtil.assertArg(!this.closed, "Protocol error: Connection closed.");
        if (Objects.isNull(timeout)) {
            timeout = this.timeout;
        }
        Callback callback = new Callback(this.id.getAndIncrement(), method, timeout);
        return this.callbacks.create(callback, (id) -> {
            ObjectNode paramsNode = OBJECTMAPPER.createObjectNode();
            paramsNode.put(METHOD, method);
            if (params != null) {
                paramsNode.putPOJO(PARAMS, params);
            }
            paramsNode.put(ID, id);
            String stringifiedMessage;
            try {
                stringifiedMessage = OBJECTMAPPER.writeValueAsString(paramsNode);
            } catch (JsonProcessingException e) {
                throw new JvppeteerException(e);
            }
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("jvppeteer:webDriverBiDi:SEND ► {}", stringifiedMessage);
            }
//            LOGGER.info("jvppeteer:webDriverBiDi:SEND ► {}", stringifiedMessage);
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
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("jvppeteer:webDriverBiDi:RECV ◀{}", message);
            }
//            LOGGER.info("jvppeteer:webDriverBiDi:RECV ◀{}", message);
            JsonNode readTree = OBJECTMAPPER.readTree(message);
            if (readTree.hasNonNull(ID)) {//long类型的id,说明属于发送请求后接收到的消息
                long id = readTree.get(ID).asLong();
                handleCallback(this.callbacks, readTree, id, true);
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
    private static void handleCallback(CallbackRegistry callbacks, JsonNode response, long id, boolean handleListenerThread) {
        if (response.hasNonNull(ERROR)) {
            callbacks.reject(id, createProtocolError(response), response.hasNonNull(CODE) ? response.get(CODE).asInt() : 0, handleListenerThread);
        } else {
            callbacks.resolve(id, response, handleListenerThread);
        }
    }

    @Override
    public String url() {
        return this.url;
    }

    // == unbind
    @Override
    public void dispose() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        //暂停接受任务
        this.handleMessageExecutorService.shutdown();
        this.callbacks.clear();
    }

    // == dispose
    @Override
    public void onClose() {
        this.dispose();
        this.transport.close();
    }


    @Override
    public boolean closed() {
        return this.closed;
    }

    public void pipeTo(EventEmitter<ConnectionEvents> emitter) {
        this.emitters.add(emitter);
    }

    @Override
    public <T> void emit(ConnectionEvents event, T param) {
        for (EventEmitter<ConnectionEvents> emitter : this.emitters) {
            try {
                emitter.emit(event, param);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
        super.emit(event, param);
    }

    @Override
    public List<ProtocolException> getPendingProtocolErrors() {
        return this.callbacks.getPendingProtocolErrors();
    }

    @Override
    public CDPSession session(String sessionId) {
        return BidiCdpSession.sessions.get(sessionId);
    }

    @Override
    public boolean isAutoAttached(String targetId) {
        throw new JvppeteerException("Not supported");
    }

    @Override
    public CDPSession _createSession(TargetInfo targetInfo, boolean isAutoAttachEmulated) {
        throw new JvppeteerException("Not supported");
    }

    private static String createProtocolError(JsonNode errorResponse) {
        String message = errorResponse.get(ERROR).asText() + ", " + errorResponse.get(Constant.MESSAGE).asText();
        if (errorResponse.hasNonNull("stacktrace")) {
            message += "\n" + errorResponse.get("stacktrace").asText();
        }
        return message;
    }

    protected Runnable handleMessageRunnable(JsonNode response) {
        return () -> {
            try {
                if (response.hasNonNull(Constant.TYPE)) {
                    switch (response.get(Constant.TYPE).asText()) {
                        case "success":
                            this.callbacks.resolve(response.get(ID).asLong(), response, false);
                            return;
                        case "error":
                            if (!response.hasNonNull(ID)) {
                                break;
                            }
                            this.callbacks.reject(response.get(ID).asLong(), createProtocolError(response), 0, false);
                            return;
                        case "event":
                            if (isCdpEvent(response)) {
                                BidiCdpSession session = (BidiCdpSession) this.session(response.at("/" + PARAMS + "/" + SESSION).asText());
                                if (Objects.nonNull(session)) {
                                    String event = response.get(PARAMS).get("event").asText();
                                    boolean match = EVENTS.contains(event);
                                    if (match) {//匹配就是有监听该事件
                                        session.emit(ConnectionEvents.valueOf(event.replace(".", "_")), Objects.isNull(LISTENER_CLASSES.get(event)) ? true : OBJECTMAPPER.treeToValue(response.get(PARAMS).get(PARAMS), LISTENER_CLASSES.get(event)));
                                    }
                                }
                                return;
                            }
                            String method = response.get(METHOD).asText();
                            this.emit(ConnectionEvents.valueOf(method.replace(".", "_")), Objects.isNull(LISTENER_CLASSES.get(method)) ? true : OBJECTMAPPER.treeToValue(response.get(PARAMS), LISTENER_CLASSES.get(method)));
                            return;
                    }
                }
                if (response.hasNonNull(ID)) {
                    this.callbacks.reject(response.get(ID).asLong(), "Protocol Error. Message is not in BiDi protocol format: " + response.asText(), 0, false);
                }
                LOGGER.error("jvppeteer error: {}", response.asText());
            } catch (Exception e) {
                LOGGER.error("jvppeteer error: ", e);
            }
        };

    }

    private boolean isCdpEvent(JsonNode event) {
        return event.get(METHOD).asText().startsWith("cdp.");
    }
}
