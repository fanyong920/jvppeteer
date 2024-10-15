package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.common.Constant.WEB_PERMISSION_TO_PROTOCOL_PERMISSION;
import static com.ruiyun.jvppeteer.util.Helper.filter;
import static com.ruiyun.jvppeteer.util.Helper.waitForCondition;

/**
 * BrowserContext 代表 browser 中的各个用户上下文。
 * <p>
 * 启动 browser 时，它至少有一个默认 浏览器上下文。其他可以使用 Browser.createBrowserContext() 创建。每个上下文都有独立的存储（cookies/localStorage/等）
 * <p>
 * BrowserContext emits 各种事件记录在 BrowserContextEvent 枚举中。
 * <p>
 * 如果 page 打开另一个 page，例如 使用 window.open，弹出窗口将属于父 页面的浏览器上下文。
 * <p>
 * 在 Chrome 中，所有非默认上下文都是隐身的，如果在启动浏览器时提供 --incognito 参数，默认浏览器上下文 可能会隐身。
 * <p>
 * 此类的构造函数被标记为内部构造函数。第三方代码不应直接调用构造函数或创建扩展 BrowserContext 类的子类。
 */
public class BrowserContext extends EventEmitter<BrowserContext.BrowserContextEvent> {
    /**
     * 浏览器对应的websocket client包装类，用于发送和接受消息
     */
    private Connection connection;
    /**
     * 浏览器上下文对应的浏览器，一个上下文只有一个浏览器，但是一个浏览器可能有多个上下文
     */
    private Browser browser;
    /**
     * 浏览器上下文id
     */
    private String id;

    public BrowserContext() {
        super();
    }

    public BrowserContext(Connection connection, Browser browser, String contextId) {
        super();
        this.connection = connection;
        this.browser = browser;
        this.id = contextId;
    }

    /**
     * 获取此 浏览器上下文 内所有活动的 targets。
     * <p>
     * 此方法通过过滤当前浏览器的所有 targets，只返回属于当前浏览器上下文（browserContext）的 targets。
     * 这对于当您想要对特定浏览器上下文中的所有页面或框架进行操作时非常有用。
     *
     * @return 返回一个 List，包含当前浏览器上下文中所有活动 targets 的列表。
     */
    public List<Target> targets() {
        return this.browser.targets().stream().filter(target -> target.browserContext() == this).collect(Collectors.toList());
    }

    /**
     * 获取此 浏览器上下文 内所有打开的 pages 的列表。
     * <p>
     * 不可见的 pages，例如 "background_page"，这里不会列出。你可以使用 Target.page() 找到它们。
     *
     * @return 所有打开的 pages
     */
    public List<Page> pages() {
        return this.targets().stream().filter(target -> TargetType.PAGE.equals(target.type()) || (TargetType.OTHER.equals(target.type()) && this.browser.getIsPageTargetCallback() != null ? this.browser.getIsPageTargetCallback().apply(target) : true)).map(Target::page).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 重写指定页面的权限设置
     *
     * @param origin         权限来源，通常是一个URL
     * @param webPermissions 权限列表，表示要重写的权限
     */
    public void overridePermissions(String origin, WebPermission... webPermissions) {
        List<String> protocolPermissions = new ArrayList<>();
        if (webPermissions != null) {
            for (WebPermission permission : webPermissions) {
                String protocolPermission = WEB_PERMISSION_TO_PROTOCOL_PERMISSION.get(permission);
                ValidateUtil.assertArg(protocolPermission != null, "Unknown permission: " + permission);
                protocolPermissions.add(protocolPermission);
            }
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("origin", origin);
        if (StringUtil.isNotEmpty(this.id)) {
            params.put("browserContextId", this.id);
        }
        params.put("permissions", protocolPermissions);
        this.connection.send("Browser.grantPermissions", params);
    }

    /**
     * 在给定的 origin 内授予此 浏览器上下文 给定的 permissions。
     */
    public void clearPermissionOverrides() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("browserContextId", this.id);
        this.connection.send("Browser.resetPermissions", params);
    }

    /**
     * 在此 浏览器上下文 中创建一个新的 page。
     *
     * @return 新创建的 Page 实例
     */
    public Page newPage() {
        synchronized (this) {
            return this.browser.createPageInContext(this.id);
        }
    }

    /**
     * 关闭此 浏览器上下文 和所有关联的 pages。
     */
    public void close() {
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.id), "Default BrowserContext cannot be closed!");
        this.browser.disposeContext(this.id);
    }

    public boolean closed() {
        return !this.browser.browserContexts().contains(this);
    }

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它。
     *
     * @param predicate 一个断言，用于测试每个target是否为匹配项
     * @return 返回与predicate匹配的target
     */
    public Target waitForTarget(Predicate<Target> predicate) {
        return this.waitForTarget(predicate, Constant.DEFAULT_TIMEOUT);
    }

    /**
     * 等待直到出现与给定 predicate 匹配的 target 并返回它。
     *
     * @param predicate 一个断言，用于测试每个target是否为匹配项
     * @param timeout   等待超时时间
     * @return 返回与predicate匹配的target
     */
    public Target waitForTarget(Predicate<Target> predicate, int timeout) {
        Supplier<Target> conditionChecker = () -> filter(this.targets(), predicate);
        return waitForCondition(conditionChecker, timeout, "waiting for target failed: timeout " + timeout + "ms exceeded");
    }

    /**
     * 获取与此 浏览器上下文 关联的 browser。
     *
     * @return 返回与此浏览器上下文关联的 Browser 对象。
     */
    public Browser browser() {
        return browser;
    }

    /**
     * 获取当前对象的ID
     *
     * @return 当前对象的ID
     */
    public String getId() {
        return this.id;
    }

    public enum BrowserContextEvent {
        TargetChanged("targetchanged"),
        TargetCreated("targetcreated"),
        TargetDestroyed("targetdestroyed");
        private final String eventName;

        BrowserContextEvent(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }
    }

}
