package com.ruiyun.jvppeteer.api.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件发布，事件监听，模仿nodejs的EventEmitter
 */
public class EventEmitter<EventType> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(EventEmitter.class);
    /**
     * 储存所有的监听器
     */
    private final Map<EventType, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * 监听事件，可用于自定义事件监听,用户监听的事件都是在别的线程中异步执行的
     *
     * @param eventType 事件类型
     * @param listener  事件的处理器
     * @return EventEmitter 本身
     */
    public EventEmitter<EventType> on(EventType eventType, Consumer<?> listener) {
        List<Consumer<?>> list = listeners.computeIfAbsent(eventType, k -> Collections.synchronizedList(new ArrayList<>()));
        list.add(listener);
        return this;
    }

    /**
     * 取消监听
     *
     * @param eventType 事件类型
     * @param listener  事件的处理器
     */
    public void off(EventType eventType, Consumer<?> listener) {
        if (Objects.isNull(eventType)) {
            Iterator<Map.Entry<EventType, List<Consumer<?>>>> iterator = listeners.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EventType, List<Consumer<?>>> entry = iterator.next();
                entry.getValue().remove(listener);
            }
        } else {
            List<Consumer<?>> list = listeners.get(eventType);
            if (Objects.isNull(list)) {
                return;
            }
            if (Objects.isNull(listener)) {
                listeners.remove(eventType);
            } else {
                list.removeAll(Collections.singleton(listener));
                if (list.isEmpty()) {
                    listeners.remove(eventType);
                }
            }
        }

    }

    /**
     * 一次性事件监听，用于自定义事件监听
     *
     * @param eventType 事件名称
     * @param listener  事件处理器
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

    @SuppressWarnings("unchecked")
    public <T> void emit(EventType eventType, T param) {
        List<Consumer<?>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }
        for (Consumer<?> listener : new ArrayList<>(list)) {
            if (listener == null) {
                continue;
            }
            try {
                ((Consumer<T>) listener).accept(param);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
    }

    /**
     * 返回某个类型的监听器数量
     *
     * @param eventType 事件类型
     * @return int
     */
    public int listenerCount(EventType eventType) {
        return this.listeners.get(eventType) == null ? 0 : this.listeners.get(eventType).size();
    }

    /**
     * 移除所有监听器
     *
     * @param eventType 事件类型
     */
    public void removeAllListeners(EventType eventType) {
        if (Objects.isNull(eventType)) {
            this.listeners.clear();
            return;
        }
        this.listeners.remove(eventType);
    }

    /**
     * 移除监听器
     *
     * @param eventType 事件类型
     * @param listener  监听器
     */
    public void removeListener(EventType eventType, Consumer<?> listener) {
        List<Consumer<?>> consumers = this.listeners.get(eventType);
        if (consumers == null) {
            return;
        }
        consumers.remove(listener);
    }

    /**
     * 释放所有监听器
     */
    public void disposeSymbol() {
        this.listeners.clear();
    }

}
