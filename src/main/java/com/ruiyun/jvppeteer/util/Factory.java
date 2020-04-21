package com.ruiyun.jvppeteer.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserPublisher;

import java.util.concurrent.ExecutionException;

/**
 * 简单工厂：创建消息发布者
 */
public class Factory {

    private static final LoadingCache<String, Object> cache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<String, Object>(){
        @Override
        public Object load(String key) {
            if(key.contains(DefaultBrowserPublisher.class.getSimpleName())){
                DefaultBrowserPublisher publisher = new DefaultBrowserPublisher();
                return publisher;
            }
           return null;
        }
    });

    public static final void put(String key,Object object){
        cache.put(key,object);
    }

    public static final <T> T  get(String key,Class<T> clazz) throws ExecutionException {
        return (T)cache.get(key);
    }

}
