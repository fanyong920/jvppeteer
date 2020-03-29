package com.ruiyun.jvppeteer.page;

import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.util.function.BiFunction;

/**
 * 截图专用
 * @param <R>
 */
public class TaskQueue<R> {

    private BiFunction<String,ScreenshotOptions,R> function;

    public TaskQueue(BiFunction<String,ScreenshotOptions,R>  function) {
        this.function = function;
    }

    public void postTask(String screenshotType, ScreenshotOptions options){
        function.apply(screenshotType,options);
    }
}
