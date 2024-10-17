package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.ScreenRecorder;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.entities.Binding;
import com.ruiyun.jvppeteer.entities.BindingPayload;
import com.ruiyun.jvppeteer.entities.BoundingBox;
import com.ruiyun.jvppeteer.entities.CallFrame;
import com.ruiyun.jvppeteer.entities.ClickOptions;
import com.ruiyun.jvppeteer.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.entities.ConsoleMessageLocation;
import com.ruiyun.jvppeteer.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.entities.Cookie;
import com.ruiyun.jvppeteer.entities.CookieParam;
import com.ruiyun.jvppeteer.entities.Credentials;
import com.ruiyun.jvppeteer.entities.DeleteCookiesRequest;
import com.ruiyun.jvppeteer.entities.Device;
import com.ruiyun.jvppeteer.entities.EvaluateType;
import com.ruiyun.jvppeteer.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.entities.GetMetricsResponse;
import com.ruiyun.jvppeteer.entities.GetNavigationHistoryResponse;
import com.ruiyun.jvppeteer.entities.GoToOptions;
import com.ruiyun.jvppeteer.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.entities.ImageType;
import com.ruiyun.jvppeteer.entities.LengthUnit;
import com.ruiyun.jvppeteer.entities.MediaFeature;
import com.ruiyun.jvppeteer.entities.Metric;
import com.ruiyun.jvppeteer.entities.Metrics;
import com.ruiyun.jvppeteer.entities.NavigationEntry;
import com.ruiyun.jvppeteer.entities.NetworkConditions;
import com.ruiyun.jvppeteer.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.entities.PDFMargin;
import com.ruiyun.jvppeteer.entities.PDFOptions;
import com.ruiyun.jvppeteer.entities.PageMetrics;
import com.ruiyun.jvppeteer.entities.PaperFormats;
import com.ruiyun.jvppeteer.entities.RemoteObject;
import com.ruiyun.jvppeteer.entities.ScreenRecorderOptions;
import com.ruiyun.jvppeteer.entities.ScreencastOptions;
import com.ruiyun.jvppeteer.entities.ScreenshotClip;
import com.ruiyun.jvppeteer.entities.ScreenshotOptions;
import com.ruiyun.jvppeteer.entities.StackTrace;
import com.ruiyun.jvppeteer.entities.UserAgentMetadata;
import com.ruiyun.jvppeteer.entities.Viewport;
import com.ruiyun.jvppeteer.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.entities.WaitForOptions;
import com.ruiyun.jvppeteer.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.events.EntryAddedEvent;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.events.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.events.JavascriptDialogOpeningEvent;
import com.ruiyun.jvppeteer.events.MetricsEvent;
import com.ruiyun.jvppeteer.events.ScreencastFrameEvent;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ruiyun.jvppeteer.common.Constant.ABOUT_BLANK;
import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.STREAM;
import static com.ruiyun.jvppeteer.common.Constant.supportedMetrics;
import static com.ruiyun.jvppeteer.util.Helper.throwError;
import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public class Page extends EventEmitter<Page.PageEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);
    private volatile boolean closed = false;
    private final TargetManager targetManager;
    private CDPSession primaryTargetClient;
    private Target primaryTarget;
    private final CDPSession tabTargetClient;
    private final Target tabTarget;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final Touchscreen touchscreen;
    private FrameManager frameManager;
    private final EmulationManager emulationManager;
    private final Tracing tracing;
    private final Map<String, Binding> bindings = new HashMap<>();
    private final Map<String, String> exposedFunctions = new HashMap<>();
    private final Coverage coverage;
    private Viewport viewport;
    private final Map<String, WebWorker> workers = new HashMap<>();
    private final Set<AwaitableResult<FileChooser>> fileChooserResults = new HashSet<>();
    private final AwaitableResult<TargetCloseException> sessionCloseResult = AwaitableResult.create();
    private boolean serviceWorkerBypassed = false;
    private boolean userDragInterceptionEnabled = false;

    protected final TimeoutSettings _timeoutSettings = new TimeoutSettings();
    final Map<Consumer<Request>, Consumer<Request>> requestHandlers = new WeakHashMap<>();
    private boolean isDragging;

    public Page(CDPSession client, Target target) {
        super();
        this.primaryTargetClient = client;
        this.tabTargetClient = client.parentSession();
         Objects.requireNonNull(this.tabTargetClient, "Tab target session is not defined.");
        this.tabTarget = this.tabTargetClient.getTarget();
         Objects.requireNonNull(this.tabTarget, "Tab target is not defined.");
        this.primaryTarget = target;
        this.targetManager = target.targetManager();
        this.keyboard = new Keyboard(client);
        this.mouse = new Mouse(client, this.keyboard);
        this.touchscreen = new Touchscreen(client, this.keyboard);
        this.frameManager = new FrameManager(client, this, this._timeoutSettings);
        this.emulationManager = new EmulationManager(client);
        this.tracing = new Tracing(client);
        this.coverage = new Coverage(client);
        this.viewport = null;
        Map<FrameManager.FrameManagerEvent, Consumer<?>> frameManagerHandlers = Collections.unmodifiableMap(new HashMap<FrameManager.FrameManagerEvent, Consumer<?>>() {{
            put(FrameManager.FrameManagerEvent.FrameAttached, ((Consumer<Frame>) (frame) -> Page.this.emit(PageEvent.FrameAttached, frame)));
            put(FrameManager.FrameManagerEvent.FrameDetached, ((Consumer<Frame>) (frame) -> Page.this.emit(PageEvent.FrameDetached, frame)));
            put(FrameManager.FrameManagerEvent.FrameNavigated, ((Consumer<Frame>) (frame) -> Page.this.emit(PageEvent.FrameNavigated, frame)));
            put(FrameManager.FrameManagerEvent.ConsoleApiCalled, (Consumer<Object[]>) arg -> Page.this.onConsoleAPI((IsolatedWorld) arg[0], (ConsoleAPICalledEvent) arg[1]));
            put(FrameManager.FrameManagerEvent.BindingCalled, (Consumer<List<Object>>) arg -> Page.this.onBindingCalled((IsolatedWorld) arg.get(0), (BindingCalledEvent) arg.get(1)));
        }});
        frameManagerHandlers.forEach(this.frameManager::on);

        Map<NetworkManager.NetworkManagerEvent, Consumer<?>> networkManagerHandlers = Collections.unmodifiableMap(new HashMap<NetworkManager.NetworkManagerEvent, Consumer<?>>() {{
            put(NetworkManager.NetworkManagerEvent.Request, ((Consumer<Request>) (request) -> Page.this.emit(PageEvent.Request, request)));
            put(NetworkManager.NetworkManagerEvent.RequestServedFromCache, ((Consumer<Request>) (request) -> Page.this.emit(PageEvent.RequestServedFromCache, request)));
            put(NetworkManager.NetworkManagerEvent.Response, ((Consumer<Response>) (response) -> Page.this.emit(PageEvent.Response, response)));
            put(NetworkManager.NetworkManagerEvent.RequestFailed, ((Consumer<Request>) (request) -> Page.this.emit(PageEvent.RequestFailed, request)));
            put(NetworkManager.NetworkManagerEvent.RequestFinished, ((Consumer<Request>) (request) -> Page.this.emit(PageEvent.RequestFinished, request)));
        }});
        networkManagerHandlers.forEach((key, value) -> this.frameManager.networkManager().on(key, value));

        this.tabTargetClient.on(CDPSession.CDPSessionEvent.CDPSession_Swapped, (Consumer<CDPSession>) Page.this::onActivation);
        this.tabTargetClient.on(CDPSession.CDPSessionEvent.CDPSession_Ready, (Consumer<CDPSession>) Page.this::onSecondaryTarget);

        Consumer<Target> onDetachedFromTarget = (cdpTarget) -> {
            if (cdpTarget.session() == null) {
                return;
            }
            String sessionId = cdpTarget.session().id();
            WebWorker webWorker = Page.this.workers.get(sessionId);
            if (webWorker == null) {
                return;
            }
            Page.this.workers.remove(sessionId);
            Page.this.emit(PageEvent.WorkerDestroyed, webWorker);
        };
        this.targetManager.on(TargetManager.TargetManagerEvent.TargetGone, onDetachedFromTarget);
        this.tabTarget.setOnCloseRunner(() -> {
            Page.this.targetManager.off(TargetManager.TargetManagerEvent.TargetGone, onDetachedFromTarget);
            Page.this.emit(PageEvent.Close, true);
            Page.this.closed = true;
        });

        this.setupPrimaryTargetListeners();
        this.attachExistingTargets();
    }

    @Override
    public EventEmitter<PageEvent> on(PageEvent type, Consumer<?> handler) {
        if (type != PageEvent.Request) {
            return super.on(type, handler);
        }
        Consumer<Request> wrapper = this.requestHandlers.get(handler);
        Consumer<Request> handlerWrapper = (Consumer<Request>) handler;
        if (wrapper == null) {
            wrapper = event -> event.enqueueInterceptAction(() -> handlerWrapper.accept(event));
        }
        this.requestHandlers.put(handlerWrapper, wrapper);
        return super.on(type, wrapper);
    }

    @Override
    public void off(PageEvent type, Consumer<?> handler) {
        if (type == PageEvent.Request) {
            handler = this.requestHandlers.get(handler);
        }
        super.off(type, handler);
    }

    private void attachExistingTargets() {
        List<Target> childTargets = this.targetManager.getChildTargets(this.primaryTarget);
        List<Target> queue = new ArrayList<>(childTargets);
        int idx = 0;
        while (idx < queue.size()) {
            Target next = queue.get(idx);
            idx++;
            CDPSession session = next.session();
            if (session != null) {
                this.onAttachedToTarget.accept(session);
            }
            queue.addAll(this.targetManager.getChildTargets(next));
        }
    }

    private void onActivation(CDPSession newSession) {
        this.primaryTargetClient = newSession;
        this.primaryTarget = this.primaryTargetClient.getTarget();
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
        Map<CDPSession.CDPSessionEvent, Consumer<?>> sessionHandlers = Collections.unmodifiableMap(new HashMap<CDPSession.CDPSessionEvent, Consumer<?>>() {{
            put(CDPSession.CDPSessionEvent.CDPSession_Ready, (session) -> Page.this.onAttachedToTarget.accept((CDPSession) session));
            put(CDPSession.CDPSessionEvent.CDPSession_Disconnected, ((ignore) -> sessionCloseResult.onSuccess(new TargetCloseException("Target closed"))));
            put(CDPSession.CDPSessionEvent.Page_domContentEventFired, ((ignore) -> Page.this.emit(PageEvent.Domcontentloaded, true)));
            put(CDPSession.CDPSessionEvent.Page_loadEventFired, ((ignore) -> Page.this.emit(PageEvent.Load, true)));
            put(CDPSession.CDPSessionEvent.Page_javascriptDialogOpening, ((Consumer<JavascriptDialogOpeningEvent>) Page.this::onDialog));
            put(CDPSession.CDPSessionEvent.Runtime_exceptionThrown, (Consumer<ExceptionThrownEvent>) Page.this::handleException);
            put(CDPSession.CDPSessionEvent.Inspector_targetCrashed, (ignore) -> Page.this.onTargetCrashed());
            put(CDPSession.CDPSessionEvent.Performance_metrics, (Consumer<MetricsEvent>) Page.this::emitMetrics);
            put(CDPSession.CDPSessionEvent.Log_entryAdded, (Consumer<EntryAddedEvent>) Page.this::onLogEntryAdded);
            put(CDPSession.CDPSessionEvent.Page_fileChooserOpened, (Consumer<FileChooserOpenedEvent>) Page.this::onFileChooser);
        }});
        sessionHandlers.forEach((eventName, handler) -> this.primaryTargetClient.on(eventName, handler));
    }

    private void onSecondaryTarget(CDPSession session) {
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
    private final Consumer<CDPSession> onAttachedToTarget = (session) -> {
        this.frameManager.onAttachedToTarget(session.getTarget());
        if ("worker".equals(session.getTarget().getTargetInfo().getType())) {
            WebWorker webWorker = new WebWorker(session, session.getTarget().url(), session.getTarget().getTargetId(), session.getTarget().type(), Page.this::addConsoleMessage, Page.this::handleException);
            this.workers.put(session.id(), webWorker);
            this.emit(PageEvent.WorkerCreated, webWorker);
        }
        session.on(CDPSession.CDPSessionEvent.CDPSession_Ready, this.onAttachedToTarget);
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
        Frame frame = this.frameManager.frame(event.getFrameId());
         Objects.requireNonNull(frame, "This should never happen.");
        IsolatedWorld mainWorld = frame.worlds().get(MAIN_WORLD);
        ElementHandle handle = null;
        try {
            handle = mainWorld.adoptBackendNode(event.getBackendNodeId()).asElement();
        } catch (JsonProcessingException e) {
            throwError(e);
        }
        FileChooser fileChooser = new FileChooser(handle, event);
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
        return this.emulationManager.getJavascriptEnabled();
    }

    /**
     * 创建并返回一个可等待的文件选择器结果
     * 当需要拦截文件选择器对话框时，此方法将非常有用
     *
     * @return 返回一个可等待的文件选择器结果，调用者可以通过这个对象来获取用户选择的文件
     */
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

    /**
     * 设置页面的地理位置<p>
     * 考虑使用 {@link BrowserContext#overridePermissions(String, WebPermission...)} 授予页面读取其地理位置的权限。
     *
     * @param options 地理位置具体信息
     */
    public void setGeolocation(GeolocationOptions options) {
        this.emulationManager.setGeolocation(options);
    }

    /**
     * 创建此页面的目标。
     *
     * @return 目标
     */
    public Target target() {
        return this.primaryTarget;
    }

    /**
     * 返回页面隶属的浏览器
     *
     * @return 浏览器实例
     */
    public Browser browser() {
        return this.primaryTarget.browser();
    }

    /**
     * 获取页面所属的浏览器上下文。
     *
     * @return 浏览器上下文
     */
    public BrowserContext browserContext() {
        return this.primaryTarget.browserContext();
    }

    private void onTargetCrashed() {
        this.emit(PageEvent.Error, new JvppeteerException("Page crashed!"));
    }

    private void onLogEntryAdded(EntryAddedEvent event) {
        if (ValidateUtil.isNotEmpty(event.getEntry().getArgs()))
            event.getEntry().getArgs().forEach(arg -> Helper.releaseObject(this.primaryTargetClient, arg));
        if (!"worker".equals(event.getEntry().getSource())) {
            List<ConsoleMessageLocation> locations = new ArrayList<>();
            locations.add(new ConsoleMessageLocation(event.getEntry().getUrl(), event.getEntry().getLineNumber()));
            this.emit(PageEvent.Console, new ConsoleMessage(convertConsoleMessageLevel(event.getEntry().getLevel()), event.getEntry().getText(), Collections.emptyList(), locations));
        }
    }

    /**
     * 页面的主框架。
     *
     * @return 主框架
     */
    public Frame mainFrame() {
        return this.frameManager.mainFrame();
    }

    /**
     * 虚拟键盘
     *
     * @return 虚拟键盘
     */
    public Keyboard keyboard() {
        return this.keyboard;
    }

    /**
     * 触控屏幕
     *
     * @return 触控屏幕
     */
    public Touchscreen touchscreen() {
        return this.touchscreen;
    }

    public Coverage coverage() {
        return this.coverage;
    }

    public Tracing tracing() {
        return this.tracing;
    }

    /**
     * 附加到页面的所有框架的数组。
     *
     * @return iframe标签
     */
    public List<Frame> frames() {
        return this.frameManager.frames();
    }

    /**
     * 该方法返回所有与页面关联的 WebWorkers
     *
     * @return WebWorkers
     */
    public List<WebWorker> workers() {
        return new ArrayList<>(this.workers.values());
    }

    /**
     * 启用请求拦截器，会激活 request.abort, request.continue 和 request.respond 方法。这提供了修改页面发出的网络请求的功能。<p>
     * 一旦启用请求拦截，每个请求都将停止，除非它继续，响应或中止<p>
     *
     * @param value 是否启用请求拦截器
     */
    public void setRequestInterception(boolean value) {
        this.frameManager.networkManager().setRequestInterception(value);
    }

    /**
     * 切换忽略每个请求的 Service Worker。
     *
     * @param bypass 是否忽略
     */
    public void setBypassServiceWorker(boolean bypass) {
        this.serviceWorkerBypassed = bypass;
        Map<String, Object> params = new HashMap<>();
        params.put("bypass", bypass);
        this.primaryTargetClient.send("Network.setBypassServiceWorker", params);
    }

    /**
     * 我们不再支持拦截拖动有效负载。使用 ElementHandle 上的新拖动 API 进行拖动（或仅使用 Page.mouse）
     *
     * @param enabled 是否启用
     */
    @Deprecated
    public void setDragInterception(boolean enabled) {
        this.userDragInterceptionEnabled = enabled;
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        this.primaryTargetClient.send("Input.setInterceptDrags", params);
    }

    /**
     * 将网络连接设置为离线。
     * <p>
     * 它不会改变 Page.emulateNetworkConditions() 中使用的参数
     *
     * @param enabled 设置 true, 启用离线模式。
     */
    public void setOfflineMode(boolean enabled) {
        this.frameManager.networkManager().setOfflineMode(enabled);
    }

    /**
     * 模拟网络条件
     * <p>
     * 这不会影响 WebSocket 和 WebRTC PeerConnections（<a href="https://crbug.com/563644">请参阅这里 </a>）。
     * <p>
     * 要将页面设置为离线，你可以使用 Page.setOfflineMode()。<p>
     * 通过导入 PredefinedNetworkConditions 可以使用预定义网络条件列表。<p>
     *
     * @param networkConditions 传递 null 将禁用网络条件模拟。
     */
    public void emulateNetworkConditions(NetworkConditions networkConditions) {
        this.frameManager.networkManager().emulateNetworkConditions(networkConditions);
    }

    /**
     * 此方法会改变下面几个方法的默认30秒等待时间：
     * ${@link Page#goTo(String)}
     * ${@link Page#goTo(String, GoToOptions, boolean)}
     * ${@link Page#goBack(WaitForOptions)}
     * ${@link Page#goForward(WaitForOptions)}
     * ${@link Page#reload(WaitForOptions)}
     * ${@link Page#setContent(String) }
     * ${@link Page#waitForNavigation()}
     *
     * @param timeout 超时时间
     */

    public void setDefaultNavigationTimeout(int timeout) {
        this._timeoutSettings.setDefaultNavigationTimeout(timeout);
    }

    public int getDefaultTimeout() {
        return this._timeoutSettings.timeout();
    }

    /**
     * 此方法会改变下面几个方法的默认30秒等待时间：
     * ${@link Page#goTo(String)}
     * ${@link Page#goTo(String, GoToOptions, boolean)}
     * ${@link Page#goBack(WaitForOptions)}
     * ${@link Page#goForward(WaitForOptions)}
     * ${@link Page#reload(WaitForOptions)}
     * ${@link Page#waitForNavigation()}
     *
     * @param timeout 超时时间
     */

    public void setDefaultTimeout(int timeout) {
        this._timeoutSettings.setDefaultTimeout(timeout);
    }

    /**
     * 此方法遍历js堆栈，找到所有带有指定原型的对象
     * <p>
     *
     * @param prototypeHandle 原型处理器
     * @return 代表页面元素的一个实例
     * @throws JsonProcessingException Json解析异常
     */
    public JSHandle queryObjects(JSHandle prototypeHandle) throws JsonProcessingException {
        ValidateUtil.assertArg(!prototypeHandle.disposed(), "Prototype JSHandle is disposed!");
        ValidateUtil.assertArg(StringUtil.isNotEmpty(prototypeHandle.getRemoteObject().getObjectId()), "Prototype JSHandle must not be referencing primitive value");
        Map<String, Object> params = new HashMap<>();
        params.put("prototypeObjectId", prototypeHandle.getRemoteObject().getObjectId());
        JsonNode response = this.mainFrame().client().send("Runtime.queryObjects", params);
        return this.mainFrame().mainRealm().createJSHandle(OBJECTMAPPER.treeToValue(response.get("objects"), RemoteObject.class));
    }

    /**
     * 返回当前页面的cookies
     *
     * @return cookies
     * @throws JsonProcessingException 如果处理JSON时发生错误
     */
    public List<Cookie> cookies() throws JsonProcessingException {
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
     * @throws JsonProcessingException 如果处理JSON时发生错误
     */
    public List<Cookie> cookies(String... urls) throws JsonProcessingException {
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
            Cookie cookie = OBJECTMAPPER.treeToValue(cookieNode, Cookie.class);
            cookies.add(cookie);
        }
        return cookies;
    }

    /**
     * 删除指定名称的cookies
     * <p>
     * 本方法接收一个或多个cookie名称，并创建对应的DeleteCookiesRequest对象列表，
     * 然后调用deleteCookie方法删除这些cookies
     *
     * @param names 需要删除的cookie名称列表
     */
    public void deleteCookie(String... names) {
        List<DeleteCookiesRequest> cookies = new ArrayList<>();
        for (String name : names) {
            cookies.add(new DeleteCookiesRequest(name));
        }
        this.deleteCookie(cookies);
    }

    /**
     * 删除指定的cookies
     *
     * @param cookies 待删除的cookies请求列表，每个元素包含待删除的cookie信息
     */
    public void deleteCookie(List<DeleteCookiesRequest> cookies) {
        String pageURL = this.url();
        for (DeleteCookiesRequest cookie : cookies) {
            if (StringUtil.isEmpty(cookie.getUrl()) && pageURL.startsWith("http")) {
                cookie.setUrl(pageURL);
            }
            Map<String, Object> params = ParamsFactory.create();
            params.put("name", cookie.getName());
            params.put("url", cookie.getUrl());
            params.put("domain", cookie.getDomain());
            params.put("path", cookie.getPath());
            this.primaryTargetClient.send("Network.deleteCookies", params);
        }
    }

    public void setCookie(List<CookieParam> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return;
        }
        String pageURL = this.url();
        boolean startsWithHTTP = pageURL.startsWith("http");
        cookies.replaceAll(cookie -> {
            if (StringUtil.isEmpty(cookie.getUrl()) && startsWithHTTP) cookie.setUrl(pageURL);
            ValidateUtil.assertArg(!ABOUT_BLANK.equals(cookie.getUrl()), "Blank page can not have cookie " + cookie.getName());
            if (StringUtil.isNotEmpty(cookie.getUrl())) {
                ValidateUtil.assertArg(!cookie.getUrl().startsWith("data:"), "Data URL page can not have cookie " + cookie.getName());
            }
            return cookie;
        });
        List<DeleteCookiesRequest> deleteCookiesParameters = new ArrayList<>();
        for (CookieParam cookie : cookies) {
            deleteCookiesParameters.add(new DeleteCookiesRequest(cookie.getName(), cookie.getUrl(), cookie.getDomain(), cookie.getPath()));
        }
        this.deleteCookie(deleteCookiesParameters);
        Map<String, Object> params = new HashMap<>();
        params.put("cookies", cookies);
        this.primaryTargetClient.send("Network.setCookies", params);
    }

    /**
     * 该方法在页面的 window 对象上添加一个名为 name 的函数。
     * <p>
     * 调用时，该函数会执行 pptrFunction，结果是 pptrFunction 的返回值。
     * <p>
     *
     * @param name         窗口对象上的函数名称
     * @param pptrFunction 将在 Puppeteer 上下文中调用的回调函数。
     * @throws JsonProcessingException 如果处理JSON时发生错误
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     */
    public void exposeFunction(String name, BindingFunction pptrFunction) throws EvaluateException, JsonProcessingException {
        ValidateUtil.assertArg(!this.bindings.containsKey(name), MessageFormat.format("Failed to add page binding with name {0}: window[{1}] already exists!", name, name));
        String source = Helper.evaluationString(addPageBinding(), "exposedFun", name, CDP_BINDING_PREFIX);
        Binding binding = new Binding(name, pptrFunction, source);
        this.bindings.put(name, binding);
        NewDocumentScriptEvaluation response = this.frameManager.evaluateOnNewDocument(source);
        this.frameManager.addExposedFunctionBinding(binding);
        this.exposedFunctions.put(name, response.getIdentifier());
    }

    /**
     * 该方法从页面的 window 对象中删除先前通过 Page.exposeFunction() 添加的名为 name 的函数。
     *
     * @param name 要删除的函数名称
     * @throws JsonProcessingException 如果处理JSON时发生错误
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     */
    public void removeExposedFunction(String name) throws JsonProcessingException, EvaluateException {
        String exposedFunctionId = this.exposedFunctions.get(name);
        if (exposedFunctionId == null) {
            throw new JvppeteerException("Failed to remove page binding with name '" + name + "' window['" + name + "'] does not exists!");
        }
        this.exposedFunctions.remove(name);
        Binding binging = this.bindings.remove(name);
        this.frameManager.removeScriptToEvaluateOnNewDocument(exposedFunctionId);
        this.frameManager.removeExposedFunctionBinding(binging);
    }

    /**
     * 为HTTP authentication 提供认证凭据 。
     * <p>
     * 传 null 禁用认证。
     * <p>
     * 将在后台打开请求拦截以实现身份验证。这可能会影响性能。
     *
     * @param credentials 验证信息
     */
    public void authenticate(Credentials credentials) {
        this.frameManager.networkManager().authenticate(credentials);
    }


    /**
     * 当前页面发起的每个请求都会带上这些请求头
     * 注意 此方法不保证请求头的顺序
     *
     * @param headers 每个 HTTP 请求都会带上这些请求头。值必须是字符串
     */
    public void setExtraHTTPHeaders(Map<String, String> headers) {
        this.frameManager.networkManager().setExtraHTTPHeaders(headers);
    }

    /**
     * 给页面设置userAgent
     *
     * @param userAgent 此页面中使用的特定用户代理
     */
    public void setUserAgent(String userAgent) {
        this.frameManager.networkManager().setUserAgent(userAgent, null);
    }

    /**
     * 给页面设置userAgent
     *
     * @param userAgent 此页面中使用的特定用户代理
     */
    public void setUserAgent(String userAgent, UserAgentMetadata userAgentMetadata) {
        this.frameManager.networkManager().setUserAgent(userAgent, userAgentMetadata);
    }

    /**
     * 获取性能指标
     * <p>
     * 本方法通过向目标客户端发送“Performance.getMetrics”请求来获取当前的性能指标
     * <p>
     * 接收到的响应将被转换成GetMetricsResponse对象，然后用于构建并返回一个Metrics对象
     * <p>
     * Metrics对象:
     * <blockquote><pre>
     *
     * Timestamp ：获取指标样本时的时间戳。
     *
     * Documents：页面中的文档数。
     *
     * ¥Documents : Number of documents in the page.
     *
     * Frames：页面中的帧数。
     *
     * JSEventListeners：页面中的事件数。
     *
     * Nodes ：页面中 DOM 节点的数量。
     *
     * LayoutCount：完整或部分页面布局的总数。
     *
     * RecalcStyleCount：页面样式重新计算的总数。
     *
     * LayoutDuration：所有页面布局的总持续时间。
     *
     * RecalcStyleDuration：所有页面样式重新计算的总持续时间。
     *
     * ScriptDuration ：JavaScript 执行的总持续时间。
     *
     * TaskDuration ：浏览器执行的所有任务的总持续时间。
     *
     * JSHeapUsedSize：使用的 JavaScript 堆大小。
     *
     * JSHeapTotalSize：JavaScript 堆总大小。
     *  </pre></blockquote>
     * <p>
     * 所有时间戳都是单调时间：自过去任意点以来单调增加的时间（以秒为单位）。
     *
     * @return Metrics对象，包含当前获取到的性能指标
     * @throws IllegalAccessException    当反射访问权限出现问题时抛出
     * @throws IntrospectionException    当自省操作出现问题时抛出
     * @throws InvocationTargetException 调用目标方法时，目标方法抛出异常
     * @throws JsonProcessingException   处理JSON时抛出异常
     */
    public Metrics metrics() throws IllegalAccessException, IntrospectionException, InvocationTargetException, JsonProcessingException {
        GetMetricsResponse response = OBJECTMAPPER.treeToValue(this.primaryTargetClient.send("Performance.getMetrics"), GetMetricsResponse.class);
        return this.buildMetricsObject(response.getMetrics());
    }

    private void emitMetrics(MetricsEvent event) {
        PageMetrics pageMetrics = new PageMetrics();
        Metrics metrics = null;
        try {
            metrics = this.buildMetricsObject(event.getMetrics());
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throwError(e);
        }
        pageMetrics.setMetrics(metrics);
        pageMetrics.setTitle(event.getTitle());
        this.emit(PageEvent.Metrics, pageMetrics);
    }

    private Metrics buildMetricsObject(List<Metric> metrics) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
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
        this.emit(PageEvent.PageError, Helper.createClientError(event.getExceptionDetails()));
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
        if (this.listenerCount(PageEvent.Console) == 0) {
            args.forEach(JSHandle::dispose);
            return;
        }
        List<String> textTokens = new ArrayList<>();
        for (JSHandle arg : args) {
            RemoteObject remoteObject = arg.getRemoteObject();
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
        ConsoleMessage message = new ConsoleMessage(type, String.join(" ", textTokens), args, stackTraceLocations);
        this.emit(PageEvent.Console, message);
    }

    /**
     * 重新加载页面
     */
    public Response reload() {
        WaitForOptions options = new WaitForOptions();
        options.setIgnoreSameDocumentNavigation(true);
        return this.waitForNavigation(options, true);
    }

    /**
     * 重新加载页面
     *
     * @param options 与${@link Page#goTo(String, GoToOptions)}中的options是一样的配置
     * @return 响应
     */
    public Response reload(WaitForOptions options) {
        options.setIgnoreSameDocumentNavigation(true);
        return this.waitForNavigation(options, true);
    }

    /**
     * 创建附加到页面的 Chrome Devtools 协议会话。
     *
     * @return CDPSession
     */
    public CDPSession createCDPSession() {
        return this.target().createCDPSession();
    }

    /**
     * 此方法导航到历史记录中的上一页
     *
     * @return 如果存在多个重定向，导航将使用最后一个重定向的响应进行解析。如果无法返回，则解析为 null。
     * @throws JsonProcessingException
     */
    public Response goBack() throws JsonProcessingException {
        return this.go(-1, new WaitForOptions());
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

    public Response goForward() throws JsonProcessingException {
        return this.go(+1, new WaitForOptions());
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
        NavigationEntry entry = history.getEntries().get(history.getCurrentIndex() + delta);
        if (entry == null) return null;
        Map<String, Object> params = new HashMap<>();
        params.put("entryId", entry.getId());
        this.primaryTargetClient.send("Page.navigateToHistoryEntry", params, null, false);
        return this.waitForNavigation(options);
    }

    /**
     * 相当于多个tab时，切换到某个tab。
     */
    public void bringToFront() {
        this.primaryTargetClient.send("Page.bringToFront");
    }

    /**
     * 是否在页面上启用 JavaScript。
     *
     * @param enabled 是否启用
     */
    public void setJavaScriptEnabled(boolean enabled) {
        this.emulationManager.setJavaScriptEnabled(enabled);
    }

    /**
     * 切换绕过页面的内容安全策略。<P>
     * 注意：CSP 绕过发生在 CSP 初始化时而不是评估时。通常，这意味着应在导航到域之前调用 page.setBypassCSP。
     * </P>
     *
     * @param enabled 是否绕过
     */
    public void setBypassCSP(boolean enabled) {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        this.primaryTargetClient.send("Page.setBypassCSP", params);
    }

    /**
     * 改变页面的css媒体类型。支持的值仅包括 'screen', 'print' 和 null。传 null 禁用媒体模拟
     *
     * @param type css媒体类型
     */
    public void emulateMediaType(MediaType type) {
        this.emulationManager.emulateMediaType(type);
    }

    /**
     * 启用 CPU 限制以模拟慢速 CPU。
     *
     * @param factor 减速系数（1 表示无油门，2 表示 2 倍减速，等等）。
     */
    public void emulateCPUThrottling(double factor) {
        this.emulationManager.emulateCPUThrottling(factor);
    }

    /**
     * 模拟媒体特征
     *
     * @param features 给定一组媒体特性对象，在页面上模拟 CSS 媒体特性,每个媒体特性对象的name必须符合正则表达式 ：
     *                 ^(?:prefers-(?:color-scheme|reduced-motion)|color-gamut)$"
     */
    public void emulateMediaFeatures(List<MediaFeature> features) {
        this.emulationManager.emulateMediaFeatures(features);
    }

    /**
     * 更改页面的时区，传null将禁用将时区仿真
     * <a href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">时区id列表</a>
     *
     * @param timezoneId 时区id
     */
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

    /**
     * 模拟页面上给定的视力障碍,不同视力障碍，截图有不同效果
     *
     * @param type 视力障碍类型
     */
    public void emulateVisionDeficiency(VisionDeficiency type) {
        this.emulationManager.emulateVisionDeficiency(type);
    }

    /**
     * 如果是一个浏览器多个页面的情况，每个页面都可以有单独的viewport
     * <p>注意 在大部分情况下，改变 viewport 会重新加载页面以设置 isMobile 或者 hasTouch</p>
     *
     * @param viewport 设置的视图
     */
    public void setViewport(Viewport viewport) {
        boolean needsReload = this.emulationManager.emulateViewport(viewport);
        this.viewport = viewport;
        if (needsReload) this.reload(new WaitForOptions());
    }

    /**
     * 获取Viewport,Viewport各个参数的含义：
     * width 宽度，单位是像素
     * height  高度，单位是像素
     * deviceScaleFactor  定义设备缩放， (类似于 dpr)。 默认 1。
     * isMobile  要不要包含meta viewport 标签。 默认 false。
     * hasTouch 指定终端是否支持触摸。 默认 false
     * isLandscape 指定终端是不是 landscape 模式。 默认 false。
     *
     * @return Viewport
     */
    public Viewport viewport() {
        return this.viewport;
    }

    /**
     * 在新dom产生之际执行给定的javascript
     * <p>当你的js代码为函数时，type={@link EvaluateType#FUNCTION}</p>
     * <p>当你的js代码为字符串时，type={@link EvaluateType#STRING}</p>
     *
     * @param pageFunction js代码
     * @param args         当你js代码是函数时，js函数的参数
     */
    public NewDocumentScriptEvaluation evaluateOnNewDocument(String pageFunction, Object... args) throws JsonProcessingException {
        String source = Helper.evaluationString(pageFunction, args);
        return this.frameManager.evaluateOnNewDocument(source);
    }

    /**
     * 在新dom产生之际执行给定的javascript
     * <p>当你的js代码为函数时，type={@link EvaluateType#FUNCTION}</p>
     * <p>当你的js代码为字符串时，type={@link EvaluateType#STRING}</p>
     *
     * @param pageFunction js代码
     * @param type         一般为PageEvaluateType#FUNCTION
     * @param args         当你js代码是函数时，js函数的参数
     * @return NewDocumentScriptEvaluation 执行脚本的标志符
     * @throws JsonProcessingException json异常
     */
    public NewDocumentScriptEvaluation evaluateOnNewDocument(String pageFunction, EvaluateType type, Object... args) throws JsonProcessingException {
        String source;
        if (Objects.equals(EvaluateType.STRING, type)) {
            ValidateUtil.assertArg(args.length == 0, "Cannot evaluate a string with arguments");
            source = pageFunction;
        } else {
            source = Helper.evaluationString(pageFunction, args);
        }
        return this.frameManager.evaluateOnNewDocument(source);
    }

    /**
     * 删除通过 Page.evaluateOnNewDocument 注入页面的脚本。
     *
     * @param identifier 脚本标识符
     */
    public void removeScriptToEvaluateOnNewDocument(String identifier) {
        Map<String, Object> identifierKeys = new HashMap<>();
        identifierKeys.put("identifier", identifier);
        this.primaryTargetClient.send("Page.removeScriptToEvaluateOnNewDocument", identifierKeys);
    }

    /**
     * 根据启用状态切换忽略每个请求的缓存。默认情况下，缓存已启用。
     *
     * @param enabled 设置缓存的 enabled 状态
     */
    public void setCacheEnabled(boolean enabled) {
        this.frameManager.networkManager().setCacheEnabled(enabled);
    }

    /**
     * <p>截图</p>
     * 备注 在OS X上 截图需要至少1/6秒。：<a href="https://crbug.com/741689">查看讨论</a>。
     *
     * @param options 截图选项
     * @return 图片base64的字节
     */
    @SuppressWarnings({"unchecked"})
    public String screenshot(ScreenshotOptions options) {
        synchronized (this.browserContext()) {//一个上下文只能有一个截图操作
            this.bringToFront();
            if (StringUtil.isNotEmpty(options.getPath())) {
                String filePath = options.getPath();
                String path = filePath.substring(0, filePath.lastIndexOf('.') + 1).toLowerCase();
                options.setPath(path + options.getType().toString());
            }
            if (options.getType().equals(ImageType.JPG)) {
                options.setType(ImageType.JPEG);
            }
            if (options.getQuality() != null) {
                ValidateUtil.assertArg(options.getQuality() > 0 && options.getQuality() <= 100, "Expected quality (" + options.getQuality() + ") to be between 0 and 100 ,inclusive).");
                ValidateUtil.assertArg(Arrays.asList("jpeg", "webp").contains(options.getType().name().toLowerCase()), options.getType().toString() + "screenshots do not support quality.");
            }

            if (options.getClip() != null) {
                ValidateUtil.assertArg(options.getClip().getWidth() > 0, "'width' in 'clip' must be positive.");
                ValidateUtil.assertArg(options.getClip().getHeight() > 0, "'height' in 'clip' must be positive.");
            }
            Viewport fullViewport = null;
            try {
                if (options.getClip() != null) {
                    // If `captureBeyondViewport` is `false`, then we set the viewport to
                    // capture the full page. Note this may be affected by on-page CSS and
                    // JavaScript.
                    ValidateUtil.assertArg(!options.getFullPage(), "'clip' and 'fullPage' are mutually exclusive");
                    options.setClip(roundRectangle(normalizeRectangle(options.getClip())));
                } else {
                    if (options.getFullPage()) {
                        if (!options.getCaptureBeyondViewport()) {
                            LinkedHashMap<String, Integer> scrollDimensions = (LinkedHashMap<String, Integer>) this.mainFrame().isolatedRealm().evaluate("() => {\n" + "              const element = document.documentElement;\n" + "              return {\n" + "                width: element.scrollWidth,\n" + "                height: element.scrollHeight,\n" + "              };\n" + "            }", null);
                            fullViewport = new Viewport(scrollDimensions.get("width"), scrollDimensions.get("height"), this.viewport.getDeviceScaleFactor(), this.viewport.getIsMobile(), this.viewport.getHasTouch(), this.viewport.getIsLandscape());
                            this.setViewport(fullViewport);
                        }
                    } else {
                        options.setCaptureBeyondViewport(false);
                    }
                }
                return this._screenshot(options);
            } catch (Exception e) {
                LOGGER.error("_screenshot error: ", e);
            } finally {
                if (fullViewport != null) {
                    this.setViewport(this.viewport);
                }
            }
            return "";
        }

    }

    private String _screenshot(ScreenshotOptions options) {
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

    /**
     * @see <a href="https://w3c.github.io/webdriver-bidi/#rectangle-intersection">href="https://w3c.github.io/webdriver-bidi/#rectangle-intersection</a>
     */
    private ScreenshotClip getIntersectionRect(ScreenshotClip clip, JsonNode viewport) {
        double x = Math.max(clip.getX(), viewport.get("x").asDouble());
        double y = Math.max(clip.getY(), viewport.get("y").asDouble());
        return new ScreenshotClip(x, y, Math.max(Math.min(clip.getX() + clip.getWidth(), viewport.get("x").asDouble() + viewport.get("width").asDouble()) - x, 0), Math.max(Math.min(clip.getY() + clip.getHeight(), viewport.get("y").asDouble() + viewport.get("height").asDouble()) - y, 0), 1);
    }

    private ScreenshotClip roundRectangle(ScreenshotClip clip) {
        double x = Math.round(clip.getX());
        double y = Math.round(clip.getY());
        double width = Math.round(clip.getWidth() + clip.getX() - x);
        double height = Math.round(clip.getHeight() + clip.getY() - y);
        ScreenshotClip screenshotClip = new ScreenshotClip(x, y, width, height);
        screenshotClip.setScale(clip.getScale());
        return screenshotClip;
    }

    /**
     * @see <a href="https://w3c.github.io/webdriver-bidi/#normalize-rect">href="https://w3c.github.io/webdriver-bidi/#normalize-rect</a>
     */
    private ScreenshotClip normalizeRectangle(ScreenshotClip clip) {
        double x;
        double y;
        double width;
        double height;
        if (clip.getWidth() < 0) {
            x = clip.getX() + clip.getWidth();
            width = -clip.getWidth();
        } else {
            x = clip.getX();
            width = clip.getWidth();
        }
        if (clip.getHeight() < 0) {
            y = clip.getY() + clip.getHeight();
            height = -clip.getHeight();
        } else {
            y = clip.getY();
            height = clip.getHeight();
        }
        return clip.copy(x, y, width, height);
    }

    /**
     * 屏幕截图
     *
     * @param path 截图文件全路径
     * @return base64编码后的图片数据
     */
    public String screenshot(String path) {
        return this.screenshot(new ScreenshotOptions(path));
    }

    /**
     * 生成当前页面的pdf格式，带着 pring css media。如果要生成带着 screen media的pdf，在page.pdf() 前面先调用 page.emulateMedia('screen')
     * <p><strong>注意 目前仅支持无头模式的 Chrome</strong></p>
     *
     * @param options 选项
     * @return pdf文件的字节数组数据
     * @throws IOException IO异常
     */
    public byte[] pdf(PDFOptions options) throws IOException {
        return this.pdf(options, LengthUnit.IN);
    }

    /**
     * 生成当前页面的pdf格式，带着 pring css media。如果要生成带着 screen media的pdf，在page.pdf() 前面先调用 page.emulateMedia('screen')
     * <p><strong>注意 目前仅支持无头模式的 Chrome</strong></p>
     *
     * @param path pdf存放的路径
     * @throws IOException IO异常
     */
    public void pdf(String path) throws IOException {
        this.pdf(new PDFOptions(path), LengthUnit.IN);
    }

    /**
     * 生成当前页面的pdf格式，带着 pring css media。如果要生成带着 screen media的pdf，在page.pdf() 前面先调用 page.emulateMedia('screen')
     * <p><strong>注意 目前仅支持无头模式的 Chrome</strong></p>
     *
     * @param options    选项
     * @param lengthUnit 单位
     * @return pdf文件的字节数组数据
     * @throws IOException IO异常
     */
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

    /**
     * page.close() 在 beforeunload 处理之前默认不执行
     * <p><strong>注意 如果 runBeforeUnload 设置为true，可能会弹出一个 beforeunload 对话框。 这个对话框需要通过页面的 'dialog' 事件手动处理</strong></p>
     */
    public void close() {
        this.close(false);
    }

    public void close(boolean runBeforeUnload) {
        synchronized (this.browserContext()) {
            ValidateUtil.assertArg(this.primaryTargetClient.getConnection() != null, "Protocol error: Connection closed. Most likely the page has been closed.");
            if (runBeforeUnload) {
                this.primaryTargetClient.send("Page.close");
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("targetId", this.primaryTarget.getTargetId());
                this.primaryTargetClient.getConnection().send("Target.closeTarget", params);
                this.tabTarget.waitForTargetClose();
            }
        }
    }

    /**
     * 表示页面是否被关闭。
     *
     * @return 页面是否被关闭。
     */
    public boolean isClosed() {
        return this.closed;
    }

    public Mouse mouse() {
        return mouse;
    }

    /**
     * 此方法通常与从 API（例如 WebBluetooth）触发设备请求的操作结合使用。<p>
     * 提醒<p>
     * 必须在发送设备请求之前调用此函数。它不会返回当前活动的设备提示。<p>
     * 默认等待事件是30s
     *
     * @return DeviceRequestPrompt 设备请求
     */
    public DeviceRequestPrompt waitForDevicePrompt() {
        return this.waitForDevicePrompt(this._timeoutSettings.timeout());
    }

    /**
     * 此方法通常与从 API（例如 WebBluetooth）触发设备请求的操作结合使用。<p>
     * 提醒<p>
     * 必须在发送设备请求之前调用此函数。它不会返回当前活动的设备提示。<p>
     *
     * @param timeout 超时时间,默认是30s
     * @return DeviceRequestPrompt
     */
    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        return this.mainFrame().waitForDevicePrompt(timeout);
    }

    /**
     * 此方法找到一个匹配 selector 选择器的元素，如果需要会把此元素滚动到可视，然后通过 page.mouse 点击它。 如果选择器没有匹配任何元素，此方法将会报错。
     * 默认是阻塞的，会等待点击完成指令返回
     *
     * @param selector 选择器
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public void click(String selector) throws JsonProcessingException, EvaluateException {
        this.click(selector, new ClickOptions());
    }


    public void click(String selector, ClickOptions options) throws JsonProcessingException, EvaluateException {
        this.mainFrame().click(selector, options);
    }

    /**
     * 此方法找到一个匹配selector的元素，并且把焦点给它。 如果没有匹配的元素，此方法将报错。
     *
     * @param selector 要给焦点的元素的选择器selector。如果有多个匹配的元素，焦点给第一个元素。
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public void focus(String selector) throws JsonProcessingException, EvaluateException {
        this.mainFrame().focus(selector);
    }

    /**
     * 此方法找到一个匹配的元素，如果需要会把此元素滚动到可视，然后通过 page.mouse 来hover到元素的中间。 如果没有匹配的元素，此方法将会报错。
     *
     * @param selector 要hover的元素的选择器。如果有多个匹配的元素，hover第一个。
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public void hover(String selector) throws JsonProcessingException, EvaluateException {
        this.mainFrame().hover(selector);
    }

    /**
     * 当提供的选择器完成选中后，触发change和input事件 如果没有元素匹配指定选择器，将报错。
     *
     * @param selector 要查找的选择器
     * @param values   要选择的选项的值。如果 select 具有 multiple 属性，则考虑所有值，否则仅考虑第一个值。
     * @return 选择器集合
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public List<String> select(String selector, List<String> values) throws JsonProcessingException, EvaluateException {
        return this.mainFrame().select(selector, values);
    }

    /**
     * 此方法找到一个匹配的元素，如果需要会把此元素滚动到视图中，然后通过 page.touchscreen 来点击元素的中间位置 如果没有匹配的元素，此方法会报错
     *
     * @param selector 要点击的元素的选择器。如果有多个匹配的元素，点击第一个
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public void tap(String selector) throws JsonProcessingException, EvaluateException {
        this.mainFrame().tap(selector);
    }

    /**
     * 每个字符输入后都会触发 keydown, keypress/input 和 keyup 事件
     * <p>要点击特殊按键，比如 Control 或 ArrowDown，用 keyboard.press</p>
     *
     * @param selector 要输入内容的元素选择器。如果有多个匹配的元素，输入到第一个匹配的元素。
     * @param text     要输入的内容
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public void type(String selector, String text) throws JsonProcessingException, EvaluateException {
        this.mainFrame().type(selector, text, 0);
    }

    /**
     * 每个字符输入后都会触发 keydown, keypress/input 和 keyup 事件
     * <p>要点击特殊按键，比如 Control 或 ArrowDown，用 keyboard.press</p>
     *
     * @param selector 要输入内容的元素选择器。如果有多个匹配的元素，输入到第一个匹配的元素。
     * @param text     要输入的内容
     * @param delay    每个字符输入的延迟，单位是毫秒。默认是 0。
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public void type(String selector, String text, long delay) throws JsonProcessingException, EvaluateException {
        this.mainFrame().type(selector, text, delay);
    }

    /**
     * 此方法在页面内执行 document.querySelector
     * <p>
     * 查找与选择器匹配的第一个元素,如果没有元素匹配指定选择器，返回值是 null。
     *
     * @param selector 要查询页面的 selector。CSS 选择器 可以按原样传递，Puppeteer 特定的选择器语法 允许通过 text、a11y 角色和名称、xpath 和 跨影子根组合这些查询 进行查询。或者，你可以使用前缀 prefix 指定选择器类型。
     * @return 匹配的第一个元素
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public ElementHandle $(String selector) throws JsonProcessingException, EvaluateException {
        return this.mainFrame().$(selector);
    }

    /**
     * 此方法在页面内执行 document.querySelectorAll。如果没有元素匹配指定选择器，返回值是 []。
     *
     * @param selector 选择器
     * @return ElementHandle集合
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public List<ElementHandle> $$(String selector) throws JsonProcessingException, EvaluateException {
        return this.mainFrame().$$(selector);
    }

    /**
     * 此方法和 page.evaluate 的唯一区别是此方法返回的是页内类型(JSHandle)
     *
     * @param pageFunction 要在页面实例上下文中执行的方法
     * @param args         要在页面实例上下文中执行的方法的参数
     * @return 代表页面元素的实例
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public JSHandle evaluateHandle(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pageFunction);
        return this.mainFrame().evaluateHandle(pageFunction, args);
    }

    /**
     * page.evaluate 和 page.evaluateHandle 之间的唯一区别是 evaluateHandle 将返回封装在页内对象中的值{@code JSHandle}。
     *
     * @param pageFunction 要执行的字符串
     * @return JSHandle
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public JSHandle evaluateHandle(String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluateHandle(pageFunction, null);
    }

    public Object $eval(String selector, String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.$eval(selector, pageFunction, null);
    }

    /**
     * 此方法在页面内执行 document.querySelector，然后把匹配到的元素作为第一个参数传给 pageFunction。
     *
     * @param selector     选择器
     * @param pageFunction 在浏览器实例上下文中要执行的方法
     * @param args         要传给 pageFunction 的参数。（比如你的代码里生成了一个变量，在页面中执行方法时需要用到，可以通过这个 args 传进去）
     * @return pageFunction 的返回值
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public Object $eval(String selector, String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("$eval", pageFunction);
        return this.mainFrame().$eval(selector, pageFunction, args);
    }

    /**
     * 此方法在页面内执行 Array.from(document.querySelectorAll(selector))，然后把匹配到的元素数组作为第一个参数传给 pageFunction。
     *
     * @param selector     一个框架选择器
     * @param pageFunction 在浏览器实例上下文中要执行的方法
     * @return pageFunction 的返回值
     * @throws JsonProcessingException JSON异常
     * @throws EvaluateException       函数执行错误
     */
    public Object $$eval(String selector, String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.$$eval(selector, pageFunction, new ArrayList<>());
    }

    /**
     * 此方法在页面内执行 Array.from(document.querySelectorAll(selector))，然后把匹配到的元素数组作为第一个参数传给 pageFunction。
     *
     * @param selector     一个框架选择器
     * @param pageFunction 在浏览器实例上下文中要执行的方法
     * @param args         要传给 pageFunction 的参数。（比如你的代码里生成了一个变量，在页面中执行方法时需要用到，可以通过这个 args 传进去）
     * @return pageFunction 的返回值
     * @throws EvaluateException       函数执行错误
     * @throws JsonProcessingException JSON异常
     */
    public Object $$eval(String selector, String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("$$eval", pageFunction);
        return this.mainFrame().$$eval(selector, pageFunction, args);
    }

    /**
     * 在当前文档的主要框架中添加脚本标签
     * <p>
     * 此方法封装了在当前文档的主要框架中添加一个脚本标签的操作它委托
     * {@link Frame#addScriptTag(FrameAddScriptTagOptions)} 方法来执行实际的操作
     *
     * @param options 脚本标签的配置选项，包含了脚本标签的各类属性如URL、位置等
     * @return 返回新添加的脚本标签的元素句柄
     * @throws IOException       当网络通信失败时抛出此异常
     * @throws EvaluateException 在页面中执行JavaScript时发生错误时抛出此异常
     */
    public ElementHandle addScriptTag(FrameAddScriptTagOptions options) throws IOException, EvaluateException {
        return this.mainFrame().addScriptTag(options);
    }


    /**
     * 将 link rel="stylesheet" 标记添加到具有所需 URL 的页面中，或将 style type="text/css" 标记添加到内容中。
     *
     * @param options link标签
     * @return 注入完成的tag标签。当style的onload触发或者代码被注入到frame。
     * @throws IOException       异常
     * @throws EvaluateException 在页面中执行JavaScript时发生错误时抛出此异常
     */
    public ElementHandle addStyleTag(FrameAddStyleTagOptions options) throws IOException, EvaluateException {
        return this.mainFrame().addStyleTag(options);
    }

    /**
     * 返回页面的地址
     *
     * @return 页面地址
     */
    public String url() {
        return this.mainFrame().url();
    }

    /**
     * 获取当前页面的内容
     * <p>
     * 此方法通过调用 mainFrame 的 content 方法来实现
     * 它封装了 mainFrame 的内容获取逻辑，以便在需要时统一处理可能的变化
     *
     * @return 当前页面的内容
     * @throws JsonProcessingException 如果在处理 JSON 时发生错误
     * @throws EvaluateException       在页面中执行JavaScript时发生错误时抛出此异常
     */
    public String content() throws JsonProcessingException, EvaluateException {
        return this.mainFrame().content();
    }

    /**
     * 给页面设置html
     *
     * @param html 分派给页面的HTML。
     * @throws JsonProcessingException 如果在处理 JSON 时发生错误
     * @throws EvaluateException       在页面中执行JavaScript时发生错误时抛出此异常
     */
    public void setContent(String html) throws JsonProcessingException, EvaluateException {
        this.setContent(html, new WaitForOptions());
    }

    /**
     * 给页面设置html
     *
     * @param html    分派给页面的HTML。
     * @param options timeout 加载资源的超时时间，默认值为30秒，传入0禁用超时. 可以使用 page.setDefaultNavigationTimeout(timeout) 或者 page.setDefaultTimeout(timeout) 方法修改默认值
     *                waitUntil  HTML设置成功的标志事件, 默认为 load。 如果给定的是一个事件数组，那么当所有事件之后，给定的内容才被认为设置成功。 事件可以是：
     *                load - load事件触发后，设置HTML内容完成。
     *                <p>domcontentloaded - DOMContentLoaded 事件触发后，设置HTML内容完成。</p>
     *                <p>networkidle0 - 不再有网络连接时（至少500毫秒之后），设置HTML内容完成。</p>
     *                <p> networkidle2 - 只剩2个网络连接时（至少500毫秒之后），设置HTML内容完成。</p>
     * @throws EvaluateException       在页面中执行JavaScript时发生错误时抛出此异常
     * @throws JsonProcessingException JSON异常
     */
    public void setContent(String html, WaitForOptions options) throws JsonProcessingException, EvaluateException {
        this.mainFrame().setContent(html, options);
    }

    /**
     * <p>导航到指定的url,可以配置是否阻塞，可以配合下面这个方法使用，但是不限于这个方法</p>
     * {@link Page#waitForResponse(String)}
     * 因为如果不阻塞的话，页面在加载完成时，waitForResponse等waitFor方法会接受不到结果而抛出超时异常
     *
     * @param url     导航的地址
     * @param isBlock true代表阻塞
     * @return 不阻塞的话返回null
     */
    public Response goTo(String url, boolean isBlock) {
        return this.goTo(url, new GoToOptions(), isBlock);
    }

    /**
     * <p>导航到指定的url
     * <p>以下情况此方法将报错：
     * <p>发生了 SSL 错误 (比如有些自签名的https证书).
     * <p>目标地址无效
     * <p>超时
     * <p>主页面不能加载
     *
     * @param url      url
     * @param options: <p>timeout 跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过page.setDefaultNavigationTimeout(timeout)方法修改默认值
     *                 <p>waitUntil  满足什么条件认为页面跳转完成，默认是 load 事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     *                 <p>load - 页面的load事件触发时
     *                 <p>domcontentloaded - 页面的 DOMContentLoaded 事件触发时
     *                 <p>networkidle0 - 不再有网络连接时触发（至少500毫秒后）
     *                 <p>networkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     *                 <p>referer  Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     * @return Response
     */
    public Response goTo(String url, GoToOptions options) {
        return this.goTo(url, options, true);
    }

    /**
     * <p>导航到指定的url
     * <p>以下情况此方法将报错：
     * <p>发生了 SSL 错误 (比如有些自签名的https证书).
     * <p>目标地址无效
     * <p>超时
     * <p>主页面不能加载
     *
     * @param url      url
     * @param options: <p>timeout 跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过page.setDefaultNavigationTimeout(timeout)方法修改默认值
     *                 <p>waitUntil  满足什么条件认为页面跳转完成，默认是 load 事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     *                 <p>load - 页面的load事件触发时
     *                 <p>domcontentloaded - 页面的 DOMContentLoaded 事件触发时
     *                 <p>networkidle0 - 不再有网络连接时触发（至少500毫秒后）
     *                 <p>networkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     *                 <p>referer  Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     * @param isBlock  是否阻塞，不阻塞代表只是发导航命令出去，并不等待导航结果，同时也不会抛异常
     * @return Response
     */
    public Response goTo(String url, GoToOptions options, boolean isBlock) {
        return this.mainFrame().goTo(url, options, isBlock);
    }

    /**
     * 创建一个page对象
     *
     * @param client   与页面通讯的客户端
     * @param target   目标
     * @param viewport 视图
     * @return 页面实例
     */
    public static Page create(CDPSession client, Target target, Viewport viewport) {
        Page page = new Page(client, target);
        page.initialize();
        if (viewport != null) {
            page.setViewport(viewport);
        }
        return page;
    }

    /**
     * 导航到某个网站
     * <p>以下情况此方法将报错：</p>
     * <p>发生了 SSL 错误 (比如有些自签名的https证书).</p>
     * <p>目标地址无效</p>
     * <p>超时</p>
     * <p>主页面不能加载</p>
     *
     * @param url 导航到的地址. 地址应该带有http协议, 比如 https://.
     * @return 响应
     */
    public Response goTo(String url) {
        return this.goTo(url, true);
    }

    /**
     * 此方法在页面跳转到一个新地址或重新加载时解析，如果你的代码会间接引起页面跳转，这个方法比较有用
     * <p>比如你在在代码中使用了Page.click()方法，引起了页面跳转
     * 注意 通过 History API 改变地址会认为是一次跳转。
     *
     * @return 响应
     */
    public Response waitForNavigation() {
        return this.mainFrame().waitForNavigation(new WaitForOptions(), false);
    }

    /**
     * 此方法在页面跳转到一个新地址或重新加载时解析，如果你的代码会间接引起页面跳转，这个方法比较有用
     * <p>比如你在在代码中使用了Page.click()方法，引起了页面跳转
     * 注意 通过 History API 改变地址会认为是一次跳转。
     *
     * @param options 可选的等待选项
     * @return 响应
     */
    public Response waitForNavigation(WaitForOptions options, boolean reload) {
        return this.mainFrame().waitForNavigation(options, reload);
    }

    /**
     * 此方法在页面跳转到一个新地址或重新加载时解析，如果你的代码会间接引起页面跳转，这个方法比较有用
     * <p>比如你在在代码中使用了Page.click()方法，引起了页面跳转
     * 注意 通过 History API 改变地址会认为是一次跳转。
     *
     * @param options 可选的等待选项
     * @return 响应
     */
    public Response waitForNavigation(WaitForOptions options) {
        return this.mainFrame().waitForNavigation(options, false);
    }

    /**
     * 等到某个请求，url或者predicate只有有一个不为空,默认等待时间是30s
     *
     * @param url 等待的请求
     * @return 要等到的请求
     */
    public Request waitForRequest(String url) {
        ValidateUtil.assertArg(StringUtil.isNotEmpty(url), "waitForRequest url must not be empty");
        return this.waitForRequest(url, null, this._timeoutSettings.timeout());
    }

    /**
     * 等到某个请求，url或者predicate只有有一个不为空
     * <p>当url不为空时， type = PageEvaluateType.STRING </p>
     * <p>当predicate不为空时， type = PageEvaluateType.FUNCTION </p>
     *
     * @param url       等待的请求
     * @param predicate 方法
     * @param timeout   超时时间
     * @return 要等到的请求
     */
    public Request waitForRequest(String url, Predicate<Request> predicate, int timeout) {
        if (timeout < 0) {
            timeout = this._timeoutSettings.timeout();
        }
        AtomicReference<Request> result = new AtomicReference<>();
        Predicate<Request> requestPredicate = request -> {
            if (StringUtil.isNotEmpty(url)) {
                return url.equals(request.url());
            } else if (predicate != null) {
                return predicate.test(request);
            }
            return false;
        };
        AtomicReference<TargetCloseException> targetCloseException = new AtomicReference<>();
        Consumer<Object> targetCloseListener = (ignore) -> targetCloseException.set(new TargetCloseException("Page closed!"));
        this.once(PageEvent.Close, targetCloseListener);
        Consumer<Request> requestListener = request -> {
            if (requestPredicate.test(request)) {
                result.set(request);
            }
        };
        this.on(PageEvent.Request, requestListener);
        Supplier<Boolean> conditionChecker = () -> {
            if (targetCloseException.get() != null) {
                throw targetCloseException.get();
            }
            return result.get() == null ? null : true;
        };
        try {
            Helper.waitForCondition(conditionChecker, timeout, "WaitForRequest timeout of " + timeout + " ms exceeded");
        } finally {
            this.off(PageEvent.Request, requestListener);
            this.off(PageEvent.Close, targetCloseListener);
        }
        return result.get();
    }

    /**
     * 等到某个请求,默认等待的时间是30s
     *
     * @param predicate 判断具体某个请求
     * @return 要等到的请求
     */
    public Response waitForResponse(Predicate<Response> predicate) {
        return this.waitForResponse(null, predicate);
    }

    /**
     * 等到某个请求,默认等待的时间是30s
     *
     * @param url 等待的请求
     * @return 要等到的请求
     */
    public Response waitForResponse(String url) {
        return this.waitForResponse(url, null, this._timeoutSettings.timeout());
    }

    /**
     * 等到某个请求，url或者predicate只有有一个不为空,默认等待的时间是30s
     * <p>当url不为空时， type = PageEvaluateType.STRING </p>
     * <p>当predicate不为空时， type = PageEvaluateType.FUNCTION </p>
     *
     * @param url       等待的请求
     * @param predicate 方法
     * @return 要等到的请求
     */
    public Response waitForResponse(String url, Predicate<Response> predicate) {
        return this.waitForResponse(url, predicate, this._timeoutSettings.timeout());
    }

    /**
     * 等到某个请求，url或者predicate只有有一个不为空
     * <p>当url不为空时， type = PageEvaluateType.STRING </p>
     * <p>当predicate不为空时， type = PageEvaluateType.FUNCTION </p>
     *
     * @param url       等待的请求
     * @param predicate 方法
     * @param timeout   超时时间
     * @return 要等到的请求
     */
    public Response waitForResponse(String url, Predicate<Response> predicate, int timeout) {
        if (timeout <= 0) timeout = this._timeoutSettings.timeout();
        Predicate<Response> predi = response -> {
            if (StringUtil.isNotEmpty(url)) {
                return url.equals(response.url());
            } else if (predicate != null) {
                return predicate.test(response);
            }
            return false;
        };
        AtomicReference<Response> result = new AtomicReference<>();
        Consumer<Response> responseListener = response -> {
            if (predi.test(response)) {
                result.set(response);
            }
        };
        this.on(PageEvent.Response, responseListener);
        AtomicReference<TargetCloseException> targetCloseException = new AtomicReference<>();
        this.once(PageEvent.Close, (s) -> targetCloseException.set(new TargetCloseException("Page closed!")));
        Supplier<Boolean> conditonChecker = () -> {
            if (targetCloseException.get() != null) {
                throw targetCloseException.get();
            }
            return result.get() == null ? null : true;
        };
        try {
            Helper.waitForCondition(conditonChecker, timeout, "WaitForResponse timeout of " + timeout + " ms exceeded");
        } finally {
            this.off(PageEvent.Response, responseListener);
        }
        return result.get();
    }

    /**
     * 等待匹配给定条件的帧出现。
     * <p>
     * 默认超时时间是30s
     *
     * @param url 匹配帧的url
     * @return 匹配的帧
     */
    public Frame waitForFrame(String url) {
        return this.waitForFrame(url, null, Constant.DEFAULT_TIMEOUT);
    }

    /**
     * 等待匹配给定条件的帧出现。
     * <p>
     * 默认超时时间是30s
     *
     * @param framePredicate 匹配的表达式
     * @return 匹配的帧
     */
    public Frame waitForFrame(Predicate<Frame> framePredicate) {
        return this.waitForFrame(null, framePredicate, Constant.DEFAULT_TIMEOUT);
    }

    /**
     * 等待匹配给定条件的帧出现。
     *
     * @param url            等待的url
     * @param framePredicate 匹配的规则
     * @param timeout        超时时间
     * @return 匹配的帧
     */
    public Frame waitForFrame(String url, Predicate<Frame> framePredicate, int timeout) {
        if (timeout <= 0) timeout = this._timeoutSettings.timeout();
        Predicate<Frame> predicate = frame -> {
            if (StringUtil.isNotEmpty(url)) {
                return url.equals(frame.url());
            } else if (framePredicate != null) {
                return framePredicate.test(frame);
            }
            return false;
        };
        AtomicReference<TargetCloseException> targetCloseException = new AtomicReference<>();
        this.once(PageEvent.Close, (s) -> targetCloseException.set(new TargetCloseException("Page closed!")));
        Supplier<Frame> conditionChecker = () -> {
            if (targetCloseException.get() != null) {
                throw targetCloseException.get();
            }
            return Helper.filter(this.frames(), predicate);
        };
        return Helper.waitForCondition(conditionChecker, timeout, "WaitForFrame timeout of " + timeout + " ms exceeded");
    }

    /**
     * 执行一段 JavaScript代码
     *
     * @param pageFunction 要执行的字符串
     * @return pageFunction执行结果
     * @throws EvaluateException       运行JS代码时，可能抛出的异常
     * @throws JsonProcessingException json序列化异常
     */
    public Object evaluate(String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluate(pageFunction, null);
    }

    /**
     * 执行一段 JavaScript代码
     *
     * @param pageFunction 要执行的字符串
     * @param args         如果pageFunction 是 Javascript 函数的话，args就是函数上的参数
     * @return pageFunction执行结果
     * @throws EvaluateException       运行JS代码时，可能抛出的异常
     * @throws JsonProcessingException json序列化异常
     */
    public Object evaluate(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluate", pageFunction);
        return this.mainFrame().evaluate(pageFunction, args);
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
        Dialog dialog = new Dialog(this.primaryTargetClient, event.getType(), event.getMessage(), event.getDefaultPrompt());
        this.emit(PageEvent.Dialog, dialog);
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

    /**
     * 等待指定的选择器匹配的元素出现在页面中，如果调用此方法时已经有匹配的元素，那么此方法立即返回。 如果指定的选择器在超时时间后扔不出现，此方法会报错。
     *
     * @param selector 要等待的元素选择器
     * @return ElementHandle
     */
    public ElementHandle waitForSelector(String selector) throws JsonProcessingException {
        return this.waitForSelector(selector, new WaitForSelectorOptions());
    }

    /**
     * 等待指定的选择器匹配的元素出现在页面中，如果调用此方法时已经有匹配的元素，那么此方法立即返回。 如果指定的选择器在超时时间后扔不出现，此方法会报错。
     *
     * @param selector 要等待的元素选择器
     * @param options  可选参数
     * @return ElementHandle 返回的handle
     */
    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws JsonProcessingException {
        return this.mainFrame().waitForSelector(selector, options);
    }

    /**
     * 等待提供的函数 pageFunction 在页面上下文中计算时返回真值。
     *
     * @param pageFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @return JSHandle 指定的页面元素 对象
     */
    public JSHandle waitForFunction(String pageFunction) {
        return this.waitForFunction(pageFunction, new WaitForSelectorOptions());
    }


    /**
     * 等待提供的函数 pageFunction 在页面上下文中计算时返回真值。
     *
     * @param pageFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @param options      等待函数的选项，包括超时设置等
     * @param args         传递给函数的参数，可以是任意类型
     * @return JSHandle 返回函数执行结果的JSHandle对象
     */
    public JSHandle waitForFunction(String pageFunction, WaitForSelectorOptions options, Object... args) {
        return this.mainFrame().waitForFunction(pageFunction, options, args == null ? null : Arrays.asList(args));
    }

    private static final Map<String, Double> unitToPixels = new HashMap<String, Double>() {
        private static final long serialVersionUID = -4861220887908575532L;

        {
            put("px", 1.00);
            put("in", 96.00);
            put("cm", 37.8);
            put("mm", 3.78);
        }
    };

    private Double convertPrintParameterToInches(String parameter, LengthUnit lengthUnit) {
        if (StringUtil.isEmpty(parameter)) {
            return null;
        }
        double pixels;
        if (Helper.isNumber(parameter)) {
            pixels = Double.parseDouble(parameter);
        } else if (parameter.endsWith("px") || parameter.endsWith("in") || parameter.endsWith("cm") || parameter.endsWith("mm")) {
            String unit = parameter.substring(parameter.length() - 2).toLowerCase();
            String valueText;
            if (unitToPixels.containsKey(unit)) {
                valueText = parameter.substring(0, parameter.length() - 2);
            } else {
                // In case of unknown unit try to parse the whole parameter as number of pixels.
                // This is consistent with phantom's paperSize behavior.
                unit = "px";
                valueText = parameter;
            }
            double value = Double.parseDouble(valueText);
            ValidateUtil.assertArg(!Double.isNaN(value), "Failed to parse parameter value: " + parameter);
            pixels = value * unitToPixels.get(unit);
        } else {
            throw new IllegalArgumentException("page.pdf() Cannot handle parameter type: " + parameter);
        }
        return pixels / unitToPixels.get(lengthUnit.getValue());
    }

    private ConsoleMessageType convertConsoleMessageLevel(String method) {
        if ("warning".equals(method)) {
            return ConsoleMessageType.WARN;
        }
        return ConsoleMessageType.valueOf(method.toUpperCase());
    }

    /**
     * 获取当前主框架的标题
     * <p>
     * 该方法通过调用主框架的title方法来获取页面标题如果在处理JSON数据时发生错误,该方法将抛出JsonProcessingException异常
     *
     * @return 当前主框架的标题
     * @throws JsonProcessingException 如果在处理JSON数据时发生错误
     */
    public String title() throws JsonProcessingException {
        return this.mainFrame().title();
    }

    /**
     * 模拟各种设备打开浏览器
     * <p>
     * 所有录制内容均为 WebM 格式，使用 VP9 视频编解码器。FPS 为 30。
     * <p>
     * 你的系统上必须安装有 ffmpeg。
     *
     * @param device 设备类型
     */
    public void emulate(Device device) {
        this.setUserAgent(device.getUserAgent());
        this.setViewport(device.getViewport());
    }

    public void setIsDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }

    public boolean isDragging() {
        return this.isDragging;
    }

    public Accessibility accessibility() {
        return this.mainFrame().accessibility();
    }

    AtomicLong screencastSessionCount = new AtomicLong(0);
    private volatile boolean startScreencasted = false;

    /**
     * 捕获此 page 的截屏视频。可录制为webm和gif
     *
     * @param options 配置截屏行为
     * @return ScreenRecorder 屏幕录制实例，用于停止录制
     * @throws IOException IO异常
     */
    public ScreenRecorder screencast(ScreencastOptions options) throws IOException {
        ValidateUtil.assertArg(StringUtil.isNotBlank(options.getPath()), "Path must be specified");
        if (options.getFormat() != null) {
            ValidateUtil.assertArg(options.getPath().endsWith(options.getFormat().getFormat()), "Extension of Path (" + options.getPath() + ")+ has to match the used output format (" + options.getFormat().getFormat() + ").");
        }
        Viewport defaultViewport = this.viewport();
        Viewport tempViewport = null;
        if (defaultViewport != null && defaultViewport.getDeviceScaleFactor() != 0) {
            tempViewport = new Viewport(defaultViewport.getWidth(), defaultViewport.getHeight(), 0.00, defaultViewport.getIsMobile(), defaultViewport.getHasTouch(), defaultViewport.getIsLandscape());
            this.setViewport(tempViewport);
        }
        ArrayList<?> response = (ArrayList<?>) this.mainFrame().isolatedRealm().evaluate("() => {\n" +
                "                    return [\n" +
                "                      window.visualViewport.width * window.devicePixelRatio,\n" +
                "                      window.visualViewport.height * window.devicePixelRatio,\n" +
                "                      window.devicePixelRatio,\n" +
                "                    ]\n" +
                "                }");
        double width = Double.parseDouble(response.get(0) + "");
        double height = Double.parseDouble(response.get(1) + "");
        double devicePixelRatio = Double.parseDouble(response.get(2) + "");
        BoundingBox crop = null;
        if (Objects.nonNull(options.getCrop())) {
            BoundingBox boundingBox = roundRectangle(normalizeRectangle(new ScreenshotClip(options.getCrop().getX(), options.getCrop().getY(), options.getCrop().getWidth(), options.getCrop().getHeight())));
            if (boundingBox.getX() < 0 || boundingBox.getY() < 0) {
                throw new JvppeteerException("crop.x and crop.y must be greater than or equal to 0.");
            }
            if (boundingBox.getWidth() <= 0 || boundingBox.getHeight() <= 0) {
                throw new JvppeteerException("crop.width and crop.height must be greater than 0.");
            }

            double viewportWidth = width / devicePixelRatio;
            double viewportHeight = height / devicePixelRatio;
            if (boundingBox.getX() + boundingBox.getWidth() > viewportWidth) {
                throw new JvppeteerException(
                        "crop.width cannot be larger than the viewport width(" + viewportWidth + ")");
            }
            if (boundingBox.getY() + boundingBox.getHeight() > viewportHeight) {
                throw new JvppeteerException(
                        "crop.height cannot be larger than the viewport width(" + viewportHeight + ")");
            }
            crop = new BoundingBox(boundingBox.getX() * devicePixelRatio, boundingBox.getY() * devicePixelRatio, boundingBox.getWidth() * devicePixelRatio, boundingBox.getHeight() * devicePixelRatio);
        }
        if (options.getSpeed() <= 0) {
            throw new JvppeteerException("speed must be greater than 0.");
        }
        if (options.getScale() <= 0) {
            throw new JvppeteerException("scale must be greater than 0.");
        }
        ScreenRecorder recorder = new ScreenRecorder(this, width, height, new ScreenRecorderOptions(options.getSpeed(), crop, options.getPath(), options.getFormat(), options.getScale(), options.getFfmpegPath()), defaultViewport, tempViewport);
        try {
            this.startScreencast();
        } catch (Exception e) {
            recorder.stop();
            LOGGER.error("startScreencast error: ", e);
            return null;
        }
        return recorder;

    }

    private void startScreencast() {
        screencastSessionCount.incrementAndGet();
        if (!startScreencasted) {
            synchronized (this) {
                if (!this.startScreencasted) {
                    AwaitableResult<Boolean> awaitableResult = AwaitableResult.create();
                    this.mainFrame().client().on(CDPSession.CDPSessionEvent.Page_screencastFrame, (Consumer<ScreencastFrameEvent>) event -> {
                        awaitableResult.complete();
                    });
                    Map<String, Object> params = ParamsFactory.create();
                    params.put("format", "png");
                    this.mainFrame().client().send("Page.startScreencast", params);
                    awaitableResult.waiting();
                    this.startScreencasted = true;
                }
            }
        }
    }

    public void stopScreencast() {
        long count = screencastSessionCount.decrementAndGet();
        if (!this.startScreencasted) {
            return;
        }
        this.startScreencasted = false;
        if (count == 0) {
            this.mainFrame().client().send("Page.stopScreencast");
        }
    }

    public enum PageEvent {
        /**
         * 页面关闭时候发射该事件
         */
        Close("close"),
        /**
         * 当页面中的 JavaScript 调用控制台 API 方法之一时发出，
         * 例如 'console.log' 或 'console.dir'。如果页面抛出
         * 错误或警告,也会触发该事件
         * <p>
         * {@link  ConsoleMessage} 代表一个console事件的相关信息
         */
        Console("console"),
        /**
         * 当 JavaScript 对话框出现时触发，例如 alert、prompt、confirm 或 beforeunload。Jvppeteer 可以通过 {@link Dialog#accept} or {@link Dialog#dismiss}. 响应对话。
         * <p>
         * {@link  Dialog} 代表一个dialog事件的相关信息
         */
        Dialog("dialog"),
        /**
         * 当页面加载到加载到DOMContentLoaded时触发<p>
         * <a href="https://developer.mozilla.org/en-US/docs/Web/Events/DOMContentLoaded">点击查看DOMContentLoaded介绍</a>
         */
        Domcontentloaded("domcontentloaded"),
        /**
         * 当页面崩溃时触发，返回一个 {@link Error}
         */
        Error("error"),
        /**
         * 当一个Frame被添加时触发，返回一个 {@link Frame}
         */
        FrameAttached("frameattached"),
        /**
         * 当一个Frame被移除时触发，返回一个 {@link Frame}
         */
        FrameDetached("framedetached"),
        /**
         * 当一个Frame 被导航到新的URL时触发，返回一个 {@link Frame}.
         */
        FrameNavigated("framenavigated"),
        /**
         * load 事件在加载整个页面时触发，包括所有依赖资源，例如样式表、脚本、iframe 和图像，但延迟加载的资源除外。 这与 DOMContentLoaded 相反，后者在页面 DOM 加载后立即触发，而无需等待资源完成加载。
         * <p>
         * 此事件不可取消，也不会冒泡。
         * <a href="https://developer.mozilla.org/en-US/docs/Web/Events/load">点击查看load</a>
         */
        Load("load"),
        /**
         * 当 JavaScript 代码调用 `console.timeStamp` 时触发此事件。
         * metrics列表 见 {@link Page#metrics}.
         * <p>
         * {@link  PageMetrics} 代表一个dialog事件的相关信息
         * <p>
         */
        Metrics("metrics"),
        /**
         * 当页面内发生未捕获的异常时触发。包含 {@link PageEvent#Error}。
         */
        PageError("pageerror"),
        /**
         * 当页面打开新选项卡或窗口时触发。<p>
         * 包含与弹出窗口对应的页。<p>
         * {@link  Page} 代表一个popup事件的相关信息
         */
        Popup("popup"),
        /**
         * 当页面发送请求并包含 HTTPRequest 时触发。
         * <p>
         * 该对象是只读的。请参阅 Page.setRequestInterception() 了解拦截和修改请求。
         */
        Request("request"),
        /**
         * 当请求最终从缓存加载时触发。包含 HTTPRequest。
         * <p>
         * 对于某些请求，可能包含未定义。<a href="https://crbug.com/750469">具体见这里</a>
         */
        RequestServedFromCache("requestservedfromcache"),
        /**
         * 当请求失败时触发，例如超时。
         * {@link Request}.代表一个requestfailed事件的相关信息
         * <p>
         * 包含 Request。
         * <p>
         * 从 HTTP 角度来看，HTTP 错误响应（例如 404 或 503）仍然是成功响应，因此请求将通过 requestfinished 事件完成，而不是通过 requestfailed 事件完成。
         */
        RequestFailed("requestfailed"),
        /**
         * 当请求成功完成时触发。包含 Request。
         * <p>
         * {@link Request}.代表一个requestfinished事件的相关信息
         */
        RequestFinished("requestfinished"),
        /**
         * 收到响应时触发。包含 HTTPResponse。
         * {@link Response}.代表一个response事件的相关信息
         */
        Response("response"),
        /**
         * 当页面生成专用 WebWorker 时触发。
         * <p>
         * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">了解WebWorker</a>
         */
        WorkerCreated("workercreated"),
        /**
         * 当页面生成专用 WebWorker 时触发。
         * <p>
         * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">了解WebWorker</a>
         */
        WorkerDestroyed("workerdestroyed");

        private String eventName;

        PageEvent(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }
}
