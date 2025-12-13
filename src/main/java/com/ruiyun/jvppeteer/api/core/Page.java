package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.cdp.core.Accessibility;
import com.ruiyun.jvppeteer.cdp.core.Coverage;
import com.ruiyun.jvppeteer.cdp.core.FileChooser;
import com.ruiyun.jvppeteer.cdp.core.Tracing;
import com.ruiyun.jvppeteer.cdp.entities.BoundingBox;
import com.ruiyun.jvppeteer.cdp.entities.ClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.Credentials;
import com.ruiyun.jvppeteer.cdp.entities.DeleteCookiesRequest;
import com.ruiyun.jvppeteer.cdp.entities.Device;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.GeolocationOptions;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.IdleOverridesState;
import com.ruiyun.jvppeteer.cdp.entities.ImageType;
import com.ruiyun.jvppeteer.cdp.entities.LengthUnit;
import com.ruiyun.jvppeteer.cdp.entities.MediaFeature;
import com.ruiyun.jvppeteer.cdp.entities.Metrics;
import com.ruiyun.jvppeteer.cdp.entities.NetworkConditions;
import com.ruiyun.jvppeteer.cdp.entities.NewDocumentScriptEvaluation;
import com.ruiyun.jvppeteer.cdp.entities.PDFOptions;
import com.ruiyun.jvppeteer.cdp.entities.ScreenRecorderOptions;
import com.ruiyun.jvppeteer.cdp.entities.ScreencastOptions;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotClip;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotOptions;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.VisionDeficiency;
import com.ruiyun.jvppeteer.cdp.entities.WaitForNetworkIdleOptions;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.cdp.events.ScreencastFrameEvent;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.ReloadOptions;
import com.ruiyun.jvppeteer.common.ScreenRecorder;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.common.UserAgentOptions;
import com.ruiyun.jvppeteer.common.WaitForOptions;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public abstract class Page extends EventEmitter<PageEvents> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Page.class);
    public final TimeoutSettings _timeoutSettings = new TimeoutSettings();
    protected final Map<Consumer<Request>, Consumer<Request>> requestHandlers = new WeakHashMap<>();
    protected boolean isDragging;
    protected List<String> inflight = new ArrayList<>();

    public Page() {
        super();
        this.on(PageEvents.Request, (Consumer<Request>) request -> {
            inflight.add(request.id());
        });

        this.on(PageEvents.RequestFinished, (Consumer<Request>) request -> {
            inflight.remove(request.id());
        });
        this.on(PageEvents.RequestFailed, (Consumer<Request>) request -> {
            inflight.remove(request.id());
        });
        this.on(PageEvents.Response, (Consumer<Response>) response -> {
            inflight.remove(response.request().id());
        });
    }

    public abstract boolean isServiceWorkerBypassed();

    /**
     * 我们不再支持拦截拖动有效负载。使用 ElementHandle 上的新拖动 API 进行拖动（或仅使用 Page.mouse）。
     *
     * @return 如果拖动事件被拦截，则为 true，否则为 false。
     */
    @Deprecated
    public abstract boolean isDragInterceptionEnabled();

    public abstract boolean isJavaScriptEnabled();

    @Override
    public EventEmitter<PageEvents> on(PageEvents type, Consumer<?> handler) {
        if (type != PageEvents.Request) {
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
    public void off(PageEvents type, Consumer<?> handler) {
        if (type == PageEvents.Request) {
            handler = this.requestHandlers.get(handler);
        }
        super.off(type, handler);
    }

    /**
     * 创建并返回一个可等待的文件选择器结果
     * 当需要拦截文件选择器对话框时，此方法将非常有用
     *
     * @return 返回一个可等待的文件选择器结果，调用者可以通过这个对象来获取用户选择的文件
     */
    public abstract AwaitableResult<FileChooser> fileChooserWaitFor();

    /**
     * 设置页面的地理位置<p>
     * 考虑使用 {@link BrowserContext#overridePermissions(String, WebPermission...)} 授予页面读取其地理位置的权限。
     *
     * @param options 地理位置具体信息
     */
    public void setGeolocation(GeolocationOptions options) {
        if (options.getLongitude() < -180 || options.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude " + options.getLongitude() + ": precondition -180 <= LONGITUDE <= 180 failed.");
        }
        if (options.getLatitude() < -90 || options.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude " + options.getLatitude() + ": precondition -90 <= LATITUDE <= 90 failed.");
        }
        if (options.getAccuracy() < 0) {
            throw new IllegalArgumentException("Invalid accuracy " + options.getAccuracy() + ": precondition 0 <= ACCURACY failed.");
        }
    }

    /**
     * 创建此页面的目标。
     *
     * @return 目标
     */
    public abstract Target target();

    /**
     * 返回页面隶属的浏览器
     *
     * @return 浏览器实例
     */
    public abstract Browser browser();

    /**
     * 获取页面所属的浏览器上下文。
     *
     * @return 浏览器上下文
     */
    public abstract BrowserContext browserContext();

    /**
     * 页面的主框架。
     *
     * @return 主框架
     */
    public abstract Frame mainFrame();

    /**
     * 创建附加到页面的 Chrome Devtools 协议会话。
     *
     * @return CDPSession
     */
    public abstract CDPSession createCDPSession();

    /**
     * 虚拟键盘
     *
     * @return 虚拟键盘
     */
    public abstract Keyboard keyboard();

    /**
     * 触控屏幕
     *
     * @return 触控屏幕
     */
    public abstract Touchscreen touchscreen();

    public abstract Coverage coverage();

    public abstract Tracing tracing();

    public abstract Accessibility accessibility();

    /**
     * 附加到页面的所有框架的数组。
     *
     * @return iframe标签
     */
    public abstract <T extends Frame> List<T> frames();


    /**
     * 该方法返回所有与页面关联的 WebWorkers
     *
     * @return WebWorkers
     */
    public abstract List<WebWorker> workers();

    /**
     * 启用请求拦截器，会激活 request.abort, request.continue 和 request.respond 方法。这提供了修改页面发出的网络请求的功能。<p>
     * 一旦启用请求拦截，每个请求都将停止，除非它继续，响应或中止<p>
     *
     * @param value 是否启用请求拦截器
     */
    public abstract void setRequestInterception(boolean value);

    /**
     * 切换忽略每个请求的 Service Worker。
     *
     * @param bypass 是否忽略
     */
    public abstract void setBypassServiceWorker(boolean bypass);

    /**
     * 我们不再支持拦截拖动有效负载。使用 ElementHandle 上的新拖动 API 进行拖动（或仅使用 Page.mouse）
     *
     * @param enabled 是否启用
     */
    @Deprecated
    public abstract void setDragInterception(boolean enabled);

    /**
     * 将网络连接设置为离线。
     * <p>
     * 它不会改变 Page.emulateNetworkConditions() 中使用的参数
     *
     * @param enabled 设置 true, 启用离线模式。
     */
    public abstract void setOfflineMode(boolean enabled);

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
    public abstract void emulateNetworkConditions(NetworkConditions networkConditions);

    /**
     * 此方法会改变下面几个方法的默认30秒等待时间：
     * ${@link Page#goTo(String)}
     * ${@link Page#goTo(String, GoToOptions)}
     * ${@link Page#goBack(WaitForOptions)}
     * ${@link Page#goForward(WaitForOptions)}
     * ${@link Page#reload(ReloadOptions)}
     * ${@link Page#setContent(String) }
     * ${@link Page#waitForNavigation()}
     *
     * @param timeout 超时时间
     */
    public abstract void setDefaultNavigationTimeout(int timeout);

    public abstract void setDefaultTimeout(int timeout);

    public abstract int getDefaultTimeout();

    public abstract int getDefaultNavigationTimeout();


    /**
     * 此方法在页面内执行 document.querySelector
     * <p>
     * 查找与选择器匹配的第一个元素,如果没有元素匹配指定选择器，返回值是 null。
     *
     * @param selector 要查询页面的 selector。CSS 选择器 可以按原样传递，Puppeteer 特定的选择器语法 允许通过 text、a11y 角色和名称、xpath 和 跨影子根组合这些查询 进行查询。或者，你可以使用前缀 prefix 指定选择器类型。
     * @return 匹配的第一个元素
     * @throws JsonProcessingException JSON异常
     */
    public ElementHandle $(String selector) throws JsonProcessingException {
        return this.mainFrame().$(selector);
    }

    /**
     * 此方法在页面内执行 document.querySelectorAll。如果没有元素匹配指定选择器，返回值是 []。
     *
     * @param selector 选择器
     * @return ElementHandle集合
     * @throws JsonProcessingException JSON异常
     */
    public List<ElementHandle> $$(String selector) throws JsonProcessingException {
        return this.mainFrame().$$(selector);
    }

    /**
     * 此方法和 page.evaluate 的唯一区别是此方法返回的是页内类型(JSHandle)
     *
     * @param pptrFunction 要在页面实例上下文中执行的方法
     * @param args         要在页面实例上下文中执行的方法的参数
     * @return 代表页面元素的实例
     * @throws JsonProcessingException JSON异常
     */
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        return this.mainFrame().evaluateHandle(pptrFunction, args);
    }


    /**
     * page.evaluate 和 page.evaluateHandle 之间的唯一区别是 evaluateHandle 将返回封装在页内对象中的值{@code JSHandle}。
     *
     * @param pptrFunction 要执行的字符串
     * @return JSHandle
     * @throws JsonProcessingException JSON异常
     */
    public JSHandle evaluateHandle(String pptrFunction) throws JsonProcessingException {
        return this.evaluateHandle(pptrFunction, null);
    }

    /**
     * 此方法遍历js堆栈，找到所有带有指定原型的对象
     * <p>
     *
     * @param prototypeHandle 原型处理器
     * @return 代表页面元素的一个实例
     * @throws JsonProcessingException Json解析异常
     */
    public abstract JSHandle queryObjects(JSHandle prototypeHandle) throws JsonProcessingException;

    /**
     * 此方法在页面内执行 document.querySelector，然后把匹配到的元素作为第一个参数传给 pptrFunction。
     *
     * @param selector     选择器
     * @param pptrFunction 在浏览器实例上下文中要执行的方法
     * @param args         要传给 pptrFunction 的参数。（比如你的代码里生成了一个变量，在页面中执行方法时需要用到，可以通过这个 args 传进去）
     * @return pptrFunction 的返回值
     * @throws JsonProcessingException JSON异常
     */
    public Object $eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("$eval", pptrFunction);
        return this.mainFrame().$eval(selector, pptrFunction, args);
    }

    /**
     * 此方法在页面内执行 document.querySelector，然后把匹配到的元素作为第一个参数传给 pptrFunction。
     *
     * @param selector     选择器
     * @param pptrFunction 在浏览器实例上下文中要执行的方法
     * @return pptrFunction 的返回值
     * @throws JsonProcessingException JSON异常
     */
    public Object $eval(String selector, String pptrFunction) throws JsonProcessingException {
        return this.$eval(selector, pptrFunction, null);
    }

    /**
     * 此方法在页面内执行 Array.from(document.querySelectorAll(selector))，然后把匹配到的元素数组作为第一个参数传给 pptrFunction。
     *
     * @param selector     一个框架选择器
     * @param pptrFunction 在浏览器实例上下文中要执行的方法
     * @return pptrFunction 的返回值
     * @throws JsonProcessingException JSON异常
     */
    public Object $$eval(String selector, String pptrFunction) throws JsonProcessingException {
        return this.$$eval(selector, pptrFunction, new ArrayList<>());
    }

    /**
     * 此方法在页面内执行 Array.from(document.querySelectorAll(selector))，然后把匹配到的元素数组作为第一个参数传给 pptrFunction。
     *
     * @param selector     一个框架选择器
     * @param pptrFunction 在浏览器实例上下文中要执行的方法
     * @param args         要传给 pptrFunction 的参数。（比如你的代码里生成了一个变量，在页面中执行方法时需要用到，可以通过这个 args 传进去）
     * @return pptrFunction 的返回值
     * @throws JsonProcessingException JSON异常
     */
    public Object $$eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("$$eval", pptrFunction);
        return this.mainFrame().$$eval(selector, pptrFunction, args);
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
     */
    public abstract List<Cookie> cookies(String... urls);

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
        this.deleteCookie(cookies.toArray(new DeleteCookiesRequest[0]));
    }

    /**
     * 删除指定的cookies
     *
     * @param cookies 待删除的cookies请求列表，每个元素包含待删除的cookie信息
     */
    public abstract void deleteCookie(DeleteCookiesRequest... cookies);


    public abstract void setCookie(CookieParam... cookies);

    /**
     * 在当前文档的主要框架中添加脚本标签
     * <p>
     * 此方法封装了在当前文档的主要框架中添加一个脚本标签的操作它委托
     * {@link Frame#addScriptTag(FrameAddScriptTagOptions)} 方法来执行实际的操作
     *
     * @param options 脚本标签的配置选项，包含了脚本标签的各类属性如URL、位置等
     * @return 返回新添加的脚本标签的元素句柄
     * @throws IOException 当网络通信失败时抛出此异常
     */
    public ElementHandle addScriptTag(FrameAddScriptTagOptions options) throws IOException {
        return this.mainFrame().addScriptTag(options);
    }

    /**
     * 将 link rel="stylesheet" 标记添加到具有所需 URL 的页面中，或将 style type="text/css" 标记添加到内容中。
     *
     * @param options link标签
     * @return 注入完成的tag标签。当style的onload触发或者代码被注入到frame。
     * @throws IOException 异常
     */
    public ElementHandle addStyleTag(FrameAddStyleTagOptions options) throws IOException {
        return this.mainFrame().addStyleTag(options);
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
     */
    public abstract void exposeFunction(String name, BindingFunction pptrFunction) throws JsonProcessingException;

    /**
     * 该方法从页面的 window 对象中删除先前通过 Page.exposeFunction() 添加的名为 name 的函数。
     *
     * @param name 要删除的函数名称
     * @throws JsonProcessingException 如果处理JSON时发生错误
     */
    public abstract void removeExposedFunction(String name) throws JsonProcessingException;

    /**
     * 为HTTP authentication 提供认证凭据 。
     * <p>
     * 传 null 禁用认证。
     * <p>
     * 将在后台打开请求拦截以实现身份验证。这可能会影响性能。
     *
     * @param credentials 验证信息
     */
    public abstract void authenticate(Credentials credentials);

    /**
     * 当前页面发起的每个请求都会带上这些请求头
     * 注意 此方法不保证请求头的顺序
     *
     * @param headers 每个 HTTP 请求都会带上这些请求头。值必须是字符串
     */
    public abstract void setExtraHTTPHeaders(Map<String, String> headers);

    /**
     * 给页面设置userAgent
     *
     * @param userAgent 此页面中使用的特定用户代理
     */
    public void setUserAgent(String userAgent) {
        this.setUserAgent(new UserAgentOptions(userAgent));
    }

    /**
     * 给页面设置userAgent
     *
     * @param options 此页面中使用的特定用户代理参数
     */
    public abstract void setUserAgent(UserAgentOptions options);


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
     * @throws JsonProcessingException 处理JSON时抛出异常
     */
    public abstract Metrics metrics() throws JsonProcessingException;

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
     */
    public String content() throws JsonProcessingException {
        return this.mainFrame().content();
    }

    /**
     * 给页面设置html
     *
     * @param html 分派给页面的HTML。
     * @throws JsonProcessingException 如果在处理 JSON 时发生错误
     */
    public void setContent(String html) throws JsonProcessingException, InterruptedException, ExecutionException {
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
     * @throws JsonProcessingException JSON异常
     */
    public void setContent(String html, WaitForOptions options) throws JsonProcessingException, InterruptedException, ExecutionException {
        this.mainFrame().setContent(html, options);
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
    public Response goTo(String url) throws ExecutionException, InterruptedException {
        return this.goTo(url, new GoToOptions());
    }

    /**
     * <p>导航到指定的url
     * <p>以下情况此方法将报错：
     * <p>发生了 SSL 错误 (比如有些自签名的https证书).
     * <p>目标地址无效
     * <p>超时
     * <p>主页面不能加载
     *
     * @param url     url
     * @param options <p>timeout 跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过page.setDefaultNavigationTimeout(timeout)方法修改默认值
     *                <p>waitUntil  满足什么条件认为页面跳转完成，默认是 load 事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     *                <p>load - 页面的load事件触发时
     *                <p>domcontentloaded - 页面的 DOMContentLoaded 事件触发时
     *                <p>networkidle0 - 不再有网络连接时触发（至少500毫秒后）
     *                <p>networkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     *                <p>referer  Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     * @return Response
     */
    public Response goTo(String url, GoToOptions options) throws ExecutionException, InterruptedException {
        return this.mainFrame().goTo(url, options);
    }

    /**
     * 重新加载页面
     */
    public Response reload() {
        ReloadOptions options = new ReloadOptions();
        options.setIgnoreSameDocumentNavigation(true);
        return this.reload(options);
    }

    /**
     * 重新加载页面
     *
     * @param options 与${@link Page#goTo(String, GoToOptions)}中的options是一样的配置
     * @return 响应
     */
    public abstract Response reload(ReloadOptions options);

    /**
     * 此方法在页面跳转到一个新地址或重新加载时解析，如果你的代码会间接引起页面跳转，这个方法比较有用
     * <p>比如你在在代码中使用了Page.click()方法，引起了页面跳转
     * 注意 通过 History API 改变地址会认为是一次跳转。
     *
     * @return 响应
     */
    public Response waitForNavigation() {
        return this.waitForNavigation(new WaitForOptions());
    }

    /**
     * 此方法在页面跳转到一个新地址或重新加载时解析，如果你的代码会间接引起页面跳转，这个方法比较有用
     * <p>比如你在在代码中使用了Page.click()方法，引起了页面跳转
     * 注意 通过 History API 改变地址会认为是一次跳转。
     *
     * @param options 可选的等待选项
     * @return 响应
     */
    public Response waitForNavigation(WaitForOptions options, Runnable navigateRunner) {
        return this.mainFrame().waitForNavigation(options, navigateRunner);
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
        return this.waitForNavigation(options, null);
    }
    /**
     * 等到某个请求
     *
     * @param predicate 等待的请求
     * @return 要等到的请求
     */
    public Request waitForRequest(Predicate<Request> predicate) {
        return this.waitForRequest(null, predicate, this._timeoutSettings.timeout());
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
    public Request waitForRequest(String url, Predicate<Request> predicate, Integer timeout) {
        if (Objects.isNull(timeout)) {
            timeout = this._timeoutSettings.timeout();
        }
        AwaitableResult<Request> result = AwaitableResult.create();
        Predicate<Request> requestPredicate = request -> {
            if (StringUtil.isNotEmpty(url)) {
                return url.equals(request.url());
            } else if (predicate != null) {
                return predicate.test(request);
            }
            return false;
        };
        AwaitableResult<TargetCloseException> targetCloseException = AwaitableResult.create();
        Consumer<Object> targetCloseListener = (ignore) -> {
            targetCloseException.complete(new TargetCloseException("Page closed!"));
            result.complete();
        };
        this.once(PageEvents.Close, targetCloseListener);
        Consumer<Request> requestListener = request -> {
            if (requestPredicate.test(request)) {
                result.complete(request);
            }
        };
        this.on(PageEvents.Request, requestListener);
        boolean waiting = result.waiting(timeout, TimeUnit.MILLISECONDS);
        if (!waiting) {
            throw new TimeoutException("WaitForRequest timeout of " + timeout + " ms exceeded");
        }
        try {
            if (targetCloseException.isDone() && Objects.nonNull(targetCloseException.get()) && Objects.isNull(result.get())) {
                throw targetCloseException.get();
            }
            return result.get();
        } finally {
            this.off(PageEvents.Request, requestListener);
            this.off(PageEvents.Close, targetCloseListener);
        }
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
     * @param timeout   超时时间,0 代表无限等待
     * @return 要等到的请求
     */
    public Response waitForResponse(String url, Predicate<Response> predicate, Integer timeout) {
        if (Objects.isNull(timeout)) {
            timeout = this._timeoutSettings.timeout();
        }
        Predicate<Response> waitForResponsePredicate = response -> {
            if (StringUtil.isNotEmpty(url)) {
                return url.equals(response.url());
            } else if (predicate != null) {
                return predicate.test(response);
            }
            return false;
        };
        AwaitableResult<Response> result = AwaitableResult.create();
        Consumer<Response> responseListener = response -> {
            if (waitForResponsePredicate.test(response)) {
                result.complete(response);
            }
        };
        this.on(PageEvents.Response, responseListener);
        AwaitableResult<TargetCloseException> targetCloseException = AwaitableResult.create();
        Consumer<Object> closeListener = (ignore) -> {
            targetCloseException.complete(new TargetCloseException("Page closed!"));
            result.complete();
        };
        this.once(PageEvents.Close, closeListener);
        try {
            boolean waiting = result.waiting(timeout, TimeUnit.MILLISECONDS);
            if (!waiting) {
                throw new com.ruiyun.jvppeteer.exception.TimeoutException("WaitForResponse timeout of " + timeout + " ms exceeded");
            }
            if (targetCloseException.isDone() && Objects.nonNull(targetCloseException.get()) && Objects.isNull(result.get())) {
                throw targetCloseException.get();
            }
            return result.get();
        } finally {
            this.off(PageEvents.Response, responseListener);
            this.off(PageEvents.Close, closeListener);
        }
    }

    public void waitForNetworkIdle(WaitForNetworkIdleOptions options) {
        AwaitableResult<Boolean> closeResult = new AwaitableResult<>();
        Consumer<Object> closeConsumer = (ignore) -> {
            closeResult.complete(true);
        };
        try {
            Integer timeout = options.getTimeout();
            if (Objects.isNull(timeout)) {
                timeout = this._timeoutSettings.timeout();
            }
            this.on(PageEvents.Close, closeConsumer);
            if (timeout == 0) {
                while (true) {
                    if (closeResult.isDone() || inflight.size() <= options.getConcurrency()) {
                        return;
                    }
                }
            } else {
                Supplier<Boolean> conditionCheck = () -> {
                    if (closeResult.isDone() || inflight.size() <= options.getConcurrency()) {
                        return true;
                    }
                    Helper.justWait(options.getIdleTime());
                    return null;
                };
                Helper.waitForCondition(conditionCheck, timeout, "WaitForNetworkIdle timeout of " + timeout + " ms exceeded");
            }
        } finally {
            this.inflight.clear();
            this.off(PageEvents.Close, closeConsumer);
        }
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
        this.once(PageEvents.Close, (s) -> targetCloseException.set(new TargetCloseException("Page closed!")));
        Supplier<Frame> conditionChecker = () -> {
            if (targetCloseException.get() != null) {
                throw targetCloseException.get();
            }
            return Helper.filter(this.frames(), predicate);
        };
        return Helper.waitForCondition(conditionChecker, timeout, "WaitForFrame timeout of " + timeout + " ms exceeded");
    }

    /**
     * 此方法导航到历史记录中的上一页
     *
     * @return 如果存在多个重定向，导航将使用最后一个重定向的响应进行解析。如果无法返回，则解析为 null。
     */
    public Response goBack() throws JsonProcessingException {
        return this.goBack(new WaitForOptions());
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
    public abstract Response goBack(WaitForOptions options) throws JsonProcessingException;

    public Response goForward() throws JsonProcessingException {
        return this.goForward(new WaitForOptions());
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
    public abstract Response goForward(WaitForOptions options) throws JsonProcessingException;

    /**
     * 相当于多个tab时，切换到某个tab。
     */
    public abstract void bringToFront();

    /**
     * 模拟各种设备打开浏览器
     * <p>
     * 所有录制内容均为 WebM 格式，使用 VP9 视频编解码器。FPS 为 30。
     * <p>
     * 你的系统上必须安装有 ffmpeg。
     *
     * @param device 设备类型
     */
    public void emulate(Device device) throws ExecutionException, InterruptedException {
        this.setUserAgent(device.getUserAgent());
        this.setViewport(device.getViewport());
    }

    /**
     * 是否在页面上启用 JavaScript。
     *
     * @param enabled 是否启用
     */
    public abstract void setJavaScriptEnabled(boolean enabled);

    /**
     * 切换绕过页面的内容安全策略。<P>
     * 注意：CSP 绕过发生在 CSP 初始化时而不是评估时。通常，这意味着应在导航到域之前调用 page.setBypassCSP。
     * </P>
     *
     * @param enabled 是否绕过
     */
    public abstract void setBypassCSP(boolean enabled);

    /**
     * 改变页面的css媒体类型。支持的值仅包括 'screen', 'print' 和 null。传 null 禁用媒体模拟
     *
     * @param type css媒体类型
     */
    public abstract void emulateMediaType(MediaType type);


    /**
     * 启用 CPU 限制以模拟慢速 CPU。
     *
     * @param factor 减速系数（1 表示无油门，2 表示 2 倍减速，等等）。
     */
    public abstract void emulateCPUThrottling(double factor);

    /**
     * 模拟媒体特征
     *
     * @param features 给定一组媒体特性对象，在页面上模拟 CSS 媒体特性,每个媒体特性对象的name必须符合正则表达式 ：
     *                 ^(?:prefers-(?:color-scheme|reduced-motion)|color-gamut)$"
     */
    public abstract void emulateMediaFeatures(List<MediaFeature> features);


    /**
     * 更改页面的时区，传null将禁用将时区仿真
     * <a href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">时区id列表</a>
     *
     * @param timezoneId 时区id
     */
    public abstract void emulateTimezone(String timezoneId);

    /**
     * 模拟空闲状态。如果未设置参数，则清除空闲状态模拟
     *
     * @param overrides 模拟空闲状态。如果未设置，则清除空闲覆盖
     */
    public abstract void emulateIdleState(IdleOverridesState.Overrides overrides);

    /**
     * 模拟页面上给定的视力障碍,不同视力障碍，截图有不同效果
     *
     * @param type 视力障碍类型
     */
    public abstract void emulateVisionDeficiency(VisionDeficiency type);

    /**
     * 如果是一个浏览器多个页面的情况，每个页面都可以有单独的viewport
     * <p>注意 在大部分情况下，改变 viewport 会重新加载页面以设置 isMobile 或者 hasTouch</p>
     *
     * @param viewport 设置的视图
     */
    public abstract void setViewport(Viewport viewport) throws ExecutionException, InterruptedException;

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
    public abstract Viewport viewport();

    /**
     * 执行一段 JavaScript代码
     *
     * @param pptrFunction 要执行的字符串
     * @param args         如果pageFunction 是 Javascript 函数的话，args就是函数上的参数
     * @return pageFunction执行结果
     * @throws JsonProcessingException json序列化异常
     */
    public Object evaluate(String pptrFunction, Object... args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluate", pptrFunction);
        if (Objects.isNull(args)) {
            return this.mainFrame().evaluate(pptrFunction);
        } else {
            return this.mainFrame().evaluate(pptrFunction, Arrays.asList(args));
        }
    }

    /**
     * 在新dom产生之际执行给定的javascript
     * <p>当你的js代码为函数时，type={@link EvaluateType#FUNCTION}</p>
     * <p>当你的js代码为字符串时，type={@link EvaluateType#STRING}</p>
     *
     * @param pptrFunction js代码
     * @param args         当你js代码是函数时，js函数的参数
     */
    public NewDocumentScriptEvaluation evaluateOnNewDocument(String pptrFunction, Object... args) throws JsonProcessingException {
        return this.evaluateOnNewDocument(pptrFunction, null, args);
    }

    /**
     * 在新dom产生之际执行给定的javascript
     * <p>当你的js代码为函数时，type={@link EvaluateType#FUNCTION}</p>
     * <p>当你的js代码为字符串时，type={@link EvaluateType#STRING}</p>
     *
     * @param pptrFunction js代码
     * @param type         一般为PageEvaluateType#FUNCTION
     * @param args         当你js代码是函数时，js函数的参数
     * @return NewDocumentScriptEvaluation 执行脚本的标志符
     * @throws JsonProcessingException json异常
     */
    public abstract NewDocumentScriptEvaluation evaluateOnNewDocument(String pptrFunction, EvaluateType type, Object... args) throws JsonProcessingException;

    /**
     * 删除通过 Page.evaluateOnNewDocument 注入页面的脚本。
     *
     * @param identifier 脚本标识符
     */
    public abstract void removeScriptToEvaluateOnNewDocument(String identifier);

    /**
     * 根据启用状态切换忽略每个请求的缓存。默认情况下，缓存已启用。
     *
     * @param enabled 设置缓存的 enabled 状态
     */
    public abstract void setCacheEnabled(boolean enabled);

    AtomicLong screencastSessionCount = new AtomicLong(0);
    private volatile boolean startScreencasted = false;

    /**
     * 捕获此 page 的截屏视频。可录制为webm和gif，录制帧率默认是30
     *
     * @param options 配置截屏行为
     * @return ScreenRecorder 屏幕录制实例，用于停止录制
     * @throws IOException IO异常
     */
    public ScreenRecorder screencast(ScreencastOptions options) throws IOException, ExecutionException, InterruptedException {
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
                "                }", null);
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
        ScreenRecorder recorder = new ScreenRecorder(this, width, height, new ScreenRecorderOptions(options.getSpeed(), crop, options.getPath(), options.getFormat(), options.getScale(), options.getFfmpegPath(), options.getFps(), options.getLoop(), options.getDelay(), options.getQuality(), options.getColors()), defaultViewport, tempViewport);
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
                    this.mainFrame().client().on(ConnectionEvents.Page_screencastFrame, (Consumer<ScreencastFrameEvent>) event -> awaitableResult.complete());
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

    protected ScreenshotClip roundRectangle(ScreenshotClip clip) {
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
    protected ScreenshotClip normalizeRectangle(ScreenshotClip clip) {
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
        ScreenshotClip copy = clip.copy(x, y, width, height);
        copy.setScale(clip.getScale());
        return copy;
    }

    /**
     * <p>截图</p>
     * 备注 在OS X上 截图需要至少1/6秒。：<a href="https://crbug.com/741689">查看讨论</a>。
     *
     * @param options 截图选项
     * @return 图片base64的字节
     */
    public String screenshot(ScreenshotOptions options) throws ExecutionException, InterruptedException {
        synchronized (this.browserContext()) {//一个上下文只能有一个截图操作
            if (StringUtil.isNotEmpty(options.getPath())) {
                String filePath = options.getPath();
                String path = filePath.substring(0, filePath.lastIndexOf('.') + 1);
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
                            Object response = this.mainFrame().isolatedRealm().evaluate("() => {\n" + "              const element = document.documentElement;\n" + "              return {\n" + "                width: element.scrollWidth,\n" + "                height: element.scrollHeight,\n" + "              };\n" + "            }");
                            BoundingBox scrollDimensions = Constant.OBJECTMAPPER.convertValue(response, BoundingBox.class);
                            fullViewport = new Viewport((int) scrollDimensions.getWidth(), (int) scrollDimensions.getHeight(), this.viewport().getDeviceScaleFactor(), this.viewport().getIsMobile(), this.viewport().getHasTouch(), this.viewport().getIsLandscape());
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
                    this.setViewport(this.viewport());
                }
            }
            return "";
        }

    }

    /**
     * 屏幕截图
     *
     * @param path 截图文件全路径
     * @return base64编码后的图片数据
     */
    public String screenshot(String path) throws ExecutionException, InterruptedException {
        return this.screenshot(new ScreenshotOptions(path));
    }

    protected abstract String _screenshot(ScreenshotOptions options) throws IOException;

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
    public abstract byte[] pdf(PDFOptions options, LengthUnit lengthUnit) throws IOException;

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
     * 表示页面是否被关闭。
     *
     * @return 页面是否被关闭。
     */
    public abstract boolean isClosed();


    public abstract Mouse mouse();

    /**
     * 此方法找到一个匹配 selector 选择器的元素，如果需要会把此元素滚动到可视，然后通过 page.mouse 点击它。 如果选择器没有匹配任何元素，此方法将会报错。
     * 默认是阻塞的，会等待点击完成指令返回
     *
     * @param selector 选择器
     * @throws JsonProcessingException JSON异常
     */
    public void click(String selector) throws JsonProcessingException {
        this.click(selector, new ClickOptions());
    }


    public void click(String selector, ClickOptions options) throws JsonProcessingException {
        this.mainFrame().click(selector, options);
    }


    /**
     * 此方法找到一个匹配selector的元素，并且把焦点给它。 如果没有匹配的元素，此方法将报错。
     *
     * @param selector 要给焦点的元素的选择器selector。如果有多个匹配的元素，焦点给第一个元素。
     * @throws JsonProcessingException JSON异常
     */
    public void focus(String selector) throws JsonProcessingException {
        this.mainFrame().focus(selector);
    }

    /**
     * 此方法找到一个匹配的元素，如果需要会把此元素滚动到可视，然后通过 page.mouse 来hover到元素的中间。 如果没有匹配的元素，此方法将会报错。
     *
     * @param selector 要hover的元素的选择器。如果有多个匹配的元素，hover第一个。
     * @throws JsonProcessingException JSON异常
     */
    public void hover(String selector) throws JsonProcessingException {
        this.mainFrame().hover(selector);
    }

    /**
     * 当提供的选择器完成选中后，触发change和input事件 如果没有元素匹配指定选择器，将报错。
     *
     * @param selector 要查找的选择器
     * @param values   要选择的选项的值。如果 select 具有 multiple 属性，则考虑所有值，否则仅考虑第一个值。
     * @return 选择器集合
     * @throws JsonProcessingException JSON异常
     */
    public List<String> select(String selector, List<String> values) throws JsonProcessingException {
        return this.mainFrame().select(selector, values);
    }

    /**
     * 此方法找到一个匹配的元素，如果需要会把此元素滚动到视图中，然后通过 page.touchscreen 来点击元素的中间位置 如果没有匹配的元素，此方法会报错
     *
     * @param selector 要点击的元素的选择器。如果有多个匹配的元素，点击第一个
     * @throws JsonProcessingException JSON异常
     */
    public void tap(String selector) throws JsonProcessingException {
        this.mainFrame().tap(selector);
    }

    /**
     * 每个字符输入后都会触发 keydown, keypress/input 和 keyup 事件
     * <p>要点击特殊按键，比如 Control 或 ArrowDown，用 keyboard.press</p>
     *
     * @param selector 要输入内容的元素选择器。如果有多个匹配的元素，输入到第一个匹配的元素。
     * @param text     要输入的内容
     * @throws JsonProcessingException JSON异常
     */
    public void type(String selector, String text) throws JsonProcessingException {
        this.mainFrame().type(selector, text, 0);
    }

    /**
     * 每个字符输入后都会触发 keydown, keypress/input 和 keyup 事件
     * <p>要点击特殊按键，比如 Control 或 ArrowDown，用 keyboard.press</p>
     *
     * @param selector 要输入内容的元素选择器。如果有多个匹配的元素，输入到第一个匹配的元素。
     * @param text     要输入的内容
     * @param delay    每个字符输入的延迟，单位是毫秒。默认是 0。
     * @throws JsonProcessingException JSON异常
     */
    public void type(String selector, String text, long delay) throws JsonProcessingException {
        this.mainFrame().type(selector, text, delay);
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
     * 等待提供的函数 pptrFunction 在页面上下文中计算时返回真值。
     *
     * @param pptrFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @return JSHandle 指定的页面元素 对象
     */
    public JSHandle waitForFunction(String pptrFunction) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        return this.waitForFunction(pptrFunction, new WaitForSelectorOptions());
    }

    /**
     * 等待提供的函数 pptrFunction 在页面上下文中计算时返回真值。
     *
     * @param pptrFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @param args         传递给函数的参数，可以是任意类型
     * @return JSHandle 返回函数执行结果的JSHandle对象
     */
    public JSHandle waitForFunction(String pptrFunction, Object... args) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        return this.waitForFunction(pptrFunction, new WaitForSelectorOptions(), args);
    }

    /**
     * 等待提供的函数 pptrFunction 在页面上下文中计算时返回真值。
     *
     * @param pptrFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @param options      等待函数的选项，包括超时设置等
     * @param args         传递给函数的参数，可以是任意类型
     * @return JSHandle 返回函数执行结果的JSHandle对象
     */
    public JSHandle waitForFunction(String pptrFunction, WaitForSelectorOptions options, Object... args) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        return this.waitForFunction(pptrFunction, options, Helper.isFunction(pptrFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING, args);
    }

    /**
     * 等待提供的函数 pptrFunction 在页面上下文中计算时返回真值。
     *
     * @param pptrFunction 将在浏览器上下文中评估函数，直到返回真值。
     * @param options      等待函数的选项，包括超时设置等
     * @param args         传递给函数的参数，可以是任意类型
     * @param type         有时候需要指定 pptrFunction 为 EvaluateType#String 才能正确执行，大多数情况不需要指定
     * @return JSHandle 返回函数执行结果的JSHandle对象
     */
    public JSHandle waitForFunction(String pptrFunction, WaitForSelectorOptions options, EvaluateType type, Object... args) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        return this.mainFrame().waitForFunction(pptrFunction, options, type, args);
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
     * Resizes the browser window the page is in so that the content area
     * (excluding browser UI) is according to the specified width and height.
     * @param contentWidth 浏览器内容宽度
     * @param contentHeight 浏览器内容高度
     */
    public abstract void resize(int contentWidth, int contentHeight);

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
     * 获取蓝牙设备
     *
     * @return BluetoothEmulation
     */
    public abstract BluetoothEmulation bluetooth();

    /**
     * page.close() 在 beforeunload 处理之前默认不执行
     * <p><strong>注意 如果 runBeforeUnload 设置为true，可能会弹出一个 beforeunload 对话框。 这个对话框需要通过页面的 'dialog' 事件手动处理</strong></p>
     */
    public void close() {
        this.close(false);
    }

    public abstract void close(boolean runBeforeUnload);

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    public boolean isDragging() {
        return isDragging;
    }

    /**
     * Opens DevTools for the current Page and returns the DevTools Page. This
     * method is only available in Chrome.
     */
    public abstract Page openDevTools();

    private static final Map<String, Double> unitToPixels = new HashMap<String, Double>() {
        private static final long serialVersionUID = -4861220887908575532L;

        {
            put("px", 1.00);
            put("in", 96.00);
            put("cm", 37.8);
            put("mm", 3.78);
        }
    };

    protected Double convertPrintParameterToInches(String parameter, LengthUnit lengthUnit) {
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

}
