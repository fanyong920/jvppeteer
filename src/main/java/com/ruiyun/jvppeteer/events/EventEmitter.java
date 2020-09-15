package com.ruiyun.jvppeteer.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    /**
     * 也是监听事件，不过这个方法只要在本项目内部使用，如果你想要自己监听事件，请使用{@link EventEmitter#on(String, EventHandler)}
     * @param method 事件名称
     * @param blistener 监听器
     * @param isOnce 是否只监听一次
     * @return Event
     */
    @Override
    public Event addListener(String method, BrowserListener<?> blistener, boolean isOnce) {
        DefaultBrowserListener listener = (DefaultBrowserListener)blistener;
        if(!method.equals(listener.getMothod())){
            LOGGER.error("addListener fail:{} is not equals listener.getMothod()[{}]",method,listener.getMothod());
            return this;
        }
        listener.setIsOnce(isOnce);
        Set<DefaultBrowserListener> browserListeners = this.listenerMap.get(method);
        if (browserListeners == null) {
            Set<DefaultBrowserListener> listeners = Helper.getConcurrentSet();
            this.listenerMap.putIfAbsent(method,listeners);
            listeners.add(listener);
        }else{
            browserListeners.add(listener);
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
        Set<DefaultBrowserListener> listeners = this.listenerMap.get(method);
        if (ValidateUtil.isNotEmpty(listeners)) {
            listeners.remove(listener);
            listenerCount.decrementAndGet();
        }
        return this;
    }

    @Override
    public void emit(String method, Object params) {
        ValidateUtil.notNull(method, "method must not be null");
        Set<DefaultBrowserListener> listeners = this.listenerMap.get(method);
        if(ValidateUtil.isEmpty(listeners))
            return;
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
                        event = readJsonObject(resolveType, (JsonNode) params);
                    }else{
                        event = params;
                    }
                }else{
                    event = null;
                }
                invokeListener(listener, event);
            } catch (IOException e) {
                LOGGER.error("publish event error:", e);
                return;
            }

        }
    }

    /**
     * 执行监听器，如果是用户的监听，则用用户的处理器去处理，不然执行onBrowserEvent方法
     * @param listener 监听器
     * @param event 事件
     */
    private void invokeListener(DefaultBrowserListener listener, Object event){
        try {
            if(listener.getIsSync()){
                Helper.commonExecutor().submit(() -> {listener.onBrowserEvent(event);});
            }else {
                listener.onBrowserEvent(event);
            }
        } finally {
            if(listener.getIsOnce()){
                listener.setIsAvaliable(false);
            }
        }
    }

    /**
     * 如果clazz属于JsonNode.class则不用转换类型，如果不是，则将jsonNode转化成clazz类型对象
     * @param clazz 目标类型
     * @param params event的具体内容
     * @param <T>  具体类型
     * @return T
     * @throws IOException 转化失败抛出的异常
     */
    private <T> T readJsonObject(Class<T> clazz, JsonNode params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException(
                    "Failed converting null response to clazz " + clazz.getName());
        }
        if(JsonNode.class.isAssignableFrom(clazz)){
            return (T)params;
        }
        return Constant.OBJECTMAPPER.treeToValue(params,clazz);
    }

    public int getListenerCount(String method){
        Set<DefaultBrowserListener> listeners = this.listenerMap.get(method);
        int i = 0;
        if(ValidateUtil.isEmpty(listeners)){
            return 0;
        }
        for (DefaultBrowserListener listener : listeners) {
            if(!listener.getIsAvaliable()){
                continue;
            }
            i++;
        }
        return i;
    }

    /**
     * 监听事件，可用于自定义事件监听,用户监听的事件都是在别的线程中异步执行的
     * @param method 事件名称
     * @param handler 事件的处理器
     * @return Event
     */
    public Event on(String method, EventHandler<?> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setIsSync(true);
        listener.setMothod(method);
        listener.setHandler(handler);
        return this.addListener(method, listener);
    }

    /**
     * 一次性事件监听，用于自定义事件监听，与{@link EventEmitter#on(String, EventHandler)}的区别就是on会一直监听
     * @param method 事件名称
     * @param handler 事件处理器
     * @return Event
     */
    public Event once(String method, EventHandler<?> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setIsSync(true);
        listener.setMothod(method);
        listener.setHandler(handler);
        return this.addListener(method, listener, true);
    }

    /**
     * 取消事件
     * @param method 事件名称
     * @param listener 事件的监听器
     * @return Event
     */
    public Event off(String method, BrowserListener<?> listener) {
        return this.removeListener(method, listener);
    }

}
