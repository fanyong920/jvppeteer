package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.transport.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Target 表示
 * <a href="https://chromedevtools.github.io/devtools-protocol/tot/Target/">CDP 目标</a>。<p>
 * 在 CDP 中，目标是可以调试的东西，例如Frame、Page或worker。
 */
public class CdpTarget implements Target {
    private CdpBrowserContext cdpBrowserContext;
    private CDPSession session;
    protected TargetInfo targetInfo;
    private TargetManager targetManager;
    protected SessionFactory sessionFactory;
    private final List<CdpTarget> childTargets = new ArrayList<>();
    public final AwaitableResult<Boolean> isClosedResult = AwaitableResult.create();
    final AwaitableResult<InitializationStatus> initializedResult = AwaitableResult.create();
    private String targetId;
    protected CdpWebWorker webWorker;
    private Runnable onCloseRunner = () -> {
    };

    public CdpTarget() {
        super();
    }

    public CdpTarget(TargetInfo targetInfo, CDPSession session, CdpBrowserContext cdpBrowserContext, TargetManager targetManager, SessionFactory sessionFactory) {
        this.session = session;
        this.targetManager = targetManager;
        this.targetInfo = targetInfo;
        this.cdpBrowserContext = cdpBrowserContext;
        this.targetId = targetInfo.getTargetId();
        this.sessionFactory = sessionFactory;
        if (this.session != null) {
            ((CdpCDPSession) this.session).setTarget(this);
        }
    }

    public void setInitializedResult(InitializationStatus status) {
        this.initializedResult.onSuccess(status);
    }

    /**
     * 强制为任何类型的目标创建页面。如果你想将 other 类型的 CDP 目标作为页面处理，那么它会很有用。<p>
     * 如果你处理常规页面目标,请使用 {@link Target#page()}。
     *
     * @return Page
     */
    public Page asPage() {
        CDPSession session = this.session();
        if (session == null) {
            session = this.createCDPSession();
            return CdpPage.create(session, this, null);
        }
        return CdpPage.create(session, this, null);
    }

    public String subtype() {
        return this.targetInfo.getSubtype();
    }

    public CDPSession session() {
        return this.session;
    }

    public void addChildTarget(CdpTarget target) {
        this.childTargets.add(target);
    }

    public void removeChildTarget(CdpTarget target) {
        this.childTargets.remove(target);
    }

    public List<CdpTarget> childTargets() {
        return this.childTargets;
    }

    public SessionFactory sessionFactory() {
        if (this.sessionFactory == null) {
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        return this.sessionFactory;
    }

    public CDPSession createCDPSession() {
        if (this.sessionFactory == null) {
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        CdpCDPSession cdpSession = (CdpCDPSession) this.sessionFactory.create(false);
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

    public CdpBrowser browser() {
        if (this.cdpBrowserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.cdpBrowserContext.browser();
    }


    public BrowserContext browserContext() {
        if (this.cdpBrowserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.cdpBrowserContext;
    }

    /**
     * 获取打开此目标的目标。顶层目标返回 null。
     *
     * @return Target
     */
    public Target opener() {
        String openerId = this.targetInfo.getOpenerId();
        if (StringUtil.isEmpty(openerId)) {
            return null;
        }
        for (CdpTarget target : this.browser().targets()) {
            if (target.getTargetId().equals(openerId)) {
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
        this.setInitializedResult(InitializationStatus.SUCCESS);
    }

    public boolean isTargetExposed() {
        return this.type() != TargetType.TAB && (this.subtype() == null);
    }

    private void checkIfInitialized() {
        if (!this.initializedResult.isDone()) {
            this.setInitializedResult(InitializationStatus.SUCCESS);
        }
    }

    public String getTargetId() {
        return this.targetId;
    }

    public void waitForTargetClose() {
        this.isClosedResult.waiting(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void close() {
        this.isClosedResult.onSuccess(true);
        if (onCloseRunner != null) {
            onCloseRunner.run();
        }
    }

    public void setOnCloseRunner(Runnable onCloseRunner) {
        this.onCloseRunner = onCloseRunner;
    }

    public enum InitializationStatus {
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
