package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.events.BrowserEvents;
import com.ruiyun.jvppeteer.cdp.entities.BrowserContextOptions;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieData;
import com.ruiyun.jvppeteer.cdp.entities.DebugInfo;
import com.ruiyun.jvppeteer.cdp.entities.DownloadOptions;
import com.ruiyun.jvppeteer.common.Constant;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.Helper.filter;
import static com.ruiyun.jvppeteer.util.Helper.waitForCondition;

public abstract class Browser extends EventEmitter<BrowserEvents> implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
    /**
     * 主动调用 browser.close时候为true
     * 当 connection 断开，浏览器进程未关闭时候，杀死浏览器进程
     */
    public volatile boolean autoClose;

    /**
     * 获取关联的 Process。
     *
     * @return 浏览器进程对象
     */
    public abstract Process process();

    /**
     * 创建一个新的 浏览器上下文。
     * <p>
     * 这不会与其他 浏览器上下文 共享 cookie/缓存
     *
     * @return 返回创建的浏览器上下文对象
     */
    public BrowserContext createBrowserContext() {
        return this.createBrowserContext(new BrowserContextOptions());
    }

    /**
     * 创建一个新的 浏览器上下文。
     * <p>
     * 这不会与其他 浏览器上下文 共享 cookie/缓存
     *
     * @param options 浏览器上下文选项，包含代理服务器设置等
     * @return 返回创建的浏览器上下文对象
     */
    public abstract BrowserContext createBrowserContext(BrowserContextOptions options);

    /**
     * 获取打开的 浏览器上下文 列表。
     * <p>
     * 在新创建的 browser 中，这将返回 BrowserContext 的单个实例。
     *
     * @return 打开的 浏览器上下文 列表
     */
    public abstract List<BrowserContext> browserContexts();

    /**
     * 获取默认 浏览器上下文。
     * <p>
     * 默认 浏览器上下文 无法关闭。
     *
     * @return 默认 浏览器上下文
     */
    public abstract BrowserContext defaultBrowserContext();

    /**
     * 获取用于连接到此 browser 的 WebSocket URL。
     * <p>
     * 这通常与 Puppeteer.connect() 一起使用。
     * <p>
     * 你可以从 http://HOST:PORT/json/version 找到调试器 URL (webSocketDebuggerUrl)。
     * <p>
     * 请参阅 <a href="https://chromedevtools.github.io/devtools-protocol/#how-do-i-access-the-browser-target">浏览器端点</a> 了解更多信息。
     *
     * @return WebSocket URL
     */
    public abstract String wsEndpoint();

    /**
     * 在 默认浏览器上下文 中创建新的 page。
     *
     * @return 新创建的页面对象
     */
    public abstract Page newPage();

    /**
     * 获取所有活动的 targets。
     * <p>
     * 如果有多个 浏览器上下文，则返回所有 浏览器上下文 中的所有 targets。
     *
     * @return 所有活动的 targets
     */
    public abstract <T extends Target> List<T> targets();

    /**
     * 获取与 默认浏览器上下文 关联的 target。
     *
     * @return 默认浏览器上下文 关联的 target
     */
    public abstract Target target();

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它.
     * 此方法用于在一定超时时间内，持续检查是否出现符合特定条件的目标对象.
     *
     * @param predicate 用于筛选目标对象的条件，符合条件的目标将被返回.
     * @param timeout   等待的最大时间（以毫秒为单位），超过此时间将抛出异常.
     * @return 返回符合 predicate 条件的目标对象.
     */
    public Target waitForTarget(Predicate<Target> predicate, int timeout) {
        Supplier<Target> conditionChecker = () -> filter(this.targets(), predicate);
        return waitForCondition(conditionChecker, timeout, "Waiting for target failed: timeout " + timeout + "ms exceeded");
    }

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它.
     * <p>
     * 默认等待时间是30s
     *
     * @param predicate 用于筛选目标对象的条件，符合条件的目标将被返回.
     * @return 返回符合 predicate 条件的目标对象.
     */
    public Target waitForTarget(Predicate<Target> predicate) {
        return this.waitForTarget(predicate, Constant.DEFAULT_TIMEOUT);
    }

    /**
     * 获取此 Browser 内所有打开的 pages 的列表。
     * <p>
     * 如果有多个 浏览器上下文，则返回所有 浏览器上下文 中的所有 pages。
     *
     * @return 所有打开的 pages
     */
    public List<Page> pages() {
        return this.browserContexts().stream().flatMap(context -> context.pages().stream()).collect(Collectors.toList());
    }

    /**
     * 获取表示此 浏览器的 名称和版本的字符串。
     * <p>
     * 对于无头浏览器，这与 "HeadlessChrome/61.0.3153.0" 类似。对于非无头或新无头，这与 "Chrome/61.0.3153.0" 类似。
     * <p>
     * 对于火狐浏览器，这与 "Firefox/116.0a1"类似
     * <p>
     * Browser.version() 的格式可能会随着浏览器的未来版本而改变。
     *
     * @return 浏览器版本
     * @throws JsonProcessingException 序列化错误
     */
    public abstract String version() throws JsonProcessingException;

    /**
     * 获取此 浏览器的 原始用户代理。
     * <p>
     * Pages 可以使用 Page.setUserAgent() 覆盖用户代理。e
     *
     * @return 原始用户代理
     */
    public abstract String userAgent();

    /**
     * 断开 Jvppeteer 与该 browser 的连接，但保持进程运行。
     */
    public abstract void disconnect();

    /**
     * 获取当前默认浏览器上下文中的所有Cookie
     * <p>
     * 此方法用于收集默认浏览器上下文中所有的Cookie信息，以便于后续处理或分析
     * 它调用了defaultBrowserContext()方法获取默认浏览器上下文，然后调用该上下文的cookies()方法来获取Cookie列表
     *
     * @return List<Cookie> 返回一个Cookie对象列表，包含了当前默认浏览器上下文中的所有Cookie
     */
    public List<Cookie> cookies() {
        return this.defaultBrowserContext().cookies();
    }

    /**
     * 设置Cookie信息
     * 此方法允许将一个或多个CookieData对象设置到默认浏览器上下文中
     * 利用可变参数的功能，允许调用者以灵活的方式传递任意数量的CookieData对象
     *
     * @param cookies 一个或多个CookieData对象，代表要设置的Cookie信息
     */
    public void setCookie(CookieData... cookies) {
        this.defaultBrowserContext().setCookie(cookies);
    }

    /**
     * Jvppeteer 是否连接到此 browser。
     *
     * @return 连接返回true, 不连接返回false
     */
    public abstract boolean connected();

    public void disposeSymbol() {
        try {
            if (Objects.nonNull(this.process())) {
                this.close();
            } else {
                this.disconnect();
            }
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        }
    }

    /**
     * 从 Jvppeteer 获取 debug 信息
     * <p>
     * 目前，信息包括待处理的协议调用。将来，我们可能会添加更多信息。
     *
     * @return debug 信息
     */
    public abstract DebugInfo debugInfo();

    public abstract void setDownloadBehavior(DownloadOptions downloadOptions);

    public abstract void cancelDownload(String key, String id);

}
