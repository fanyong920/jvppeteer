package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class OtherTarget extends CdpTarget {
    public OtherTarget(TargetInfo targetInfo, CDPSession session, CdpBrowserContext cdpBrowserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        super(targetInfo, session, cdpBrowserContext, targetManager, sessionFactory);
    }
}
