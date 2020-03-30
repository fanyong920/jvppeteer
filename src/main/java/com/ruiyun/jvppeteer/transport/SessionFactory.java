package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

@FunctionalInterface
public interface SessionFactory {

    CDPSession create();
}
