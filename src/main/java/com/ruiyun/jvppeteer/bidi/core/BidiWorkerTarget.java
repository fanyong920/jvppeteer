package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;

public class BidiWorkerTarget implements Target {
    private final BidiWebWorker worker;

    public BidiWorkerTarget(BidiWebWorker worker) {
        super();
        this.worker = worker;
    }

    @Override
    public Page page() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page asPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String url() {
        return this.worker.url();
    }

    @Override
    public CDPSession createCDPSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TargetType type() {
        return TargetType.OTHER;
    }

    @Override
    public BidiBrowser browser() {
        return this.browserContext().browser();
    }

    @Override
    public BidiBrowserContext browserContext() {
        return this.worker.frame().page().browserContext();
    }

    @Override
    public Target opener() {
        throw new UnsupportedOperationException();
    }

}
