package com.ruiyun.jvppeteer.events.definition;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * publish event
 *
 * @author fff
 */
@FunctionalInterface
public interface BrowserEventPublisher {

    ThreadPoolExecutor executor = getThreadPoolExecutor();

    static ThreadPoolExecutor getThreadPoolExecutor() {
        Runtime runtime = Runtime.getRuntime();

        int processorNum = runtime.availableProcessors();

        if(processorNum < 3){
            processorNum = 3;
        }
        return  new ThreadPoolExecutor(processorNum,processorNum,30, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
    }


    void publishEvent(String method,Object event);
}
