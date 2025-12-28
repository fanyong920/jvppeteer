package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.BrowserContextEvents;
import com.ruiyun.jvppeteer.api.events.BrowserEvents;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.DebugInfo;
import com.ruiyun.jvppeteer.cdp.entities.DownloadOptions;
import com.ruiyun.jvppeteer.cdp.entities.DownloadPolicy;
import com.ruiyun.jvppeteer.cdp.entities.GetVersionResponse;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.common.AddScreenParams;
import com.ruiyun.jvppeteer.common.BrowserContextOptions;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.CreatePageOptions;
import com.ruiyun.jvppeteer.common.CreateType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.ScreenInfo;
import com.ruiyun.jvppeteer.common.WindowBounds;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


import static com.ruiyun.jvppeteer.util.Helper.getVersion;

/**
 * Browser 代表一个浏览器实例，它是：
 * <p>
 * 通过 Puppeteer.connect() 连接到或 - 由 Puppeteer.launch() 产生。
 * <p>
 * Browser emits 各种事件记录在 BrowserEvent 枚举中。
 * <p>
 * 第三方代码不应直接调用构造函数或创建扩展 Browser 类的子类。
 */
public class CdpBrowser extends Browser {
    private final Viewport defaultViewport;
    private final Process process;
    private final Connection connection;
    private final Runnable closeCallback;
    private Function<Target, Boolean> isPageTargetCallback;
    private final CdpBrowserContext defaultContext;
    private final Map<String, CdpBrowserContext> contexts = new HashMap<>();
    private final TargetManager targetManager;
    private String executablePath;
    private List<String> defaultArgs;
    private final boolean networkEnabled;
    private final boolean handleDevToolsAsPage;

    protected CdpBrowser(Connection connection, List<String> contextIds, Viewport viewport, Process process, Runnable closeCallback, Function<Target, Boolean> targetFilterCallback, Function<Target, Boolean> isPageTargetCallback, boolean waitForInitiallyDiscoveredTargets, boolean networkEnabled, boolean handleDevToolsAsPage) {
        super();
        this.networkEnabled = networkEnabled;
        this.defaultViewport = viewport;
        this.process = process;
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = () -> {
            };
        }
        this.closeCallback = closeCallback;
        if (targetFilterCallback == null) {
            targetFilterCallback = (ignore) -> true;
        }
        this.handleDevToolsAsPage = handleDevToolsAsPage;
        this.setIsPageTargetCallback(isPageTargetCallback);
        this.targetManager = new TargetManager(connection, this.createTarget(), targetFilterCallback, waitForInitiallyDiscoveredTargets);
        this.defaultContext = new CdpBrowserContext(connection, this, "");
        if (ValidateUtil.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                this.contexts.putIfAbsent(contextId, new CdpBrowserContext(this.connection, this, contextId));
            }
        }
    }

    private final Consumer<Object> emitDisconnected = (ignore) -> this.emit(BrowserEvents.Disconnected, true);
    private final Consumer<Object> emitDownloadProgress = (event) -> this.emit(BrowserEvents.DownloadProgress, event);
    private final Consumer<Object> emitDownloadWillBegin = (event) -> this.emit(BrowserEvents.DownloadWillBegin, event);


    private void attach() {
        this.connection.on(ConnectionEvents.CDPSession_Disconnected, this.emitDisconnected);
        this.connection.on(ConnectionEvents.Browser_downloadProgress, this.emitDownloadProgress);
        this.connection.on(ConnectionEvents.Browser_downloadWillBegin, this.emitDownloadWillBegin);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetAvailable, this.onAttachedToTarget);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetGone, this.onDetachedFromTarget);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetChanged, this.onTargetChanged);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetDiscovered, this.onTargetDiscovered);
        this.targetManager.initialize();
    }

    private void detach() {
        this.connection.off(ConnectionEvents.CDPSession_Disconnected, this.emitDisconnected);
        this.connection.off(ConnectionEvents.Browser_downloadProgress, this.emitDownloadProgress);
        this.connection.off(ConnectionEvents.Browser_downloadWillBegin, this.emitDownloadWillBegin);
        this.targetManager.off(TargetManager.TargetManagerEvent.TargetAvailable, this.onAttachedToTarget);
        this.targetManager.off(TargetManager.TargetManagerEvent.TargetGone, this.onDetachedFromTarget);
        this.targetManager.off(TargetManager.TargetManagerEvent.TargetChanged, this.onTargetChanged);
        this.targetManager.off(TargetManager.TargetManagerEvent.TargetDiscovered, this.onTargetDiscovered);
    }

    /**
     * 获取关联的 Process。
     *
     * @return 浏览器进程对象
     */
    public Process process() {
        return this.process;
    }

    /**
     * 返回浏览器的可执行路径。
     *
     * @return 可执行路径
     */
    public String executablePath() {
        return this.executablePath;
    }

    public TargetManager targetManager() {
        return this.targetManager;
    }

    private void setIsPageTargetCallback(Function<Target, Boolean> isPageTargetCallback) {
        if (isPageTargetCallback == null) {
            isPageTargetCallback = (target -> TargetType.PAGE.equals(target.type()) || TargetType.BACKGROUND_PAGE.equals(target.type()) || TargetType.WEBVIEW.equals(target.type()) || (this.handleDevToolsAsPage && TargetType.OTHER.equals(target.type()) && isDevToolsPageTarget(target.url())));
        }
        this.isPageTargetCallback = isPageTargetCallback;
    }

    Function<Target, Boolean> getIsPageTargetCallback() {
        return this.isPageTargetCallback;
    }

    public CdpBrowserContext createBrowserContext(BrowserContextOptions options) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("proxyServer", options.getProxyServer());
        if (ValidateUtil.isNotEmpty(options.getProxyBypassList())) {
            params.put("proxyBypassList", String.join(",", options.getProxyBypassList()));
        }
        JsonNode result = this.connection.send("Target.createBrowserContext", params);
        CdpBrowserContext context = new CdpBrowserContext(this.connection, this, result.get("browserContextId").asText());
        this.contexts.put(result.get("browserContextId").asText(), context);
        return context;
    }

    public List<BrowserContext> browserContexts() {
        List<BrowserContext> contexts = new ArrayList<>();
        contexts.add(this.defaultBrowserContext());
        contexts.addAll(this.contexts.values());
        return contexts;
    }

    public CdpBrowserContext defaultBrowserContext() {
        return this.defaultContext;
    }

    void disposeContext(String contextId) {
        if (StringUtil.isEmpty(contextId)) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("browserContextId", contextId);
        this.connection.send("Target.disposeBrowserContext", params);
        this.contexts.remove(contextId);
    }

    private TargetManager.TargetFactory createTarget() {
        return (targetInfo, session, parentSession) -> {
            String browserContextId = targetInfo.getBrowserContextId();
            CdpBrowserContext context;
            if (StringUtil.isNotEmpty(browserContextId) && this.contexts.containsKey(browserContextId)) {
                context = this.contexts.get(browserContextId);
            } else {
                context = this.defaultContext;
            }
            if (context == null) {
                throw new JvppeteerException("Missing browser context");
            }
            SessionFactory createSession = (isAutoAttachEmulated) -> this.connection._createSession(targetInfo, isAutoAttachEmulated);
            OtherTarget otherTarget = new OtherTarget(targetInfo, session, context, this.targetManager, createSession);
            if (StringUtil.isNotEmpty(targetInfo.getUrl()) && isDevToolsPageTarget(targetInfo.getUrl())) {
                return new DevToolsTarget(targetInfo, session, context, this.targetManager, createSession, this.defaultViewport);
            }
            if (this.isPageTargetCallback.apply(otherTarget)) {
                return new PageTarget(targetInfo, session, context, this.targetManager, createSession, this.defaultViewport);
            }
            if ("service_worker".equals(targetInfo.getType()) || "shared_worker".equals(targetInfo.getType())) {
                return new WorkerTarget(targetInfo, session, context, this.targetManager, createSession);
            }
            return otherTarget;
        };
    }

    private final Consumer<CdpTarget> onAttachedToTarget = (target) -> {
        if (target.isTargetExposed() && Objects.equals(target.initializedResult.waitingGetResult(), CdpTarget.InitializationStatus.SUCCESS)) {
            this.emit(BrowserEvents.TargetCreated, target);
            target.browserContext().emit(BrowserContextEvents.TargetCreated, target);
        }
    };
    private final Consumer<CdpTarget> onDetachedFromTarget = (target) -> {
        boolean initializedSuccess = Objects.equals(target.initializedResult.waitingGetResult(), CdpTarget.InitializationStatus.SUCCESS);
        target.setInitializedResult(CdpTarget.InitializationStatus.ABORTED);
        target.close();
        if (target.isTargetExposed() && initializedSuccess) {
            this.emit(BrowserEvents.TargetDestroyed, target);
            target.browserContext().emit(BrowserContextEvents.TargetDestroyed, target);
        }
    };

    private final Consumer<CdpTarget> onTargetChanged = (target) -> {
        this.emit(BrowserEvents.TargetChanged, target);
        target.browserContext().emit(BrowserContextEvents.TargetChanged, target);
    };
    private final Consumer<TargetInfo> onTargetDiscovered = (target) -> this.emit(BrowserEvents.TargetDiscovered, target);

    public String wsEndpoint() {
        return this.connection.url();
    }

    @Override
    public WindowBounds getWindowBounds(int windowId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("windowId", windowId);
        JsonNode response = this.connection.send("Browser.getWindowBounds", params).get("bounds");
        return Constant.OBJECTMAPPER.convertValue(response, WindowBounds.class);
    }

    @Override
    public void setWindowBounds(int windowId, WindowBounds windowBounds) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("windowId", windowId);
        params.put("bounds", windowBounds);
        this.connection.send("Browser.setWindowBounds", params);
    }

    public Page newPage(CreatePageOptions options) {
        return this.defaultContext.newPage(options);
    }

    Page createPageInContext(String contextId, CreatePageOptions options) {
        boolean hasTargets = this.targets().stream()
                .anyMatch(t -> t.browserContext().id().equals(contextId));
        WindowBounds windowBounds =
                (options != null && CreateType.Window.equals(options.getType())) ?
                        options.getWindowBounds() :
                        null;
        Map<String, Object> params = ParamsFactory.create();
        params.put("url", "about:blank");
        if (StringUtil.isNotEmpty(contextId)) {
            params.put("browserContextId", contextId);
        }
        if (Objects.nonNull(windowBounds)) {
            params.put("left", windowBounds.getLeft());
            params.put("top", windowBounds.getTop());
            params.put("width", windowBounds.getWidth());
            params.put("height", windowBounds.getHeight());
            params.put("windowState", windowBounds.getWindowState());
        }
        if (hasTargets && Objects.nonNull(options) && Objects.equals(options.getType(), CreateType.Window)) {
            params.put("newWindow", true);
        }
        JsonNode result = this.connection.send("Target.createTarget", params);
        if (result != null) {
            String targetId = result.get(Constant.TARGET_ID).asText();
            CdpTarget target = (CdpTarget) this.waitForTarget(t -> ((CdpTarget) t).getTargetId().equals(targetId), Constant.DEFAULT_TIMEOUT);
            if (target == null) {
                throw new JvppeteerException("Missing target for page (id = " + targetId + ")");
            }
            if (!Objects.equals(target.initializedResult.waitingGetResult(), CdpTarget.InitializationStatus.SUCCESS)) {
                throw new JvppeteerException("Failed to create target for page (id =" + targetId + ")");
            }
            Page page = target.page();
            if (page == null) {
                throw new JvppeteerException("Failed to create a page for context (id = " + contextId + ")");
            }
            return page;
        } else {
            throw new JvppeteerException("Failed to create target for page (id =" + contextId + ")");
        }
    }

    public Target target() {
        for (Target target : this.targets()) {
            if (TargetType.BROWSER.equals(target.type())) {
                return target;
            }
        }
        throw new JvppeteerException("Browser target is not found");
    }

    public List<CdpTarget> targets() {
        return this.targetManager.getAvailableTargets().values().stream().filter(target -> target.isTargetExposed() && Objects.equals(target.initializedResult.waitingGetResult(), CdpTarget.InitializationStatus.SUCCESS)).collect(Collectors.toList());
    }

    public String version() throws JsonProcessingException {
        GetVersionResponse version = getVersion(this.connection);
        return version.getProduct();
    }

    public String userAgent() {
        GetVersionResponse version = getVersion(this.connection);
        return version.getUserAgent();
    }

    public volatile boolean closed;

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closeCallback.run();
        this.disconnect();
        this.closed = true;
    }

    public void disconnect() {
        if (this.closed) {
            return;
        }
        this.targetManager.dispose();
        this.connection.dispose();
        this.detach();
        this.closed = true;
    }

    public boolean connected() {
        return !this.connection.closed();
    }


    public DebugInfo debugInfo() {
        return new DebugInfo(this.connection.getPendingProtocolErrors());
    }


    public static CdpBrowser create(Connection connection, List<String> contextIds, boolean acceptInsecureCerts, Viewport defaultViewport, Process process, Runnable closeCallback, Function<Target, Boolean> targetFilterCallback, Function<Target, Boolean> IsPageTargetCallback, boolean waitForInitiallyDiscoveredTargets, boolean networkEnabled, boolean handleDevToolsAsPage) {
        CdpBrowser cdpBrowser = new CdpBrowser(connection, contextIds, defaultViewport, process, closeCallback, targetFilterCallback, IsPageTargetCallback, waitForInitiallyDiscoveredTargets, networkEnabled, handleDevToolsAsPage);
        if (acceptInsecureCerts) {
            Map<String, Object> params = ParamsFactory.create();
            params.put("ignore", true);
            connection.send("Security.setIgnoreCertificateErrors", params);
        }
        cdpBrowser.attach();
        return cdpBrowser;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    /**
     * 返回默认的运行的参数
     *
     * @return 默认参数集合
     */
    public List<String> defaultArgs() {
        return this.defaultArgs;
    }

    public void setDefaultArgs(List<String> defaultArgs) {
        this.defaultArgs = defaultArgs;
    }

    /**
     * 设置下载行为
     *
     * @param options 可选配置，可以设置下载的存放路径，是否接受下载事件，拒绝还是接受下载
     *                如果没有指定 browserContextId,则设置默认浏览器上下文的下载行为
     */
    public void setDownloadBehavior(DownloadOptions options) {
        if (Objects.isNull(options.getBehavior())) {
            options.setBehavior(DownloadPolicy.Default);
        }
        if (options.getBehavior().equals(DownloadPolicy.Allow) || options.getBehavior().equals(DownloadPolicy.AllowAndName)) {
            if (StringUtil.isBlank(options.getDownloadPath())) {
                throw new JvppeteerException("This is required if behavior is set to 'allow' or 'allowAndName'.");
            }
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("behavior", options.getBehavior().getBehavior());
        params.put("downloadPath", options.getDownloadPath());
        params.put("browserContextId", options.getBrowserContextId());
        params.put("eventsEnabled", options.getEventsEnabled());
        this.connection.send("Browser.setDownloadBehavior", params);
    }

    /**
     * 设置下载行为
     *
     * @param guid             下载的全局唯一标识符。
     * @param browserContextId BrowserContext 在其中执行操作。省略时，将使用默认浏览器上下文。
     */
    public void cancelDownload(String guid, String browserContextId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("guid", guid);
        params.put("browserContextId", browserContextId);
        this.connection.send("Browser.cancelDownload", params);
    }

    @Override
    public boolean isNetworkEnabled() {
        return this.networkEnabled;
    }

    @Override
    public String installExtension(String path) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("path", path);
        return this.connection.send("Extensions.loadUnpacked", params).asText();
    }

    @Override
    public void uninstallExtension(String id) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("id", id);
        this.connection.send("Extensions.uninstall", params);
    }

    @Override
    public List<ScreenInfo> screens() throws JsonProcessingException {
        return Constant.OBJECTMAPPER.treeToValue(this.connection.send("Emulation.getScreenInfos").get("screenInfos"), new TypeReference<ArrayList<ScreenInfo>>() {
        });
    }

    @Override
    public ScreenInfo addScreen(AddScreenParams params) throws JsonProcessingException {
        return Constant.OBJECTMAPPER.treeToValue(this.connection.send("Emulation.addScreen", params).get("screenInfo"), ScreenInfo.class);
    }

    @Override
    public void removeScreen(String screenId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("screenId", screenId);
        this.connection.send("Emulation.removeScreen", params);
    }

    public Page createDevToolsPage(String pageTargetId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("targetId", pageTargetId);
        JsonNode openDevToolsResponse = this.connection.send("Target.openDevTools", params);
        CdpTarget target = (CdpTarget) this.waitForTarget(t -> ((CdpTarget) t).getTargetId().equals(openDevToolsResponse.get("targetId").asText()));
        if (Objects.isNull(target)) {
            throw new JvppeteerException("Missing target for DevTools page (id = " + pageTargetId + ")");
        }
        boolean initialized = target.initializedResult.waitingGetResult().equals(CdpTarget.InitializationStatus.SUCCESS);
        if (!initialized) {
            throw new JvppeteerException("Failed to create target for DevTools page (id = " + pageTargetId + ")");
        }
        Page page = target.page();
        if (Objects.isNull(page)) {
            throw new JvppeteerException("Failed to create a DevTools Page for target (id = " + pageTargetId + ")");
        }
        return page;
    }

    public static boolean isDevToolsPageTarget(String url) {
        return url.startsWith("devtools://devtools/bundled/devtools_app.html");
    }
}
