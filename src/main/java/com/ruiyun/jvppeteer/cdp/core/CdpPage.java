package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.BluetoothEmulation;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.entities.Binding;
import com.ruiyun.jvppeteer.cdp.entities.BindingPayload;
import com.ruiyun.jvppeteer.cdp.entities.CallFrame;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageLocation;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.Credentials;
import com.ruiyun.jvppeteer.cdp.entities.DeleteCookiesRequest;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.cdp.entities.GetMetricsResponse;
import com.ruiyun.jvppeteer.cdp.entities.GetNavigationHistoryResponse;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.cdp.entities.ImageType;
import com.ruiyun.jvppeteer.cdp.entities.LengthUnit;
import com.ruiyun.jvppeteer.cdp.entities.MediaFeature;
import com.ruiyun.jvppeteer.cdp.entities.Metric;
import com.ruiyun.jvppeteer.cdp.entities.Metrics;
import com.ruiyun.jvppeteer.cdp.entities.NavigationEntry;
import com.ruiyun.jvppeteer.cdp.entities.NetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.cdp.entities.PDFMargin;
import com.ruiyun.jvppeteer.cdp.entities.PDFOptions;
import com.ruiyun.jvppeteer.cdp.entities.PageMetrics;
import com.ruiyun.jvppeteer.cdp.entities.PaperFormats;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotClip;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotOptions;
import com.ruiyun.jvppeteer.cdp.entities.StackTrace;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.cdp.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.EntryAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.cdp.events.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.cdp.events.JavascriptDialogOpeningEvent;
import com.ruiyun.jvppeteer.cdp.events.MetricsEvent;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.ReloadOptions;
import com.ruiyun.jvppeteer.common.UserAgentOptions;
import com.ruiyun.jvppeteer.common.WaitForOptions;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;


import static com.ruiyun.jvppeteer.common.Constant.ABOUT_BLANK;
import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.STREAM;
import static com.ruiyun.jvppeteer.common.Constant.supportedMetrics;
import static com.ruiyun.jvppeteer.util.Helper.convertCookiesPartitionKeyFromPuppeteerToCdp;
import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class CdpPage extends Page {

    private volatile boolean closed = false;
    private final TargetManager targetManager;
    private final CdpBluetoothEmulation cdpBluetoothEmulation;
    private volatile CDPSession primaryTargetClient;
    private CdpTarget primaryTarget;
    private final CDPSession tabTargetClient;
    private final CdpTarget tabTarget;
    private final CdpKeyboard keyboard;
    private final CdpMouse mouse;
    private final CdpTouchscreen touchscreen;
    private FrameManager frameManager;
    private final EmulationManager emulationManager;
    private final Tracing tracing;
    private final Map<String, Binding> bindings = new HashMap<>();
    private final Map<String, String> exposedFunctions = new HashMap<>();
    private final Coverage coverage;
    private Viewport viewport;
    private final Map<String, CdpWebWorker> workers = new HashMap<>();
    private final Set<AwaitableResult<FileChooser>> fileChooserResults = new HashSet<>();
    private final AwaitableResult<TargetCloseException> sessionCloseResult = AwaitableResult.create();
    private boolean serviceWorkerBypassed = false;
    private boolean userDragInterceptionEnabled = false;

    public CdpPage(CDPSession client, CdpTarget target) {
        super();
        this.primaryTargetClient = client;
        this.tabTargetClient = client.parentSession();
        Objects.requireNonNull(this.tabTargetClient, "Tab target session is not defined.");
        this.tabTarget = ((CdpCDPSession) this.tabTargetClient).getTarget();
        Objects.requireNonNull(this.tabTarget, "Tab target is not defined.");
        this.primaryTarget = target;
        this.targetManager = target.targetManager();
        this.keyboard = new CdpKeyboard(client);
        this.mouse = new CdpMouse(client, this.keyboard);
        this.touchscreen = new CdpTouchscreen(client, this.keyboard);
        this.frameManager = new FrameManager(client, this, this._timeoutSettings);
        this.emulationManager = new EmulationManager(client);
        this.tracing = new Tracing(client);
        this.coverage = new Coverage(client);
        this.viewport = null;
        this.cdpBluetoothEmulation = new CdpBluetoothEmulation(this.primaryTargetClient.connection());
        Map<FrameManager.FrameManagerEvent, Consumer<?>> frameManagerHandlers = Collections.unmodifiableMap(new HashMap<FrameManager.FrameManagerEvent, Consumer<?>>() {{
            put(FrameManager.FrameManagerEvent.FrameAttached, ((Consumer<CdpFrame>) (frame) -> CdpPage.this.emit(PageEvents.FrameAttached, frame)));
            put(FrameManager.FrameManagerEvent.FrameDetached, ((Consumer<CdpFrame>) (frame) -> CdpPage.this.emit(PageEvents.FrameDetached, frame)));
            put(FrameManager.FrameManagerEvent.FrameNavigated, ((Consumer<CdpFrame>) (frame) -> CdpPage.this.emit(PageEvents.FrameNavigated, frame)));
            put(FrameManager.FrameManagerEvent.ConsoleApiCalled, (Consumer<Object[]>) arg -> CdpPage.this.onConsoleAPI((IsolatedWorld) arg[0], (ConsoleAPICalledEvent) arg[1]));
            put(FrameManager.FrameManagerEvent.BindingCalled, (Consumer<List<Object>>) arg -> CdpPage.this.onBindingCalled((IsolatedWorld) arg.get(0), (BindingCalledEvent) arg.get(1)));
        }});
        frameManagerHandlers.forEach(this.frameManager::on);

        Map<NetworkManager.NetworkManagerEvent, Consumer<?>> networkManagerHandlers = Collections.unmodifiableMap(new HashMap<NetworkManager.NetworkManagerEvent, Consumer<?>>() {{
            put(NetworkManager.NetworkManagerEvent.Request, ((Consumer<CdpRequest>) (request) -> CdpPage.this.emit(PageEvents.Request, request)));
            put(NetworkManager.NetworkManagerEvent.RequestServedFromCache, ((Consumer<CdpRequest>) (request) -> CdpPage.this.emit(PageEvents.RequestServedFromCache, request)));
            put(NetworkManager.NetworkManagerEvent.Response, ((Consumer<CdpResponse>) (response) -> CdpPage.this.emit(PageEvents.Response, response)));
            put(NetworkManager.NetworkManagerEvent.RequestFailed, ((Consumer<CdpRequest>) (request) -> CdpPage.this.emit(PageEvents.RequestFailed, request)));
            put(NetworkManager.NetworkManagerEvent.RequestFinished, ((Consumer<CdpRequest>) (request) -> CdpPage.this.emit(PageEvents.RequestFinished, request)));
        }});
        networkManagerHandlers.forEach((key, value) -> this.frameManager.networkManager().on(key, value));

        this.tabTargetClient.on(ConnectionEvents.CDPSession_Swapped, (Consumer<CdpCDPSession>) CdpPage.this::onActivation);
        this.tabTargetClient.on(ConnectionEvents.CDPSession_Ready, (Consumer<CdpCDPSession>) CdpPage.this::onSecondaryTarget);

        Consumer<CdpTarget> onDetachedFromTarget = (cdpTarget) -> {
            if (cdpTarget.session() == null) {
                return;
            }
            String sessionId = cdpTarget.session().id();
            CdpWebWorker webWorker = CdpPage.this.workers.get(sessionId);
            if (webWorker == null) {
                return;
            }
            CdpPage.this.workers.remove(sessionId);
            CdpPage.this.emit(PageEvents.WorkerDestroyed, webWorker);
        };
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetGone, onDetachedFromTarget);
        this.tabTarget.setOnCloseRunner(() -> {
            CdpPage.this.targetManager.off(TargetManager.TargetManagerEvent.TargetGone, onDetachedFromTarget);
            CdpPage.this.emit(PageEvents.Close, true);
            CdpPage.this.closed = true;
        });

        this.setupPrimaryTargetListeners();
        this.attachExistingTargets();
    }

    private void attachExistingTargets() {
        List<CdpTarget> childTargets = this.targetManager.getChildTargets(this.primaryTarget);
        List<CdpTarget> queue = new ArrayList<>(childTargets);
        int idx = 0;
        while (idx < queue.size()) {
            CdpTarget next = queue.get(idx);
            idx++;
            CdpCDPSession session = (CdpCDPSession) next.session();
            if (Objects.nonNull(session)) {
                this.onAttachedToTarget.accept(session);
            }
            queue.addAll(this.targetManager.getChildTargets(next));
        }
    }

    private void onActivation(CDPSession newSession) {
        this.primaryTargetClient = newSession;
        this.primaryTarget = ((CdpCDPSession) this.primaryTargetClient).getTarget();
        Objects.requireNonNull(this.primaryTarget, "Missing target on swap");
        this.keyboard.updateClient(newSession);
        this.mouse.updateClient(newSession);
        this.touchscreen.updateClient(newSession);
        this.emulationManager.updateClient(newSession);
        this.tracing.updateClient(newSession);
        this.coverage.updateClient(newSession);
        this.frameManager.swapFrameTree(newSession);
        this.setupPrimaryTargetListeners();
    }

    /**
     * 为主要目标设置侦听器。在导航到预置页面期间，主要目标可能会更改。
     */
    private void setupPrimaryTargetListeners() {
        Map<ConnectionEvents, Consumer<?>> sessionHandlers = Collections.unmodifiableMap(new HashMap<ConnectionEvents, Consumer<?>>() {{
            put(ConnectionEvents.CDPSession_Ready, (session) -> CdpPage.this.onAttachedToTarget.accept((CdpCDPSession) session));
            put(ConnectionEvents.CDPSession_Disconnected, ((ignore) -> sessionCloseResult.onSuccess(new TargetCloseException("Target closed"))));
            put(ConnectionEvents.Page_domContentEventFired, ((ignore) -> CdpPage.this.emit(PageEvents.Domcontentloaded, true)));
            put(ConnectionEvents.Page_loadEventFired, ((ignore) -> CdpPage.this.emit(PageEvents.Load, true)));
            put(ConnectionEvents.Page_javascriptDialogOpening, ((Consumer<JavascriptDialogOpeningEvent>) CdpPage.this::onDialog));
            put(ConnectionEvents.Runtime_exceptionThrown, (Consumer<ExceptionThrownEvent>) CdpPage.this::handleException);
            put(ConnectionEvents.Inspector_targetCrashed, (ignore) -> CdpPage.this.onTargetCrashed());
            put(ConnectionEvents.Performance_metrics, (Consumer<MetricsEvent>) CdpPage.this::emitMetrics);
            put(ConnectionEvents.Log_entryAdded, (Consumer<EntryAddedEvent>) CdpPage.this::onLogEntryAdded);
            put(ConnectionEvents.Page_fileChooserOpened, (Consumer<FileChooserOpenedEvent>) CdpPage.this::onFileChooser);
        }});
        sessionHandlers.forEach((eventName, handler) -> this.primaryTargetClient.on(eventName, handler));
    }

    private void onSecondaryTarget(CdpCDPSession session) {
        if (!"prerender".equals(session.getTarget().subtype())) {
            return;
        }
        try {
            this.frameManager.registerSpeculativeSession(session);
        } catch (Exception e) {
            LOGGER.error("frameManager registerSpeculativeSession error: ", e);
        }
        try {
            this.emulationManager.registerSpeculativeSession(session);
        } catch (Exception e) {
            LOGGER.error("emulationManager registerSpeculativeSession error: ", e);
        }
    }

    /**
     * 这里是WebSocketConnectReadThread 线程执行的方法，不能暂停！！
     */
    private final Consumer<CdpCDPSession> onAttachedToTarget = (session) -> {
        this.frameManager.onAttachedToTarget(session.getTarget());
        if ("worker".equals(session.getTarget().getTargetInfo().getType())) {
            CdpWebWorker webWorker = new CdpWebWorker(session, session.getTarget().url(), session.getTarget().getTargetId(), session.getTarget().type(), CdpPage.this::addConsoleMessage, CdpPage.this::handleException, this.frameManager.networkManager());
            this.workers.put(session.id(), webWorker);
            this.emit(PageEvents.WorkerCreated, webWorker);
        }
        session.on(ConnectionEvents.CDPSession_Ready, this.onAttachedToTarget);
    };

    private void initialize() {
        try {
            frameManager.initialize(this.primaryTargetClient, null);
            Map<String, Object> params = new HashMap<>();
            this.primaryTargetClient.send("Performance.enable", params);
            this.primaryTargetClient.send("Log.enable", params);
        } catch (Exception e) {
            if (e instanceof ProtocolException) {
                LOGGER.error("initialize error: ", e);
            } else {
                throw e;
            }
        }
    }

    private void onFileChooser(FileChooserOpenedEvent event) {
        if (this.fileChooserResults.isEmpty()) {
            return;
        }
        CdpFrame frame = this.frameManager.frame(event.getFrameId());
        Objects.requireNonNull(frame, "This should never happen.");
        IsolatedWorld mainWorld = frame.worlds().get(MAIN_WORLD);
        ElementHandle handle = null;
        try {
            handle = mainWorld.adoptBackendNode(event.getBackendNodeId()).asElement();
        } catch (JsonProcessingException e) {
            throwError(e);
        }
        FileChooser fileChooser = new FileChooser(handle, !Objects.equals(event.getMode(), "selectSingle"));
        for (AwaitableResult<FileChooser> subject : this.fileChooserResults) {
            subject.onSuccess(fileChooser);
        }
        this.fileChooserResults.clear();
    }

    public CDPSession client() {
        return this.primaryTargetClient;
    }

    public boolean isServiceWorkerBypassed() {
        return this.serviceWorkerBypassed;
    }

    /**
     * 我们不再支持拦截拖动有效负载。使用 ElementHandle 上的新拖动 API 进行拖动（或仅使用 Page.mouse）。
     *
     * @return 如果拖动事件被拦截，则为 true，否则为 false。
     */
    @Deprecated
    public boolean isDragInterceptionEnabled() {
        return this.userDragInterceptionEnabled;
    }

    public boolean isJavaScriptEnabled() {
        return this.emulationManager.javascriptEnabled();
    }

    public AwaitableResult<FileChooser> fileChooserWaitFor() {
        AwaitableResult<FileChooser> result = AwaitableResult.create();
        boolean needsEnable = this.fileChooserResults.isEmpty();
        this.fileChooserResults.add(result);
        if (needsEnable) {
            Map<String, Object> params = new HashMap<>();
            params.put("enabled", true);
            this.primaryTargetClient.send("Page.setInterceptFileChooserDialog", params);
        }
        return result;
    }

    public void setGeolocation(GeolocationOptions options) {
        super.setGeolocation(options);
        this.emulationManager.setGeolocation(options);
    }

    public Target target() {
        return this.primaryTarget;
    }

    public Browser browser() {
        return this.primaryTarget.browser();
    }

    public BrowserContext browserContext() {
        return this.primaryTarget.browserContext();
    }

    private void onTargetCrashed() {
        this.emit(PageEvents.Error, new JvppeteerException("Page crashed!"));
    }

    private void onLogEntryAdded(EntryAddedEvent event) {
        if (ValidateUtil.isNotEmpty(event.getEntry().getArgs()))
            event.getEntry().getArgs().forEach(arg -> Helper.releaseObject(this.primaryTargetClient, arg));
        if (!"worker".equals(event.getEntry().getSource())) {
            List<ConsoleMessageLocation> locations = new ArrayList<>();
            locations.add(new ConsoleMessageLocation(event.getEntry().getUrl(), event.getEntry().getLineNumber()));
            this.emit(PageEvents.Console, new ConsoleMessage(convertConsoleMessageLevel(event.getEntry().getLevel()), event.getEntry().getText(), Collections.emptyList(), locations, null, event.getEntry().getStackTrace()));
        }
    }

    public CdpFrame mainFrame() {
        return this.frameManager.mainFrame();
    }

    public CdpKeyboard keyboard() {
        return this.keyboard;
    }

    public CdpTouchscreen touchscreen() {
        return this.touchscreen;
    }

    public Coverage coverage() {
        return this.coverage;
    }

    public Tracing tracing() {
        return this.tracing;
    }

    public List<CdpFrame> frames() {
        return this.frameManager.frames();
    }

    public List<WebWorker> workers() {
        return new ArrayList<>(this.workers.values());
    }

    public void setRequestInterception(boolean value) {
        this.frameManager.networkManager().setRequestInterception(value);
    }

    public void setBypassServiceWorker(boolean bypass) {
        this.serviceWorkerBypassed = bypass;
        Map<String, Object> params = new HashMap<>();
        params.put("bypass", bypass);
        this.primaryTargetClient.send("Network.setBypassServiceWorker", params);
    }

    @Deprecated
    public void setDragInterception(boolean enabled) {
        this.userDragInterceptionEnabled = enabled;
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        this.primaryTargetClient.send("Input.setInterceptDrags", params);
    }

    public void setOfflineMode(boolean enabled) {
        this.frameManager.networkManager().setOfflineMode(enabled);
    }

    public void emulateNetworkConditions(NetworkConditions networkConditions) {
        this.frameManager.networkManager().emulateNetworkConditions(networkConditions);
    }

    public void setDefaultNavigationTimeout(int timeout) {
        this._timeoutSettings.setDefaultNavigationTimeout(timeout);
    }

    public int getDefaultTimeout() {
        return this._timeoutSettings.timeout();
    }

    @Override
    public int getDefaultNavigationTimeout() {
        return this._timeoutSettings.navigationTimeout();
    }

    /**
     * 此方法会改变下面几个方法的默认30秒等待时间：
     * ${@link CdpPage#goTo(String)}
     * ${@link CdpPage#goTo(String, GoToOptions)}
     * ${@link CdpPage#goBack(WaitForOptions)}
     * ${@link CdpPage#goForward(WaitForOptions)}
     * ${@link CdpPage#reload(ReloadOptions)}
     * ${@link CdpPage#waitForNavigation()}
     *
     * @param timeout 超时时间
     */

    public void setDefaultTimeout(int timeout) {
        this._timeoutSettings.setDefaultTimeout(timeout);
    }

    public JSHandle queryObjects(JSHandle prototypeHandle) throws JsonProcessingException {
        ValidateUtil.assertArg(!prototypeHandle.disposed(), "Prototype JSHandle is disposed!");
        ValidateUtil.assertArg(StringUtil.isNotEmpty(prototypeHandle.remoteObject().getObjectId()), "Prototype JSHandle must not be referencing primitive value");
        Map<String, Object> params = new HashMap<>();
        params.put("prototypeObjectId", prototypeHandle.remoteObject().getObjectId());
        JsonNode response = this.mainFrame().client().send("Runtime.queryObjects", params);
        return this.mainFrame().mainRealm().createJSHandle(OBJECTMAPPER.treeToValue(response.get("objects"), RemoteObject.class));
    }

    /**
     * 返回当前页面的cookies
     *
     * @return cookies
     */
    public List<Cookie> cookies() {
        return this.cookies(this.url());
    }

    /**
     * 根据提供的URL列表获取Cookies
     * <p>
     * 如果未指定 URL，则此方法返回当前页面 URL 的 cookie。如果指定了 URL，则仅返回这些 URL 的 cookie。
     * <p>
     *
     * @param urls URL列表，如果没有提供或为null，将使用当前页面的URL
     * @return Cookie对象列表，表示请求的cookies
     */
    public List<Cookie> cookies(String... urls) {
        if (urls == null || urls.length == 0) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("urls", urls);
        JsonNode result = this.primaryTargetClient.send("Network.getCookies", params);
        JsonNode cookiesNode = result.get("cookies");
        Iterator<JsonNode> elements = cookiesNode.elements();
        List<Cookie> cookies = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode cookieNode = elements.next();
            Cookie cookie = OBJECTMAPPER.convertValue(cookieNode, Cookie.class);
            JsonNode partitionKey = cookieNode.path("partitionKey");
            if (!partitionKey.isMissingNode()) {
                cookie.setPartitionKey(partitionKey.get("topLevelSite"));
            } else {
                cookie.setPartitionKey(null);
            }
            cookies.add(cookie);
        }
        return cookies;
    }

    public void deleteCookie(DeleteCookiesRequest... cookies) {
        if (cookies == null || cookies.length == 0) {
            return;
        }
        String pageURL = this.url();
        for (DeleteCookiesRequest cookie : cookies) {
            cookie.setPartitionKey(convertCookiesPartitionKeyFromPuppeteerToCdp(cookie.getPartitionKey()));
            if (StringUtil.isEmpty(cookie.getUrl()) && pageURL.startsWith("http")) {
                cookie.setUrl(pageURL);
            }
            this.primaryTargetClient.send("Network.deleteCookies", cookie);
            if (pageURL.startsWith("http") && Objects.isNull(cookie.getPartitionKey())) {
                URI uri = URI.create(pageURL);
                ObjectNode partitionKey = OBJECTMAPPER.createObjectNode();
                partitionKey.put("topLevelSite", getOrigin(uri));
                partitionKey.put("hasCrossSiteAncestor", false);
                cookie.setPartitionKey(partitionKey);
                this.primaryTargetClient.send("Network.deleteCookies", cookie);
            }
        }
    }

    private String getOrigin(URI uri) {
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            return scheme + "://" + uri.getHost();
        } else {
            return getOrigin(URI.create(uri.getSchemeSpecificPart()));
        }
    }

    public void setCookie(CookieParam... cookies) {
        if (cookies == null || cookies.length == 0) {
            return;
        }
        String pageURL = this.url();
        boolean startsWithHTTP = pageURL.startsWith("http");
        ArrayList<CookieParam> cookieParams = new ArrayList<>(Arrays.asList(cookies));
        cookieParams.replaceAll(cookie -> {
            if (StringUtil.isEmpty(cookie.getUrl()) && startsWithHTTP) cookie.setUrl(pageURL);
            ValidateUtil.assertArg(!ABOUT_BLANK.equals(cookie.getUrl()), "Blank page can not have cookie " + cookie.getName());
            if (StringUtil.isNotEmpty(cookie.getUrl())) {
                ValidateUtil.assertArg(!cookie.getUrl().startsWith("data:"), "Data URL page can not have cookie " + cookie.getName());
            }
            return cookie;
        });
        List<DeleteCookiesRequest> deleteCookiesParameters = new ArrayList<>();
        for (CookieParam cookie : cookieParams) {
            deleteCookiesParameters.add(new DeleteCookiesRequest(cookie.getName(), cookie.getUrl(), cookie.getDomain(), cookie.getPath()));
        }
        this.deleteCookie(deleteCookiesParameters.toArray(new DeleteCookiesRequest[0]));
        Map<String, Object> params = new HashMap<>();
        params.put("cookies", cookieParams);
        this.primaryTargetClient.send("Network.setCookies", params);
    }

    public void exposeFunction(String name, BindingFunction pptrFunction) throws EvaluateException, JsonProcessingException {
        ValidateUtil.assertArg(!this.bindings.containsKey(name), MessageFormat.format("Failed to add page binding with name {0}: window[{1}] already exists!", name, name));
        String source = Helper.evaluationString(addPageBinding(), "exposedFun", name, CDP_BINDING_PREFIX);
        Binding binding = new Binding(name, pptrFunction, source);
        this.bindings.put(name, binding);
        NewDocumentScriptEvaluation response = this.frameManager.evaluateOnNewDocument(source);
        this.frameManager.addExposedFunctionBinding(binding);
        this.exposedFunctions.put(name, response.getIdentifier());
    }

    public void removeExposedFunction(String name) throws JsonProcessingException {
        String exposedFunctionId = this.exposedFunctions.get(name);
        if (exposedFunctionId == null) {
            throw new JvppeteerException("Failed to remove page binding with name '" + name + "' window['" + name + "'] does not exists!");
        }
        this.exposedFunctions.remove(name);
        Binding binging = this.bindings.remove(name);
        this.frameManager.removeScriptToEvaluateOnNewDocument(exposedFunctionId);
        this.frameManager.removeExposedFunctionBinding(binging);
    }

    public void authenticate(Credentials credentials) {
        this.frameManager.networkManager().authenticate(credentials);
    }

    public void setExtraHTTPHeaders(Map<String, String> headers) {
        this.frameManager.networkManager().setExtraHTTPHeaders(headers);
    }

    public void setUserAgent(UserAgentOptions options) {
        if (Objects.isNull(options.getUserAgent())) {
            options.setUserAgent(this.browser().userAgent());
        }
        this.frameManager.networkManager().setUserAgent(options);
    }

    public Metrics metrics() throws JsonProcessingException {
        GetMetricsResponse response = OBJECTMAPPER.treeToValue(this.primaryTargetClient.send("Performance.getMetrics"), GetMetricsResponse.class);
        return this.buildMetricsObject(response.getMetrics());
    }

    private void emitMetrics(MetricsEvent event) {
        PageMetrics pageMetrics = new PageMetrics();
        Metrics metrics = this.buildMetricsObject(event.getMetrics());
        pageMetrics.setMetrics(metrics);
        pageMetrics.setTitle(event.getTitle());
        this.emit(PageEvents.Metrics, pageMetrics);
    }

    private Metrics buildMetricsObject(List<Metric> metrics) {
        Metrics result = new Metrics();
        if (ValidateUtil.isNotEmpty(metrics)) {
            for (Metric metric : metrics) {
                if (supportedMetrics.contains(metric.getName())) {
                    switch (metric.getName()) {
                        case "Timestamp":
                            result.setTimestamp(metric.getValue());
                            break;
                        case "Documents":
                            result.setDocuments(metric.getValue());
                            break;
                        case "Frames":
                            result.setFrames(metric.getValue());
                            break;
                        case "JSEventListeners":
                            result.setJSEventListeners(metric.getValue());
                            break;
                        case "Nodes":
                            result.setNodes(metric.getValue());
                            break;
                        case "LayoutCount":
                            result.setLayoutCount(metric.getValue());
                            break;
                        case "RecalcStyleCount":
                            result.setRecalcStyleCount(metric.getValue());
                            break;
                        case "LayoutDuration":
                            result.setLayoutDuration(metric.getValue());
                            break;
                        case "RecalcStyleDuration":
                            result.setRecalcStyleDuration(metric.getValue());
                            break;
                        case "ScriptDuration":
                            result.setScriptDuration(metric.getValue());
                            break;
                        case "TaskDuration":
                            result.setTaskDuration(metric.getValue());
                            break;
                        case "JSHeapUsedSize":
                            result.setJSHeapUsedSize(metric.getValue());
                            break;
                        case "JSHeapTotalSize":
                            result.setJSHeapTotalSize(metric.getValue());
                            break;
                    }
                }
            }
        }
        return result;
    }

    private void handleException(ExceptionThrownEvent event) {
        this.emit(PageEvents.PageError, Helper.createClientError(event.getExceptionDetails()));
    }

    private void onConsoleAPI(IsolatedWorld world, ConsoleAPICalledEvent event) {
        List<JSHandle> values = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(event.getArgs())) {
            for (int i = 0; i < event.getArgs().size(); i++) {
                RemoteObject arg = event.getArgs().get(i);
                values.add(world.createJSHandle(arg));
            }
        }
        this.addConsoleMessage(convertConsoleMessageLevel(event.getType()), values, event.getStackTrace());
    }

    //不能阻塞 WebSocketConnectReadThread
    private void addConsoleMessage(ConsoleMessageType type, List<JSHandle> args, StackTrace stackTrace) {
        if (this.listenerCount(PageEvents.Console) == 0) {
            args.forEach(JSHandle::dispose);
            return;
        }
        List<String> textTokens = new ArrayList<>();
        for (JSHandle arg : args) {
            RemoteObject remoteObject = arg.remoteObject();
            if (StringUtil.isNotEmpty(remoteObject.getObjectId())) {
                textTokens.add(arg.toString());
            } else {
                textTokens.add(Helper.valueFromRemoteObject(remoteObject) + "");
            }
        }
        List<ConsoleMessageLocation> stackTraceLocations = new ArrayList<>();
        if (stackTrace != null) {
            if (ValidateUtil.isNotEmpty(stackTrace.getCallFrames())) {
                for (CallFrame callFrame : stackTrace.getCallFrames()) {
                    stackTraceLocations.add(new ConsoleMessageLocation(callFrame.getUrl(), callFrame.getLineNumber(), callFrame.getColumnNumber()));
                }
            }
        }
        ConsoleMessage message = new ConsoleMessage(type, String.join(" ", textTokens), args, stackTraceLocations, null, stackTrace);
        this.emit(PageEvents.Console, message);
    }

    @Override
    public Response reload(ReloadOptions options) {
        options.setIgnoreSameDocumentNavigation(true);
        return this.waitForNavigation(options, () -> {
            Map<String, Object> params = ParamsFactory.create();
            params.put("ignoreCache", !Objects.isNull(options.getIgnoreCache()) && options.getIgnoreCache());
            this.primaryTargetClient.send("Page.reload", params, null, false);
        });
    }

    public CDPSession createCDPSession() {
        return this.target().createCDPSession();
    }

    /**
     * 导航到页面历史的前一个页面
     * <p>
     * options 导航配置，可选值：
     * <p>otimeout  跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过page.setDefaultNavigationTimeout(timeout)方法修改默认值
     * <p>owaitUntil 满足什么条件认为页面跳转完成，默认是load事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     * <blockquote><pre>
     * oload - 页面的load事件触发时
     * odomcontentloaded - 页面的DOMContentLoaded事件触发时
     * onetworkidle0 - 不再有网络连接时触发（至少500毫秒后）
     * onetworkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     * </pre></blockquote>
     *
     * @param options 见上面注释
     * @return 响应
     * @throws JsonProcessingException 如果JSON解析失败
     */
    public Response goBack(WaitForOptions options) throws JsonProcessingException {
        return this.go(-1, options);
    }

    /**
     * 导航到页面历史的后一个页面。
     * options 导航配置，可选值：
     * <p>otimeout  跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过page.setDefaultNavigationTimeout(timeout)方法修改默认值
     * <p>owaitUntil 满足什么条件认为页面跳转完成，默认是load事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     * <blockquote><pre>
     * oload - 页面的load事件触发时
     * odomcontentloaded - 页面的DOMContentLoaded事件触发时
     * onetworkidle0 - 不再有网络连接时触发（至少500毫秒后）
     * onetworkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     * </pre></blockquote>
     *
     * @param options 等待选项
     */
    public Response goForward(WaitForOptions options) throws JsonProcessingException {
        return this.go(+1, options);
    }

    private Response go(int delta, WaitForOptions options) throws JsonProcessingException {
        JsonNode historyNode = this.primaryTargetClient.send("Page.getNavigationHistory");
        GetNavigationHistoryResponse history = OBJECTMAPPER.treeToValue(historyNode, GetNavigationHistoryResponse.class);
        if ((history.getCurrentIndex() + delta) < 0)
            throw new JvppeteerException("History entry to navigate to not found.");
        NavigationEntry entry = history.getEntries().get(history.getCurrentIndex() + delta);
        if (Objects.isNull(entry)) throw new JvppeteerException("History entry to navigate to not found.");
        Map<String, Object> params = new HashMap<>();
        params.put("entryId", entry.getId());
        this.primaryTargetClient.send("Page.navigateToHistoryEntry", params, null, false);
        return this.waitForNavigation(options);
    }

    public void bringToFront() {
        this.primaryTargetClient.send("Page.bringToFront");
    }

    public void setJavaScriptEnabled(boolean enabled) {
        this.emulationManager.setJavaScriptEnabled(enabled);
    }

    public void setBypassCSP(boolean enabled) {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        this.primaryTargetClient.send("Page.setBypassCSP", params);
    }

    public void emulateMediaType(MediaType type) {
        this.emulationManager.emulateMediaType(type);
    }

    public void emulateCPUThrottling(double factor) {
        this.emulationManager.emulateCPUThrottling(factor);
    }

    public void emulateMediaFeatures(List<MediaFeature> features) {
        this.emulationManager.emulateMediaFeatures(features);
    }

    public void emulateTimezone(String timezoneId) {
        this.emulationManager.emulateTimezone(timezoneId);
    }

    /**
     * 模拟空闲状态。如果未设置参数，则清除空闲状态模拟
     *
     * @param overrides 模拟空闲状态。如果未设置，则清除空闲覆盖
     */
    public void emulateIdleState(IdleOverridesState.Overrides overrides) {
        this.emulationManager.emulateIdleState(overrides);
    }

    public void emulateVisionDeficiency(VisionDeficiency type) {
        this.emulationManager.emulateVisionDeficiency(type);
    }

    public void setViewport(Viewport viewport) {
        boolean needsReload = this.emulationManager.emulateViewport(viewport);
        this.viewport = viewport;
        if (needsReload) this.reload();
    }

    public Viewport viewport() {
        return this.viewport;
    }

    public NewDocumentScriptEvaluation evaluateOnNewDocument(String pptrFunction, EvaluateType type, Object... args) throws JsonProcessingException {
        String source;
        if (Objects.equals(EvaluateType.STRING, type)) {
            ValidateUtil.assertArg(args.length == 0, "Cannot evaluate a string with arguments");
            source = pptrFunction;
        } else {
            source = Helper.evaluationString(pptrFunction, args);
        }
        return this.frameManager.evaluateOnNewDocument(source);
    }

    public void removeScriptToEvaluateOnNewDocument(String identifier) {
        Map<String, Object> identifierKeys = new HashMap<>();
        identifierKeys.put("identifier", identifier);
        this.primaryTargetClient.send("Page.removeScriptToEvaluateOnNewDocument", identifierKeys);
    }

    public void setCacheEnabled(boolean enabled) {
        this.frameManager.networkManager().setCacheEnabled(enabled);
    }

    protected String _screenshot(ScreenshotOptions options) {
        Map<String, Object> params = ParamsFactory.create();
        try {
            if (options.getOmitBackground() && (ImageType.PNG.equals(options.getType()) || ImageType.WEBP.equals(options.getType()))) {
                this.emulationManager.setTransparentBackgroundColor();
            }
            ScreenshotClip clip = options.getClip();
            if (clip != null && !options.getCaptureBeyondViewport()) {
                Object response = this.mainFrame().isolatedRealm().evaluate("() => {\n" + "          const {\n" + "            height,\n" + "            pageLeft: x,\n" + "            pageTop: y,\n" + "            width,\n" + "          } = window.visualViewport;\n" + "          return {x, y, height, width};\n" + "        }", null);
                JsonNode responseNode = OBJECTMAPPER.readTree(OBJECTMAPPER.writeValueAsString(response));
                clip = getIntersectionRect(clip, responseNode);
            }
            params.put("format", options.getType().toString());
            if (options.getOptimizeForSpeed()) {
                params.put("optimizeForSpeed", options.getOptimizeForSpeed());
            }
            if (options.getQuality() != null) {
                params.put("quality", Math.round(options.getQuality()));
            }
            if (clip != null) {
                params.put("clip", clip);
            }
            if (!options.getFromSurface()) {
                params.put("fromSurface", options.getFromSurface());
            }
            params.put("captureBeyondViewport", options.getCaptureBeyondViewport());
            JsonNode result = this.primaryTargetClient.send("Page.captureScreenshot", params);
            String data = result.get(Constant.DATA).asText();
            byte[] buffer = Base64.getDecoder().decode(data);
            if (StringUtil.isNotEmpty(options.getPath())) {
                Files.write(Paths.get(options.getPath()), buffer, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
            return data;
        } catch (Exception var) {
            LOGGER.error("_screenshot error: ", var);
        } finally {
            if (options.getOmitBackground() && (ImageType.PNG.equals(options.getType()) || ImageType.WEBP.equals(options.getType()))) {
                this.emulationManager.resetDefaultBackgroundColor();
            }
        }
        return null;
    }

    @Override
    public void emulateFocusedPage(boolean enabled) {
        this.emulationManager.emulateFocus(enabled);
    }

    /**
     * @see <a href="https://w3c.github.io/webdriver-bidi/#rectangle-intersection">href="https://w3c.github.io/webdriver-bidi/#rectangle-intersection</a>
     */
    private ScreenshotClip getIntersectionRect(ScreenshotClip clip, JsonNode viewport) {
        double x = Math.max(clip.getX(), viewport.get("x").asDouble());
        double y = Math.max(clip.getY(), viewport.get("y").asDouble());
        return new ScreenshotClip(x, y, Math.max(Math.min(clip.getX() + clip.getWidth(), viewport.get("x").asDouble() + viewport.get("width").asDouble()) - x, 0), Math.max(Math.min(clip.getY() + clip.getHeight(), viewport.get("y").asDouble() + viewport.get("height").asDouble()) - y, 0), 1);
    }

    public byte[] pdf(PDFOptions options, LengthUnit lengthUnit) throws IOException {
        double paperWidth = 8.5;
        double paperHeight = 11;
        if (options.getFormat() != null) {
            PaperFormats format = options.getFormat();
            paperWidth = format.getWidth();
            paperHeight = format.getHeight();
        } else {
            Double width = convertPrintParameterToInches(options.getWidth(), lengthUnit);
            if (width != null) {
                paperWidth = width;
            }
            Double height = convertPrintParameterToInches(options.getHeight(), lengthUnit);
            if (height != null) {
                paperHeight = height;
            }
        }
        PDFMargin margin = options.getMargin();
        Number marginTop, marginLeft, marginBottom, marginRight;
        if ((marginTop = convertPrintParameterToInches(margin.getTop(), lengthUnit)) == null) {
            marginTop = 0;
        }
        if ((marginLeft = convertPrintParameterToInches(margin.getLeft(), lengthUnit)) == null) {
            marginLeft = 0;
        }
        if ((marginBottom = convertPrintParameterToInches(margin.getBottom(), lengthUnit)) == null) {
            marginBottom = 0;
        }
        if ((marginRight = convertPrintParameterToInches(margin.getRight(), lengthUnit)) == null) {
            marginRight = 0;
        }
        if (options.getOutline()) {
            options.setTagged(true);
        }
        if (options.getOmitBackground()) {
            this.emulationManager.setTransparentBackgroundColor();
        }
        if (options.getWaitForFonts()) {
            this.mainFrame().evaluate("() => { return document.fonts.ready;}");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("transferMode", "ReturnAsStream");
        params.put("landscape", options.getLandscape());
        params.put("displayHeaderFooter", options.getDisplayHeaderFooter());
        params.put("headerTemplate", options.getHeaderTemplate());
        params.put("footerTemplate", options.getFooterTemplate());
        params.put("printBackground", options.getPrintBackground());
        params.put("scale", options.getScale());
        params.put("paperWidth", paperWidth);
        params.put("paperHeight", paperHeight);
        params.put("marginTop", marginTop);
        params.put("marginBottom", marginBottom);
        params.put("marginLeft", marginLeft);
        params.put("marginRight", marginRight);
        params.put("pageRanges", options.getPageRanges());
        params.put("preferCSSPageSize", options.getPreferCSSPageSize());
        params.put("generateTaggedPDF", options.getTagged());
        params.put("generateDocumentOutline", options.getOutline());

        JsonNode result = this.primaryTargetClient.send("Page.printToPDF", params);

        if (options.getOmitBackground()) {
            this.emulationManager.resetDefaultBackgroundColor();
        }
        JsonNode handle = result.get(STREAM);
        ValidateUtil.assertArg(handle != null, "Page.printToPDF result has no stream handle. Please check your chrome version. result=" + result);
        return Helper.readProtocolStream(this.primaryTargetClient, handle.asText(), options.getPath());

    }

    public void close(boolean runBeforeUnload) {
        synchronized (this.browserContext()) {
            ValidateUtil.assertArg(this.primaryTargetClient.connection() != null, "Protocol error: Connection closed. Most likely the page has been closed.");
            if (runBeforeUnload) {
                this.primaryTargetClient.send("Page.close");
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("targetId", this.primaryTarget.getTargetId());
                this.primaryTargetClient.connection().send("Target.closeTarget", params);
                this.tabTarget.waitForTargetClose();
            }
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public CdpMouse mouse() {
        return mouse;
    }

    @Override
    public void resize(int contentWidth, int contentHeight) {
        int windowId = this.primaryTargetClient.send("Browser.getWindowForTarget").get("windowId").asInt();
        Map<String, Object> params = ParamsFactory.create();
        params.put("windowId", windowId);
        params.put("width", contentWidth);
        params.put("height", contentHeight);
        this.primaryTargetClient.send("Browser.setContentsSize", params);
    }

    @Override
    public BluetoothEmulation bluetooth() {
        return this.cdpBluetoothEmulation;
    }

    /**
     * 创建一个page对象
     *
     * @param client   与页面通讯的客户端
     * @param target   目标
     * @param viewport 视图
     * @return 页面实例
     */
    public static CdpPage create(CDPSession client, CdpTarget target, Viewport viewport) {
        CdpPage page = new CdpPage(client, target);
        page.initialize();
        if (viewport != null) {
            page.setViewport(viewport);
        }
        return page;
    }


    private void onBindingCalled(IsolatedWorld world, BindingCalledEvent event) {
        String payloadStr = event.getPayload();
        BindingPayload payload;
        try {
            payload = OBJECTMAPPER.readValue(payloadStr, BindingPayload.class);
        } catch (JsonProcessingException e) {
            return;
        }
        if (!"exposedFun".equals(payload.getType())) {
            return;
        }
        if (world.context() == null) {
            return;
        }
        Binding binding = this.bindings.get(payload.getName());
        Optional.ofNullable(binding).ifPresent(b -> b.run(world.context(), payload.getSeq(), payload.getArgs(), payload.getIsTrivial()));
    }

    /**
     * 当js对话框出现的时候触发，比如alert, prompt, confirm 或者 beforeunload。Puppeteer可以通过Dialog's accept 或者 dismiss来响应弹窗。
     *
     * @param event 触发事件
     */
    private void onDialog(JavascriptDialogOpeningEvent event) {
        CdpDialog dialog = new CdpDialog(this.primaryTargetClient, event.getType(), event.getMessage(), event.getDefaultPrompt());
        this.emit(PageEvents.Dialog, dialog);
    }

    /**
     * 根据启用状态切换忽略每个请求的缓存。默认情况下，缓存已启用。
     */
    private String addPageBinding() {
        return "function addPageBinding(type, name, prefix) {\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    if (globalThis[name]) {\n" +
                "        return;\n" +
                "    }\n" +
                "\n" +
                "    Object.assign(globalThis, {\n" +
                "        [name](...args) {\n" +
                "\n" +
                "\n" +
                "            const callPuppeteer = globalThis[name];\n" +
                "            callPuppeteer.args ??= new Map();\n" +
                "            callPuppeteer.callbacks ??= new Map();\n" +
                "            const seq = (callPuppeteer.lastSeq ?? 0) + 1;\n" +
                "            callPuppeteer.lastSeq = seq;\n" +
                "            callPuppeteer.args.set(seq, args);\n" +
                "\n" +
                "\n" +
                "            globalThis[prefix + name](JSON.stringify({\n" +
                "                type,\n" +
                "                name,\n" +
                "                seq,\n" +
                "                args,\n" +
                "                isTrivial: !args.some(value => {\n" +
                "                    return value instanceof Node;\n" +
                "                }),\n" +
                "            }));\n" +
                "            return new Promise((resolve, reject) => {\n" +
                "                callPuppeteer.callbacks.set(seq, {\n" +
                "                    resolve(value) {\n" +
                "                        callPuppeteer.args.delete(seq);\n" +
                "                        resolve(value);\n" +
                "                    },\n" +
                "                    reject(value) {\n" +
                "                        callPuppeteer.args.delete(seq);\n" +
                "                        reject(value);\n" +
                "                    },\n" +
                "                });\n" +
                "            });\n" +
                "        },\n" +
                "    });\n" +
                "}";
    }

    private ConsoleMessageType convertConsoleMessageLevel(String method) {
        if ("warning".equals(method)) {
            return ConsoleMessageType.warn;
        }
        return ConsoleMessageType.valueOf(method);
    }


    public void setIsDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }

    public boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public Page openDevTools() {
        CdpTarget cdpTarget = (CdpTarget) this.target();
        String pageTargetId = cdpTarget.getTargetId();
        CdpBrowser browser = (CdpBrowser) this.browser();
        return browser.createDevToolsPage(pageTargetId);
    }

    public Accessibility accessibility() {
        return this.mainFrame().accessibility();
    }

}
