package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;

import java.util.List;

/**
 * 打印页面console事件接口
 */
@FunctionalInterface
public interface ConsoleAPI{
    void call(String type, List<JSHandle> handles, StackTrace stackTrace);
}
