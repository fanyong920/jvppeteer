package com.ruiyun.jvppeteer.transport.factory;

import com.ruiyun.jvppeteer.transport.CDPSession;

@FunctionalInterface
public interface SessionFactory {
    CDPSession create();
}
