package com.ruiyun.jvppeteer.protocol.accessbility;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class Accessibility {

    private CDPSession client;

    public Accessibility(CDPSession client) {
        this.client = client;
    }
}
