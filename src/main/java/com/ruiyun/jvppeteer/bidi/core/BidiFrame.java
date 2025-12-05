package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Response;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.bidi.entities.LogEntry;
import com.ruiyun.jvppeteer.bidi.entities.NavigationInfo;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.bidi.entities.ResponseData;
import com.ruiyun.jvppeteer.bidi.entities.SharedReference;
import com.ruiyun.jvppeteer.cdp.core.Accessibility;
import com.ruiyun.jvppeteer.cdp.entities.CallFrame;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageLocation;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.StackTrace;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.cdp.entities.WaitForNetworkIdleOptions;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CdpConnection;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.NETWORK_IDLE_TIME;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;

public class BidiFrame extends Frame {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BidiFrame.class);
    private final BidiCdpSession client;
    private BidiPage bidiPage;
    private BidiFrame bidiFrame;
    BrowsingContext browsingContext;
    Map<String, BidiFrameRealm> realms = new HashMap<>();
    private static final String DEFAULT = "default";
    private static final String INTERNAL = "internal";
    Accessibility accessibility;
    private final Map<BrowsingContext, BidiFrame> frames = new HashMap<>();
    private final Map<String, BidiExposeableFunction> exposedFunctions = new HashMap<>();

    public BidiFrame(BidiFrame parent, BrowsingContext browsingContext) {
        super();
        this.bidiFrame = parent;
        this.browsingContext = browsingContext;
        this.id = browsingContext.id();
        this.client = new BidiCdpSession(this, null);
        this.realms.put(DEFAULT, BidiFrameRealm.from(this.browsingContext.defaultRealm(), this));
        this.realms.put(INTERNAL, BidiFrameRealm.from(this.browsingContext.createWindowRealm("__puppeteer_internal_" + Math.ceil((Math.random() * 10000))), this));
        this.accessibility = new Accessibility(this.realms.get(DEFAULT), this.id);
    }

    public BidiFrame(BidiPage parent, BrowsingContext browsingContext) {
        super();
        this.bidiPage = parent;
        this.browsingContext = browsingContext;
        this.id = browsingContext.id();
        this.client = new BidiCdpSession(this, null);
        this.realms.put(DEFAULT, BidiFrameRealm.from(this.browsingContext.defaultRealm(), this));
        this.realms.put(INTERNAL, BidiFrameRealm.from(this.browsingContext.createWindowRealm("__puppeteer_internal_" + Math.ceil((Math.random() * 10000))), this));
        this.accessibility = new Accessibility(this.realms.get(DEFAULT), this.id);
    }

    public static BidiFrame from(BidiFrame parent, BrowsingContext browsingContext) {
        BidiFrame frame = new BidiFrame(parent, browsingContext);
        frame.initialize();
        return frame;
    }

    public static BidiFrame from(BidiPage parent, BrowsingContext browsingContext) {
        BidiFrame frame = new BidiFrame(parent, browsingContext);
        frame.initialize();
        return frame;
    }

    private void initialize() {
        for (BrowsingContext child : this.browsingContext.children()) {
            this.createFrameTarget(child);
        }
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.browsingcontext, (Consumer<BrowsingContext>) this::createFrameTarget);
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, ignored -> {
            for (BidiCdpSession session : BidiCdpSession.sessions.values()) {
                if (session.frame == this) {
                    session.onClosed();
                }
            }
            this.page().trustedEmitter().emit(PageEvents.FrameDetached, this);
        });
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.request, (Consumer<RequestCore>) request -> {
            BidiRequest httpRequest = BidiRequest.from(request, this, null);
            request.once(RequestCore.RequestCoreEvents.success, (Consumer<ResponseData>) ignored -> {
                this.page().trustedEmitter().emit(PageEvents.RequestFinished, httpRequest);
            });

            request.once(RequestCore.RequestCoreEvents.error, (Consumer<String>) ignored -> {
                this.page().trustedEmitter().emit(PageEvents.RequestFailed, httpRequest);
            });
            httpRequest.finalizeInterceptions();
        });

        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.navigation, (Consumer<Navigation>) navigation -> {
            navigation.once(Navigation.NavigationEvents.fragment, (Consumer<NavigationInfo>) ignored -> {
                this.page().trustedEmitter().emit(PageEvents.FrameNavigated, this);
            });
        });
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.load, ignored -> {
            this.page().trustedEmitter().emit(PageEvents.Load, true);
        });
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.DOMContentLoaded, ignored -> {
            this.hasStartedLoading = true;
            this.page().trustedEmitter().emit(PageEvents.Domcontentloaded, true);
            this.page().trustedEmitter().emit(PageEvents.FrameNavigated, this);
        });

        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.userprompt, (Consumer<UserPrompt>) userPrompt -> {
            this.page().trustedEmitter().emit(PageEvents.Dialog, BidiDialog.from(userPrompt));
        });
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.log, (Consumer<LogEntry>) entry -> {
            if (!Objects.equals(this.id, entry.getSource().getContext())) {
                return;
            }
            if (isConsoleLogEntry(entry)) {
                List<JSHandle> args = entry.getArgs().stream().map(arg -> this.mainRealm().createHandle(arg)).collect(Collectors.toList());
                StringBuilder text = new StringBuilder();
                for (Object arg : args) {
                    Object parsedValue;
                    if (arg instanceof BidiJSHandle) {
                        BidiJSHandle jsHandle = (BidiJSHandle) arg;
                        if (jsHandle.isPrimitiveValue()) {
                            parsedValue = BidiDeserializer.deserialize(jsHandle.remoteValue());
                        } else {
                            parsedValue = jsHandle.toString();
                        }
                    } else {
                        BidiElementHandle elementHandle = (BidiElementHandle) arg;
                        parsedValue = elementHandle.toString();
                    }
                    text.append(parsedValue).append(" ");
                }
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                    this.page().trustedEmitter().emit(
                            PageEvents.Console,
                            new ConsoleMessage(convertConsoleMessageLevel(entry.getMethod()), text.toString(), args, getStackTraceLocations(entry.getStackTrace()), this));
                }
            } else if (isJavaScriptLogEntry(entry)) {
                StringBuilder stackLines = new StringBuilder();
                if (Objects.nonNull(entry.getStackTrace())) {
                    for (CallFrame frame : entry.getStackTrace().getCallFrames()) {
                        // Note we need to add `1` because the values are 0-indexed.
                        stackLines.append("    at ").append(frame.getFunctionName()).append(" ").append(frame.getUrl()).append(":").append(frame.getLineNumber() + 1).append(":").append(frame.getColumnNumber() + 1).append("\n");
                    }
                }
                String message = StringUtil.isEmpty(entry.getText()) ? stackLines.toString() : stackLines + entry.getText();
                EvaluateException error = new EvaluateException(message);
                this.page().trustedEmitter().emit(PageEvents.PageError, error);
            } else {
                LOGGER.error("Unhandled LogEntry with type {}, text {} and level {}", entry.getType(), entry.getText(), entry.getLevel());
            }
        });

        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.worker, (Consumer<DedicatedWorkerRealm>) realm -> {
            BidiWebWorker worker = BidiWebWorker.from(this, realm);
            realm.on(BidiRealmCore.RealmCoreEvents.destroyed, (Consumer<String>) reason -> {
                this.page().trustedEmitter().emit(PageEvents.WorkerDestroyed, worker);
            });
            this.page().trustedEmitter().emit(PageEvents.WorkerCreated, worker);
        });
    }

    private BidiFrame createFrameTarget(BrowsingContext browsingContext) {
        BidiFrame frame = BidiFrame.from(this, browsingContext);
        this.frames.put(browsingContext, frame);
        this.page().trustedEmitter().emit(PageEvents.FrameAttached, frame);
        browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, ignored -> {
            this.frames.remove(browsingContext);
        });
        return frame;
    }

    public TimeoutSettings timeoutSettings() {
        return this.page()._timeoutSettings;
    }

    @Override
    public BidiFrameRealm mainRealm() {
        return this.realms.get(DEFAULT);
    }

    @Override
    public BidiFrameRealm isolatedRealm() {
        return this.realms.get(INTERNAL);
    }

    public BidiRealm realm(String id) {
        for (BidiFrameRealm realm : this.realms.values()) {
            if (Objects.equals(realm.realm.id, id)) {
                return realm;
            }
        }
        return null;
    }

    public BidiPage page() {
        BidiPage bidiPage = this.bidiPage;
        BidiFrame bidiFrame = this.bidiFrame;
        while (Objects.nonNull(bidiFrame)) {
            bidiPage = bidiFrame.bidiPage;
            bidiFrame = bidiFrame.bidiFrame;
        }
        return bidiPage;
    }

    @Override
    public String url() {
        return this.browsingContext.url();
    }

    @Override
    public Frame parentFrame() {
        if (Objects.nonNull(this.bidiFrame)) {
            return this.bidiFrame;
        }
        return null;
    }

    @Override
    public List<BidiFrame> childFrames() {
        return this.browsingContext.children().stream().map(this.frames::get).collect(Collectors.toList());
    }

    @Override
    public Response goTo(String url, GoToOptions options) {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        Runnable navigateRunner = () -> {
            try {
                this.browsingContext.navigate(url, ReadinessState.Interactive, true);
            } catch (Exception e) {
                if (!Objects.equals("net::ERR_HTTP_RESPONSE_CODE_FAILURE", e.getMessage()) && !Objects.equals("navigation canceled", e.getMessage())) {
                    Helper.rewriteNavigationError(url, Objects.isNull(options.getTimeout()) ? this.timeoutSettings().navigationTimeout() : options.getTimeout(), e);
                }
            }
        };
        try {
            return this.waitForNavigation(Constant.OBJECTMAPPER.convertValue(options, WaitForOptions.class), navigateRunner);
        } catch (Exception e) {
            Helper.rewriteNavigationError(url, Objects.isNull(options.getTimeout()) ? this.timeoutSettings().navigationTimeout() : options.getTimeout(), e);
            return null;
        }
    }

    @Override
    public BidiResponse waitForNavigation(WaitForOptions options, Runnable navigateRunner) {
        BidiLifeCycleWatch lifeCycleWatch = new BidiLifeCycleWatch(this, options);
        Optional.ofNullable(navigateRunner).ifPresent(Runnable::run);
        try {

            Supplier<Boolean> conditionCheck = () -> {
                if (lifeCycleWatch.checkNavigationFinished()) {
                    return true;
                }
                return null;
            };
            int timeout = Objects.isNull(options.getTimeout()) ? this.timeoutSettings().navigationTimeout() : options.getTimeout();
            Helper.waitForCondition(conditionCheck, timeout, "Waiting for navigation failed: timeout " + timeout + "ms exceeded");
            //根据需要等待网络空闲
            this.waitForNetworkIdle(options);
            return lifeCycleWatch.getResponse();
        } finally {
            lifeCycleWatch.dispose();
        }
    }

    private void waitForNetworkIdle(WaitForOptions options) {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        if (Objects.isNull(options)) {
            options = new WaitForOptions();
        }
        List<PuppeteerLifeCycle> waitUntils = Objects.nonNull(options.getWaitUntil()) ? options.getWaitUntil() : Collections.singletonList(PuppeteerLifeCycle.load);
        int timeout = Objects.nonNull(options.getTimeout()) ? options.getTimeout() : this.timeoutSettings().timeout();
        int concurrency = Integer.MAX_VALUE;

        for (PuppeteerLifeCycle waitUntil : waitUntils) {
            switch (waitUntil) {
                case networkIdle:
                    concurrency = 0;
                    break;
                case networkIdle2:
                    concurrency = 2;
                    break;
            }
        }
        if (concurrency == Integer.MAX_VALUE) {
            return;
        }
        this.page().waitForNetworkIdle(new WaitForNetworkIdleOptions(NETWORK_IDLE_TIME, concurrency, timeout));
    }

    private Navigation requestFinished(Navigation navigation, RequestCore request) {
        // Reduces flakiness if the response events arrive after
        // the load event.
        // Usually, the response or error is already there at this point.

        if (Objects.nonNull(request)) {
            if (Objects.nonNull(request.response()) || Objects.nonNull(request.error())) {
                return navigation;
            }
            if (Objects.nonNull(request.redirect())) {
                return requestFinished(navigation, request.redirect());
            }
            AwaitableResult<Boolean> successFuture = new AwaitableResult<>();
            request.once(RequestCore.RequestCoreEvents.success, (Consumer<ResponseData>) info -> {
                successFuture.complete(true);
            });
            AwaitableResult<Boolean> errorFuture = new AwaitableResult<>();
            request.once(RequestCore.RequestCoreEvents.error, (Consumer<String>) errorMsg -> {
                errorFuture.complete(true);
            });
            AwaitableResult<Boolean> redirectFuture = new AwaitableResult<>();
            request.once(RequestCore.RequestCoreEvents.redirect, (Consumer<RequestCore>) requestCore -> {
                requestFinished(navigation, requestCore);
                redirectFuture.complete(true);
            });
            while (true) {
                if (successFuture.isDone() || errorFuture.isDone() || redirectFuture.isDone()) {
                    return navigation;
                }
            }
        }
        return navigation;
    }

    @Override
    public void setContent(String html, WaitForOptions options) throws JsonProcessingException, InterruptedException, ExecutionException {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        int timeout = Objects.isNull(options.getTimeout()) ? this.timeoutSettings().navigationTimeout() : options.getTimeout();
        BidiLifeCycleWatch lifeCycleWatch = new BidiLifeCycleWatch(this, options);
        this.setFrameContent(html);
        Supplier<Boolean> conditionCheck = () -> {
            if (lifeCycleWatch.loadFinished()) {
                return true;
            }
            return null;
        };
        Helper.waitForCondition(conditionCheck, timeout, "Waiting for setContent failed: timeout " + timeout + "ms exceeded");
        this.waitForNetworkIdle(options);
    }

    @Override
    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        return this.browsingContext.waitForDevicePrompt(timeout);
    }

    @Override
    public boolean detached() {
        return this.browsingContext.closed();
    }

    public void exposeFunction(String name, BindingFunction apply) {
        if (this.exposedFunctions.containsKey(name)) {
            throw new JvppeteerException("Failed to add page binding with name " + name + ": globalThis['" + name + "'] already exists!");
        }
        BidiExposeableFunction exposeable = BidiExposeableFunction.from(this, name, apply, false);
        this.exposedFunctions.put(name, exposeable);
    }

    public BidiCdpSession client() {
        return this.client;
    }

    @Override
    public Accessibility accessibility() {
        return this.accessibility;
    }

    public void removeExposedFunction(String name) {
        BidiExposeableFunction exposedFunction = this.exposedFunctions.remove(name);
        if (Objects.isNull(exposedFunction)) {
            throw new JvppeteerException("Failed to remove page binding with name " + name + " window[" + name + "] does not exists!");
        }
        exposedFunction.dispose();
    }

    public CDPSession createCDPSession() {
        if (!this.page().browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        CdpConnection cdpConnection = this.page().browser().cdpConnection();
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setTargetId(this.id);
        return cdpConnection._createSession(targetInfo, true);
    }

    public void waitForLoad(WaitForOptions options, Runnable navigateRunner) {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        if (Objects.isNull(options)) {
            options = new WaitForOptions();
        }
        List<PuppeteerLifeCycle> waitUntil = Objects.nonNull(options.getWaitUntil()) ? options.getWaitUntil() : Collections.singletonList(PuppeteerLifeCycle.load);
        int timeout = Objects.nonNull(options.getTimeout()) ? options.getTimeout() : this.timeoutSettings().navigationTimeout();

        if (ValidateUtil.isEmpty(waitUntil)) {
            waitUntil = Collections.singletonList(PuppeteerLifeCycle.load);
        }
        Set<String> events = new HashSet<>();
        AwaitableResult<Boolean> loadResult = new AwaitableResult<>();
        AwaitableResult<Boolean> domResult = new AwaitableResult<>();
        Consumer<Object> loadListener = event -> loadResult.complete(true);
        Consumer<Object> domListener = event -> domResult.complete(true);
        waitUntil.forEach(value -> {
            switch (value) {
                case load:
                    events.add(value.getValue());
                    this.browsingContext.once(BrowsingContext.BrowsingContextEvents.load, loadListener);
                    break;
                case domcontentloaded:
                    events.add(value.getValue());
                    this.browsingContext.once(BrowsingContext.BrowsingContextEvents.DOMContentLoaded, domListener);
                    break;
            }
        });
        if (events.isEmpty()) {
            return;
        }
        if (!waitUntil.contains(PuppeteerLifeCycle.load)) {
            loadResult.complete(true);
        }
        if (!waitUntil.contains(PuppeteerLifeCycle.domcontentloaded)) {
            domResult.complete(true);
        }
        AwaitableResult<Boolean> detachedResult = new AwaitableResult<>();
        Consumer<Frame> detachedListener = detachedFrame -> {
            if (detachedFrame == this || this.detached()) {
                detachedResult.complete(true);
            }
        };
        this.page().trustedEmitter().once(PageEvents.FrameDetached, detachedListener);
        Optional.ofNullable(navigateRunner).ifPresent(Runnable::run);
        Supplier<Boolean> conditionalCheck = () -> {
            if (detachedResult.isDone()) {
                throw new JvppeteerException("Frame detached.");
            }
            if (loadResult.isDone() && domResult.isDone()) {
                return true;
            }
            return null;
        };
        try {
            Helper.waitForCondition(conditionalCheck, timeout, "Waiting for load failed: timeout " + timeout + "ms exceeded");
        } finally {
            this.page().trustedEmitter().off(PageEvents.FrameDetached, detachedListener);
            this.browsingContext.off(BrowsingContext.BrowsingContextEvents.DOMContentLoaded, domListener);
            this.browsingContext.off(BrowsingContext.BrowsingContextEvents.load, loadListener);
        }
    }

    public void setFiles(BidiElementHandle elementHandle, List<String> files) throws JsonProcessingException {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        SharedReference reference = OBJECTMAPPER.readValue(OBJECTMAPPER.writeValueAsString(elementHandle.remoteValue()), SharedReference.class);
        this.browsingContext.setFiles(reference, files);
    }

    public List<RemoteValue> locateNodes(BidiElementHandle elementHandle, ObjectNode locator) throws JsonProcessingException {
        ValidateUtil.assertArg(!this.detached(), "Attempted to use detached Frame " + this.id);
        SharedReference reference = OBJECTMAPPER.readValue(OBJECTMAPPER.writeValueAsString(elementHandle.remoteValue()), SharedReference.class);
        return this.browsingContext.locateNodes(locator, Collections.singletonList(reference));
    }

    private boolean isConsoleLogEntry(LogEntry entry) {
        return "console".equals(entry.getType());
    }

    private boolean isJavaScriptLogEntry(LogEntry entry) {
        return "javascript".equals(entry.getType());
    }

    private List<ConsoleMessageLocation> getStackTraceLocations(StackTrace stackTrace) {
        List<ConsoleMessageLocation> stackTraceLocations = new ArrayList<>();
        if (Objects.nonNull(stackTrace)) {
            for (CallFrame callFrame : stackTrace.getCallFrames()) {
                stackTraceLocations.add(new ConsoleMessageLocation(callFrame.getUrl(), callFrame.getLineNumber(), callFrame.getColumnNumber()));
            }
        }
        return stackTraceLocations;
    }

    private ConsoleMessageType convertConsoleMessageLevel(String method) {
        switch (method) {
            case "group":
                return ConsoleMessageType.startGroup;
            case "groupCollapsed":
                return ConsoleMessageType.startGroupCollapsed;
            case "groupEnd":
                return ConsoleMessageType.endGroup;
            case "assert":
                return ConsoleMessageType.Assert;
            default:
                return ConsoleMessageType.valueOf(method);
        }
    }

}
