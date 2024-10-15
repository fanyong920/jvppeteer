package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.exception.ProtocolException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.ruiyun.jvppeteer.common.Constant.JV_EMIT_EVENT_THREAD;
import static com.ruiyun.jvppeteer.util.Helper.throwError;


public class CallbackRegistry {
    /**
     * 用于存放所有回调
     */
    private final Map<Long, Callback> callbacks = new ConcurrentHashMap<>();
    /**
     * 用于单独记录JV_EMIT_EVENT_THREAD线程请求的回调
     */
    private final Map<Long, Callback> eventCallbacks = new ConcurrentHashMap<>();

    public JsonNode create(Callback callback, Consumer<Long> request, boolean isBlocking) {
        put(callback, isBlocking);
        try {
            //send request
            request.accept(callback.id());
            //不阻塞时，不关心结果
            if (!isBlocking) return null;
            return callback.waitForResponse();
        } catch (InterruptedException e) {
            //发生错误，移除回调
            this.callbacks.remove(callback.id());
            //放行线程
            callback.reject();
            throwError(e);
            return null;
        }
    }

    private void put(Callback callback, boolean isBlocking) {
        if (isBlocking) {//只有等待结果的，才放进去
            String name = Thread.currentThread().getName();
            if (name.startsWith(JV_EMIT_EVENT_THREAD)) {//说明是JV_EMIT_EVENT_THREAD线程中发送的请求接受到的消息
                eventCallbacks.put(callback.id(), callback);
            } else {
                callbacks.put(callback.id(), callback);
            }
        }
    }

    public void reject(long id, String message, int code, boolean handleListenerThread) {
        if (handleListenerThread) {
            Callback eventCallback = this.eventCallbacks.remove(id);
            if (eventCallback != null) {
                this._reject(eventCallback, message, code);
                this.callbacks.remove(id);
            }
        } else {
            Callback callback = this.callbacks.remove(id);
            if (callback != null) {
                this._reject(callback, message, code);
            }
        }
    }

    private void _reject(Callback callback, String errorMessage, int code) {
        callback.reject("Protocol error (method：" + callback.label() + "): " + errorMessage, code);
    }

    public void resolve(long id, JsonNode value, boolean handleListenerThread) {
        if (handleListenerThread) {//处理JvExecuteEventThread线程的回调
            Callback eventCallback = this.eventCallbacks.remove(id);
            if (eventCallback != null) {
                eventCallback.resolve(value);
                this.callbacks.remove(id);
            }
        } else {
            Callback callback = this.callbacks.remove(id);
            if (callback != null) {
                callback.resolve(value);
            }
        }


    }

    //这里会释放线程等待，避免死锁
    public void clear() {
        this.callbacks.forEach((key, callback) -> this._reject(callback, "Target closed", 0));
    }

    public List<ProtocolException> getPendingProtocolErrors() {
        List<ProtocolException> results = new ArrayList<>();
        this.callbacks.forEach((key, callback) -> {
            String errorMsg = callback.errorMsg();
            if (errorMsg != null) {
                results.add(new ProtocolException(errorMsg + " timed out. Trace: "));
            }
        });
        return results;
    }

}
