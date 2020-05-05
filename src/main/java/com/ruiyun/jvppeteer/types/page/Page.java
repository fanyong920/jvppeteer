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
import com.ruiyun.jvppeteer.exception.TerminateException;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.ClipOverwrite;
import com.ruiyun.jvppeteer.options.Device;
import com.ruiyun.jvppeteer.options.PDFOptions;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.options.WaitForOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.emulation.ScreenOrientation;
import com.ruiyun.jvppeteer.protocol.network.CookieParam;
import com.ruiyun.jvppeteer.protocol.network.DeleteCookiesParameters;
import com.ruiyun.jvppeteer.protocol.performance.Metric;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.types.browser.Browser;
import com.ruiyun.jvppeteer.types.browser.BrowserContext;
import com.ruiyun.jvppeteer.protocol.runtime.BindingCalledPayload;
import com.ruiyun.jvppeteer.protocol.runtime.ConsoleAPICalledPayload;
import com.ruiyun.jvppeteer.protocol.webAuthn.Credentials;
import com.ruiyun.jvppeteer.protocol.log.EntryAddedPayload;
import com.ruiyun.jvppeteer.protocol.page.FileChooserOpenedPayload;
import com.ruiyun.jvppeteer.protocol.page.GetNavigationHistoryReturnValue;
import com.ruiyun.jvppeteer.protocol.page.JavascriptDialogOpeningPayload;
import com.ruiyun.jvppeteer.protocol.DOM.Margin;
import com.ruiyun.jvppeteer.protocol.emulation.MediaFeature;
import com.ruiyun.jvppeteer.protocol.performance.Metrics;
import com.ruiyun.jvppeteer.protocol.performance.MetricsPayload;
import com.ruiyun.jvppeteer.protocol.page.NavigationEntry;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.ruiyun.jvppeteer.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.Constant.supportedMetrics;

public class Page extends EventEmitter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Page.class);

    private Set<BiConsumer<Object, FileChooser>> fileChooserInterceptors;

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

    public static final Map<String, Double> unitToPixels = new HashMap() {
        {
            put("px", 1.00);
            put("in", 96);
            put("cm", 37.8);
            put("mm", 3.78);
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
                try {
                    page.onFileChooser(event);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
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
    public ElementHandle $(String selector) throws JsonProcessingException {
        return this.mainFrame().$(selector);
    }

    /**
     * @param {string} selector
     * @return {!Promise<!Array<!ElementHandle>>}
     */
    public List<ElementHandle> $$(String selector) throws JsonProcessingException {
        return this.mainFrame().$$(selector);
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object... args) throws JsonProcessingException {
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
    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object... args) throws JsonProcessingException {
        return this.mainFrame().$eval(selector, pageFunction, type, args);
    }

    /**
     * @param {string} expression
     * @return {!Promise<!Array<!ElementHandle>>}
     */
    public List<ElementHandle> $x(String expression) throws JsonProcessingException {
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

    public void click(String selector, ClickOptions options) throws JsonProcessingException, InterruptedException {
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

    /**
     * 截图
     *
     * @return String
     */
    public String screenshot(ScreenshotOptions options) throws IOException {
        String screenshotType = null;
        // options.type takes precedence over inferring the type from options.path
        // because it may be a 0-length file with no extension created beforehand (i.e. as a temp file).
        if (StringUtil.isNotEmpty(options.getType())) {
            ValidateUtil.assertBoolean("png".equals(options.getType()) || "jpeg".equals(options.getType()), "Unknown options.type value: " + options.getType());
            screenshotType = options.getType();
        } else if (StringUtil.isNotEmpty(options.getPath())) {
            String mimeType = Files.probeContentType(Paths.get(options.getPath()));
            if ("image/png".equals(mimeType))
                screenshotType = "png";
            else if ("image/jpeg".equals(mimeType))
                screenshotType = "jpeg";
            ValidateUtil.assertBoolean(StringUtil.isNotEmpty(screenshotType), "Unsupported screenshot mime type: " + mimeType);
        }

        if (StringUtil.isEmpty(screenshotType))
            screenshotType = "png";

        if (options.getQuality() >= 0) {
            ValidateUtil.assertBoolean("jpeg".equals(screenshotType), "options.quality is unsupported for the " + screenshotType + " screenshots");
            ValidateUtil.assertBoolean(options.getQuality() <= 100, "Expected options.quality to be between 0 and 100 (inclusive), got " + options.getQuality());
        }
        ValidateUtil.assertBoolean(options.getClip() == null || !options.getFullPage(), "options.clip and options.fullPage are exclusive");
        if (options.getClip() != null) {
            ValidateUtil.assertBoolean(options.getClip().getWidth() != 0, "Expected options.clip.width not to be 0.");
            ValidateUtil.assertBoolean(options.getClip().getHeight() != 0, "Expected options.clip.height not to be 0.");
        }
        return (String) this.screenshotTaskQueue.postTask((type, op) -> screenshotTask((String) type, (ScreenshotOptions) op), screenshotType, options);
    }

    public List<String> select(String selector, List<String> values) throws JsonProcessingException {
        return this.mainFrame().select(selector, values);
    }

    public String title() throws JsonProcessingException {
        return this.mainFrame().title();
    }

    public void setBypassCSP(boolean enabled) {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        this.client.send("Page.setBypassCSP", params, true);
    }

    public void setCacheEnabled(boolean enabled) {
        this.frameManager.networkManager().setCacheEnabled(enabled);
    }

    public void setContent(String html, PageNavigateOptions options) throws JsonProcessingException {
        this.frameManager.mainFrame().setContent(html, options);
    }

    public void setCookie(List<CookieParam> cookies) {
        String pageURL = this.url();
        boolean startsWithHTTP = pageURL.startsWith("http");
        cookies.replaceAll(cookie -> {
            if (StringUtil.isEmpty(cookie.getUrl()) && startsWithHTTP)
                cookie.setUrl(pageURL);
            ValidateUtil.assertBoolean(!"about:blank".equals(cookie.getUrl()), "Blank page can not have cookie " + cookie.getName());
            ValidateUtil.assertBoolean(!StringUtil.isNotEmpty(cookie.getUrl()) && !cookie.getUrl().startsWith("data:"), "Data URL page can not have cookie " + cookie.getName());
            return cookie;
        });
        List<DeleteCookiesParameters> deleteCookiesParameters = new ArrayList<>();
        for (CookieParam cookie : cookies) {
            deleteCookiesParameters.add(new DeleteCookiesParameters(cookie.getName(), cookie.getUrl(), cookie.getDomain(), cookie.getPath()));
        }

        this.deleteCookie(deleteCookiesParameters);
        Map<String, Object> params = new HashMap<>();
        params.put("cookies", cookies);
        this.client.send("Network.setCookies", params, true);
    }

    public void setDefaultNavigationTimeout(int timeout) {
        this.timeoutSettings.setDefaultNavigationTimeout(timeout);
    }

    public void setExtraHTTPHeaders(Map<String, String> headers) {
        this.frameManager.networkManager().setExtraHTTPHeaders(headers);
    }

    public void setGeolocation(int longitude, int latitude, int accuracy) {

        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Invalid longitude " + longitude + ": precondition -180 <= LONGITUDE <= 180 failed.");
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Invalid latitude " + latitude + ": precondition -90 <= LATITUDE <= 90 failed.");
        if (accuracy < 0)
            throw new IllegalArgumentException("Invalid accuracy " + accuracy + ": precondition 0 <= ACCURACY failed.");
        Map<String, Object> params = new HashMap<>();
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        params.put("accuracy", accuracy);
        this.client.send("Emulation.setGeolocationOverride", params, true);
    }

    public void setJavaScriptEnabled(boolean enabled) {
        if (this.javascriptEnabled == enabled)
            return;
        this.javascriptEnabled = enabled;
        Map<String, Object> params = new HashMap<>();
        params.put("value", !enabled);
        this.client.send("Emulation.setScriptExecutionDisabled", params, true);
    }

    public void setOfflineMode(boolean enabled) {
        this.frameManager.networkManager().setOfflineMode(enabled);
    }

    public void setRequestInterception(boolean value) {
        this.frameManager.networkManager().setRequestInterception(value);
    }

    private String screenshotTask(String format, ScreenshotOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", this.target.getTargetId());
        this.client.send("Target.activateTarget", params, true);
        ClipOverwrite clip = null;
        if (options.getClip() != null) {
            clip = processClip(options.getClip());
        }
        if (options.getFullPage()) {
            JsonNode metrics = this.client.send("Page.getLayoutMetrics", null, true);
            double width = Math.ceil(metrics.get("contentSize").get("width").asDouble());
            double height = Math.ceil(metrics.get("contentSize").get("height").asDouble());
            clip = new ClipOverwrite(0, 0, width, height, 1);
            ScreenOrientation screenOrientation = null;
            if (this.viewport.getIsLandscape()) {
                screenOrientation = new ScreenOrientation(90, "landscapePrimary");
            } else {
                screenOrientation = new ScreenOrientation(0, "portraitPrimary");
            }
            params.clear();
            params.put("mobile", this.viewport.getIsMobile());
            params.put("width", width);
            params.put("height", height);
            params.put("deviceScaleFactor", this.viewport.getDeviceScaleFactor());
            params.put("screenOrientation", screenOrientation);
            this.client.send("Emulation.setDeviceMetricsOverride", params, true);
        }
        boolean shouldSetDefaultBackground = options.getOmitBackground() && "png".equals(format);
        if (shouldSetDefaultBackground) {
            params.clear();
            Map<String, Integer> colorMap = new HashMap<>();
            colorMap.put("r", 0);
            colorMap.put("g", 0);
            colorMap.put("b", 0);
            colorMap.put("a", 0);
            params.put("color", colorMap);
            this.client.send("Emulation.setDefaultBackgroundColorOverride", params, true);
        }
        params.clear();
        params.put("format", format);
        params.put("quality", options.getQuality());
        params.put("clip", clip);
        JsonNode result = this.client.send("Page.captureScreenshot", params, true);
        if (shouldSetDefaultBackground) {
            this.client.send("Emulation.setDefaultBackgroundColorOverride", null, true);
        }
        if (options.getFullPage() && this.viewport != null)
            this.setViewport(this.viewport);
        BASE64Decoder decoder = new BASE64Decoder();
        String data = result.get("data").toString();
        try {
            byte[] buffer = decoder.decodeBuffer(data);
            if (StringUtil.isNotEmpty(options.getPath())) {
                //TODO 验证
                Files.write(Paths.get(options.getPath()), buffer, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private ClipOverwrite processClip(Clip clip) {
        long x = Math.round(clip.getX());
        long y = Math.round(clip.getY());
        long width = Math.round(clip.getWidth() + clip.getX() - x);
        long height = Math.round(clip.getHeight() + clip.getY() - y);
        ClipOverwrite result = new ClipOverwrite(x, y, width, height, 1);
        return result;
    }

    private void onFileChooser(FileChooserOpenedPayload event) throws JsonProcessingException {
        if (ValidateUtil.isEmpty(this.fileChooserInterceptors))
            return;
        Frame frame = this.frameManager.frame(event.getFrameId());
        ExecutionContext context = frame.executionContext();
        ElementHandle element = context.adoptBackendNodeId(event.getBackendNodeId());
        Set<BiConsumer<Object, FileChooser>> interceptors = new HashSet<>(this.fileChooserInterceptors);
        this.fileChooserInterceptors.clear();
        FileChooser fileChooser = new FileChooser(this.client, element, event);
        for (BiConsumer interceptor : interceptors)
            interceptor.accept(null, fileChooser);
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

    public void setViewport(Viewport viewport) {
        boolean needsReload = this.emulationManager.emulateViewport(viewport);
        this.viewport = viewport;
        if (needsReload)
            this.reload(null);
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
    public String content() throws JsonProcessingException {
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

    public void tap(String selector) throws JsonProcessingException {
        this.mainFrame().tap(selector);
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

    public void focus(String selector) throws JsonProcessingException {
        this.mainFrame().focus(selector);
    }

    public List<Frame> frames() {
        return this.frameManager.frames();
    }

    /**
     * options 的 referer不用填
     *
     * @param options
     * @return
     */
    public Response goBack(PageNavigateOptions options) {
        return this.go(-1, options);
    }

    /**
     * options 的 referer不用填
     *
     * @param options
     * @return Response
     */
    public Response goForward(PageNavigateOptions options) {
        return this.go(+1, options);
    }

    public void hover(String selector) throws JsonProcessingException {
        this.mainFrame().hover(selector);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }

    public Metrics metrics() throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        JsonNode responseNode = this.client.send("Performance.getMetrics", null, true);
        List<Metric> metrics = new ArrayList<>();
        Iterator<JsonNode> elements = responseNode.get("metrics").elements();
        while (elements.hasNext()) {
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
        if (StringUtil.isNotEmpty(options.getFormat())) {
            PaperFormats format = PaperFormats.valueOf(options.getFormat().toLowerCase());
            ValidateUtil.assertBoolean(format != null, "Unknown paper format: " + options.getFormat());
            paperWidth = format.getWidth();
            paperHeight = format.getHeight();
        } else {
            Double width = convertPrintParameterToInches(options.getWidth());
            if (width != null) {
                paperWidth = width;
            }
            Double height = convertPrintParameterToInches(options.getHeight());
            if (height != null) {
                paperHeight = height;
            }
        }
        Margin margin = options.getMargin();
        Number marginTop, marginLeft, marginBottom, marginRight;
        if ((marginTop = convertPrintParameterToInches(margin.getTop())) == null) {
            marginTop = 0;
        }
        if ((marginLeft = convertPrintParameterToInches(margin.getLeft())) == null) {
            marginLeft = 0;
        }

        if ((marginBottom = convertPrintParameterToInches(margin.getBottom())) == null) {
            marginBottom = 0;
        }

        if ((marginRight = convertPrintParameterToInches(margin.getRight())) == null) {
            marginRight = 0;
        }
        Map<String, Object> params = new HashMap<>();
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
        JsonNode result = this.client.send("Page.printToPDF", params, true);
        if (result != null)
            Helper.readProtocolStream(this.client, result.get("stream"), options.getPath());
    }

    public void setDefaultTimeout(int timeout) {
        this.timeoutSettings.setDefaultTimeout(timeout);
    }

    public JSHandle queryObjects(JSHandle prototypeHandle) throws JsonProcessingException {
        ExecutionContext context = this.mainFrame().executionContext();
        return context.queryObjects(prototypeHandle);
    }

    private Double convertPrintParameterToInches(String parameter) {
        if (StringUtil.isEmpty(parameter)) {
            return null;
        }
        double pixels;
        if (Helper.isNumber(parameter)) {
            pixels = Double.parseDouble(parameter);
        } else if (parameter.endsWith("px") || parameter.endsWith("in") || parameter.endsWith("cm") || parameter.endsWith("mm")) {

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
        } else {
            throw new IllegalArgumentException("page.pdf() Cannot handle parameter type: " + parameter);
        }
        return pixels / 96;
    }

    public Response reload(PageNavigateOptions options) {
        Response response = this.waitForNavigation(options);
        this.client.send("Page.reload", null, true);
        return response;
    }

    private Metrics buildMetricsObject(List<Metric> metrics) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Metrics result = new Metrics();
        BeanInfo beanInfo = Introspector.getBeanInfo(Metric.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String, PropertyDescriptor> propertyMap = new HashMap<>();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            propertyMap.put(propertyDescriptors[i].getName(), propertyDescriptors[i]);
        }
        for (Metric metric : metrics) {
            if (supportedMetrics.contains(metric.getName())) {
                propertyMap.get(metric.getName()).getWriteMethod().invoke(result, metric.getValue());
            }
        }
        return result;
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

    public void evaluate(String pageFunction, PageEvaluateType type, Object... args) throws JsonProcessingException {
        this.frameManager.mainFrame().evaluate(pageFunction, type, args);
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) throws JsonProcessingException {
        ExecutionContext context = this.mainFrame().executionContext();
        return (JSHandle)context.evaluateHandle(pageFunction, type, args);
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

    public JSHandle waitFor(String selectorOrFunctionOrTimeout, PageEvaluateType type, WaitForOptions options, Object... args) throws JsonProcessingException {
        return this.mainFrame().waitFor(selectorOrFunctionOrTimeout, type, options, args);
    }

    public FileChooser waitForFileChooser(int timeout){
        if (ValidateUtil.isEmpty(this.fileChooserInterceptors)) {
            Map<String, Object> params = new HashMap<>();
            params.put("enabled",true);
            this.client.send("Page.setInterceptFileChooserDialog", params,true);
        }
    if(timeout <=0 )
        timeout = this.timeoutSettings.timeout();

        //TODO
//        this.fileChooserInterceptors.add(callback);
//        return helper.waitWithTimeout(promise, 'waiting for file chooser', timeout).catch(error = > {
//            this._fileChooserInterceptors.delete(callback);

        return null;
    }
    public JSHandle waitForFunction(String pageFunction, PageEvaluateType type,WaitForOptions options ,Object... args) throws JsonProcessingException {
        return this.mainFrame().waitForFunction(pageFunction, type,options, args);
    }

    public Request waitForRequest(String url,Predicate<Request> predicate, PageEvaluateType type ,int timeout) throws InterruptedException {
    if(timeout <= 0){
        timeout = this.timeoutSettings.timeout();
    }
        Predicate<Request> predi= request -> {
            if(PageEvaluateType.STRING.equals(type)){
                return url.equals(request.getUrl());
            }else if(PageEvaluateType.FUNCTION.equals(type)){
                return predicate.test(request);
            }
            return false;
        };
        DefaultBrowserListener listener= null;
        try {
            listener = sessionClosePromise();

            return Helper.waitForEvent(this.frameManager.networkManager(), Events.NETWORK_MANAGER_REQUEST.getName(), predi, timeout, "Wait for request timeout");
        } finally {
            if(listener != null)
            this.client.removeListener(Events.CDPSESSION_DISCONNECTED.getName(),listener);
        }
    }

    private DefaultBrowserListener sessionClosePromise() {
        DefaultBrowserListener disConnectLis = new DefaultBrowserListener(){
            @Override
            public void onBrowserEvent(Object event) {
                throw  new TerminateException("Target closed");
            }
        };
        disConnectLis.setMothod(Events.CDPSESSION_DISCONNECTED.getName());
        this.client.once(disConnectLis.getMothod(),disConnectLis);
        return  disConnectLis;
    }

    public Response waitForResponse(String url,Predicate predicate, PageEvaluateType type,int timeout ) throws InterruptedException {
        if(timeout <= 0)
            timeout = this.timeoutSettings.timeout();
        Predicate<Response> predi= response -> {
            if(PageEvaluateType.STRING.equals(type)){
                return url.equals(response.getUrl());
            }else if(PageEvaluateType.FUNCTION.equals(type)){
                return predicate.test(response);
            }
            return false;
        };
        DefaultBrowserListener listener= null;
        try {
            listener = sessionClosePromise();
        return Helper.waitForEvent(this.frameManager.networkManager(), Events.NETWORK_MANAGER_RESPONSE.getName(), predi, timeout, "Wait for response timeout");
        } finally {
            if(listener != null)
                this.client.removeListener(Events.CDPSESSION_DISCONNECTED.getName(),listener);
        }
    }
    public ElementHandle waitForSelector(String selector,WaitForOptions options) throws JsonProcessingException {
        return this.mainFrame().waitForSelector(selector, options);
    }

    public JSHandle waitForXPath(String xpath, WaitForOptions options) throws JsonProcessingException {
        return this.mainFrame().waitForXPath(xpath, options);
    }
    public List<Worker> workers() {
        return new ArrayList<>(this.workers.values());
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

    public Target target() {
        return this.target;
    }

    public Touchscreen touchscreen() {
        return this.touchscreen;
    }

    public Tracing tracing() {
        return this.tracing;
    }

    public void type(String selector, String text, int delay) throws JsonProcessingException, InterruptedException {
        this.mainFrame().type(selector, text, delay);
    }

    public boolean getJavascriptEnabled() {
        return javascriptEnabled;
    }

    public Touchscreen getTouchscreen() {
        return touchscreen;
    }

    public Keyboard getKeyboard() {
        return this.keyboard;
    }
    public Viewport viewport() {
        return this.viewport;
    }
}
