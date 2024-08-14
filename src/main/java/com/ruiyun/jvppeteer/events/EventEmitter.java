package com.ruiyun.jvppeteer.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 事件发布，事件监听，模仿nodejs的EventEmitter
 */
public class EventEmitter<EventType>   {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventEmitter.class);
    /**
     * 事件发布，事件监听，模仿nodejs的EventEmitter
     */
    private final Map<EventType, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();
    /**
     * 监听事件，可用于自定义事件监听,用户监听的事件都是在别的线程中异步执行的
     * @param eventType 事件类型
     * @param listener 事件的处理器
     * @return EventEmitter 本身
     */
    public com.ruiyun.jvppeteer.events.EventEmitter<EventType> on(EventType eventType, Consumer<?> listener) {
        List<Consumer<?>> list = listeners.computeIfAbsent(eventType, k -> new ArrayList<>());
        list.add(listener);
        return this;
    }
    /**
     * 取消监听
     * @param eventType 事件类型
     * @param listener 事件的处理器
     */
    public void off(EventType eventType, Consumer<?> listener) {
        List<Consumer<?>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }
        list.removeAll(Collections.singleton(listener));
        if (list.isEmpty()) {
            listeners.remove(eventType);
        }
    }
    /**
     * 一次性事件监听，用于自定义事件监听，与{@link com.ruiyun.jvppeteer.events.EventEmitter#on(EventType, Consumer)}的区别就是on会一直监听
     * @param eventType 事件名称
     * @param listener 事件处理器
     */
    public void once(EventType eventType, Consumer<?> listener) {
        AtomicReference<Consumer<?>> consumerRef = new AtomicReference<>();
        Consumer<Object> offConsumer = (s) -> {
            this.off(eventType, consumerRef.get());//取消的就是合并后的Consumer
        };
        Consumer<?> consumer = listener.andThen(offConsumer);
        consumerRef.set(consumer);//set合并后的Consumer
        this.on(eventType, consumer);//监听合并后的Consumer
    }
    /**
     * 执行监听器
     * @param eventType 监听类型
     * @param param 参数
     */
    @SuppressWarnings("unchecked")
    public <T> void emit(EventType eventType, T param) {
        List<Consumer<?>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }
        for (Consumer<?> listener: new ArrayList<>(list)) {
            ((Consumer<T>) listener).accept(param);
        }
    }
    /**
     * 返回某个类型的监听器数量
     * @param eventType 事件类型
     * @return int
     */
    public int listenerCount(EventType eventType){
        return this.listeners.get(eventType) == null ? 0 : this.listeners.get(eventType).size();
    }
    /**
     * 移除所有监听器
     * @param eventType 事件类型
     */
    public void removeAllListener(EventType eventType) {
        if (eventType == null){
            this.listeners.clear();
            return;
        }
        this.listeners.remove(eventType);
    }

    /**
     * 移除所有监听器
     * @param eventType 事件类型
     */
    public void removeListener(EventType eventType,Consumer<?> listener) {
        List<Consumer<?>> consumers = this.listeners.get(eventType);
        if (consumers == null){
            return;
        }
        consumers.remove(listener);
    }

    /**
     * 释放所有监听器
     */
    public void dispose() {
//        this.listeners.forEach((eventType, consumers) -> consumers.forEach(consumer -> {
//            this.off(eventType, consumer);
//        }));
        this.listeners.clear();
    }

}
