package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.cdp.entities.StackTrace;
import java.util.List;

/**
 * 打印页面console事件接口
 */
@FunctionalInterface
public interface ConsoleAPI{
    void call(ConsoleMessageType type, List<JSHandle> handles, StackTrace stackTrace);
}
