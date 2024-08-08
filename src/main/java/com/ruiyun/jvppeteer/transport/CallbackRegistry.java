package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CallbackRegistry  {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackRegistry.class);
    private final Map<Integer, Callback> callbacks = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    public JsonNode create(String label, Integer timeout, Consumer<Integer> request,boolean isBlocking){
        Callback callback = new Callback(idGenerator.incrementAndGet(), label);
        this.callbacks.put(callback.id(), callback);
        //完成时移除回调
        callback.getSubject().doAfterTerminate(() -> this.callbacks.remove(callback.id())).subscribe();
        try {
            request.accept(callback.id());
            //不阻塞时，不关心结果
            if(!isBlocking) return null;
        } catch (Exception e) {
            try {
                if(timeout > 0){
                    return callback.getSubject().timeout(timeout, TimeUnit.MILLISECONDS).blockingGet();
                }else if(timeout == 0){
                    return callback.getSubject().blockingGet();
                }else {
                  throw new JvppeteerException("Timeout < 0,It shouldn't happen");
                }
            }catch (Exception ex){
                LOGGER.error("Callback waiting Error:" ,e);
            }
            callback.reject(e);
            LOGGER.error("There was an error sending the request:" ,e);
        }
        if(timeout > 0){
            return callback.getSubject().timeout(timeout, TimeUnit.MILLISECONDS).blockingGet();
        }else if(timeout == 0){
            return callback.getSubject().blockingGet();
        }else {
            throw new JvppeteerException("Timeout < 0");
        }
    }
    public void reject(int id, String message,String originalMessage){
        Callback callback = this.callbacks.get(id);
        if(callback != null){
            this._reject(callback, message,originalMessage);
        }
    }

    private void _reject(Callback callback, String errorMessage, String originalMessage) {
        ProtocolException protocolException = new ProtocolException("Protocol error (" + callback.label() + "): " + errorMessage);
        protocolException.setCode(callback.error().getCode());
        if(StringUtil.isNotEmpty(originalMessage)){
            protocolException.setOriginalMessage(originalMessage);
        }
        callback.setError(protocolException);
        callback.reject(protocolException);
    }

    private void _reject(Callback callback, ProtocolException error, String originalMessage) {//todo 用法
        String message = error.getMessage();
        ProtocolException protocolException = new ProtocolException("Protocol error (" + callback.label() + "): " + message, callback.error());
        protocolException.setCode(error.getCode());
        if(StringUtil.isNotEmpty(originalMessage)){
            protocolException.setOriginalMessage(originalMessage);
        }
        callback.setError(protocolException);
        callback.reject(protocolException);
    }
    public void resolve(int id, JsonNode value){
        Callback callback = this.callbacks.get(id);
        if(callback != null){
            callback.resolve(value);
        }
    }
    //这里会释放线程等待，避免死锁
    public void clear(){
        this.callbacks.forEach((key,callback)->{
            this._reject(callback,"Target closed","");
        });
    }
    public List<ProtocolException> getPendingProtocolErrors(){
        List<ProtocolException> results = new ArrayList<>();
        this.callbacks.forEach((key,callback)->{
            ProtocolException error = callback.error();
            if(error != null){
                results.add(new ProtocolException(callback.error() + " timed out. Trace: " + Arrays.toString(callback.error().getStackTrace())));
            }
        });
        return results;
    }
}
