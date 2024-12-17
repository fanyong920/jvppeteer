package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class DevToolsTarget extends PageTarget {

    public DevToolsTarget(TargetInfo targetInfo, CDPSession session, CdpBrowserContext cdpBrowserContext, TargetManager targetManager, SessionFactory sessionFactory, Viewport defaultViewport) {
        super(targetInfo, session, cdpBrowserContext, targetManager, sessionFactory, defaultViewport);
    }
}
