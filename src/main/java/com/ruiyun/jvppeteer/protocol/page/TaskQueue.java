package com.ruiyun.jvppeteer.protocol.page;

import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.util.function.BiFunction;

/**
 * 截图专用
 * @param <R>
 */
public class TaskQueue<R> {

    public TaskQueue() {
    }

    private BiFunction<String,ScreenshotOptions,R> function;

    public TaskQueue(BiFunction<String,ScreenshotOptions,R>  function) {
        this.function = function;
    }

    public void postTask(String screenshotType, ScreenshotOptions options){
        function.apply(screenshotType,options);
    }
}
