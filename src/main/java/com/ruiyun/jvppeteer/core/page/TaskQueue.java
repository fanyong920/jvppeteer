package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.options.ScreenshotOptions;

import java.util.function.BiFunction;

/**
 * 截图专用
 * @param <R>
 */
public class TaskQueue<R> {

    public TaskQueue() {
    }

    public Object postTask(BiFunction<String,ScreenshotOptions,R> function,String screenshotType,ScreenshotOptions options){
        return function.apply(screenshotType,options);
    }
}
