package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.transport.CDPSession;
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
public class Target {
    private BrowserContext browserContext;
    private CDPSession session;
    protected TargetInfo targetInfo;
    private TargetManager targetManager;
    protected SessionFactory sessionFactory;
    private final List<Target> childTargets = new ArrayList<>();
    public final AwaitableResult<Boolean> isClosedResult = AwaitableResult.create();
    final AwaitableResult<InitializationStatus> initializedResult = AwaitableResult.create();
    private String targetId;
    protected WebWorker webWorker;
    private Runnable onCloseRunner = () -> {
    };

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

    public List<Target> childTargets() {
        return this.childTargets;
    }

    public SessionFactory sessionFactory() {
        if (this.sessionFactory == null) {
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        return this.sessionFactory;
    }

    /**
     * 创建附加到目标的 Chrome Devtools 协议会话。
     *
     * @return 会话
     */
    public CDPSession createCDPSession() {
        if (this.sessionFactory == null) {
            throw new JvppeteerException("sessionFactory is not initialized");
        }
        CDPSession cdpSession = this.sessionFactory.create(false);
        cdpSession.setTarget(this);
        return cdpSession;
    }

    public String url() {
        return this.targetInfo.getUrl();
    }

    /**
     * 确定这是什么类型的目标。<p>
     * 注意：背景页是谷歌插件里的页面
     *
     * @return 目标类型
     */
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

    /**
     * 获取目标所属的浏览器。
     *
     * @return Browser
     */
    public Browser browser() {
        if (this.browserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.browserContext.browser();
    }

    /**
     * 获取目标所属的浏览器上下文。
     *
     * @return BrowserContext
     */
    public BrowserContext browserContext() {
        if (this.browserContext == null) {
            throw new JvppeteerException("browserContext is not initialized");
        }
        return this.browserContext;
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
        for (Target target : this.browser().targets()) {
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

    /**
     * 如果目标类型不是“page”，"webview","background_page",那么返回null
     *
     * @return Page
     */
    public Page page() {
        return null;
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
