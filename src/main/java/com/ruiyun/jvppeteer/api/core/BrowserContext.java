package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.api.events.BrowserContextEvents;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieData;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.CreatePageOptions;
import com.ruiyun.jvppeteer.common.WebPermission;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;


import static com.ruiyun.jvppeteer.util.Helper.filter;
import static com.ruiyun.jvppeteer.util.Helper.waitForCondition;

public abstract class BrowserContext extends EventEmitter<BrowserContextEvents> {
    /**
     * 获取此 浏览器上下文 内所有活动的 targets。
     * <p>
     * 此方法通过过滤当前浏览器的所有 targets，只返回属于当前浏览器上下文（browserContext）的 targets。
     * 这对于当您想要对特定浏览器上下文中的所有页面或框架进行操作时非常有用。
     *
     * @return 返回一个 List，包含当前浏览器上下文中所有活动 targets 的列表。
     */
    public abstract List<Target> targets();

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
     * 获取此 浏览器上下文 内所有打开的 pages 的列表。
     * <p>
     * 不可见的 pages，例如 "background_page"，这里不会列出。你可以使用 Target.page() 找到它们。
     *
     * @return 所有打开的 pages
     */
    public List<Page> pages() {
        return this.pages(false);
    }

    /**
     * 获取此 浏览器上下文 内所有打开的 pages 的列表。
     * <p>
     * 不可见的 pages，例如 "background_page"，这里不会列出。你可以使用 Target.page() 找到它们。
     * @param includeAll 是否包含所有类型的页面
     * @return 所有打开的 pages
     */
    public abstract List<Page> pages(boolean includeAll);

    /**
     * 授予指定页面的权限设置
     *
     * @param origin         权限来源，通常是一个URL
     * @param webPermissions 权限列表，表示要授予的权限，没有授予的权限默认是拒绝
     */
    public abstract void overridePermissions(String origin, WebPermission... webPermissions);

    /**
     * 在给定的 origin 内授予此 浏览器上下文 给定的 permissions。
     */
    public abstract void clearPermissionOverrides();

    /**
     * 在此 浏览器上下文 中创建一个新的 page。
     *
     * @return 新创建的 Page 实例
     */
    public Page newPage() {
        return this.newPage(null);
    }

    /**
     * 在此 浏览器上下文 中创建一个新的 page。
     *
     * @param options 创建page的参数
     * @return 新创建的 Page 实例
     */
    public abstract Page newPage(CreatePageOptions options);

    /**
     * 获取与此 浏览器上下文 关联的 browser。
     *
     * @return 返回与此浏览器上下文关联的 Browser 对象。
     */
    public abstract Browser browser();

    /**
     * 关闭此 浏览器上下文 和所有关联的 pages。
     */
    public abstract void close();

    /**
     * 获取当前浏览器上下文的所有cookie
     *
     * @return 该浏览器上下文的所有cookie
     */
    public abstract List<Cookie> cookies();


    /**
     * 在当前浏览器上下文的设置cookie
     */
    public abstract void setCookie(CookieData... cookies);

    /**
     * 在当前浏览器上下文删除指定cookie
     *
     * @param cookies 指定删除的cookie
     */
    public void deleteCookie(Cookie... cookies) {
        if (Objects.isNull(cookies)) {
            return;
        }
        for (Cookie cookie : cookies) {
            cookie.setExpires(1);
            this.setCookie(Constant.OBJECTMAPPER.convertValue(cookie, CookieData.class));
        }

    }

    /**
     * 该浏览器上下文是否关闭
     *
     * @return 是否关闭
     */
    public boolean closed() {
        return !this.browser().browserContexts().contains(this);
    }

    /**
     * 获取当前对象的ID
     *
     * @return 当前对象的ID
     */
    public abstract String id();
}
