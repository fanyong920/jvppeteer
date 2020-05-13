package com.ruiyun.jvppeteer.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件发布，事件监听，模仿nodejs的EventEmitter
 */
public class EventEmitter implements Event {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventEmitter.class);


    private Map<String,Set<DefaultBrowserListener>> listenerMap = new ConcurrentHashMap<>();


    private AtomicInteger listenerCount = new AtomicInteger(0);

    @Override
    public Event addListener(String method, BrowserListener<?> listener, boolean isOnce) {
        DefaultBrowserListener defaultBrowserListener = (DefaultBrowserListener)listener;
        if(!method.equals(defaultBrowserListener.getMothod())){
            LOGGER.error("addListener fail:{} is not equals listener.getMothod()[{}]",method,defaultBrowserListener.getMothod());
            System.out.println(MessageFormat.format("addListener fail:{} is not equals listener.getMothod()[{}]",method,defaultBrowserListener.getMothod()));
            return this;
        }
        defaultBrowserListener.setIsOnce(isOnce);
        Set<DefaultBrowserListener> browserListeners = this.listenerMap.get(method);
        if (browserListeners == null) {
            Set<DefaultBrowserListener> listeners = Helper.getConcurrentSet();
            this.listenerMap.putIfAbsent(method,listeners);
            listeners.add(defaultBrowserListener);
        }else{
            browserListeners.add(defaultBrowserListener);
        }
        listenerCount.incrementAndGet();
        return this;
    }

    /**
     * 移除监听器
     * @param method 监听器对应的方法
     * @param listener 要移除的监听器
     * @return Event
     */
    @Override
    public Event removeListener(String method, BrowserListener<?> listener) {
        Set<DefaultBrowserListener> defaultBrowserListeners = this.listenerMap.get(method);
        if (ValidateUtil.isNotEmpty(defaultBrowserListeners)) {
            defaultBrowserListeners.remove(listener);
            listenerCount.decrementAndGet();
        }
        return this;
    }

    @Override
    public boolean emit(String method, Object params) {
        ValidateUtil.notNull(method, "method must not be null");
        Set<DefaultBrowserListener> listeners = this.listenerMap.get(method);
        if(ValidateUtil.isEmpty(listeners))
            return false;
        for (DefaultBrowserListener listener : listeners) {
            if(!listener.getIsAvaliable()){
                listeners.remove(listener);
                listenerCount.decrementAndGet();
                continue;
            }
            if(listener.getIsOnce()){
                listeners.remove(listener);
                listenerCount.decrementAndGet();
            }
            try {
                Object event ;
                if(params != null){
                    Class<?> resolveType = null;
                    Type genericSuperclass = listener.getClass().getGenericSuperclass();
                    if(genericSuperclass instanceof ParameterizedType){
                        ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if(actualTypeArguments.length == 1){
                            resolveType = (Class)actualTypeArguments[0];
                        }
                    }else{
                        resolveType = listener.getResolveType();
                    }

                    if(JsonNode.class.isAssignableFrom(params.getClass())){
                        event = readJsonObject(resolveType, (JsonNode)params);
                    }else{
                        event = params;
                    }
                }else{
                    event = null;
                }
                invokeListener(listener, event);
//                Constant.executor.execute(() -> );
            } catch (IOException e) {
                LOGGER.error("publish event error:", e);
                return false;
            }

        }
        return true;
    }

    @Override
    public Event addListener(String method, BrowserListener<?> listener) {
        return addListener(method,listener,false);
    }

    /**
     * 执行监听器，如果是用户的监听，则用用户的处理器去处理，不然执行onBrowserEvent方法
     * @param listener 监听器
     * @param event 事件
     */
    private void invokeListener(DefaultBrowserListener listener, Object event){
        try {
            listener.onBrowserEvent(event);
        } finally {
            if(listener.getIsOnce()){
                listener.setIsAvaliable(false);
            }
        }
    }
    /**
     * 如果clazz属于JsonNode.class则不用转换类型，如果不是，则将jsonNode转化成clazz类型对象
     * @param clazz 目标类型
     * @param jsonNode event
     * @param <T>  具体类型
     * @return T
     * @throws IOException 转化失败抛出的异常
     */
    private <T> T readJsonObject(Class<T> clazz, JsonNode jsonNode) throws IOException {
        if (jsonNode == null) {
            throw new IllegalArgumentException(
                    "Failed converting null response to clazz " + clazz.getName());
        }
        if(JsonNode.class.isAssignableFrom(clazz)){
            return (T)jsonNode;
        }
        return Constant.OBJECTMAPPER.treeToValue(jsonNode,clazz);
    }

    public int getListenerCount(String method){
        Set<DefaultBrowserListener> defaultBrowserListeners = this.listenerMap.get(method);
        int i = 0;
        if(ValidateUtil.isEmpty(defaultBrowserListeners)){
            return 0;
        }
        for (DefaultBrowserListener listener : defaultBrowserListeners) {
            if(!listener.getIsAvaliable()){
                continue;
            }
            i++;
        }
        return i;
    }


}
