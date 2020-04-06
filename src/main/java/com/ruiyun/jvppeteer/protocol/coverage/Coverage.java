package com.ruiyun.jvppeteer.protocol.coverage;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

public class Coverage {

    private CSSCoverage cssCoverage;

    private JSCoverage jsCoverage;

    public Coverage(CDPSession client) {
        this.cssCoverage = new CSSCoverage(client);
        this.jsCoverage = new JSCoverage(client);
    }
}
