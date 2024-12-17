package com.ruiyun.jvppeteer.bidi.core;


import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;

public class BidiPageTarget implements Target {
    private final BidiPage page;

    public BidiPageTarget(BidiPage page) {
        super();
        this.page = page;
    }

    @Override
    public Page page() {
        return this.page;
    }

    @Override
    public Page asPage() {
        return BidiPage.from(this.browserContext(), this.page.mainFrame().browsingContext);
    }

    @Override
    public String url() {
        return this.page.url();
    }

    @Override
    public CDPSession createCDPSession() {
        return this.page.createCDPSession();
    }

    @Override
    public TargetType type() {
        return TargetType.PAGE;
    }

    @Override
    public BidiBrowser browser() {
        return this.browserContext().browser();
    }

    @Override
    public BidiBrowserContext browserContext() {
        return this.page.browserContext();
    }

    @Override
    public Target opener() {
        throw new UnsupportedOperationException();
    }

}
