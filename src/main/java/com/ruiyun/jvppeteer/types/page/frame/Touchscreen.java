package com.ruiyun.jvppeteer.types.page.frame;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class Touchscreen {

    private CDPSession client;

    private Keyboard keyboard;

    public Touchscreen(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
    }
}
