package com.ruiyun.jvppeteer.types.page.frame;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashSet;
import java.util.Set;

public class Keyboard {

    private CDPSession client;

    private int modifiers;

    private Set<String> pressedKeys;

    public Keyboard(CDPSession client) {
        this.client = client;
        this.modifiers = 0;
        this.pressedKeys = new HashSet<>();
    }
}
