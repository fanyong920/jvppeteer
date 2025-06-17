package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Keyboard;
import com.ruiyun.jvppeteer.api.core.Mouse;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.core.Touchscreen;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.api.events.TrustedEmitter;
import com.ruiyun.jvppeteer.bidi.entities.AddInterceptOptions;
import com.ruiyun.jvppeteer.bidi.entities.AddPreloadScriptOptions;
import com.ruiyun.jvppeteer.bidi.entities.BidiViewport;
import com.ruiyun.jvppeteer.bidi.entities.BytesValue;
import com.ruiyun.jvppeteer.bidi.entities.CaptureScreenshotOptions;
import com.ruiyun.jvppeteer.bidi.entities.ClipRectangle;
import com.ruiyun.jvppeteer.bidi.entities.CookieFilter;
import com.ruiyun.jvppeteer.bidi.entities.GeolocationCoordinates;
import com.ruiyun.jvppeteer.bidi.entities.GetCookiesOptions;
import com.ruiyun.jvppeteer.bidi.entities.ImageFormat;
import com.ruiyun.jvppeteer.bidi.entities.InterceptPhase;
import com.ruiyun.jvppeteer.bidi.entities.Orientation;
import com.ruiyun.jvppeteer.bidi.entities.Origin;
import com.ruiyun.jvppeteer.bidi.entities.PartialCookie;
import com.ruiyun.jvppeteer.bidi.entities.PrintMarginParameters;
import com.ruiyun.jvppeteer.bidi.entities.PrintOptions;
import com.ruiyun.jvppeteer.bidi.entities.PrintPageParameters;
import com.ruiyun.jvppeteer.bidi.entities.ReloadParameters;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.bidi.entities.SetGeoLocationOverrideOptions;
import com.ruiyun.jvppeteer.bidi.entities.SetViewportParameters;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.core.Accessibility;
import com.ruiyun.jvppeteer.cdp.core.Coverage;
import com.ruiyun.jvppeteer.cdp.core.EmulationManager;
import com.ruiyun.jvppeteer.cdp.core.FileChooser;
import com.ruiyun.jvppeteer.cdp.core.Tracing;
import com.ruiyun.jvppeteer.cdp.entities.BoundingBox;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.Credentials;
import com.ruiyun.jvppeteer.cdp.entities.DeleteCookiesRequest;
import com.ruiyun.jvppeteer.cdp.entities.Device;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.cdp.entities.InternalNetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.LengthUnit;
import com.ruiyun.jvppeteer.cdp.entities.MediaFeature;
import com.ruiyun.jvppeteer.cdp.entities.Metrics;
import com.ruiyun.jvppeteer.cdp.entities.NetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.cdp.entities.PDFMargin;
import com.ruiyun.jvppeteer.cdp.entities.PDFOptions;
import com.ruiyun.jvppeteer.cdp.entities.PaperFormats;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotOptions;
import com.ruiyun.jvppeteer.cdp.entities.UserAgentMetadata;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.Base64Util;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;


import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.util.Helper.bidiToPuppeteerCookie;
import static com.ruiyun.jvppeteer.util.Helper.convertCookiesSameSiteCdpToBiDi;
import static com.ruiyun.jvppeteer.util.Helper.rewriteNavigationError;

public class BidiPage extends Page {
    List<HeaderEntry> userAgentHeaders;
    List<HeaderEntry> extraHTTPHeaders;
    private final TrustedEmitter<PageEvents> trustedEmitter = new TrustedEmitter<>();
    private final BidiBrowserContext browserContext;
    private final BidiFrame frame;
    private Viewport viewport;
    private final Set<BidiWebWorker> workers = Collections.synchronizedSet(new HashSet<>());
    private final BidiKeyboard keyboard;
    private final BidiMouse mouse;
    private final BidiTouchscreen touchscreen;
    private final Tracing tracing;
    private final Coverage coverage;
    private final EmulationManager cdpEmulationManager;
    private InternalNetworkConditions emulatedNetworkConditions;
    private String userAgentInterception;
    private String userAgentPreloadScript;
    Credentials credentials;
    private String userInterception;
    private String authInterception;
    private String extraHeadersInterception;

    private BidiPage(BidiBrowserContext browserContext, BrowsingContext browsingContext) {
        super();
        this.browserContext = browserContext;
        this.frame = BidiFrame.from(this, browsingContext);
        this.cdpEmulationManager = new EmulationManager(this.frame.client());
        this.tracing = new Tracing(this.frame.client());
        this.coverage = new Coverage(this.frame.client());
        this.keyboard = new BidiKeyboard(this);
        this.mouse = new BidiMouse(this);
        this.touchscreen = new BidiTouchscreen(this);
        this.trustedEmitter.pipeTo(this);
    }

    public static BidiPage from(BidiBrowserContext browserContext, BrowsingContext browsingContext) {
        BidiPage page = new BidiPage(browserContext, browsingContext);
        page.initialize();
        return page;
    }

    private BidiCdpSession client() {
        return this.frame.client();
    }

    private void initialize() {
        this.frame.browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, (ignored) -> {
            this.trustedEmitter.emit(PageEvents.Close, true);
            this.trustedEmitter.removeAllListeners(null);
        });

        this.trustedEmitter.on(PageEvents.WorkerCreated, (Consumer<BidiWebWorker>) this.workers::add);
        this.trustedEmitter.on(PageEvents.WorkerDestroyed, (Consumer<BidiWebWorker>) this.workers::remove);
    }

    @Override
    public void setUserAgent(String userAgent, UserAgentMetadata userAgentMetadata) {
        if (!this.browserContext.browser().cdpSupported() && Objects.nonNull(userAgentMetadata)) {
            throw new UnsupportedOperationException(
                    "Current Browser does not support " + userAgentMetadata);
        } else if (this.browserContext.browser().cdpSupported() && Objects.nonNull(userAgentMetadata)) {
            Map<String, Object> params = ParamsFactory.create();
            params.put("userAgent", userAgent);
            params.put("userAgentMetadata", userAgentMetadata);
            this.client().send("Network.setUserAgentOverride", params);
        }
        boolean enable = !Objects.equals(userAgent, "");
        if (Objects.isNull(userAgent)) {
            userAgent = this.browserContext.browser().userAgent();
        }
        this.userAgentHeaders = new ArrayList<>();
        if (enable) {
            this.userAgentHeaders.add(new HeaderEntry("User-Agent", userAgent));
        }
        this.userAgentInterception = this.toggleInterception(Collections.singletonList(InterceptPhase.BEFORE_REQUEST_SENT.toString()), this.userAgentInterception, enable);
        List<BidiFrame> frames = new ArrayList<>();
        frames.add(this.frame);
        Iterator<BidiFrame> iterator = frames.iterator();
        while (iterator.hasNext()) {
            BidiFrame frame = iterator.next();
            frames.addAll(frame.childFrames());
        }
            if (StringUtil.isNotEmpty(this.userAgentPreloadScript)) {
            this.removeScriptToEvaluateOnNewDocument(this.userAgentPreloadScript);
        }

        if (enable) {
            try {
                NewDocumentScriptEvaluation evaluateToken = this.evaluateOnNewDocument("(userAgent) => {\n" +
                        "  Object.defineProperty(navigator, 'userAgent', {\n" +
                        "    value: userAgent,\n" +
                        "    configurable: true,\n" +
                        "  });\n" +
                        "}", EvaluateType.STRING, userAgent);
                this.userAgentPreloadScript = evaluateToken.getIdentifier();
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to evaluate userAgent", e);
            }
        }
        for (BidiFrame frame : frames) {
            try {
                frame.evaluate("(userAgent) => {\n" +
                        "  Object.defineProperty(navigator, 'userAgent', {\n" +
                        "    value: userAgent,\n" +
                        "  });\n" +
                        "}", Collections.singletonList(userAgent));
            } catch (Exception e) {
                LOGGER.error("Failed to evaluate frame", e);
            }
        }
    }

    @Override
    public void setBypassCSP(boolean enabled) {
        // TODO: handle CDP-specific cases such as mprach.
        Map<String, Object> params = ParamsFactory.create();
        params.put("enabled", enabled);
        this.client().send("Page.setBypassCSP", params);
    }

    @Override
    public BidiJSHandle queryObjects(JSHandle prototypeHandle) throws JsonProcessingException {
        ValidateUtil.assertArg(!prototypeHandle.disposed(), "Prototype JSHandle is disposed");
        ValidateUtil.assertArg(StringUtil.isNotEmpty(prototypeHandle.id()), "Prototype JSHandle must not be referencing primitive value");
        Map<String, Object> params = ParamsFactory.create();
        params.put("prototypeObjectId", prototypeHandle.id());
        JsonNode response = this.frame.client().send("Runtime.queryObjects", params);
        RemoteValue remoteValue = new RemoteValue();
        remoteValue.setType("array");
        remoteValue.setHandle(response.at("/objects/objectId").asText());
        return (BidiJSHandle) this.frame.mainRealm().createHandle(remoteValue);
    }

    public BidiBrowser browser() {
        return this.browserContext().browser();
    }

    @Override
    public BidiBrowserContext browserContext() {
        return this.browserContext;
    }

    @Override
    public BidiFrame mainFrame() {
        return this.frame;
    }

    public BidiFrame focusedFrame() throws JsonProcessingException {
        BidiFrame frame;
        JSHandle handle = this.mainFrame().isolatedRealm().evaluateHandle("() => {\n" +
                "  let win = window;\n" +
                "  while (\n" +
                "    win.document.activeElement instanceof win.HTMLIFrameElement ||\n" +
                "    win.document.activeElement instanceof win.HTMLFrameElement\n" +
                "  ) {\n" +
                "    if (win.document.activeElement.contentWindow === null) {\n" +
                "      break;\n" +
                "    }\n" +
                "    win = win.document.activeElement.contentWindow;\n" +
                "  }\n" +
                "  return win;\n" +
                "}");
        try {

            RemoteValue value = ((BidiElementHandle) handle.asElement()).remoteValue();
            assert (Objects.equals(value.getType(), "window"));
            frame = this.frames().stream().filter(item -> Objects.equals(item.id(), value.getValue().get("context").asText())).findFirst().orElse(null);
            assert (Objects.nonNull(frame));
        } finally {
            handle.dispose();
        }
        return frame;
    }

    @Override
    public List<BidiFrame> frames() {
        List<BidiFrame> frames = new CopyOnWriteArrayList<>();
        frames.add(this.frame);
        Iterator<BidiFrame> iterator = frames.iterator();
        while (iterator.hasNext()) {
            BidiFrame frame = iterator.next();
            frames.addAll(frame.childFrames());
        }
        return frames;
    }

    @Override
    public boolean isClosed() {
        return this.frame.detached();
    }

    @Override
    public void close(boolean runBeforeUnload) {
        synchronized (this.browserContext()) {
            try {
                this.frame.browsingContext.close(runBeforeUnload);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public BidiResponse reload(WaitForOptions options) {
        Runnable navigationRunner = () -> {
            try {
                this.frame.browsingContext.reload(new ReloadParameters());
            } catch (Exception e) {
                rewriteNavigationError(this.url(), Objects.isNull(options.getTimeout()) ? this._timeoutSettings.navigationTimeout() : options.getTimeout(), e);
            }
        };
        return this.frame.waitForNavigation(options, navigationRunner);
    }

    @Override
    public void setDefaultNavigationTimeout(int timeout) {
        this._timeoutSettings.setDefaultNavigationTimeout(timeout);
    }

    @Override
    public void setDefaultTimeout(int timeout) {
        this._timeoutSettings.setDefaultTimeout(timeout);
    }

    @Override
    public int getDefaultTimeout() {
        return this._timeoutSettings.timeout();
    }

    @Override
    public int getDefaultNavigationTimeout() {
        return this._timeoutSettings.navigationTimeout();
    }

    @Override
    public boolean isJavaScriptEnabled() {
        return this.cdpEmulationManager.javascriptEnabled();
    }

    @Override
    public void setGeolocation(GeolocationOptions options) {
        super.setGeolocation(options);
        SetGeoLocationOverrideOptions setGeoLocationOverrideOptions = new SetGeoLocationOverrideOptions();
        GeolocationCoordinates coordinates = new GeolocationCoordinates();
        coordinates.setLatitude(options.getLatitude());
        coordinates.setLongitude(options.getLongitude());
        coordinates.setAccuracy(options.getAccuracy());
        setGeoLocationOverrideOptions.setCoordinates(coordinates);
        this.frame.browsingContext.setGeolocationOverride(setGeoLocationOverrideOptions);
    }

    @Override
    public void setJavaScriptEnabled(boolean enabled) {
        this.cdpEmulationManager.setJavaScriptEnabled(enabled);
    }

    @Override
    public void emulateMediaType(MediaType type) {
        this.cdpEmulationManager.emulateMediaType(type);
    }

    @Override
    public void emulateCPUThrottling(double factor) {
        this.cdpEmulationManager.emulateCPUThrottling(factor);
    }

    @Override
    public void emulateMediaFeatures(List<MediaFeature> features) {
        this.cdpEmulationManager.emulateMediaFeatures(features);
    }

    @Override
    public void emulateTimezone(String timezoneId) {
        this.cdpEmulationManager.emulateTimezone(timezoneId);
    }

    @Override
    public void emulateIdleState(IdleOverridesState.Overrides overrides) {
        this.cdpEmulationManager.emulateIdleState(overrides);
    }

    @Override
    public void emulateVisionDeficiency(VisionDeficiency type) {
        this.cdpEmulationManager.emulateVisionDeficiency(type);
    }

    @Override
    public void setViewport(Viewport viewport) throws ExecutionException, InterruptedException {
        if (!this.browser().cdpSupported()) {
            SetViewportParameters options = new SetViewportParameters();
            if (Objects.nonNull(viewport) && viewport.getWidth() > 0 && viewport.getHeight() > 0) {
                options.setViewport(new BidiViewport(viewport.getWidth(), viewport.getHeight()));
            }
            if (Objects.nonNull(viewport) && viewport.getDeviceScaleFactor() != null) {
                options.setDevicePixelRatio(viewport.getDeviceScaleFactor());
            }
            this.frame.browsingContext.setViewport(options);
            this.viewport = viewport;
            return;
        }
        boolean needsReload = this.cdpEmulationManager.emulateViewport(viewport);
        this.viewport = viewport;
        if (needsReload) {
            this.reload();
        }
    }

    @Override
    public Viewport viewport() {
        return this.viewport;
    }

    @Override
    public byte[] pdf(PDFOptions options) throws IOException {
        return this.pdf(options, LengthUnit.CM);
    }

    @Override
    public void pdf(String path) throws IOException {
        this.pdf(new PDFOptions(path), LengthUnit.CM);
    }

    @Override
    public byte[] pdf(PDFOptions options, LengthUnit lengthUnit) throws IOException {
        double paperWidth = 8.5;
        double paperHeight = 11;
        if (Objects.nonNull(options.getFormat())) {
            PaperFormats format = options.getFormat();
            paperWidth = format.getWidth();
            paperHeight = format.getHeight();
        } else {
            Double width = convertPrintParameterToInches(options.getWidth(), lengthUnit);
            if (Objects.nonNull(width)) {
                paperWidth = width;
            }
            Double height = convertPrintParameterToInches(options.getHeight(), lengthUnit);
            if (Objects.nonNull(height)) {
                paperHeight = height;
            }
        }
        PDFMargin margin = options.getMargin();
        Double marginTop, marginLeft, marginBottom, marginRight;
        if ((marginTop = convertPrintParameterToInches(margin.getTop(), lengthUnit)) == null) {
            marginTop = 0.0;
        }
        if ((marginLeft = convertPrintParameterToInches(margin.getLeft(), lengthUnit)) == null) {
            marginLeft = 0.0;
        }
        if ((marginBottom = convertPrintParameterToInches(margin.getBottom(), lengthUnit)) == null) {
            marginBottom = 0.0;
        }
        if ((marginRight = convertPrintParameterToInches(margin.getRight(), lengthUnit)) == null) {
            marginRight = 0.0;
        }
        if (options.getOutline()) {
            options.setTagged(true);
        }
        List<String> pageRanges = new ArrayList<>();
        if (StringUtil.isNotEmpty(options.getPageRanges())) {
            pageRanges = Arrays.asList(options.getPageRanges().split(","));
        }
        this.mainFrame().isolatedRealm().evaluate("() => { return document.fonts.ready;}");
        PrintOptions printOptions = new PrintOptions(options.getPrintBackground(), new PrintMarginParameters(marginBottom, marginLeft, marginRight, marginTop), options.getLandscape() ? Orientation.Landscape : Orientation.Portrait, new PrintPageParameters(paperWidth, paperHeight), pageRanges, options.getScale(), !options.getPreferCSSPageSize());
        JsonNode data = this.frame.browsingContext.print(printOptions);
        byte[] bytes = Base64Util.decode(data.asText().getBytes(StandardCharsets.UTF_8));
        if (StringUtil.isNotEmpty(options.getPath())) {
            FileUtil.createNewFile(options.getPath());
            Files.write(Paths.get(options.getPath()), bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        return bytes;
    }

    @Override
    protected String _screenshot(ScreenshotOptions options) throws IOException {
        if (options.getOmitBackground()) {
            throw new UnsupportedOperationException("BiDi does not support 'omitBackground'.");
        }
        if (options.getOptimizeForSpeed()) {
            throw new UnsupportedOperationException("BiDi does not support 'optimizeForSpeed'.");
        }
        if (!options.getFromSurface()) {
            throw new UnsupportedOperationException("BiDi does not support 'fromSurface'.");
        }
        if (Objects.nonNull(options.getClip()) && options.getClip().getScale() != 1) {
            throw new UnsupportedOperationException("BiDi does not support 'scale' in 'clip'.");
        }
        BoundingBox box = null;
        if (Objects.nonNull(options.getClip())) {
            if (options.getCaptureBeyondViewport()) {
                box = options.getClip();
            } else {
                // The clip is always with respect to the document coordinates, so we
                // need to convert this to viewport coordinates when we aren't capturing
                // beyond the viewport.
                Object response = this.evaluate("() => {\n" +
                        "  if (!window.visualViewport) {\n" +
                        "    throw new Error('window.visualViewport is not supported.');\n" +
                        "  }\n" +
                        "  return {\n" +
                        "    pageLeft: window.visualViewport.pageLeft,\n" +
                        "    pageTop: window.visualViewport.pageTop,\n" +
                        "  };\n" +
                        "}");
                JsonNode visualViewport = OBJECTMAPPER.convertValue(response, JsonNode.class);
                box = new BoundingBox(options.getClip().getX() - visualViewport.get("pageLeft").asDouble(), options.getClip().getY() - visualViewport.get("pageTop").asDouble(), options.getClip().getWidth(), options.getClip().getHeight());
            }

        }
        CaptureScreenshotOptions screenshotOptions = new CaptureScreenshotOptions();
        if (options.getCaptureBeyondViewport()) {
            screenshotOptions.setOrigin(Origin.Document);
        } else {
            screenshotOptions.setOrigin(Origin.Viewport);
        }
        ImageFormat format = new ImageFormat();
        format.setType("image/" + options.getType());
        if (Objects.nonNull(options.getQuality())) {
            format.setQuality(options.getQuality());
        }
        screenshotOptions.setFormat(format);
        if (Objects.nonNull(box)) {
            ClipRectangle clip = new ClipRectangle();
            clip.setType("box");
            clip.setX(box.getX());
            clip.setY(box.getY());
            clip.setWidth(box.getWidth());
            clip.setHeight(box.getHeight());
            screenshotOptions.setClip(clip);
        }
        JsonNode data = this.frame.browsingContext.captureScreenshot(screenshotOptions);
        String imageBase64String = data.asText();
        byte[] bytes = Base64Util.decode(imageBase64String.getBytes(StandardCharsets.UTF_8));
        if (StringUtil.isNotEmpty(options.getPath())) {
            FileUtil.createNewFile(options.getPath());
            Files.write(Paths.get(options.getPath()), bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        return imageBase64String;
    }

    @Override
    public CDPSession createCDPSession() {
        return this.frame.createCDPSession();
    }

    @Override
    public void bringToFront() {
        this.frame.browsingContext.activate();
    }

    @Override
    public NewDocumentScriptEvaluation evaluateOnNewDocument(String pptrFunction, EvaluateType type, Object... args) throws JsonProcessingException {
        String expression = evaluationExpression(pptrFunction, args);
        String identifier = this.frame.browsingContext.addPreloadScript(expression, new AddPreloadScriptOptions());
        return new NewDocumentScriptEvaluation(identifier);
    }

    @Override
    public void removeScriptToEvaluateOnNewDocument(String identifier) {
        this.frame.browsingContext.removePreloadScript(identifier);
    }

    @Override
    public void exposeFunction(String name, BindingFunction pptrFunction) {
        this.mainFrame().exposeFunction(name, pptrFunction);
    }

    @Override
    public boolean isDragInterceptionEnabled() {
        return false;
    }

    @Override
    public void setCacheEnabled(boolean enabled) {
        if (!this.browserContext.browser().cdpSupported()) {
            this.frame.browsingContext.setCacheBehavior(enabled ? "default" : "bypass");
            return;
        }
        // TODO: handle CDP-specific cases such as mprach.
        Map<String, Object> params = ParamsFactory.create();
        params.put("cacheDisabled", !enabled);
        this.client().send("Network.setCacheDisabled", params);
    }

    @Override
    public List<Cookie> cookies(String... urls) {
        if (urls == null || urls.length == 0) {
            return new ArrayList<>();
        }
        JsonNode cookies = this.frame.browsingContext.getCookies(new GetCookiesOptions());
        Iterator<JsonNode> elements = cookies.elements();
        List<Cookie> cookiesList = new ArrayList<>();
        while (elements.hasNext()) {
            Cookie cookie = bidiToPuppeteerCookie(elements.next());
            List<String> urlList = new ArrayList<>(Arrays.asList(urls));
            boolean match = testUrlMatchCookie(urlList, cookie);
            if (match) {
                cookiesList.add(cookie);
            }
        }
        return cookiesList;
    }

    @Override
    public boolean isServiceWorkerBypassed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Target target() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AwaitableResult<FileChooser> fileChooserWaitFor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebWorker> workers() {
        return Collections.unmodifiableList(new ArrayList<>(this.workers));
    }

    @Override
    public void setRequestInterception(boolean enable) {
        this.userInterception = this.toggleInterception(Collections.singletonList(InterceptPhase.BEFORE_REQUEST_SENT.toString()), this.userInterception, enable);
    }

    @Override
    public void setExtraHTTPHeaders(Map<String, String> headers) {
        List<HeaderEntry> extraHTTPHeaders = new ArrayList<>();
        headers.forEach((key, value) -> extraHTTPHeaders.add(new HeaderEntry(key.toLowerCase(), value)));
        this.extraHTTPHeaders = extraHTTPHeaders;
        this.extraHeadersInterception = this.toggleInterception(
                Collections.singletonList(InterceptPhase.BEFORE_REQUEST_SENT.toString()),
                this.extraHeadersInterception,
                !extraHTTPHeaders.isEmpty());
    }

    @Override
    public void authenticate(Credentials credentials) {
        this.authInterception = this.toggleInterception(Collections.singletonList(InterceptPhase.AUTH_REQUIRED.toString()),
                this.authInterception,
                Objects.nonNull(credentials)
        );

        this.credentials = credentials;
    }

    @Override
    public void setDragInterception(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBypassServiceWorker(boolean bypass) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setOfflineMode(boolean enabled) {
        if (!this.browserContext.browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }

        if (Objects.isNull(this.emulatedNetworkConditions)) {
            this.emulatedNetworkConditions = new InternalNetworkConditions(false, -1, -1, 0);
        }
        this.emulatedNetworkConditions.setOffline(enabled);
        this.applyNetworkConditions();
    }

    @Override
    public void emulateNetworkConditions(NetworkConditions networkConditions) {
        if (!this.browserContext.browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        if (Objects.isNull(this.emulatedNetworkConditions)) {
            this.emulatedNetworkConditions = new InternalNetworkConditions(false, -1, -1, 0);
        }
        this.emulatedNetworkConditions.setUpload(Objects.nonNull(networkConditions) ? networkConditions.getUpload() : -1);
        this.emulatedNetworkConditions.setDownload(Objects.nonNull(networkConditions) ? networkConditions.getDownload() : -1);
        this.emulatedNetworkConditions.setLatency(Objects.nonNull(networkConditions) ? networkConditions.getLatency() : 0);
        this.applyNetworkConditions();
    }

    private void applyNetworkConditions() {
        if (Objects.isNull(this.emulatedNetworkConditions)) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("offline", this.emulatedNetworkConditions.getOffline());
        params.put("latency", this.emulatedNetworkConditions.getLatency());
        params.put("uploadThroughput", this.emulatedNetworkConditions.getUpload());
        params.put("downloadThroughput", this.emulatedNetworkConditions.getDownload());
        this.client().send("Network.emulateNetworkConditions", params);
    }

    @Override
    public void setCookie(CookieParam... cookies) {
        if (Objects.isNull(cookies) || cookies.length == 0) {
            return;
        }
        String pageURL = this.url();
        boolean pageUrlStartsWithHTTP = pageURL.startsWith("http");
        for (CookieParam cookie : cookies) {
            String cookieUrl = StringUtil.isNotEmpty(cookie.getUrl()) ? cookie.getUrl() : "";
            if (StringUtil.isEmpty(cookieUrl) && pageUrlStartsWithHTTP) {
                cookieUrl = pageURL;
            }
            ValidateUtil.assertArg(!Objects.equals(cookieUrl, "about:blank"), "Blank page can not have cookie " + cookie.getName());
            ValidateUtil.assertArg(Objects.nonNull(cookieUrl) && !cookieUrl.startsWith("data:"), "Data URL page can not have cookie " + cookie.getName());
            ValidateUtil.assertArg(Objects.isNull(cookie.getPartitionKey()) || cookie.getPartitionKey().isTextual(), "BiDi only allows domain partition keys");

            URL normalizedUrl = null;
            try {
                normalizedUrl = new URL(cookieUrl);
            } catch (MalformedURLException ignored) {

            }

            String domain = StringUtil.isNotEmpty(cookie.getDomain()) ? cookie.getDomain() : Objects.nonNull(normalizedUrl) ? normalizedUrl.getHost() : null;
            Objects.requireNonNull(domain, "At least one of the url and domain needs to be specified");

            PartialCookie bidiCookie = new PartialCookie();
            bidiCookie.setDomain(domain);
            bidiCookie.setName(cookie.getName());
            bidiCookie.setValue(new BytesValue("string", cookie.getValue()));
            bidiCookie.setPath(cookie.getPath());
            bidiCookie.setHttpOnly(cookie.getHttpOnly());
            bidiCookie.setSecure(cookie.getSecure());
            if (Objects.nonNull(cookie.getSameSite())) {
                bidiCookie.setSameSite(convertCookiesSameSiteCdpToBiDi(cookie.getSameSite()));
            }
            bidiCookie.setExpiry(cookie.getExpires());
            bidiCookie.setSameParty(cookie.getSameParty());
            bidiCookie.setSourceScheme(cookie.getSourceScheme());
            bidiCookie.setPriority(cookie.getPriority());
            bidiCookie.setUrl(cookie.getUrl());
            if (cookie.getPartitionKey() != null) {
                this.browserContext().userContext.setCookie(bidiCookie, cookie.getPartitionKey().asText());
            } else {
                this.frame.browsingContext.setCookie(bidiCookie);
            }
        }
    }

    @Override
    public void deleteCookie(DeleteCookiesRequest... cookies) {
        if (cookies == null || cookies.length == 0) {
            return;
        }
        List<CookieFilter> filters = new ArrayList<>();
        for (DeleteCookiesRequest cookie : cookies) {
            String cookieUrl = StringUtil.isNotEmpty(cookie.getUrl()) ? cookie.getUrl() : this.url();
            URL normalizedUrl = null;
            try {
                normalizedUrl = new URL(cookieUrl);
            } catch (MalformedURLException ignored) {

            }
            String domain = StringUtil.isNotEmpty(cookie.getDomain()) ? cookie.getDomain() : Objects.nonNull(normalizedUrl) ? normalizedUrl.getHost() : null;
            CookieFilter cookieFilter = new CookieFilter();
            cookieFilter.setDomain(domain);
            cookieFilter.setName(cookie.getName());
            cookieFilter.setPath(cookie.getPath());
            filters.add(cookieFilter);
        }
        this.frame.browsingContext.deleteCookie(filters);
    }

    @Override
    public Response goBack(WaitForOptions options) {
        return this.go(-1, options);
    }

    @Override
    public Response goForward(WaitForOptions options) {
        return this.go(1, options);
    }

    private Response go(int delta, WaitForOptions options) {
        try {
            return this.waitForNavigation(options, () -> {
                this.frame.browsingContext.traverseHistory(delta);
            });
        } catch (Exception e) {
            if (e instanceof EvaluateException && e.getMessage().contains("no such history entry")) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        throw new UnsupportedOperationException();
    }

    private boolean testUrlMatchCookie(List<String> urls, Cookie cookie) {
        List<URL> uris = urls.stream().map(url -> {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        for (URL uri : uris) {
            if (!testUrlMatchCookieHostname(cookie, uri)) {
                return false;
            }
            return testUrlMatchCookiePath(cookie, uri);
        }
        return false;
    }

    private boolean testUrlMatchCookiePath(Cookie cookie, URL url) {
        String uriPath = url.getPath();
        String cookiePath = cookie.getPath();
        if (Objects.equals(uriPath, cookiePath)) {
            // The cookie-path and the request-path are identical.
            return true;
        }
        if (uriPath.startsWith(cookiePath)) {
            // The cookie-path is a prefix of the request-path.
            if (cookiePath.endsWith("/")) {
                // The last character of the cookie-path is %x2F ("/").
                return true;
            }
            // The first character of the request-path that is not included in the cookie-path
            // is a %x2F ("/") character.
            return uriPath.length() > cookiePath.length() && uriPath.charAt(cookiePath.length()) == '/';
        }
        return false;
    }

    private boolean testUrlMatchCookieHostname(Cookie cookie, URL url) {
        String hostName = url.getHost().toLowerCase();
        String domain = cookie.getDomain().toLowerCase();
        if (Objects.equals(hostName, domain)) {
            return true;
        }
        return domain.startsWith(".") && hostName.endsWith(domain);
    }


    @Override
    public void removeExposedFunction(String name) {
        this.frame.removeExposedFunction(name);
    }

    @Override
    public Metrics metrics() throws JsonProcessingException {
        throw new UnsupportedOperationException();
    }

    private String toggleInterception(List<String> phases, String interception, boolean expected) {
        if (expected && StringUtil.isEmpty(interception)) {
            return this.frame.browsingContext.addIntercept(new AddInterceptOptions(phases));
        } else if (!expected && StringUtil.isNotEmpty(interception)) {
            this.frame.browsingContext.userContext.browser.removeIntercept(interception);
            return null;
        }
        return interception;
    }


    @Override
    public Keyboard keyboard() {
        return this.keyboard;
    }

    @Override
    public Touchscreen touchscreen() {
        return this.touchscreen;
    }

    @Override
    public Coverage coverage() {
        return this.coverage;
    }

    @Override
    public Tracing tracing() {
        return this.tracing;
    }

    @Override
    public Accessibility accessibility() {
        throw new UnsupportedOperationException();
    }

    private String evaluationExpression(String pptrFunction, Object... args) throws JsonProcessingException {
        return "() => {" + Helper.evaluationString(pptrFunction, args) + "}";
    }


    @Override
    public Mouse mouse() {
        return this.mouse;
    }


    EventEmitter<PageEvents> trustedEmitter() {
        return this.trustedEmitter;
    }

    @Override
    public void emulate(Device device) {
        throw new UnsupportedOperationException();
    }
}
