package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class FrameManager {

    private CDPSession client;

    private Page page;

    private boolean ignoreHTTPSErrors;

    private TimeoutSettings timeoutSettings;

    public FrameManager(CDPSession client, Page page, boolean ignoreHTTPSErrors, TimeoutSettings timeoutSettings) {
        this.client = client;
        this.page = page;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.timeoutSettings = timeoutSettings;
    }

    public void initialize(){

    }
}
