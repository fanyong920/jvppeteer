package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserContext;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Target {
    private Boolean initializedPromise;
    private CountDownLatch initializedCountDown;
    private TargetInfo targetInfo;
    private BrowserContext browserContext;
    private boolean ignoreHTTPSErrors;
    private Viewport viewport;
    private TaskQueue<String> screenshotTaskQueue;
    private String targetId;
    private Page pagePromise;
    private Worker workerPromise;
    private boolean isInitialized;
    private SessionFactory sessionFactory;
    private String sessionId;
    private CountDownLatch isClosedPromiseLatch;
    public Target() {
        super();
    }
    public Target(TargetInfo targetInfo, BrowserContext browserContext, SessionFactory sessionFactory, boolean ignoreHTTPSErrors, Viewport defaultViewport, TaskQueue<String> screenshotTaskQueue) {
        super();
        this.targetInfo = targetInfo;
        this.browserContext = browserContext;
        this.targetId = targetInfo.getTargetId();
        this.sessionFactory = sessionFactory;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.viewport = defaultViewport;
        this.screenshotTaskQueue = screenshotTaskQueue;
        this.pagePromise = null;
        this.workerPromise = null;
        this.isClosedPromiseLatch = new CountDownLatch(1);
        this.isInitialized = !"page".equals(this.targetInfo.getType()) || !StringUtil.isEmpty(this.targetInfo.getUrl());
        if (isInitialized) {//初始化
            this.initializedPromise = this.initializedCallback(true);
        } else {
            this.initializedPromise = true;
        }
    }

    public CDPSession createCDPSession() {
        return this.sessionFactory.create();
    }

    public Worker worker() {
        if (!"service_worker".equals(this.targetInfo.getType()) && !"shared_worker".equals(this.targetInfo.getType()))
            return null;
        if (this.workerPromise == null) {
            synchronized (this) {
                if (this.workerPromise == null) {
                    CDPSession client = this.sessionFactory.create();
                    this.workerPromise = new Worker(client, this.targetInfo.getUrl(), (arg0, arg1, arg2) -> {
                    } /* consoleAPICalled */, (arg) -> {
                    } /* exceptionThrown */);
                }
            }

        }
        return this.workerPromise;
    }


    public void closedCallback() {
        if (pagePromise != null) {
            this.pagePromise.emit(Page.PageEvent.CLOSE, null);
            this.pagePromise.setClosed(true);
        }
        this.isClosedPromiseLatch.countDown();
    }

    /**
     * 如果目标不是 "page" 或 "background_page" 类型，则返回 null。
     *
     * @return Page
     */
    public Page page() {
        String type;
        if (("page".equals(type = this.targetInfo.getType()) || "background_page".equals(type)) && this.pagePromise == null) {
            try {
                this.pagePromise = Page.create(this.sessionFactory.create(), this, this.ignoreHTTPSErrors, this.viewport, this.screenshotTaskQueue);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.pagePromise;
    }

    /**
     * 确定目标是怎么样的类型。 可以是 "page"，"background_page"，"service_worker"，"browser" 或 "其他"。
     *
     * @return 目标类型
     */
    public String type() {
        String type = this.targetInfo.getType();
        if ("page".equals(type) || "background_page".equals(type) || "service_worker".equals(type) || "shared_worker".equals(type) || "browser".equals(type)) {
            return type;
        }
        return "other";
    }

    public boolean initializedCallback(boolean success) {
        try {
            if (!success) {
                this.initializedPromise = false;
                return false;
            }
            Target opener = this.opener();
            if (opener == null || opener.getPagePromise() == null || "page".equals(this.type())) {
                this.initializedPromise = true;
                return true;
            }
            Page openerPage = opener.getPagePromise();
            if (openerPage.listenerCount(Page.PageEvent.POPUP) <= 0) {
                this.initializedPromise = true;
                return true;
            }
            Page pupopPage = this.page();
            pupopPage.emit(Page.PageEvent.POPUP, pupopPage);
            this.initializedPromise = true;
            return true;
        } finally {
            if (initializedCountDown != null) {
                initializedCountDown.countDown();
                initializedCountDown = null;
            }
        }
    }

    public boolean waitInitializedPromise() {
        if (initializedPromise == null) {
            this.initializedCountDown = new CountDownLatch(1);
            try {
                initializedCountDown.await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Wait for InitializedPromise fail:", e);
            }
        }
        return this.initializedPromise;
    }

    /**
     * 获取打开此目标的目标。 顶级目标返回null。
     *
     * @return Target
     */
    public Target opener() {
        String openerId = this.targetInfo.getOpenerId();
        if (StringUtil.isEmpty(openerId)) {
            return null;
        }
        return this.browser().getTargets().get(openerId);
    }

    /**
     * 返回目标的url
     *
     * @return url
     */
    public String url() {
        return this.targetInfo.getUrl();
    }

    /**
     * 获取目标所属的浏览器。
     *
     * @return Browser
     */
    public Browser browser() {
        return this.browserContext.browser();
    }

    public Page getPagePromise() {
        return pagePromise;
    }

    public void setPagePromise(Page pagePromise) {
        this.pagePromise = pagePromise;
    }

    public Worker getWorkerPromise() {
        return workerPromise;
    }

    public void setWorkerPromise(Worker workerPromise) {
        this.workerPromise = workerPromise;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }

    /**
     * 目标所属的浏览器上下文。
     *
     * @return 浏览器上下文
     */
    public BrowserContext browserContext() {
        return browserContext;
    }

    public void setBrowserContext(BrowserContext browserContext) {
        this.browserContext = browserContext;
    }

    public boolean isIgnoreHTTPSErrors() {
        return ignoreHTTPSErrors;
    }

    public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    }

    public Viewport getDefaultViewport() {
        return viewport;
    }

    public void setDefaultViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public boolean getIsInitialized() {
        return isInitialized;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void targetInfoChanged(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
        if (!this.isInitialized && (!"page".equals(this.targetInfo.getType()) || !"".equals(this.targetInfo.getUrl()))) {
            this.isInitialized = true;
            this.initializedCallback(true);
        }
    }

    public boolean WaiforisClosedPromise() throws InterruptedException {
        return this.isClosedPromiseLatch.await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

}
