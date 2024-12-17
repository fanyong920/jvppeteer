package com.ruiyun.jvppeteer.common;

public enum PuppeteerLifeCycle {
    /**
     * 当前页面不再有网络连接时触发（至少500毫秒后）
     */
    networkIdle("networkIdle"),
    /**
     * 网页所有资源载入后触发，浏览器上加载转环停止旋转
     */
    load("load"),
    /**
     * 当 HTML 文档已完全解析，并且所有延迟脚本（script defer src=“...” 和 script type=“module”）都已下载并执行时，将触发 DOMContentLoaded 事件。<p>
     * 它不会等待其他内容（如图像、子帧和异步脚本）完成加载。
     */
    domcontentloaded("DOMContentLoaded"),
    /**
     * 当前网络连接数少于2后触发
     */
    networkIdle2("networkAlmostIdle");
    private final String value;

    PuppeteerLifeCycle(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

