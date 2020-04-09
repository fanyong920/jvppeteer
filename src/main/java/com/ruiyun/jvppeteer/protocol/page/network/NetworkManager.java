package com.ruiyun.jvppeteer.protocol.page.network;

import com.ruiyun.jvppeteer.protocol.page.frame.FrameManager;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class NetworkManager {

    /**
     * cdpsession
     */
    private CDPSession client;

    private boolean ignoreHTTPSErrors;

    private FrameManager frameManager;

    public NetworkManager(CDPSession client, boolean ignoreHTTPSErrors, FrameManager frameManager) {
        this.client = client;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.frameManager = frameManager;
    }
}
