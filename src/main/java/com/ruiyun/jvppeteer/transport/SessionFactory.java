package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.api.core.CDPSession;

@FunctionalInterface
public interface SessionFactory {
    CDPSession create(boolean isAutoAttachEmulated);
}
