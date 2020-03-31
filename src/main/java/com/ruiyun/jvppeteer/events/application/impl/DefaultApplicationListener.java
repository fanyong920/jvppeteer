package com.ruiyun.jvppeteer.events.application.impl;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.application.definition.ApplicationEvent;
import com.ruiyun.jvppeteer.events.application.definition.ApplicationListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class DefaultApplicationListener implements ApplicationListener,Constant {


    @Override
    public void on(String name, Consumer<?> function) {
        addListener(name,function,false);
    }

    @Override
    public void once(String name, Consumer<?> function) {
        addListener(name,function,true);
    }

    @Override
    public void addListener(String name, Consumer<?> function, boolean isOnce) {
        if(isOnce){
            ONCE_LISTNERS_MAP.computeIfAbsent(name,this::getSynchronizedSet).add(function);
        }else{
            LISTNERS_MAP.computeIfAbsent(name,this::getConcurrentSet).add(function);
        }
    }

    @Override
    public void off(String name) {
        removeListener(name);
    }

    @Override
    public void removeListener(String name) {
        Map<String, Set<Consumer<?>>> listnerMap = getListnerMap(name);
        listnerMap.remove(name);
    }

    @Override
    public void removeListeners(Collection listeners) {

    }

    @Override
    public void removeListener(Consumer function) {

    }


    @Override
    public boolean emit(String name, Object event) {
        Map<String, Set<Consumer<?>>> listnerMap = getListnerMap(name);
        if(listnerMap != null && listnerMap.size() > 0){
            Set<Consumer<?>> consumers = listnerMap.get(name);
            for (Consumer consumer : consumers) {
                executor.execute(() -> {
                    consumer.accept(event);
                });
            }
            return true;
        }else{
            return true;
        }
    }

    public int getListenerCount(String name) {
        Map<String, Set<Consumer<?>>> listnerMap = getListnerMap(name);
        if(listnerMap != null && listnerMap.size() > 0){
            return listnerMap.size();
        }
        return 0;
    }
    public Map<String, Set<Consumer<?>>>  getListnerMap(String name){
        if(LISTNERS_MAP.containsKey(name)){
            return LISTNERS_MAP;
        }else if(ONCE_LISTNERS_MAP.containsKey(name)){
            return ONCE_LISTNERS_MAP;
        }
        return null;
    }

    /**
     * create synchronized set
     * @param s
     * @return
     */
    private Set<Consumer<?>> getSynchronizedSet(String s) {
        return Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * create thread safe set
     * @param s
     * @return
     */
    private Set<Consumer<?>> getConcurrentSet(String s) {
        return new CopyOnWriteArraySet<>();
    }
}
