package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.events.Events;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ruiyun.jvppeteer.core.Constant.DEFAULT_TIMEOUT;
import static com.ruiyun.jvppeteer.core.Constant.RECV_MESSAGE_ERROR_PROPERTY;
import static com.ruiyun.jvppeteer.core.Constant.RECV_MESSAGE_ID_PROPERTY;
import static com.ruiyun.jvppeteer.core.Constant.RECV_MESSAGE_METHOD_PROPERTY;
import static com.ruiyun.jvppeteer.core.Constant.RECV_MESSAGE_PARAMS_PROPERTY;
import static com.ruiyun.jvppeteer.core.Constant.RECV_MESSAGE_RESULT_PROPERTY;

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

    private final Map<Long, SendMsg> callbacks = new ConcurrentHashMap<>();

    private final String targetType;

    private final String sessionId;

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
        this.emit(Events.CDPSESSION_DISCONNECTED.getName(),null);
    }

    /**
     * 发送消息到浏览器
     * @param method 消息签名中的方法
     * @param params 消息签名中的参数
     * @param isBlock 是否是阻塞，阻塞的话会等待结果返回
     * @param outLatch 是否自己提供Countdownlatch
     * @param timeout 超时时间
     * @return 结果
     */
    public JsonNode send(String method,Map<String, Object> params,boolean isBlock,CountDownLatch outLatch,int timeout)  {
        if(connection == null){
            throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
        }
        SendMsg  message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        long id = this.connection.rawSend(message);
        try {
            if(isBlock){
                this.callbacks.putIfAbsent(id,message);
                if(outLatch != null){
                    message.setCountDownLatch(outLatch);
                }else {
                    CountDownLatch latch = new CountDownLatch(1);
                    message.setCountDownLatch(latch);
                }
                boolean hasResult = message.waitForResult(timeout <= 0 ? timeout : DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
                if(!hasResult){
                    throw new TimeoutException("Wait "+method+" for "+(timeout <= 0 ? timeout : DEFAULT_TIMEOUT)+" MILLISECONDS with no response");
                }
                if(StringUtil.isNotEmpty(message.getErrorText())){
                    throw new ProtocolException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            }else{
                if(outLatch != null) {
                    message.setNeedRemove(true);
                    message.setCountDownLatch(outLatch);
                    this.callbacks.putIfAbsent(id,message);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    /**
     * cdpsession send message
     * @param method 方法
     * @param params 参数
     * @param isBlock 是否阻塞，阻塞会等待结果放回
     * @return result
     */
    public JsonNode send(String method,Map<String, Object> params,boolean isBlock)  {
        if(connection == null){
            throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
        }
        SendMsg  message = new SendMsg();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        long id = this.connection.rawSend(message);
        try {
            if(isBlock){
                this.callbacks.putIfAbsent(id,message);
                CountDownLatch latch = new CountDownLatch(1);
                message.setCountDownLatch(latch);
                boolean hasResult = message.waitForResult(DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
                if(!hasResult){
                    throw new TimeoutException("Wait "+method+" for "+DEFAULT_TIMEOUT+" MILLISECONDS with no response");
                }
                if(StringUtil.isNotEmpty(message.getErrorText())){
                    throw new ProtocolException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
                try {
                    JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
                    if (errNode != null) {
                        if(callback.getCountDownLatch() != null){
                            callback.setErrorText(Helper.createProtocolError(node));
                            callback.getCountDownLatch().countDown();
                            callback.setCountDownLatch(null);
                        }
                    }else {
                        JsonNode result = node.get(RECV_MESSAGE_RESULT_PROPERTY);
                        callback.setResult(result);
                        if(callback.getCountDownLatch() != null){
                            callback.getCountDownLatch().countDown();
                            callback.setCountDownLatch(null);
                        }
                    }
                }finally {//最后把callback都移除掉，免得关闭页面后打印错误
                    if(callback.getNeedRemove()){
                        this.callbacks.remove(idLong);
                    }
                }

            }
        }else {
            JsonNode paramsNode = node.get(RECV_MESSAGE_PARAMS_PROPERTY);
            JsonNode method = node.get(RECV_MESSAGE_METHOD_PROPERTY);
            if(method != null)
                this.emit(method.asText(),paramsNode);
        }

    }


    public Connection getConnection() {
        return connection;
    }

    public String getSessionId() {
        return sessionId;
    }
}
