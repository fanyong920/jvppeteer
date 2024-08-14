package com.ruiyun.jvppeteer.core.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.*;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.util.Helper.fromEmitterEvent;

/**
 * 浏览器实例
 */
public class Browser extends EventEmitter<Browser.BrowserEvent> {
    private final Viewport defaultViewport;
    private final Process process;
    private final Connection connection ;
    private final Runnable closeCallback;
    Function<Target, Boolean> targetFilterCallback;
    Function<Target, Boolean> isPageTargetCallback;
    private final BrowserContext defaultContext ;
    private final Map<String, BrowserContext> contexts = new HashMap<>();
    private final TargetManager targetManager;
    public Browser(String product, Connection connection, List<String> contextIds, Viewport viewport, Process process, Runnable closeCallback, Function<Target, Boolean> targetFilterCallback, Function<Target, Boolean> isPageTargetCallback,boolean waitForInitiallyDiscoveredTargets) {
        super();
        product =  StringUtil.isEmpty(product) ? "chrome" : product;
        this.defaultViewport = viewport;
        this.process = process;
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = () -> {};
        }
        this.closeCallback = closeCallback;
        if(targetFilterCallback == null){
            targetFilterCallback = (ignore) -> true;
        }
        this.targetFilterCallback = targetFilterCallback;
        this.setIsPageTargetCallback(isPageTargetCallback);
        if ("firefox".equals(product)) {
            throw new JvppeteerException("Not Support firefox");
        } else {
            this.targetManager = new ChromeTargetManager(connection, this.createTarget(), this.targetFilterCallback, waitForInitiallyDiscoveredTargets);
        }
        this.defaultContext = new BrowserContext(connection, this, "");
        if (ValidateUtil.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                this.contexts.putIfAbsent(contextId, new BrowserContext(this.connection, this, contextId));
            }
        }
    }
    private final Consumer<Object> emitDisconnected = (ignore) -> {
        this.emit(BrowserEvent.Disconnected, null);
    };
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
    public Process process() {
        return this.process;
    }
    public TargetManager targetManager() {
        return this.targetManager;
    }
    private void setIsPageTargetCallback(Function<Target, Boolean> isPageTargetCallback) {
        if(isPageTargetCallback == null){
            isPageTargetCallback = (target -> TargetType.PAGE.equals(target.type()) || TargetType.BACKGROUND_PAGE.equals(target.type()) || TargetType.WEBVIEW.equals(target.type()));
        }
        this.isPageTargetCallback = isPageTargetCallback;
    }
    public Function<Target, Boolean> getIsPageTargetCallback(){
        return this.isPageTargetCallback;
    }
    public BrowserContext createBrowserContext(BrowserContextOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("proxyServer", options.getProxyServer());
        if(ValidateUtil.isNotEmpty(options.getProxyBypassList())){
            params.put("proxyBypassList", String.join(",",options.getProxyBypassList()));
        }
        JsonNode result = this.connection.send("Target.createBrowserContext", params);
        BrowserContext context = new BrowserContext(this.connection, this, result.get("browserContextId").asText());
        this.contexts.put(result.get("browserContextId").asText(), context);
        return context;
    }
    public List<BrowserContext> browserContexts() {
        List<BrowserContext> contexts = new ArrayList<>();
        contexts.add(this.defaultBrowserContext());
        contexts.addAll(this.contexts.values());
        return contexts;
    }
    public BrowserContext defaultBrowserContext() {
        return this.defaultContext;
    }
    public void disposeContext(String contextId) {
        if(StringUtil.isEmpty(contextId)){
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("browserContextId", contextId);
        this.connection.send("Target.disposeBrowserContext", params);
        this.contexts.remove(contextId);
    }
    private TargetManager.TargetFactory createTarget() {
        return (targetInfo, session, parentSession) -> {
            String browserContextId = targetInfo.getBrowserContextId();
            BrowserContext context;
            if (StringUtil.isNotEmpty(browserContextId) && this.contexts.containsKey(browserContextId)) {
                context = this.contexts.get(browserContextId);
            }else {
                context = this.defaultContext;
            }
            if(context == null){
                throw new JvppeteerException("Missing browser context");
            }
            SessionFactory createSession = (isAutoAttachEmulated) -> this.connection._createSession(targetInfo,isAutoAttachEmulated);
            OtherTarget otherTarget = new OtherTarget(targetInfo, session, context, this.targetManager, createSession);
            if(StringUtil.isNotEmpty(targetInfo.getUrl()) && targetInfo.getUrl().startsWith("devtools://")){
                return new DevToolsTarget(targetInfo, session, context, this.targetManager, createSession, this.defaultViewport);
            }
            if(this.isPageTargetCallback.apply(otherTarget)){
                return new PageTarget(targetInfo, session, context, this.targetManager, createSession, this.defaultViewport);
            }
            if("service_worker".equals(targetInfo.getType()) || "shared_worker".equals(targetInfo.getType())){
                return new WorkerTarget(targetInfo, session, context, this.targetManager, createSession);
            }
            return otherTarget;
        };
    };
    private final Consumer<Target> onAttachedToTarget = (target) -> {
        if(target.isTargetExposed() && target.initializedSubject.blockingGet().equals(Target.InitializationStatus.SUCCESS)){
            this.emit(BrowserEvent.TargetCreated,target);
            target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetCreated,target);
        }
    };
    private final Consumer<Target> onDetachedFromTarget = (target) -> {
        target.initializedSubject.onSuccess(Target.InitializationStatus.ABORTED);
        target.isClosedSubject.onSuccess(true);
        if(target.isTargetExposed() && target.initializedSubject.blockingGet().equals(Target.InitializationStatus.SUCCESS)){
            this.emit(BrowserEvent.TargetDestroyed,target);
            target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetDestroyed,target);
        }
    };

    private final Consumer<Target> onTargetChanged = (target) -> {
        this.emit(BrowserEvent.TargetChanged,target);
        target.browserContext().emit(BrowserContext.BrowserContextEvent.TargetChanged,target);
    };
    private final Consumer<TargetInfo> onTargetDiscovered = (target) -> {
        this.emit(BrowserEvent.TargetDiscovered,target);
    };
    public String wsEndpoint() {
        return this.connection.url();
    }
    public Page newPage() {
        return this.defaultContext.newPage();
    }
    public Page createPageInContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "about:blank");
        if (StringUtil.isNotEmpty(contextId)) {
            params.put("browserContextId", contextId);
        }
        JsonNode result = this.connection.send("Target.createTarget", params);
        if (result != null) {
            String targetId = result.get(Constant.MESSAGE_TARGETID_PROPERTY).asText();
            Target target = this.waitForTarget(t -> t.getTargetId().equals(targetId), Constant.DEFAULT_TIMEOUT);
            if (target == null) {
                throw new JvppeteerException("Missing target for page (id = " + targetId + ")");
            }
            if (!target.initializedSubject.blockingGet().equals(Target.InitializationStatus.SUCCESS)) {
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
    public List<Target> targets() {
        return this.targetManager.getAvailableTargets().values().stream().filter(target -> target.isTargetExposed() && target.initializedSubject.blockingGet().equals(Target.InitializationStatus.SUCCESS)).collect(Collectors.toList());
    }
    public String version() {
        GetVersionResponse version = this.getVersion();
        return version.getProduct();
    }
    public String userAgent() {
        GetVersionResponse version = this.getVersion();
        return version.getUserAgent();
    }
    public void close() {
        this.closeCallback.run();
        this.disconnect();
    }
    public void disconnect() {
        this.targetManager.dispose();
        this.connection.dispose();
        this.detach();
    }
    public boolean connected() {
        return !this.connection.closed;
    }
    private GetVersionResponse getVersion() {
        try {
            return Constant.OBJECTMAPPER.treeToValue(this.connection.send("Browser.getVersion"),GetVersionResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public DebugInfo debugInfo() {
        return new DebugInfo(this.connection.getPendingProtocolErrors());
    }
    public Target waitForTarget(Predicate<Target> predicate, int timeout){
        Observable<Target> targetCreateObservable = Helper.fromEmitterEvent(this, BrowserEvent.TargetCreated);
        Observable<Target> TargetChangeObservable = Helper.fromEmitterEvent(this, BrowserEvent.TargetChanged);
        @NonNull Observable<@NonNull Target> targetsObservable =  Observable.fromIterable(this.targets());
        return Observable.mergeArray(targetCreateObservable, TargetChangeObservable, targetsObservable).filter(predicate::test).timeout(timeout, TimeUnit.MILLISECONDS).blockingFirst();
    }
    public static Browser create(String product,Connection connection, List<String> contextIds, boolean acceptInsecureCerts, Viewport defaultViewport, Process process, Runnable closeCallback,Function<Target,Boolean> targetFilterCallback,Function<Target,Boolean> IsPageTargetCallback,boolean waitForInitiallyDiscoveredTargets) {
        Browser browser = new Browser(product,connection, contextIds, defaultViewport, process, closeCallback, targetFilterCallback,IsPageTargetCallback,waitForInitiallyDiscoveredTargets);
        if(acceptInsecureCerts){
            Map<String, Object> params = new HashMap<>();
            params.put("ignore", true);
            connection.send("Security.setIgnoreCertificateErrors",params);
        }
        browser.attach();
        return browser;
    }

    public enum BrowserEvent{
        CONNECTION_DISCONNECTED("Connection.Disconnected"),
        CDPSESSION_DISCONNECTED("CDPSession.Disconnected"),
        TargetCreated("targetcreated"),
        TargetDestroyed("targetdestroyed"),
        TargetChanged("targetchanged"),
        TargetDiscovered("targetdiscovered"),
        Disconnected("disconnected");
        private String eventName;
        BrowserEvent(String eventName){
            this.eventName = eventName;
        }
        public String getEventName() {
            return eventName;
        }
    }

}
