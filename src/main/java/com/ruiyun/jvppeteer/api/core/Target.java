package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.cdp.entities.TargetType;

public interface Target {

    /**
     * 如果目标类型不是“page”，"webview","background_page",那么返回null
     *
     * @return Page
     */
    default Page page() {
        return null;
    }

    /**
     * If the target is not of type `"service_worker"` or `"shared_worker"`, returns `null`.
     *
     * @return WebWorker
     */
    default WebWorker worker() {
        return null;
    }

    /**
     * 强制为任何类型的目标创建页面。如果你想将 other 类型的 CDP 目标作为页面处理，那么它会很有用。<p>
     * 如果你处理常规页面目标,请使用 {@link Target#page()}。
     *
     * @return Page
     */
    Page asPage();

    String url();

    /**
     * 创建附加到目标的 Chrome Devtools 协议会话。
     *
     * @return 会话
     */
    CDPSession createCDPSession();

    /**
     * 确定这是什么类型的目标。<p>
     * 注意：背景页是谷歌插件里的页面
     *
     * @return 目标类型
     */
    TargetType type();

    /**
     * 获取目标所属的浏览器。
     *
     * @return Browser
     */
    Browser browser();

    /**
     * 获取目标所属的浏览器上下文。
     *
     * @return BrowserContext
     */
    BrowserContext browserContext();

    /**
     * Get the target that opened this target. Top-level targets return `null`.
     * @return target
     */
    Target opener();
}
