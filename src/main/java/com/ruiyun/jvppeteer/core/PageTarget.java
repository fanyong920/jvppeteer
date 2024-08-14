package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.options.TargetType;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageTarget extends Target {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageTarget.class);
    private final Viewport defaultViewport;
    protected SingleSubject<Page> pageSubject;
    public PageTarget(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory, Viewport defaultViewport) {
        super(targetInfo, session, browserContext, targetManager, sessionFactory);
        this.defaultViewport = defaultViewport;
    }
    public void initialize() {
        this.initializedSubject.doAfterSuccess(result -> {
            try {
                if ("aborted".equals(result.getStatus())) {
                    return;
                }
                Target opener = this.opener();
                if (opener == null) {
                    return ;
                }
                if (!(opener instanceof PageTarget)) {
                    return;
                }
                if (((PageTarget) opener).pageSubject == null || !TargetType.PAGE.equals(this.type())) {
                    return ;
                }
                Page openerPage = ((PageTarget) opener).pageSubject.blockingGet();
                if (openerPage.listenerCount(Page.PageEvent.POPUP) == 0) {
                    return ;
                }
                Page pupopPage = this.page();
                pupopPage.emit(Page.PageEvent.POPUP, pupopPage);
            } catch (Exception e){
                LOGGER.error("jvppeteer error:",e);
            }
        }).subscribe();
        this.checkIfInitialized();
    }
    public Page page() {
        if(this.pageSubject == null){
            pageSubject = SingleSubject.create();
            CDPSession session = this.session();
            if(session == null){
                session = this.sessionFactory().create(false);
            }
            pageSubject.onSuccess(Page.create( session, this,this.defaultViewport));
        }
        return this.pageSubject.getValue();
    }
    public void checkIfInitialized() {
        if (this.initializedSubject.hasValue()) {
            return;
        }
        if (!"".equals(this.getTargetInfo().getUrl())) {
            this.initializedSubject.onSuccess(InitializationStatus.SUCCESS);
        }
    }
}
