package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;

public class OtherTarget extends Target {
    public OtherTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
    }
}
