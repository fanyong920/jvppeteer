package com.ruiyun.jvppeteer.core.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.core.page.TaskQueue;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.TargetCreatedEvent;
import com.ruiyun.jvppeteer.events.TargetDestroyedEvent;
import com.ruiyun.jvppeteer.events.TargetInfoChangedEvent;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 浏览器实例
 */
public class Browser extends EventEmitter<Browser.BrowserEvent> {
    /**
     * 浏览器对应的websocket client包装类，用于发送和接受消息
     */
    private final Connection connection;
    /**
     * 是否忽略https错误
     */
    private final boolean ignoreHTTPSErrors;

    /**
     * 浏览器内的页面视图
     */
    private final Viewport viewport;
    /**
     * 当前浏览器内的所有页面，也包括浏览器自己，{@link Page}和 {@link Browser} 都属于target
     */
    private final Map<String, Target> targets;
    /**
     * 默认浏览器上下文
     */
    private final BrowserContext defaultContext;
    /**
     * 浏览器上下文
     */
    private final Map<String, BrowserContext> contexts;

    private final Process process;

    private final TaskQueue<String> screenshotTaskQueue;

    private final Runnable closeCallback;

    public Browser(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors,
                   Viewport defaultViewport, Process process, Runnable closeCallback) {
        super();
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.viewport = defaultViewport;
        this.process = process;
        this.screenshotTaskQueue = new TaskQueue<>();
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = () -> {};
        }
        this.closeCallback = closeCallback;
        this.defaultContext = new BrowserContext(connection, this, "");
        this.contexts = new HashMap<>();
        if (ValidateUtil.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                contexts.putIfAbsent(contextId, new BrowserContext(this.connection, this, contextId));
            }
        }
        this.targets = new ConcurrentHashMap<>();
        this.connection.on(CDPSession.CDPSessionEvent.disconnected,(ignore) -> this.emit(BrowserEvent.CONNECTION_DISCONNECTED,null));
        this.connection.on(CDPSession.CDPSessionEvent.Target_targetCreated,(event) -> this.targetCreated((TargetCreatedEvent) event));
        this.connection.on(CDPSession.CDPSessionEvent.Target_targetDestroyed,(event) -> this.targetDestroyed((TargetDestroyedEvent) event));
        this.connection.on(CDPSession.CDPSessionEvent.Target_targetInfoChanged, (event) -> this.targetInfoChanged((TargetInfoChangedEvent) event));
    }

    private void targetDestroyed(TargetDestroyedEvent event) {
        Target target = this.targets.remove(event.getTargetId());
        target.initializedCallback(false);
        target.closedCallback();
        if (target.waitInitializedPromise()) {
            this.emit(BrowserEvent.TARGETDESTROYED, target);
            target.browserContext().emit(BrowserEvent.TARGETDESTROYED, target);
        }

    }

    private void targetInfoChanged(TargetInfoChangedEvent event) {
        Target target = this.targets.get(event.getTargetInfo().getTargetId());
        ValidateUtil.assertArg(target != null, "target should exist before targetInfoChanged");
        String previousURL = target.url();
        boolean wasInitialized = target.getIsInitialized();
        target.targetInfoChanged(event.getTargetInfo());
        if (wasInitialized && !previousURL.equals(target.url())) {
            this.emit(BrowserEvent.TARGETCHANGED, target);
            target.browserContext().emit(BrowserEvent.TARGETCHANGED, target);
        }
    }

    public String wsEndpoint() {
        return this.connection.url();
    }

    /**
     * 获取浏览器的所有target
     *
     * @return 所有target
     */
    public List<Target> targets() {
        return this.targets.values().stream().filter(Target::getIsInitialized).collect(Collectors.toList());
    }

    public Process process() {
        return this.process;
    }

    public BrowserContext createIncognitoBrowserContext() {
        JsonNode result = this.connection.send("Target.createBrowserContext");
        String browserContextId = result.get("browserContextId").asText();
        BrowserContext context = new BrowserContext(this.connection, this, browserContextId);
        this.contexts.put(browserContextId, context);
        return context;
    }

    public void disposeContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("browserContextId", contextId);
        this.connection.send("Target.disposeBrowserContext",params);
        this.contexts.remove(contextId);
    }

    /**
     * 创建一个浏览器
     *
     * @param connection        浏览器对应的websocket client包装类
     * @param contextIds        上下文id集合
     * @param ignoreHTTPSErrors 是否忽略https错误
     * @param viewport          视图
     * @param closeCallback 关闭浏览器的回调
     * @param process 浏览器进程
     * @return 浏览器
     */
    public static Browser create(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors, Viewport viewport, Process process, Runnable closeCallback) {
        Browser browser = new Browser(connection, contextIds, ignoreHTTPSErrors, viewport, process, closeCallback);
        Map<String, Object> params = new HashMap<>();
        params.put("discover", true);
        connection.send("Target.setDiscoverTargets", params, null,false);
        return browser;
    }

    /**
     * 当前浏览器有target创建时会调用的方法
     *
     * @param event 创建的target具体信息
     */
    protected void targetCreated(TargetCreatedEvent event) {
        BrowserContext context;
        TargetInfo targetInfo = event.getTargetInfo();
        if (StringUtil.isNotEmpty(targetInfo.getBrowserContextId()) && this.contexts().containsKey(targetInfo.getBrowserContextId())) {
            context = this.contexts().get(targetInfo.getBrowserContextId());
        } else {
            context = this.defaultBrowserContext();
        }
        Target target = new Target(targetInfo, context, () -> this.getConnection().createSession(targetInfo), this.getIgnoreHTTPSErrors(), this.getViewport(), this.screenshotTaskQueue);
        if (this.targets.get(targetInfo.getTargetId()) != null) {
            throw new RuntimeException("Target should not exist befor targetCreated");
        }
        this.targets.put(targetInfo.getTargetId(), target);
        if (target.waitInitializedPromise()) {
            this.emit(BrowserEvent.TARGETCREATED, target);
            context.emit(BrowserEvent.TARGETCREATED, target);
        }
    }

    /**
     * 浏览器启动时必须初始化一个target
     *
     * @param predicate target的断言
     * @param options   浏览器启动参数
     * @return target
     */
    public Target waitForTarget(Predicate<Target> predicate, ChromeArgOptions options) {
        int timeout = options.getTimeout();
        long base = System.currentTimeMillis();
        long now = 0;
        while (true){
            long delay = timeout - now;
            if (delay <= 0) {
                break;
            }
            Target existingTarget = find(targets(), predicate);
            if (null != existingTarget) {
                return existingTarget;
            }
            now = System.currentTimeMillis() - base;
        }
        throw new TimeoutException("waiting for target failed: timeout " + options.getTimeout() + "ms exceeded");
    }

    /**
     * @return {!Target}
     */
    public Target target() {
        for (Target target : this.targets()) {
            if ("browser".equals(target.type())) {
                return target;
            }
        }
        return null;
    }

    /**
     * 返回BrowserContext集合
     * @return BrowserContext集合
     */
    public Collection<BrowserContext> browserContexts() {
        Collection<BrowserContext> contexts = new ArrayList<>();
        contexts.add(this.defaultBrowserContext());
        contexts.addAll(this.contexts().values());
        return contexts;
    }

    public List<Page> pages() {
        return this.browserContexts().stream().flatMap(context -> context.pages().stream()).collect(Collectors.toList());
    }

    public String version() {
        JsonNode version = this.getVersion();
        return version.get("product").asText();
    }

    public String userAgent() {
        JsonNode version = this.getVersion();
        return version.get("userAgent").asText();
    }

    public void close() {
        this.closeCallback.run();//关闭浏览器
        this.disconnect();//关闭websocket,清理connection资源
    }

    public void disconnect() {
        this.connection.dispose();
    }

    private JsonNode getVersion() {
        return this.connection.send("Browser.getVersion");
    }

    public boolean isConnected() {
        return !this.connection.closed;
    }

    private Target find(List<Target> targets, Predicate<Target> predicate) {
        if (ValidateUtil.isNotEmpty(targets)) {
            for (Target target : targets) {
                if (predicate.test(target)) {
                    return target;
                }
            }
        }
        return null;
    }

    /**
     * 在当前浏览器上新建一个页面
     *
     * @return 新建页面
     */
    public Page newPage() {
        return this.defaultContext.newPage();
    }

    /**
     * 在当前浏览器上下文新建一个页面
     *
     * @param contextId 上下文id 如果为空，则使用默认上下文
     * @return 新建页面
     */
    public Page createPageInContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "about:blank");
        if(StringUtil.isNotEmpty(contextId)) {
            params.put("browserContextId", contextId);
        }
        JsonNode recevie = this.connection.send("Target.createTarget", params);
        if (recevie != null) {
            Target target = this.targets.get(recevie.get(Constant.MESSAGE_TARGETID_PROPERTY).asText());
            ValidateUtil.assertArg(target.waitInitializedPromise(), "Failed to create target for page");
            return target.page();
        } else {
            throw new RuntimeException("can't create new page: ");
        }
    }

    /**
     * <p>监听浏览器事件disconnected</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onDisconnected(Consumer<Object> handler) {
        this.on(BrowserEvent.DISCONNECTED, handler);
    }

    /**
     * <p>监听浏览器事件targetchanged</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTargetChanged(Consumer<Target> handler) {
        this.on(BrowserEvent.TARGETCHANGED, handler);
    }

    /**
     * <p>监听浏览器事件targetcreated</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTargetcreated(Consumer<Target> handler) {
        this.on(BrowserEvent.TARGETCREATED, handler);
    }

    /**
     * <p>监听浏览器事件targetcreated</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTargetdestroyed(Consumer<Target> handler) {
        this.on(BrowserEvent.TARGETDESTROYED, handler);
    }

    public Map<String, Target> getTargets() {
        return this.targets;
    }


    public Map<String, BrowserContext> contexts() {
        return contexts;
    }

    public BrowserContext defaultBrowserContext() {
        return defaultContext;
    }

    protected Connection getConnection() {
        return connection;
    }

    private boolean getIgnoreHTTPSErrors() {
        return ignoreHTTPSErrors;
    }

    protected Viewport getViewport() {
        return viewport;
    }
    public enum BrowserEvent{
        CONNECTION_DISCONNECTED("Connection.Disconnected"),
        CDPSESSION_DISCONNECTED("CDPSession.Disconnected"),
        TARGETCREATED ("targetcreated"),
        TARGETDESTROYED ("targetdestroyed"),
        TARGETCHANGED ("targetchanged"),
        DISCONNECTED ("disconnected");
        private String eventName;
        BrowserEvent(String eventName){
            this.eventName = eventName;
        }
        public String getEventName() {
            return eventName;
        }
    }

}
