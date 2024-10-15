package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.core.JSHandle;
import com.ruiyun.jvppeteer.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.entities.StackTrace;

import java.util.List;

/**
 * 打印页面console事件接口
 */
@FunctionalInterface
public interface ConsoleAPI{
    void call(ConsoleMessageType type, List<JSHandle> handles, StackTrace stackTrace);
}
