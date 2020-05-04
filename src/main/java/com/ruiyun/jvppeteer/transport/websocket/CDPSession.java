package com.ruiyun.jvppeteer.transport.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.events.impl.EventEmitter;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.message.SendMsg;
import com.ruiyun.jvppeteer.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ruiyun.jvppeteer.Constant.DEFAULT_TIMEOUT;
import static com.ruiyun.jvppeteer.Constant.RECV_MESSAGE_ERROR_PROPERTY;
import static com.ruiyun.jvppeteer.Constant.RECV_MESSAGE_ID_PROPERTY;
import static com.ruiyun.jvppeteer.Constant.RECV_MESSAGE_METHOD_PROPERTY;
import static com.ruiyun.jvppeteer.Constant.RECV_MESSAGE_PARAMS_PROPERTY;
import static com.ruiyun.jvppeteer.Constant.RECV_MESSAGE_RESULT_PROPERTY;

/**
 *The CDPSession instances are used to talk raw Chrome Devtools Protocol:
 *
 * protocol methods can be called with session.send method.
 * protocol events can be subscribed to with session.on method.
 * Useful links:
 *
 * Documentation on DevTools Protocol can be found here: DevTools Protocol Viewer.
 * Getting Started with DevTools Protocol: https://github.com/aslushnikov/getting-started-with-cdp/blob/master/README.md
 */
public class CDPSession extends EventEmitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDPSession.class);

    private Map<Long, SendMsg> callbacks = new HashMap<>();

    private String targetType;

    private String sessionId;

    private Connection connection;

    public CDPSession(Connection connection,String targetType, String sessionId) {
        super();
        this.targetType = targetType;
        this.sessionId = sessionId;
        this.connection = connection;
    }

    public void onClosed() {
        for (SendMsg callback : callbacks.values()){
            LOGGER.error("Protocol error ("+callback.getMethod()+"): Target closed.");
        }
        connection = null;
        callbacks.clear();
        //TODO
        connection.emit(Events.CDPSESSION_DISCONNECTED.getName(),null);
    }

    /**
     * cdpsession send message
     * @param method 方法
     * @param params 参数
     * @return result
     */
    public JsonNode send(String method,Map<String, Object> params,boolean isLock,CountDownLatch outLatch,int timeout)  {
        if(connection == null){
            throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
        }
        SendMsg  message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        long id = this.connection.rawSend(message);
        this.callbacks.putIfAbsent(id,message);
        try {
            if(isLock){
                if(outLatch != null){
                    message.setCountDownLatch(outLatch);
                }else {
                    CountDownLatch latch = new CountDownLatch(1);
                    message.setCountDownLatch(latch);
                }
                boolean hasResult = message.waitForResult(timeout <= 0 ? timeout : DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
                if(!hasResult){
                    throw new TimeoutException("Wait result for "+DEFAULT_TIMEOUT+" MILLISECONDS with no response");
                }
                return callbacks.remove(id).getResult();
            }else{
                if(outLatch != null)
                    message.setCountDownLatch(outLatch);
            }

        } catch (InterruptedException e) {
            LOGGER.error("wait message result is interrupted:",e);
        }
        return null;
    }
    /**
     * cdpsession send message
     * @param method 方法
     * @param params 参数
     * @return result
     */
    public JsonNode send(String method,Map<String, Object> params,boolean isWait)  {
        if(connection == null){
            throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
        }
        SendMsg  message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        try {
            if(isWait){
                CountDownLatch latch = new CountDownLatch(1);
                message.setCountDownLatch(latch);
            }
            long id = this.connection.rawSend(message);
            this.callbacks.putIfAbsent(id,message);
            boolean hasResult = message.waitForResult(DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
            if(!hasResult){
                throw new TimeoutException("Wait result for "+DEFAULT_TIMEOUT+" MILLISECONDS with no response");
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

    public void onMessage(JsonNode node) {
        JsonNode id = node.get(RECV_MESSAGE_ID_PROPERTY);
        if(id != null) {
            Long idLong = id.asLong();
            SendMsg callback = this.callbacks.get(idLong);
            if (callback != null) {
                JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
//                JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
                System.out.println("errNode="+errNode);
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
                if(method != null)
                this.emit(method.asText(),paramsNode);
            }
        }else {
            throw new RuntimeException("recv message id property is null");
        }

    }


    public Connection getConnection() {
        return connection;
    }

    public String getSessionId() {
        return sessionId;
    }
}
