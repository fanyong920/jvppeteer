package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import java.util.Objects;

public class BidiFrameTarget implements Target {
    private final BidiFrame frame;
    private BidiPage page;

    public BidiFrameTarget(BidiFrame frame) {
        super();
        this.frame = frame;
    }

    @Override
    public Page page() {
        if (Objects.isNull(page)) {
            this.page = BidiPage.from(this.browserContext(),
                    this.frame.browsingContext
            );
        }
        return this.page;
    }

    @Override
    public BidiPage asPage() {
        return BidiPage.from(this.browserContext(), this.frame.browsingContext);
    }

    @Override
    public String url() {
        return this.frame.url();
    }

    @Override
    public CDPSession createCDPSession() {
        return this.frame.createCDPSession();
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
        return this.frame.page().browserContext();
    }

    @Override
    public Target opener() {
        throw new UnsupportedOperationException();
    }


}
