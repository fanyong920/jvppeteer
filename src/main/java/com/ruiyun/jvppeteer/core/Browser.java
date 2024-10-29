package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.entities.BrowserContextOptions;
import com.ruiyun.jvppeteer.entities.DebugInfo;
import com.ruiyun.jvppeteer.entities.GetVersionResponse;
import com.ruiyun.jvppeteer.entities.TargetInfo;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.util.Helper.filter;
import static com.ruiyun.jvppeteer.util.Helper.waitForCondition;

/**
 * Browser 代表一个浏览器实例，它是：
 * <p>
 * 通过 Puppeteer.connect() 连接到或 - 由 Puppeteer.launch() 产生。
 * <p>
 * Browser emits 各种事件记录在 BrowserEvent 枚举中。
 * <p>
 * 第三方代码不应直接调用构造函数或创建扩展 Browser 类的子类。
 */
public class Browser extends EventEmitter<Browser.BrowserEvent> implements AutoCloseable {
    private final Viewport defaultViewport;
    private final Process process;
    private final Connection connection;
    private final Runnable closeCallback;
    private final Product product;
    private Function<Target, Boolean> isPageTargetCallback;
    private final BrowserContext defaultContext;
    private final Map<String, BrowserContext> contexts = new HashMap<>();
    private final TargetManager targetManager;
    private String executablePath;
    private List<String> defaultArgs;


    Browser(Product product, Connection connection, List<String> contextIds, Viewport viewport, Process process, Runnable closeCallback, Function<Target, Boolean> targetFilterCallback, Function<Target, Boolean> isPageTargetCallback, boolean waitForInitiallyDiscoveredTargets) {
        super();
        this.product = product;
        this.defaultViewport = viewport;
        this.process = process;
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = () -> {};
        }
        this.closeCallback = closeCallback;
        if (targetFilterCallback == null) {
            targetFilterCallback = (ignore) -> true;
        }
        Function<Target, Boolean> targetFilterCallback1 = targetFilterCallback;
        this.setIsPageTargetCallback(isPageTargetCallback);
        if (Product.FIREFOX.equals(product)) {
            throw new JvppeteerException("Not Support firefox");
        } else {
            this.targetManager = new ChromeTargetManager(connection, this.createTarget(), targetFilterCallback1, waitForInitiallyDiscoveredTargets);
        }
        this.defaultContext = new BrowserContext(connection, this, "");
        if (ValidateUtil.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                this.contexts.putIfAbsent(contextId, new BrowserContext(this.connection, this, contextId));
            }
        }
    }

    private final Consumer<Object> emitDisconnected = (ignore) -> this.emit(BrowserEvent.Disconnected, true);

    private void attach() {
        this.connection.on(CDPSession.CDPSessionEvent.CDPSession_Disconnected, this.emitDisconnected);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetAvailable, this.onAttachedToTarget);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetGone, this.onDetachedFromTarget);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetChanged, this.onTargetChanged);
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetDiscovered, this.onTargetDiscovered);
        this.targetManager.initialize();
    }

    private void detach() {
        this.connection.off(CDPSession.CDPSessionEvent.CDPSession_Disconnected, this.emitDisconnected);
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
            isPageTargetCallback = (target -> TargetType.PAGE.equals(target.type()) || TargetType.BACKGROUND_PAGE.equals(target.type()) || TargetType.WEBVIEW.equals(target.type()));
        }
        this.isPageTargetCallback = isPageTargetCallback;
    }

    Function<Target, Boolean> getIsPageTargetCallback() {
        return this.isPageTargetCallback;
    }

    public BrowserContext createBrowserContext() {
        return this.createBrowserContext(new BrowserContextOptions());
    }

    /**
     * 创建一个新的 浏览器上下文。
     * <p>
     * 这不会与其他 浏览器上下文 共享 cookie/缓存
     *
     * @param options 浏览器上下文选项，包含代理服务器设置等
     * @return 返回创建的浏览器上下文对象
     */
    public BrowserContext createBrowserContext(BrowserContextOptions options) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("proxyServer", options.getProxyServer());
        if (ValidateUtil.isNotEmpty(options.getProxyBypassList())) {
            params.put("proxyBypassList", String.join(",", options.getProxyBypassList()));
        }
        JsonNode result = this.connection.send("Target.createBrowserContext", params);
        BrowserContext context = new BrowserContext(this.connection, this, result.get("browserContextId").asText());
        this.contexts.put(result.get("browserContextId").asText(), context);
        return context;
    }

    /**
     * 获取打开的 浏览器上下文 列表。
     * <p>
     * 在新创建的 browser 中，这将返回 BrowserContext 的单个实例。
     *
     * @return 打开的 浏览器上下文 列表
     */
    public List<BrowserContext> browserContexts() {
        List<BrowserContext> contexts = new ArrayList<>();
        contexts.add(this.defaultBrowserContext());
        contexts.addAll(this.contexts.values());
        return contexts;
    }

    /**
     * 获取默认 浏览器上下文。
     * <p>
     * 默认 浏览器上下文 无法关闭。
     *
     * @return 默认 浏览器上下文
     */
    public BrowserContext defaultBrowserContext() {
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

    /**
     * 获取此 Browser 内所有打开的 pages 的列表。
     * <p>
     * 如果有多个 浏览器上下文，则返回所有 浏览器上下文 中的所有 pages。
     *
     * @return 所有打开的 pages
     */
    public List<Page> pages() {
        return this.browserContexts().stream().flatMap(context -> context.pages().stream()).collect(Collectors.toList());
    }

    private TargetManager.TargetFactory createTarget() {
        return (targetInfo, session, parentSession) -> {
            String browserContextId = targetInfo.getBrowserContextId();
            BrowserContext context;
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
            if (StringUtil.isNotEmpty(targetInfo.getUrl()) && targetInfo.getUrl().startsWith("devtools://")) {
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

    private final Consumer<Target> onAttachedToTarget = (target) -> {
        if (target.isTargetExposed() && target.initializedResult.waitingGetResult().equals(Target.InitializationStatus.SUCCESS)) {
            this.emit(BrowserEvent.TargetCreated, target);
            target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetCreated, target);
        }
    };
    private final Consumer<Target> onDetachedFromTarget = (target) -> {
        boolean initializedSuccess = target.initializedResult.waitingGetResult().equals(Target.InitializationStatus.SUCCESS);
        target.setInitializedResult(Target.InitializationStatus.ABORTED);
        target.close();
        if (target.isTargetExposed() && initializedSuccess) {
            this.emit(BrowserEvent.TargetDestroyed, target);
            target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetDestroyed, target);
        }
    };

    private final Consumer<Target> onTargetChanged = (target) -> {
        this.emit(BrowserEvent.TargetChanged, target);
        target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetChanged, target);
    };
    private final Consumer<TargetInfo> onTargetDiscovered = (target) -> this.emit(BrowserEvent.TargetDiscovered, target);

    /**
     * 获取用于连接到此 browser 的 WebSocket URL。
     * <p>
     * 这通常与 Puppeteer.connect() 一起使用。
     * <p>
     * 你可以从 http://HOST:PORT/json/version 找到调试器 URL (webSocketDebuggerUrl)。
     * <p>
     * 请参阅 <a href="https://chromedevtools.github.io/devtools-protocol/#how-do-i-access-the-browser-target">浏览器端点</a> 了解更多信息。
     *
     * @return WebSocket URL
     */
    public String wsEndpoint() {
        return this.connection.url();
    }

    /**
     * 在 默认浏览器上下文 中创建新的 page。
     *
     * @return 新创建的页面对象
     */
    public Page newPage() {
        return this.defaultContext.newPage();
    }

    public Page createPageInContext(String contextId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("url", "about:blank");
        if (StringUtil.isNotEmpty(contextId)) {
            params.put("browserContextId", contextId);
        }
        JsonNode result = this.connection.send("Target.createTarget", params);
        if (result != null) {
            String targetId = result.get(Constant.TARGET_ID).asText();
            Target target = this.waitForTarget(t -> t.getTargetId().equals(targetId), Constant.DEFAULT_TIMEOUT);
            if (target == null) {
                throw new JvppeteerException("Missing target for page (id = " + targetId + ")");
            }
            if (!target.initializedResult.waitingGetResult().equals(Target.InitializationStatus.SUCCESS)) {
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

    /**
     * 获取与 默认浏览器上下文 关联的 target。
     *
     * @return 默认浏览器上下文 关联的 target
     */
    public Target target() {
        for (Target target : this.targets()) {
            if (TargetType.BROWSER.equals(target.type())) {
                return target;
            }
        }
        throw new JvppeteerException("Browser target is not found");
    }

    /**
     * 获取所有活动的 targets。
     * <p>
     * 如果有多个 浏览器上下文，则返回所有 浏览器上下文 中的所有 targets。
     *
     * @return 所有活动的 targets
     */
    public List<Target> targets() {
        return this.targetManager.getAvailableTargets().values().stream().filter(target -> target.isTargetExposed() && target.initializedResult.waitingGetResult().equals(Target.InitializationStatus.SUCCESS)).collect(Collectors.toList());
    }

    /**
     * 获取表示此 浏览器的 名称和版本的字符串。
     * <p>
     * 对于无头浏览器，这与 "HeadlessChrome/61.0.3153.0" 类似。对于非无头或新无头，这与 "Chrome/61.0.3153.0" 类似。
     * <p>
     * Browser.version() 的格式可能会随着浏览器的未来版本而改变。
     *
     * @return 浏览器版本
     * @throws JsonProcessingException 序列化错误
     */
    public String version() throws JsonProcessingException {
        GetVersionResponse version = this.getVersion();
        return version.getProduct();
    }

    /**
     * 获取此 浏览器的 原始用户代理。
     * <p>
     * Pages 可以使用 Page.setUserAgent() 覆盖用户代理。
     *
     * @return 原始用户代理
     * @throws JsonProcessingException 序列化错误
     */
    public String userAgent() throws JsonProcessingException {
        GetVersionResponse version = this.getVersion();
        return version.getUserAgent();
    }

    @Override
    public void close() {
        this.closeCallback.run();
        this.disconnect();
    }

    /**
     * 断开 Puppeteer 与该 browser 的连接，但保持进程运行。
     */
    public void disconnect() {
        this.targetManager.dispose();
        this.connection.dispose();
        this.detach();
    }

    /**
     * Puppeteer 是否连接到此 browser。
     *
     * @return 连接返回true, 不连接返回false
     */
    public boolean connected() {
        return !this.connection.closed();
    }

    private GetVersionResponse getVersion() throws JsonProcessingException {
        return Constant.OBJECTMAPPER.treeToValue(this.connection.send("Browser.getVersion"), GetVersionResponse.class);
    }

    public DebugInfo debugInfo() {
        return new DebugInfo(this.connection.getPendingProtocolErrors());
    }

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它.
     * <p>
     * 默认等待时间是30s
     *
     * @param predicate 用于筛选目标对象的条件，符合条件的目标将被返回.
     * @return 返回符合 predicate 条件的目标对象.
     */
    public Target waitForTarget(Predicate<Target> predicate) {
        Supplier<Target> conditionChecker = () -> filter(this.targets(), predicate);
        return waitForCondition(conditionChecker, Constant.DEFAULT_TIMEOUT, "Waiting for target failed: timeout " + Constant.DEFAULT_TIMEOUT + "ms exceeded");
    }

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它.
     * 此方法用于在一定超时时间内，持续检查是否出现符合特定条件的目标对象.
     *
     * @param predicate 用于筛选目标对象的条件，符合条件的目标将被返回.
     * @param timeout   等待的最大时间（以毫秒为单位），超过此时间将抛出异常.
     * @return 返回符合 predicate 条件的目标对象.
     */
    public Target waitForTarget(Predicate<Target> predicate, int timeout) {
        Supplier<Target> conditionChecker = () -> filter(this.targets(), predicate);
        return waitForCondition(conditionChecker, timeout, "Waiting for target failed: timeout " + timeout + "ms exceeded");
    }

    public static Browser create(Product product, Connection connection, List<String> contextIds, boolean acceptInsecureCerts, Viewport defaultViewport, Process process, Runnable closeCallback, Function<Target, Boolean> targetFilterCallback, Function<Target, Boolean> IsPageTargetCallback, boolean waitForInitiallyDiscoveredTargets) {
        Browser browser = new Browser(product, connection, contextIds, defaultViewport, process, closeCallback, targetFilterCallback, IsPageTargetCallback, waitForInitiallyDiscoveredTargets);
        if (acceptInsecureCerts) {
            Map<String, Object> params = ParamsFactory.create();
            params.put("ignore", true);
            connection.send("Security.setIgnoreCertificateErrors", params);
        }
        browser.attach();
        return browser;
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

    public Product product() {
        return product;
    }

    public enum BrowserEvent {
        /**
         * 创建target
         * {@link Target}
         */
        TargetCreated("targetcreated"),
        /**
         * 销毁target
         * {@link Target}
         */
        TargetDestroyed("targetdestroyed"),
        /**
         * target变化
         * {@link Target}
         */
        TargetChanged("targetchanged"),
        /**
         * 发现target
         * {@link TargetInfo}
         */
        TargetDiscovered("targetdiscovered"),
        /**
         * 断开连接
         * Object
         */
        Disconnected("disconnected");
        private final String eventName;

        BrowserEvent(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }
    }

}
