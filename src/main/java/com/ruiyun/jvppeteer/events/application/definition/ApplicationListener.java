package com.ruiyun.jvppeteer.events.application.definition;

import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public interface ApplicationListener extends EventListener {

    Map<String, Set<Consumer<?>>> LISTENERS_MAP = new ConcurrentHashMap<>();

    Map<String, Set<Consumer<?>>> ONCE_LISTNERS_MAP = new ConcurrentHashMap<>();

    void on(String name, Consumer<?> function);

    void once(String name, Consumer<?> function);

    void addListener(String name,Consumer<?> function,boolean isOnce);

    void off(String name);

    void removeListener(String name);

    void removeListener(Consumer<?> function);

    void removeListeners(Collection<Consumer<?>> listeners);

    boolean emit(String name,Object event);
}
