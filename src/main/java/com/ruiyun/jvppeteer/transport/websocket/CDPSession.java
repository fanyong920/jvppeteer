package com.ruiyun.jvppeteer.transport.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEventPublisher;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeOutException;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.message.SendMsg;
import com.ruiyun.jvppeteer.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CDPSession implements BrowserEventPublisher, Constant {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDPSession.class);

    private Map<Long, SendMsg> callbacks = new HashMap<Long, SendMsg>();

    private String targetType;

    private String sessionId;

    private Connection connection;

    public CDPSession(Connection connection,String targetType, String sessionId) {
        super();
        this.targetType = targetType;
        this.sessionId = sessionId;
        this.connection = connection;
    }

    public void onClosed() throws ExecutionException {
        for (SendMsg callback : callbacks.values()){
            LOGGER.error("Protocol error ("+callback.getMethod()+"): Target closed.");
        }
        connection = null;
        callbacks.clear();
        //TODO
        connection.publishEvent(Events.CDPSESSION_DISCONNECTED.getName(),null);
    }

    public Object send(String method,Map<String, Object> params)  {
        if(connection == null){
            throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
        }
        SendMsg  message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            message.setCountDownLatch(latch);
            long id = this.connection.rawSend(message);
            this.callbacks.putIfAbsent(id,message);
            boolean hasResult = message.waitForResult(DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
            if(!hasResult){
                throw new TimeOutException("Wait result for "+DEFAULT_TIMEOUT+" MILLISECONDS with no response");
            }
            return callbacks.remove(id).getResult();
        } catch (InterruptedException e) {
            LOGGER.error("wait message result is interrupted:",e);
        }
        return null;
    }

    /**
     * 页面分离浏览器
     */
    public void detach(){
        if(connection == null){
            throw new RuntimeException("Session already detached. Most likely the"+this.targetType+"has been closed.");
        }
        Map<String,Object> params = new HashMap<>();
        params.put("sessionId",this.sessionId);
        this.connection.send("Target.detachFromTarget",params,false);
    }

    public void onMessage(JsonNode node) throws ExecutionException {
        JsonNode id = node.get(RECV_MESSAGE_ID_PROPERTY);
        if(id != null) {
            Long idLong = id.asLong();
            SendMsg callback = this.callbacks.get(idLong);
            if (callback != null) {
                JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
//                JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
                if (errNode != null) {
                    if(callback.getCountDownLatch() != null && callback.getCountDownLatch().getCount() > 0){
                        callback.getCountDownLatch().countDown();
                    }
                    throw new ProtocolException(Helper.createProtocolError(node));
                }else {
                    JsonNode result = node.get(RECV_MESSAGE_RESULT_PROPERTY);
                    callback.setResult(result);
                    if(callback.getCountDownLatch() != null && callback.getCountDownLatch().getCount() > 0){
                        callback.getCountDownLatch().countDown();
                    }
                }
            } else {
                JsonNode paramsNode = node.get(RECV_MESSAGE_PARAMS_PROPERTY);
                JsonNode method = node.get(RECV_MESSAGE_METHOD_PROPERTY);
                connection.publishEvent(method.asText(),paramsNode);
            }
        }else {
            throw new RuntimeException("recv message id property is null");
        }

    }

    @Override
    public void publishEvent(String method, Object event) {

    }
}
