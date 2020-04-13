package com.ruiyun.jvppeteer.options;

import java.util.List;

/**
 * 页面${@link com.ruiyun.jvppeteer.protocol.page.Page#go2}用到
 * 跳转到具体页面时可选择的参数
 */
public class PageOptions {

    /**
     * Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     */
    private String referer;

    /**
     * 跳转等待时间，单位是毫秒, 默认是30秒, 传 0 表示无限等待。可以通过
     */
    private int timeout;

    /**
     *  满足什么条件认为页面跳转完成，默认是 load 事件触发时。指定事件数组，那么所有事件触发后才认为是跳转完成。事件包括：
     * load - 页面的load事件触发时
     * domcontentloaded - 页面的 DOMContentLoaded 事件触发时
     * networkidle0 - 不再有网络连接时触发（至少500毫秒后）
     * networkidle2 - 只有2个网络连接时触发（至少500毫秒后）
     */
    private List<String> waitUntil;
}
