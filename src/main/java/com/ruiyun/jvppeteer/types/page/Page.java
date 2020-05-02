package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.EmulationManager;
import com.ruiyun.jvppeteer.events.definition.EventHandler;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.impl.EventEmitter;
import com.ruiyun.jvppeteer.exception.NavigateException;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.Device;
import com.ruiyun.jvppeteer.options.PDFOptions;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.accessbility.Accessibility;
import com.ruiyun.jvppeteer.protocol.console.ConsoleMessage;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.coverage.Coverage;
import com.ruiyun.jvppeteer.protocol.dom.ElementHandle;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.protocol.log.Dialog;
import com.ruiyun.jvppeteer.protocol.network.DeleteCookiesParameters;
import com.ruiyun.jvppeteer.protocol.performance.Metric;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;
import com.ruiyun.jvppeteer.protocol.target.Target;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.protocol.work.Worker;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.types.browser.Browser;
import com.ruiyun.jvppeteer.types.browser.BrowserContext;
import com.ruiyun.jvppeteer.types.page.frame.Frame;
import com.ruiyun.jvppeteer.types.page.frame.FrameManager;
import com.ruiyun.jvppeteer.types.page.frame.Keyboard;
import com.ruiyun.jvppeteer.types.page.frame.Mouse;
import com.ruiyun.jvppeteer.types.page.frame.Request;
import com.ruiyun.jvppeteer.types.page.frame.Touchscreen;
import com.ruiyun.jvppeteer.types.page.payload.BindingCalledPayload;
import com.ruiyun.jvppeteer.types.page.payload.ConsoleAPICalledPayload;
import com.ruiyun.jvppeteer.types.page.payload.Credentials;
import com.ruiyun.jvppeteer.types.page.payload.EntryAddedPayload;
import com.ruiyun.jvppeteer.types.page.payload.FileChooserOpenedPayload;
import com.ruiyun.jvppeteer.types.page.payload.GetNavigationHistoryReturnValue;
import com.ruiyun.jvppeteer.types.page.payload.JavascriptDialogOpeningPayload;
import com.ruiyun.jvppeteer.types.page.payload.Margin;
import com.ruiyun.jvppeteer.types.page.payload.MediaFeature;
import com.ruiyun.jvppeteer.types.page.payload.Metrics;
import com.ruiyun.jvppeteer.types.page.payload.MetricsPayload;
import com.ruiyun.jvppeteer.types.page.payload.NavigationEntry;
import com.ruiyun.jvppeteer.types.page.payload.NetworkManager;
import com.ruiyun.jvppeteer.types.page.payload.Response;
import com.ruiyun.jvppeteer.types.page.trace.Tracing;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.ruiyun.jvppeteer.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.Constant.supportedMetrics;

public class Page extends EventEmitter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Page.class);

    private Set<Object> fileChooserInterceptors;

    private boolean closed = true;

    @JsonIgnore
    private CDPSession client;

    private Target target;

    @JsonIgnore
    private Keyboard keyboard;

    @JsonIgnore
    private Mouse mouse;

    private TimeoutSettings timeoutSettings;

    @JsonIgnore
    private Touchscreen touchscreen;

    @JsonIgnore
    private Accessibility accessibility;

    @JsonIgnore
    private FrameManager frameManager;

    @JsonIgnore
    private EmulationManager emulationManager;

    @JsonIgnore
    private Tracing tracing;

    private Map<String, Function> pageBindings;

    @JsonIgnore
    private Coverage coverage;

    private boolean javascriptEnabled;

    private Viewport viewport;

    private TaskQueue screenshotTaskQueue;

    private Map<String, Worker> workers;

    public static final String ABOUT_BLANK = "about:blank";

    public static final Map<String,Double> unitToPixels = new HashMap(){
        {
            put("px",1.00);
            put("in",96);
            put("cm",37.8);
            put("mm",3.78);
        }
    };
    public Page(CDPSession client, Target target, boolean ignoreHTTPSErrors, TaskQueue screenshotTaskQueue) {
        super();
        this.closed = false;
        this.client = client;
        this.client.getConnection();
        System.out.println("this.client.getConnection();" + this.client.getConnection());
        this.target = target;
        this.keyboard = new Keyboard(client);
        this.mouse = new Mouse(client, keyboard);
        this.timeoutSettings = new TimeoutSettings();
        this.touchscreen = new Touchscreen(client, keyboard);
        this.accessibility = new Accessibility(client);
        this.frameManager = new FrameManager(client, this, ignoreHTTPSErrors, timeoutSettings);
        this.emulationManager = new EmulationManager(client);
        this.tracing = new Tracing(client);
        this.pageBindings = new HashMap<>();
        this.coverage = new Coverage(client);
        this.javascriptEnabled = true;
        this.viewport = null;
        this.screenshotTaskQueue = screenshotTaskQueue;
        this.workers = new HashMap<>();
        DefaultBrowserListener<Target> attachedListener = new DefaultBrowserListener<Target>() {
            @Override
            public void onBrowserEvent(Target event) {
                Page page = (Page) this.getTarget();
                if (!"worker".equals(event.getTargetInfo().getType())) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("sessionId", event.getSessionId());
                        /*
                          If we don't detach from service workers, they will never die
                         */
                    client.send("Target.detachFromTarget", params, false);
                    return;
                }
                CDPSession session = Connection.fromSession(page.getClient()).session(event.getSessionId());
                Worker worker = new Worker(session, event.getTargetInfo().getUrl(), page::addConsoleMessage, page::handleException);
                page.getWorkers().putIfAbsent(event.getSessionId(), worker);
                page.emit(Events.PAGE_WORKERCREATED.getName(), worker);
            }
        };
        attachedListener.setMothod("Target.attachedToTarget");
        attachedListener.setTarget(this);
        attachedListener.setResolveType(Target.class);
        this.client.on(attachedListener.getMothod(), attachedListener);

        DefaultBrowserListener<Target> detachedListener = new DefaultBrowserListener<Target>() {
            @Override
            public void onBrowserEvent(Target event) {
                Page page = (Page) this.getTarget();
                Worker worker = page.getWorkers().get(event.getSessionId());
                if (worker == null) {
                    return;
                }
                page.emit(Events.PAGE_WORKERDESTROYED.getName(), worker);
                page.getWorkers().remove(event.getSessionId());
            }
        };
        detachedListener.setMothod("Target.detachedFromTarget");
        detachedListener.setTarget(this);
        detachedListener.setResolveType(Target.class);
        this.client.on(detachedListener.getMothod(), detachedListener);

        DefaultBrowserListener<Object> frameAttachedListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_FRAMEATTACHED.getName(), event);
            }
        };
        frameAttachedListener.setMothod(Events.FRAME_MANAGER_FRAME_ATTACHED.getName());
        frameAttachedListener.setTarget(this);
        this.frameManager.on(frameAttachedListener.getMothod(), frameAttachedListener);

        DefaultBrowserListener<Object> frameDetachedListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_FRAMEDETACHED.getName(), event);
            }
        };
        frameDetachedListener.setMothod(Events.FRAME_MANAGER_FRAME_DETACHED.getName());
        frameDetachedListener.setTarget(this);
        this.frameManager.on(frameDetachedListener.getMothod(), frameDetachedListener);

        DefaultBrowserListener<Object> frameNavigatedListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_FRAMENAVIGATED.getName(), event);
            }
        };
        frameNavigatedListener.setMothod(Events.FRAME_MANAGER_FRAME_NAVIGATED.getName());
        frameNavigatedListener.setTarget(this);
        this.frameManager.on(frameNavigatedListener.getMothod(), frameNavigatedListener);

        NetworkManager networkManager = this.frameManager.getNetworkManager();

        DefaultBrowserListener<Request> requestLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_REQUEST.getName(), event);
            }
        };
        requestLis.setMothod(Events.NETWORK_MANAGER_REQUEST.getName());
        requestLis.setTarget(this);
        networkManager.on(requestLis.getMothod(), requestLis);

        DefaultBrowserListener<Response> responseLis = new DefaultBrowserListener<Response>() {
            @Override
            public void onBrowserEvent(Response event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_RESPONSE.getName(), event);
            }
        };
        responseLis.setMothod(Events.NETWORK_MANAGER_RESPONSE.getName());
        responseLis.setTarget(this);
        networkManager.on(responseLis.getMothod(), responseLis);

        DefaultBrowserListener<Request> requestFailedLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_REQUESTFAILED.getName(), event);
            }
        };
        requestFailedLis.setMothod(Events.NETWORK_MANAGER_REQUEST_FAILED.getName());
        requestFailedLis.setTarget(this);
        networkManager.on(requestFailedLis.getMothod(), requestFailedLis);

        DefaultBrowserListener<Request> requestFinishedLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_REQUESTFINISHED.getName(), event);
            }
        };
        requestFinishedLis.setMothod(Events.NETWORK_MANAGER_REQUEST_FINISHED.getName());
        requestFinishedLis.setTarget(this);
        networkManager.on(requestFinishedLis.getMothod(), requestFinishedLis);

        //TODO ${fileChooserInterceptors}
        this.fileChooserInterceptors = new HashSet<>();

        DefaultBrowserListener<Object> domContentEventFiredLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_DOMContentLoaded.getName(), event);
            }
        };
        domContentEventFiredLis.setMothod("Page.domContentEventFired");
        domContentEventFiredLis.setTarget(this);
        this.client.on(domContentEventFiredLis.getMothod(), domContentEventFiredLis);

        DefaultBrowserListener<Object> loadEventFiredLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.emit(Events.PAGE_LOAD.getName(), event);
            }
        };
        loadEventFiredLis.setMothod("Page.loadEventFired");
        loadEventFiredLis.setTarget(this);
        this.client.on(loadEventFiredLis.getMothod(), loadEventFiredLis);

        DefaultBrowserListener<ConsoleAPICalledPayload> consoleAPICalledLis = new DefaultBrowserListener<ConsoleAPICalledPayload>() {
            @Override
            public void onBrowserEvent(ConsoleAPICalledPayload event) {
                Page page = (Page) this.getTarget();
                page.onConsoleAPI(event);
            }
        };
        consoleAPICalledLis.setMothod("Runtime.consoleAPICalled");
        consoleAPICalledLis.setTarget(this);
        this.client.on(consoleAPICalledLis.getMothod(), consoleAPICalledLis);

        DefaultBrowserListener<BindingCalledPayload> bindingCalledLis = new DefaultBrowserListener<BindingCalledPayload>() {
            @Override
            public void onBrowserEvent(BindingCalledPayload event) {
                Page page = (Page) this.getTarget();
                page.onBindingCalled(event);
            }
        };
        bindingCalledLis.setMothod("Runtime.bindingCalled");
        bindingCalledLis.setTarget(this);
        this.client.on(bindingCalledLis.getMothod(), bindingCalledLis);

        DefaultBrowserListener<JavascriptDialogOpeningPayload> javascriptDialogOpeningLis = new DefaultBrowserListener<JavascriptDialogOpeningPayload>() {
            @Override
            public void onBrowserEvent(JavascriptDialogOpeningPayload event) {
                Page page = (Page) this.getTarget();
                page.onDialog(event);
            }
        };
        javascriptDialogOpeningLis.setMothod("Page.javascriptDialogOpening");
        javascriptDialogOpeningLis.setTarget(this);
        this.client.on(javascriptDialogOpeningLis.getMothod(), javascriptDialogOpeningLis);

        DefaultBrowserListener<JsonNode> exceptionThrownLis = new DefaultBrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                Page page = (Page) this.getTarget();
                JsonNode exceptionDetails = event.get("exceptionDetails");
                try {
                    if (exceptionDetails == null) {
                        return;
                    }
                    ExceptionDetails value = OBJECTMAPPER.treeToValue(exceptionDetails, ExceptionDetails.class);
                    page.handleException(value);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        exceptionThrownLis.setMothod("Runtime.exceptionThrown");
        exceptionThrownLis.setTarget(this);
        this.client.on(exceptionThrownLis.getMothod(), exceptionThrownLis);

        DefaultBrowserListener<Object> targetCrashedLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page) this.getTarget();
                page.onTargetCrashed();
            }
        };
        targetCrashedLis.setMothod("Inspector.targetCrashed");
        targetCrashedLis.setTarget(this);
        this.client.on(targetCrashedLis.getMothod(), targetCrashedLis);

        DefaultBrowserListener<MetricsPayload> metricsLis = new DefaultBrowserListener<MetricsPayload>() {
            @Override
            public void onBrowserEvent(MetricsPayload event) {
                Page page = (Page) this.getTarget();
                page.emitMetrics(event);
            }
        };
        metricsLis.setMothod("Inspector.targetCrashed");
        metricsLis.setTarget(this);
        this.client.on(metricsLis.getMothod(), metricsLis);

        DefaultBrowserListener<EntryAddedPayload> entryAddedLis = new DefaultBrowserListener<EntryAddedPayload>() {
            @Override
            public void onBrowserEvent(EntryAddedPayload event) {
                Page page = (Page) this.getTarget();
                page.onLogEntryAdded(event);
            }
        };
        entryAddedLis.setMothod("Log.entryAdded");
        entryAddedLis.setTarget(this);
        this.client.on(entryAddedLis.getMothod(), entryAddedLis);

        DefaultBrowserListener<FileChooserOpenedPayload> fileChooserOpenedLis = new DefaultBrowserListener<FileChooserOpenedPayload>() {
            @Override
            public void onBrowserEvent(FileChooserOpenedPayload event) {
                Page page = (Page) this.getTarget();
                page.onFileChooser(event);
            }
        };
        fileChooserOpenedLis.setMothod("Page.fileChooserOpened");
        fileChooserOpenedLis.setTarget(this);
        this.client.on(fileChooserOpenedLis.getMothod(), fileChooserOpenedLis);

    }

    /**
     * 监听页面的关闭事件
     *
     * @param handler 要提供的处理器
     */
    public void onClose(EventHandler handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_CLOSE.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onConsole(EventHandler<ConsoleMessage> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_CONSOLE.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onDialg(EventHandler<Dialog> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_DIALOG.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onError(EventHandler<Error> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_ERROR.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onFrameattached(EventHandler<Frame> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_FRAMEATTACHED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onFramedetached(EventHandler<Frame> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_FRAMEDETACHED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onFramenavigated(EventHandler<Frame> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_FRAMENAVIGATED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onLoad(EventHandler handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_LOAD.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onMetrics(EventHandler<MetricsPayload> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_METRICS.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onPageerror(EventHandler<Error> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_ERROR.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onPopup(EventHandler<Error> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_POPUP.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onRequest(EventHandler<Request> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_REQUEST.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onRequestfailed(EventHandler<Request> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_REQUESTFAILED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onRequestfinished(EventHandler<Request> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_REQUESTFINISHED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onResponse(EventHandler<Response> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_RESPONSE.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onWorkercreated(EventHandler<Worker> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_WORKERCREATED.getName());
        this.on(listener.getMothod(), listener);
    }

    public void onWorkerdestroyed(EventHandler<Worker> handler) {
        DefaultBrowserListener listener = new DefaultBrowserListener();
        listener.setHandler(handler);
        listener.setMothod(Events.PAGE_WORKERDESTROYED.getName());
        this.on(listener.getMothod(), listener);
    }

    /**
     * @param {string} selector
     * @return {!Promise<?ElementHandle>}
     */
    public ElementHandle $(String selector) {
        return this.mainFrame().$(selector);
    }

    /**
     * @param {string} selector
     * @return {!Promise<!Array<!ElementHandle>>}
     */
    public List<ElementHandle> $$(String selector) {
        return this.mainFrame().$$(selector);
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        return this.mainFrame().$$eval(selector, pageFunction, type, args);
    }

    /**
     * @return {!Frame}
     */
    public Frame mainFrame() {
        return this.frameManager.mainFrame();
    }

    /**
     * @param {string}          selector
     * @param {Function|string} pageFunction
     * @param {!Array<*>}       args
     * @return {!Promise<(!Object|undefined)>}
     */
    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object... args) {
        return this.mainFrame().$eval(selector, pageFunction, type, args);
    }

    /**
     * @param {string} expression
     * @return {!Promise<!Array<!ElementHandle>>}
     */
    public List<ElementHandle> $x(String expression) {
        return this.mainFrame().$x(expression);
    }

    public ElementHandle addScriptTag(ScriptTagOptions options) throws IOException {
        return this.mainFrame().addScriptTag(options);
    }

    /**
     * @param {!{url?: string, path?: string, content?: string}} options
     * @return {!Promise<!ElementHandle>}
     */
    public ElementHandle addStyleTag(StyleTagOptions options) {
        return this.mainFrame().addStyleTag(options);
    }

    /**
     * @param {?{username: string, password: string}} credentials
     */
    public void authenticate(Credentials credentials) {
        this.frameManager.networkManager().authenticate(credentials);
    }

    public void bringToFront() {
        this.client.send("Page.bringToFront", null, true);
    }

    /**
     * @return {@link Browser}
     */
    public Browser browser() {
        return this.target.browser();
    }

    /**
     * @return {@link BrowserContext}
     */
    public BrowserContext browserContext() {
        return this.target.browserContext();
    }

    public void click(String selector, ClickOptions options) {
        this.mainFrame().click(selector, options);
    }

    /**
     * @param {!{runBeforeUnload: (boolean|undefined)}=} options
     */
    public void close(boolean runBeforeUnload) {
        ValidateUtil.assertBoolean(this.client.getConnection() != null, "Protocol error: Connection closed. Most likely the page has been closed.");

        if (runBeforeUnload) {
            this.client.send("Page.close", null, false);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", this.target.getTargetId());
            this.client.getConnection().send("Target.closeTarget", params, true);
            this.target.waitInitializedPromise();
        }
    }

    private void onFileChooser(FileChooserOpenedPayload event) {

    }

    private void onLogEntryAdded(EntryAddedPayload event) {
    }

    private void emitMetrics(MetricsPayload event) {

    }

    private void onTargetCrashed() {

    }

    public static Page create(CDPSession client, Target target, boolean ignoreHTTPSErrors, Viewport viewport, TaskQueue screenshotTaskQueue) {
        Page page = new Page(client, target, ignoreHTTPSErrors, screenshotTaskQueue);
        try {
            page.initialize();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Create new Page fail: ", e);
        }
        if (viewport != null) {
            page.setViewport(viewport);
        }
        return page;
    }


    private void onDialog(JavascriptDialogOpeningPayload event) {

    }

    private void onConsoleAPI(ConsoleAPICalledPayload event) {

    }

    private void onBindingCalled(BindingCalledPayload event) {

    }

    private void setViewport(Viewport viewport) {

    }

    public void initialize() throws JsonProcessingException {
        frameManager.initialize();
        Map<String, Object> params = new HashMap<>();
        params.put("autoAttach", true);
        params.put("waitForDebuggerOnStart", false);
        params.put("flatten", true);
        this.client.send("Target.setAutoAttach", params, false);
        params.clear();
        this.client.send("Performance.enable", params, false);
        this.client.send("Log.enable", params, true);
    }

    private void addConsoleMessage(String type, List<JSHandle> args, StackTrace stackTrace) {

    }

    private void handleException(ExceptionDetails exceptionDetails) {
        String message = Helper.getExceptionMessage(exceptionDetails);

    }

    /**
     * 以字符串形式返回页面的内容
     *
     * @return 页面内容
     */
    public String content() {
        return this.frameManager.getMainFrame().content();
    }

    /**
     * 导航到指定的url,因为goto是java的关键字，所以就采用了goTo方法名
     * the main resource failed to load.
     *
     * @param url url
     * @return Response
     */
    public Response goTo(String url, PageNavigateOptions options) throws InterruptedException {
        return this.frameManager.getMainFrame().goTo(url, options);
    }

    public Response goTo(String url) throws InterruptedException {
        return this.frameManager.getMainFrame().goTo(url, null);
    }

    /**
     * @param {Array<Protocol.Network.deleteCookiesParameters>} cookies
     */
    public void deleteCookie(List<DeleteCookiesParameters> cookies) {
        String pageURL = this.url();
        for (DeleteCookiesParameters cookie : cookies) {
            if (StringUtil.isEmpty(cookie.getUrl()) && pageURL.startsWith("http"))
                cookie.setUrl(pageURL);
            Map<String, Object> params = getProperties(cookie);
            this.client.send("Network.deleteCookies", params, true);
        }
    }

    private Map<String, Object> getProperties(DeleteCookiesParameters cookie) {
        Map<String, Object> params = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(cookie.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                params.put(descriptor.getName(), descriptor.getReadMethod().invoke(cookie));
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 把浏览器的界面和userAgent设置成手机设备的样子
     *
     * @param options Device 枚举类
     */
    public void emulate(Device options) {
        this.setViewport(options.getViewport());
        this.setUserAgent(options.getUserAgent());
    }

    private void setUserAgent(String userAgent) {
        this.frameManager.networkManager().setUserAgent(userAgent);
    }


    public void emulateMediaType(String type) {
        this.emulateMedia(type);
    }


    public void emulateTimezone(String timezoneId) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (timezoneId == null) {
                timezoneId = "";
            }
            params.put("timezoneId", timezoneId);
            this.client.send("Emulation.setTimezoneOverride", params, true);
        } catch (Exception e) {
            if (e.getMessage().contains("Invalid timezone"))
                throw new IllegalArgumentException("Invalid timezone ID: " + timezoneId);
            throw e;
        }
    }

    public void evaluateOnNewDocument(String pageFunction, PageEvaluateType type, Object... args) {
        Map<String, Object> params = new HashMap<>();

        if (PageEvaluateType.STRING.equals(type)) {
            ValidateUtil.assertBoolean(args.length == 0, "Cannot evaluate a string with arguments");
            params.put("source", pageFunction);
            this.client.send("Page.addScriptToEvaluateOnNewDocument", params, true);
        } else {
            List<Object> objects = Arrays.asList(args);
            List<String> argsList = new ArrayList<>();
            objects.forEach(arg -> {
                if (arg == null) {
                    argsList.add("undefined");
                } else {
                    try {
                        argsList.add(OBJECTMAPPER.writeValueAsString(arg));
                    } catch (JsonProcessingException e) {
                        argsList.add("undefined");
                    }
                }
            });
            String source = "(" + pageFunction + ")" + String.join(",", argsList);
            params.put("source", source);
            this.client.send("Page.addScriptToEvaluateOnNewDocument", params, true);
        }

    }

    //TODO
    public void exposeFunction(String name, String puppeteerFunction) {

    }

    public void focus(String selector) {
        this.mainFrame().focus(selector);
    }

    public List<Frame> frames() {
        return this.frameManager.frames();
    }

    /**
     * options 的 referer不用填
     * @param options
     * @return
     */
    public Response goBack(PageNavigateOptions options) {
        return this.go(-1, options);
    }

    /**
     * options 的 referer不用填
     * @param options
     * @return Response
     */
    public Response goForward(PageNavigateOptions options) {
        return this.go(+1, options);
    }

    public void hover(String selector) {
        this.mainFrame().hover(selector);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }

    public Metrics metrics() throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        JsonNode responseNode =  this.client.send("Performance.getMetrics",null,true);
        List<Metric> metrics =  new ArrayList<>();
        Iterator<JsonNode> elements = responseNode.get("metrics").elements();
        while (elements.hasNext()){
            JsonNode next = elements.next();
            try {
                Metric value = OBJECTMAPPER.treeToValue(next, Metric.class);
                metrics.add(value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return this.buildMetricsObject(metrics);
    }
    public void pdf(PDFOptions options) throws IOException {
        double paperWidth = 8.5;
        double paperHeight = 11;
        if(StringUtil.isNotEmpty(options.getFormat())){
            PaperFormats format = PaperFormats.valueOf(options.getFormat().toLowerCase());
            ValidateUtil.assertBoolean(format != null,"Unknown paper format: "+options.getFormat());
            paperWidth = format.getWidth();
            paperHeight = format.getHeight();
        }else{
            Double width = convertPrintParameterToInches(options.getWidth());
            if(width != null){
                paperWidth = width;
            }
            Double height = convertPrintParameterToInches(options.getHeight());
            if(height != null){
                paperHeight = height;
            }
        }
        Margin margin = options.getMargin();
        Number marginTop,marginLeft,marginBottom,marginRight;
        if((marginTop = convertPrintParameterToInches(margin.getTop())) == null){
            marginTop = 0;
        }
        if((marginLeft = convertPrintParameterToInches(margin.getLeft())) == null){
            marginLeft = 0;
        }

        if((marginBottom = convertPrintParameterToInches(margin.getBottom())) == null){
            marginBottom = 0;
        }

        if((marginRight = convertPrintParameterToInches(margin.getRight())) == null){
            marginRight = 0;
        }
        Map<String,Object> params = new HashMap<>();
        params.put("transferMode","ReturnAsStream");
        params.put("landscape",options.getLandscape());
        params.put("displayHeaderFooter",options.getDisplayHeaderFooter());
        params.put("headerTemplate",options.getHeaderTemplate());
        params.put("footerTemplate",options.getFooterTemplate());
        params.put("printBackground",options.getPrintBackground());
        params.put("scale",options.getScale());
        params.put("paperWidth",paperWidth);
        params.put("paperHeight",paperHeight);
        params.put("marginTop",marginTop);
        params.put("marginBottom",marginBottom);
        params.put("marginLeft",marginLeft);
        params.put("marginRight",marginRight);
        params.put("pageRanges",options.getPageRanges());
        params.put("preferCSSPageSize",options.getPreferCSSPageSize());
        JsonNode result = this.client.send("Page.printToPDF", params, true);
        if(result != null)
        Helper.readProtocolStream(this.client,result.get("stream"),options.getPath());
    }

    private Double convertPrintParameterToInches(String parameter) {
        if(StringUtil.isEmpty(parameter)){
            return null;
        }
        double pixels;
        if(Helper.isNumber(parameter)){
            pixels = Double.parseDouble(parameter);
        }else if(parameter.endsWith("px") || parameter.endsWith("in") || parameter.endsWith("cm") || parameter.endsWith("mm")){

            String unit = parameter.substring(parameter.length() - 2).toLowerCase();
            String valueText = "";
            if (unitToPixels.containsKey(unit)) {
                valueText = parameter.substring(0, parameter.length() - 2);
            } else {
                // In case of unknown unit try to parse the whole parameter as number of pixels.
                // This is consistent with phantom's paperSize behavior.
                unit = "px";
                valueText = parameter;
            }
            Double value = Double.parseDouble(valueText);
            ValidateUtil.assertBoolean(!Double.isNaN(value), "Failed to parse parameter value: " + parameter);
            pixels = value * unitToPixels.get(unit);
        }else {
            throw new IllegalArgumentException("page.pdf() Cannot handle parameter type: "+parameter);
        }
        return pixels / 96;
    }

    private Metrics buildMetricsObject(List<Metric> metrics) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Metrics result = new Metrics();
        BeanInfo beanInfo = Introspector.getBeanInfo(Metric.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String ,PropertyDescriptor> propertyMap = new HashMap<>();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            propertyMap.put(propertyDescriptors[i].getName(),propertyDescriptors[i]);
        }
        for (Metric metric : metrics) {
            if(supportedMetrics.contains(metric.getName())){
                propertyMap.get(metric.getName()).getWriteMethod().invoke(result,metric.getValue());
            }
        }
        return  result;
    }

    private Response go(int delta, PageNavigateOptions options) {
        JsonNode historyNode = this.client.send("Page.getNavigationHistory", null, true);
        GetNavigationHistoryReturnValue history = null;
        try {
            history = OBJECTMAPPER.treeToValue(historyNode, GetNavigationHistoryReturnValue.class);
        } catch (JsonProcessingException e) {
            throw new NavigateException(e);
        }
        NavigationEntry entry = history.getEntries().get(history.getCurrentIndex() + delta);
        if (entry == null)
            return null;
        Response response = this.waitForNavigation(options);
        Map<String, Object> params = new HashMap<>();
        params.put("entryId", entry.getId());
        this.client.send("Page.navigateToHistoryEntry", params, true);
        return response;
    }

    private Response waitForNavigation(PageNavigateOptions options) {
        return this.frameManager.mainFrame().waitForNavigation(options);
    }

    public void evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        this.frameManager.mainFrame().evaluate(pageFunction, type, args);
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        ExecutionContext context = this.mainFrame().executionContext();
        return context.evaluateHandle(pageFunction, type, args);
    }

    public void emulateMedia(String type) {
        ValidateUtil.assertBoolean("screen".equals(type) || "print".equals(type) || type == null, "Unsupported media type: " + type);
        Map<String, Object> params = new HashMap<>();
        params.put("media", type);
        this.client.send("Emulation.setEmulatedMedia", params, true);
    }

    public void emulateMediaFeatures(List<MediaFeature> features) {
        Pattern pattern = Pattern.compile("^prefers-(?:color-scheme|reduced-motion)$");
        Map<String, Object> params = new HashMap<>();
        if (features == null) {
            params.put("features", null);
            this.client.send("Emulation.setEmulatedMedia", params, true);
        }

        features.forEach(mediaFeature -> {
            String name = mediaFeature.getName();
            ValidateUtil.assertBoolean(pattern.matcher(name).find(), "Unsupported media feature: " + name);
        });
        params.put("features", features);
        this.client.send("Emulation.setEmulatedMedia", params, true);
    }

    private String url() {
        return this.mainFrame().url();
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Map<String, Worker> getWorkers() {
        return workers;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Mouse getMouse() {
        return mouse;
    }
}