package com.ruiyun.jvppeteer.transport.factory;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

@FunctionalInterface
public interface SessionFactory {

    CDPSession create();
}
