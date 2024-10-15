package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class WorkerTarget extends Target {
    public WorkerTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
    }

    /**
     * 如果目标不是 "service_worker" 或 "shared_worker" 类型，则返回 null。
     */
    public WebWorker worker() {
        if (!"service_worker".equals(this.targetInfo.getType()) && !"shared_worker".equals(this.targetInfo.getType()))
            return null;
        if (this.webWorker == null) {
            CDPSession session = this.session();
            if (this.session() == null){
                session = this.sessionFactory.create(false);
            }
            this.webWorker = new WebWorker(session, this.targetInfo.getUrl(), this.getTargetId(),this.type(),(arg1, arg2, arg3) -> {} /* consoleAPICalled */, (arg) -> {} /* exceptionThrown */);
        }
        return this.webWorker;
    }
}
