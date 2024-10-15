package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class DevToolsTarget extends PageTarget {

    public DevToolsTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory, Viewport defaultViewport) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory, defaultViewport);
    }
}
