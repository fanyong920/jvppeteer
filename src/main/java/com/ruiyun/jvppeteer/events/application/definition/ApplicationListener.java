package com.ruiyun.jvppeteer.events.application.definition;

import java.util.Collection;
import java.util.EventListener;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    void on(String name, Consumer<E> function);

    void once(String name, Consumer<E> function);

    void addListener(String name,Consumer<E> function,boolean isOnce);

    void off(String name);

    void removeListener(String name);

    void removeListener(Consumer<E> function);

    void removeListeners(Collection<Consumer<E>> listeners);

    boolean emit(String name,ApplicationEvent event);
}
