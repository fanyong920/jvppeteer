package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class Tracing {

    private  CDPSession client;

    private  boolean recording;

    private  String path;

    public  Tracing(CDPSession client) {
        this.client = client;
        this.recording = false;
        this.path = "";
    }
}
