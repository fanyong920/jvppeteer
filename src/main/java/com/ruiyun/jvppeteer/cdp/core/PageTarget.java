package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.transport.SessionFactory;

public class PageTarget extends CdpTarget {
    private final Viewport defaultViewport;
    protected AwaitableResult<CdpPage> pageResult;

    public PageTarget(TargetInfo targetInfo, CDPSession session, CdpBrowserContext cdpBrowserContext, TargetManager targetManager, SessionFactory sessionFactory, Viewport defaultViewport) {
        super(targetInfo, session, cdpBrowserContext, targetManager, sessionFactory);
        this.defaultViewport = defaultViewport;
    }

    public void initialize() {
        this.initializedCallback(InitializationStatus.SUCCESS);
        this.checkIfInitialized();
    }

    public CdpPage page() {
        if (this.pageResult == null) {
            pageResult = AwaitableResult.create();
            CDPSession session = this.session();
            if (session == null) {
                session = this.sessionFactory().create(false);
            }
            pageResult.onSuccess(CdpPage.create(session, this, this.defaultViewport));
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
        CdpPage openerPage = ((PageTarget) opener).pageResult.waitingGetResult();
        if (openerPage.listenerCount(PageEvents.Popup) == 0) {
            super.initialize();
            return;
        }
        CdpPage pupopPage = this.page();
        openerPage.emit(PageEvents.Popup, pupopPage);
        super.initialize();
    }
}
