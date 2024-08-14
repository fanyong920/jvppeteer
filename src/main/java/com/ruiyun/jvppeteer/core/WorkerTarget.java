package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.core.page.Worker;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;

public class WorkerTarget extends Target {
    public WorkerTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
    }
    public Worker worker() {
        if (!"service_worker".equals(this.targetInfo.getType()) && !"shared_worker".equals(this.targetInfo.getType()))
            return null;
        if (this.worker == null) {
            CDPSession session = this.session();
            if (this.session() == null){
                session = this.sessionFactory.create(false);
            }
            this.worker = new Worker(session, this.targetInfo.getUrl(), this.getTargetId(),this.type(),(arg1, arg2,arg3) -> {} /* consoleAPICalled */, (arg) -> {} /* exceptionThrown */);
        }
        return this.worker;
    }
}
