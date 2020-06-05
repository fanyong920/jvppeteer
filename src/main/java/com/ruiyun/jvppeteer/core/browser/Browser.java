package com.ruiyun.jvppeteer.core.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.core.page.TaskQueue;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.EventHandler;
import com.ruiyun.jvppeteer.events.Events;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.target.TargetCreatedPayload;
import com.ruiyun.jvppeteer.protocol.target.TargetDestroyedPayload;
import com.ruiyun.jvppeteer.protocol.target.TargetInfoChangedPayload;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 浏览器实例
 */
public class Browser extends EventEmitter {

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

    private final Function<Object,Object> closeCallback;

    private CountDownLatch waitforTargetLatch;


    public Browser(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors,
                   Viewport defaultViewport, Process process, Function<Object,Object> closeCallback) {
        super();
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.viewport = defaultViewport;
        this.process = process;
        this.screenshotTaskQueue = new TaskQueue<>();
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = o -> null;
        }
        this.closeCallback = closeCallback;
        this.defaultContext = new BrowserContext(connection, this, null);
        this.contexts = new HashMap<>();
        if (ValidateUtil.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                contexts.putIfAbsent(contextId, new BrowserContext(this.connection, this, contextId));
            }
        }
        this.targets = new ConcurrentHashMap<>();
        waitforTargetLatch = new CountDownLatch(1);
        DefaultBrowserListener<Object> disconnectedLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Browser browser = (Browser) this.getTarget();
                browser.emit(Events.BROWSER_DISCONNECTED.getName(), null);
            }
        };
        disconnectedLis.setTarget(this);
        disconnectedLis.setMothod(Events.CONNECTION_DISCONNECTED.getName());
        this.connection.on(disconnectedLis.getMothod(), disconnectedLis);

        DefaultBrowserListener<TargetCreatedPayload> targetCreatedLis = new DefaultBrowserListener<TargetCreatedPayload>() {
            @Override
            public void onBrowserEvent(TargetCreatedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetCreated(event);
            }
        };
        targetCreatedLis.setTarget(this);
        targetCreatedLis.setMothod("Target.targetCreated");
        this.connection.on(targetCreatedLis.getMothod(), targetCreatedLis);

        DefaultBrowserListener<TargetDestroyedPayload> targetDestroyedLis = new DefaultBrowserListener<TargetDestroyedPayload>() {
            @Override
            public void onBrowserEvent(TargetDestroyedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetDestroyed(event);
            }
        };
        targetDestroyedLis.setTarget(this);
        targetDestroyedLis.setMothod("Target.targetDestroyed");
        this.connection.on(targetDestroyedLis.getMothod(), targetDestroyedLis);

        DefaultBrowserListener<TargetInfoChangedPayload> targetInfoChangedLis = new DefaultBrowserListener<TargetInfoChangedPayload>() {
            @Override
            public void onBrowserEvent(TargetInfoChangedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetInfoChanged(event);
            }
        };
        targetInfoChangedLis.setTarget(this);
        targetInfoChangedLis.setMothod("Target.targetInfoChanged");
        this.connection.on(targetInfoChangedLis.getMothod(), targetInfoChangedLis);
    }

    private void targetDestroyed(TargetDestroyedPayload event) {
        Target target = this.targets.remove(event.getTargetId());
        target.initializedCallback(false);
        target.closedCallback();
        if (target.waitInitializedPromise()) {
            this.emit(Events.BROWSER_TARGETDESTROYED.getName(), target);
            target.browserContext().emit(Events.BROWSER_TARGETDESTROYED.getName(), target);
        }

    }

    private void targetInfoChanged(TargetInfoChangedPayload event) {
        Target target = this.targets.get(event.getTargetInfo().getTargetId());
        ValidateUtil.assertArg(target != null, "target should exist before targetInfoChanged");
        String previousURL = target.url();
        boolean wasInitialized = target.getIsInitialized();
        target.targetInfoChanged(event.getTargetInfo());
        if (wasInitialized && !previousURL.equals(target.url())) {
            this.emit(Events.BROWSER_TARGETCHANGED.getName(), target);
            target.browserContext().emit(Events.BROWSERCONTEXT_TARGETCHANGED.getName(), target);
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
        JsonNode result = this.connection.send("Target.createBrowserContext", null, true);
        String browserContextId = result.get("browserContextId").asText();
        BrowserContext context = new BrowserContext(this.connection, this, browserContextId);
        this.contexts.put(browserContextId, context);
        return context;
    }

    public void disposeContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("browserContextId", contextId);
        this.connection.send("Target.disposeBrowserContext", params, true);
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
    public static Browser create(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors, Viewport viewport, Process process, Function<Object, Object> closeCallback) {
        Browser browser = new Browser(connection, contextIds, ignoreHTTPSErrors, viewport, process, closeCallback);
        Map<String, Object> params = new HashMap<>();
        params.put("discover", true);
        connection.send("Target.setDiscoverTargets", params, false);
        return browser;
    }

    /**
     * 当前浏览器有target创建时会调用的方法
     *
     * @param event 创建的target具体信息
     */
    protected void targetCreated(TargetCreatedPayload event) {
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
            this.emit(Events.BROWSER_TARGETCREATED.getName(), target);
            context.emit(Events.BROWSERCONTEXT_TARGETCREATED.getName(), target);
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
        Target existingTarget = find(targets(), predicate);
        if (null != existingTarget) {
            return existingTarget;
        }
        DefaultBrowserListener<Target> createLis = null;
        DefaultBrowserListener<Target> changeLis = null;
        try {
            createLis = new DefaultBrowserListener<Target>() {
                @Override
                public void onBrowserEvent(Target event) {
                    boolean test = predicate.test(event);
                    Browser browser = (Browser) this.getTarget();
                    if (test) {
                        browser.getWaitforTargetLatch().countDown();
                    }
                }
            };
            createLis.setMothod(Events.BROWSER_TARGETCREATED.getName());
            createLis.setTarget(this);
            this.on(createLis.getMothod(), createLis);

            changeLis = new DefaultBrowserListener<Target>() {
                @Override
                public void onBrowserEvent(Target event) {
                    boolean test = predicate.test(event);
                    Browser browser = (Browser) this.getTarget();
                    if (test) {
                        browser.getWaitforTargetLatch().countDown();
                    }
                }
            };
            changeLis.setMothod(Events.BROWSER_TARGETCHANGED.getName());
            changeLis.setTarget(this);
            this.on(changeLis.getMothod(), changeLis);
            this.getWaitforTargetLatch().await(options.getTimeout(), TimeUnit.MILLISECONDS);
            Target target = find(targets(), predicate);
            if (target != null) {
                return target;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.removeListener(Events.BROWSER_TARGETCREATED.getName(), createLis);
            this.removeListener(Events.BROWSER_TARGETCHANGED.getName(), changeLis);
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
        this.closeCallback.apply(null);
        this.disconnect();
    }

    public void disconnect() {
        this.connection.dispose();
    }

    private JsonNode getVersion() {
        return this.connection.send("Browser.getVersion", null, true);
    }

    public boolean isConnected() {
        return !this.connection.getClosed();
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
        params.put("browserContextId", contextId);
        JsonNode recevie = this.connection.send("Target.createTarget", params, true);
        if (recevie != null) {
            Target target = this.targets.get(recevie.get(Constant.RECV_MESSAGE_TARFETINFO_TARGETID_PROPERTY).asText());
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
    public void onDisconnected(EventHandler<Object> handler) {
        DefaultBrowserListener<Object> listener = new DefaultBrowserListener<>();
        listener.setMothod("disconnected");
        listener.setHandler(handler);
        this.on(listener.getMothod(), listener);
    }

    /**
     * <p>监听浏览器事件targetchanged</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTargetchanged(EventHandler<Target> handler) {
        DefaultBrowserListener<Target> listener = new DefaultBrowserListener<>();
        listener.setMothod("targetchanged");
        listener.setHandler(handler);
        this.on(listener.getMothod(), listener);
    }

    /**
     * <p>监听浏览器事件targetcreated</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTrgetcreated(EventHandler<Target> handler) {
        DefaultBrowserListener<Target> listener = new DefaultBrowserListener<>();
        listener.setMothod("targetcreated");
        listener.setHandler(handler);
        this.on(listener.getMothod(), listener);
    }

    /**
     * <p>监听浏览器事件targetcreated</p>
     * <p>浏览器一共有四种事件</p>
     * <p>method ="disconnected","targetchanged","targetcreated","targetdestroyed"</p>
     *
     * @param handler 事件处理器
     */
    public void onTargetdestroyed(EventHandler<Target> handler) {
        DefaultBrowserListener<Target> listener = new DefaultBrowserListener<>();
        listener.setMothod("targetdestroyed");
        listener.setHandler(handler);
        this.on(listener.getMothod(), listener);
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


    private CountDownLatch getWaitforTargetLatch() {
        return waitforTargetLatch;
    }

}
