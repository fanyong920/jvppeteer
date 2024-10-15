package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class PageTarget extends Target {
    private final Viewport defaultViewport;
    protected AwaitableResult<Page> pageResult;

    public PageTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory, Viewport defaultViewport) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
        this.defaultViewport = defaultViewport;
    }

    public void initialize() {
        this.initializedCallback(InitializationStatus.SUCCESS);
        this.checkIfInitialized();
    }

    public Page page() {
        if (this.pageResult == null) {
            pageResult = AwaitableResult.create();
            CDPSession session = this.session();
            if (session == null) {
                session = this.sessionFactory().create(false);
            }
            pageResult.onSuccess(Page.create(session, this, this.defaultViewport));
        }
        return this.pageResult.get();
    }

    public void checkIfInitialized() {
        if (this.initializedResult.isDone()) {
            return;
        }
        if (!"".equals(this.getTargetInfo().getUrl())) {
            this.setInitializedResult(InitializationStatus.SUCCESS);
        }
    }

    @Override
    public void setInitializedResult(InitializationStatus status) {
        super.setInitializedResult(status);

    }

    private void initializedCallback(InitializationStatus result) {
        if (InitializationStatus.ABORTED.equals(result)) {
            this.setInitializedResult(InitializationStatus.ABORTED);
            return;
        }
        Target opener = this.opener();
        if (opener == null) {
            super.initialize();
            return;
        }
        if (!(opener instanceof PageTarget)) {
            this.setInitializedResult(InitializationStatus.ABORTED);
            return;
        }
        if (((PageTarget) opener).pageResult == null || !TargetType.PAGE.equals(this.type())) {
            super.initialize();
            return;
        }
        Page openerPage = ((PageTarget) opener).pageResult.waitingGetResult();
        if (openerPage.listenerCount(Page.PageEvent.Popup) == 0) {
            super.initialize();
            return;
        }
        Page pupopPage = this.page();
        openerPage.emit(Page.PageEvent.Popup, pupopPage);
        super.initialize();
    }
}
