package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class Mouse {

    private CDPSession client;

    private Keyboard keyboard;

    private int x;

    private int y;

    private String button;

    public Mouse(CDPSession client,Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
        this.x = 0;
        this.y = 0;
        /** @type {'none'|'left'|'right'|'middle'} */
        this.button = "none";
    }
}
