package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class OtherTarget extends Target {
    public OtherTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
    }
}
