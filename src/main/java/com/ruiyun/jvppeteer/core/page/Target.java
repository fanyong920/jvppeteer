package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.TargetManager;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.options.TargetType;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import io.reactivex.rxjava3.subjects.SingleSubject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Target {
    private BrowserContext browserContext;
    private  CDPSession session;
    protected TargetInfo targetInfo;
    private TargetManager targetManager;
    protected SessionFactory sessionFactory;
    private final Set<Target> childTargets = new HashSet<>();
    public SingleSubject<Boolean> isClosedSubject = SingleSubject.create();
    public SingleSubject<InitializationStatus> initializedSubject = SingleSubject.create();
    private String targetId;
    protected Worker worker;
    public Target() {
        super();
    }
    public Target(TargetInfo targetInfo, CDPSession session, BrowserContext browserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        this.session = session;
        this.targetManager = targetManager;
        this.targetInfo = targetInfo;
        this.browserContext = browserContext;
        this.targetId = targetInfo.getTargetId();
        this.sessionFactory = sessionFactory;
        if (this.session != null) {
            this.session.setTarget(this);
        }
    }
    public Page asPage() {
        CDPSession session = this.session();
        if(session == null){
            session = this.createCDPSession();
            return Page.create(session, this, null);
        }
        return Page.create(session, this, null);
    }
    public String subtype() {
        return this.targetInfo.getSubtype();
    }
    public CDPSession session() {
        return this.session;
    }
    public void addChildTarget(Target target) {
        this.childTargets.add(target);
    }
    public void removeChildTarget(Target target) {
        this.childTargets.remove(target);
    }
    public Set<Target> childTargets(){
        return this.childTargets;
    }
    public SessionFactory sessionFactory() {
        if(this.sessionFactory == null){
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        return this.sessionFactory;
    }
    public CDPSession createCDPSession() {
        if(this.sessionFactory == null){
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        CDPSession cdpSession = this.sessionFactory.create(false);
        cdpSession.setTarget(this);
        return cdpSession;
    }
    public String url() {
        return this.targetInfo.getUrl();
    }
    public TargetType type() {
        String type = this.targetInfo.getType();
        switch (type) {
            case "page":
                return TargetType.PAGE;
            case "background_page":
                return TargetType.BACKGROUND_PAGE;
            case "service_worker":
                return TargetType.SERVICE_WORKER;
            case "shared_worker":
                return TargetType.SHARED_WORKER;
            case "browser":
                return TargetType.BROWSER;
            case "webview":
                return TargetType.WEBVIEW;
            case "tab":
                return TargetType.TAB;
            default:
                return TargetType.OTHER;
        }
    }
    public TargetManager targetManager() {
        if (this.targetManager == null) {
            throw new JvppeteerException("targetManager is not initialized");
        }
        return this.targetManager;
    }
    public TargetInfo getTargetInfo() {
        return this.targetInfo;
    }
    public Browser browser() {
        if (this.browserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.browserContext.browser();
    }
    public BrowserContext browserContext() {
        if (this.browserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.browserContext;
    }
    public Target opener() {
        String openerId = this.targetInfo.getOpenerId();
        if (StringUtil.isEmpty(openerId)) {
            return null;
        }
        for (Target target : this.browser().targets()) {
            if(target.getTargetId().equals(openerId)){
                return target;
            }
        }
        return null;
    }
    public void targetInfoChanged(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
        this.checkIfInitialized();
    }
    public void initialize() {
        this.initializedSubject.onSuccess(InitializationStatus.SUCCESS);
    }
    public boolean isTargetExposed() {
        return this.type() != TargetType.TAB && (this.subtype() == null);
    }
    private void checkIfInitialized() {
        if (!this.initializedSubject.hasValue()) {
            this.initializedSubject.onSuccess(InitializationStatus.SUCCESS);
        }
    }

    public Page page(){
        return null;
    }
    public String getTargetId() {
        return this.targetId;
    }
    //todo 删除？
    public void waitForTargetClose() {
        // 使用blockingFirst来阻塞直到接收到关闭信号或超时
        this.isClosedSubject.timeout(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS).blockingGet();
    }

    public enum InitializationStatus{
        SUCCESS("success"),
        ABORTED("aborted");
        private final String status;
        InitializationStatus(String status) {
            this.status = status;
        }
        public String getStatus() {
            return this.status;
        }
    }
}
