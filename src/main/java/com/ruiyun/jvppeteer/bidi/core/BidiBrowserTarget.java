package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;

public class BidiBrowserTarget implements Target {
    private final BidiBrowser browser;

    public BidiBrowserTarget(BidiBrowser browser) {
        super();
        this.browser = browser;
    }

    @Override
    public Page asPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String url() {
        return "";
    }

    @Override
    public CDPSession createCDPSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TargetType type() {
        return TargetType.BROWSER;
    }

    @Override
    public BidiBrowser browser() {
        return this.browser;
    }

    @Override
    public BidiBrowserContext browserContext() {
        return this.browser.defaultBrowserContext();
    }

    @Override
    public Target opener() {
        throw new UnsupportedOperationException();
    }

}
