package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.BrowserContextEvents;
import com.ruiyun.jvppeteer.api.events.BrowserEvents;
import com.ruiyun.jvppeteer.api.events.TrustedEmitter;
import com.ruiyun.jvppeteer.bidi.entities.SupportedWebDriverCapabilities;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptHandler;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptHandlerType;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.BrowserContextOptions;
import com.ruiyun.jvppeteer.cdp.entities.DebugInfo;
import com.ruiyun.jvppeteer.cdp.entities.DownloadOptions;
import com.ruiyun.jvppeteer.cdp.entities.DownloadPolicy;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CdpConnection;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidiBrowser extends Browser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidiBrowser.class);
    private static final List<String> subscribeModules = new ArrayList<>();
    private static final List<String> subscribeCdpEvents = new ArrayList<>();
    private final Process process;
    private final Runnable closeCallback;
    private final BrowserCore browserCore;
    private final Viewport defaultViewport;
    private final CdpConnection cdpConnection;
    private final Map<UserContext, BidiBrowserContext> browserContexts = new WeakHashMap<>();
    private final TrustedEmitter<BrowserEvents> trustedEmitter = new TrustedEmitter<>();
    private final BidiBrowserTarget target = new BidiBrowserTarget(this);

    static {
        subscribeModules.add("browsingContext");
        subscribeModules.add("network");
        subscribeModules.add("log");
        subscribeModules.add("script");
        subscribeCdpEvents.add("goog:cdp.Debugger.scriptParsed");
        subscribeCdpEvents.add("goog:cdp.CSS.styleSheetAdded");
        subscribeCdpEvents.add("goog:cdp.Runtime.executionContextsCleared");
        subscribeCdpEvents.add("goog:cdp.Tracing.tracingComplete");
        subscribeCdpEvents.add("goog:cdp.Network.requestWillBeSent");
        subscribeCdpEvents.add("goog:cdp.Debugger.scriptParsed");
        subscribeCdpEvents.add("goog:cdp.Page.screencastFrame");
    }

    public static BidiBrowser create(Process process, Runnable closeCallback, BidiConnection connection, CdpConnection cdpConnection, Viewport defaultViewport, boolean acceptInsecureCerts, SupportedWebDriverCapabilities capabilities) throws JsonProcessingException {
        ObjectNode capabilitiesNode = Constant.OBJECTMAPPER.createObjectNode();
        if (Objects.nonNull(capabilities)) {
            capabilitiesNode.putPOJO("firstMatch", capabilities.getFirstMatch());
        }
        ObjectNode alwaysMatch;
        if (Objects.nonNull(capabilities)) {
            alwaysMatch = Constant.OBJECTMAPPER.valueToTree(capabilities.getAlwaysMatch());
        } else {
            alwaysMatch = Constant.OBJECTMAPPER.createObjectNode();
        }
        alwaysMatch.put("acceptInsecureCerts", acceptInsecureCerts);
        UserPromptHandler userPromptHandler = new UserPromptHandler();
        userPromptHandler.setDefault1(UserPromptHandlerType.Ignore);
        alwaysMatch.putPOJO("unhandledPromptBehavior", userPromptHandler);
        alwaysMatch.put("webSocketUrl", true);
        alwaysMatch.put("goog:prerenderingDisabled", true);
        capabilitiesNode.set("alwaysMatch", alwaysMatch);
        Session session = Session.from(connection, capabilitiesNode);
        boolean isFirefox = session.capabilities().getBrowserName().toLowerCase().contains("firefox");
        List<String> subscribes = new ArrayList<>(subscribeModules);
        if (!isFirefox) {
            subscribes.addAll(subscribeCdpEvents);
        }
        session.subscribe(subscribes, null);
        BidiBrowser browser = new BidiBrowser(session.browser, process, closeCallback, cdpConnection, defaultViewport);
        browser.initialize();
        return browser;
    }

    private BidiBrowser(BrowserCore browserCore, Process process, Runnable closeCallback, CdpConnection cdpConnection, Viewport defaultViewport) {
        super();
        this.process = process;
        this.closeCallback = closeCallback;
        this.browserCore = browserCore;
        this.defaultViewport = defaultViewport;
        this.cdpConnection = cdpConnection;
        this.trustedEmitter.pipeTo(this);
    }

    private void initialize() {
        for (UserContext userContext : this.browserCore.userContexts()) {
            this.createBrowserContext(userContext);
        }
        this.browserCore.once(BrowserCore.BrowserCoreEvent.disconnected, ignored ->{
            this.trustedEmitter.emit(BrowserEvents.Disconnected, true);
            this.trustedEmitter.removeAllListeners(null);
        });
    }

    private BrowserContext createBrowserContext(UserContext userContext) {
        BidiBrowserContext browserContext = BidiBrowserContext.from(this, userContext, this.defaultViewport);
        this.browserContexts.put(userContext, browserContext);
        browserContext.trustedEmitter().on(BrowserContextEvents.TargetCreated, (Consumer<Target>) target -> this.trustedEmitter.emit(BrowserEvents.TargetCreated, target));
        browserContext.trustedEmitter().on(BrowserContextEvents.TargetChanged, (Consumer<Target>) target -> this.trustedEmitter.emit(BrowserEvents.TargetChanged, target));
        browserContext.trustedEmitter().on(BrowserContextEvents.TargetDestroyed, (Consumer<Target>) target -> this.trustedEmitter.emit(BrowserEvents.TargetDestroyed, target));
        return browserContext;
    }

    public boolean cdpSupported() {
        return this.cdpConnection != null;
    }

    public CdpConnection cdpConnection() {
        return this.cdpConnection;
    }

    public Connection connection() {
        return this.browserCore.session().connection();
    }

    @Override
    public void close() throws Exception {
        if (this.connection().closed()) {
            return;
        }
        try {
            this.browserCore.close();
            Optional.ofNullable(this.closeCallback).ifPresent(Runnable::run);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        } finally {
            this.connection().dispose();
        }
    }

    @Override
    public Process process() {
        return this.process;
    }

    @Override
    public BrowserContext createBrowserContext(BrowserContextOptions options) {
        UserContext userContext = this.browserCore.createUserContext();
        return this.createBrowserContext(userContext);
    }

    @Override
    public List<BrowserContext> browserContexts() {
        return this.browserCore.userContexts().stream().map(this.browserContexts::get).collect(Collectors.toList());
    }

    @Override
    public BidiBrowserContext defaultBrowserContext() {
        return this.browserContexts.get(this.browserCore.defaultUserContext());
    }

    @Override
    public String wsEndpoint() {
        return this.connection().url();
    }

    @Override
    public Page newPage() {
        return this.defaultBrowserContext().newPage();
    }

    @Override
    public List<Target> targets() {
        List<Target> targets = new ArrayList<>();
        targets.add(this.target());
        targets.addAll(this.browserContexts().stream().flatMap(context -> context.targets().stream()).collect(Collectors.toList()));
        return targets;
    }

    @Override
    public Target target() {
        return this.target;
    }


    @Override
    public String version() throws JsonProcessingException {
        return this.browserName() +"/"+ this.browserVersion();
    }

    private String browserName() {
        return this.browserCore.session().capabilities().getBrowserName();
    }

    private String browserVersion() {
        return this.browserCore.session().capabilities().getBrowserVersion();
    }

    @Override
    public String userAgent() {
        return this.browserCore.session().capabilities().getUserAgent();
    }

    @Override
    public void disconnect() {
        try {
            this.browserCore.session().end();
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        } finally {
            this.connection().dispose();
        }
    }

    @Override
    public boolean connected() {
        return !this.browserCore.disconnected();
    }

    @Override
    public DebugInfo debugInfo() {
        return new DebugInfo(this.connection().getPendingProtocolErrors());
    }

    /**
     * 设置下载行为
     *
     * @param options 可选配置，可以设置下载的存放路径，是否接受下载事件，拒绝还是接受下载
     *                如果没有指定 browserContextId,则设置默认浏览器上下文的下载行为
     */
    @Override
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
        this.browserCore.session().send("Browser.setDownloadBehavior", params);
    }

    /**
     * 设置下载行为
     *
     * @param guid             下载的全局唯一标识符。
     * @param browserContextId BrowserContext 在其中执行操作。省略时，将使用默认浏览器上下文。
     */
    @Override
    public void cancelDownload(String guid, String browserContextId) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("guid", guid);
        params.put("browserContextId", browserContextId);
        this.browserCore.session().send("Browser.cancelDownload", params);
    }

}
