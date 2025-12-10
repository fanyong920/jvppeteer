package com.ruiyun.jvppeteer.api.events;

import com.ruiyun.jvppeteer.cdp.core.CdpDialog;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.PageMetrics;

public enum PageEvents {
    /**
     * 页面关闭时候发射该事件
     */
    Close,
    /**
     * 当页面中的 JavaScript 调用控制台 API 方法之一时发出，
     * 例如 'console.log' 或 'console.dir'。如果页面抛出
     * 错误或警告,也会触发该事件
     * <p>
     * {@link  ConsoleMessage} 代表一个console事件的相关信息
     */
    Console,
    /**
     * 当 JavaScript 对话框出现时触发，例如 alert、prompt、confirm 或 beforeunload。Jvppeteer 可以通过 {@link CdpDialog#accept} or {@link CdpDialog#dismiss}. 响应对话。
     * <p>
     * {@link  CdpDialog} 代表一个dialog事件的相关信息
     */
    Dialog,
    /**
     * 当页面加载到加载到DOMContentLoaded时触发<p>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/Events/DOMContentLoaded">点击查看DOMContentLoaded介绍</a>
     */
    Domcontentloaded,
    /**
     * 当页面崩溃时触发，返回一个 {@link Error}
     */
    Error,
    /**
     * 当一个Frame被添加时触发，返回一个 {@link com.ruiyun.jvppeteer.api.core.Frame}
     */
    FrameAttached,
    /**
     * 当一个Frame被移除时触发，返回一个 {@link com.ruiyun.jvppeteer.api.core.Frame}
     */
    FrameDetached,
    /**
     * 当一个Frame 被导航到新的URL时触发，返回一个 {@link com.ruiyun.jvppeteer.api.core.Frame}.
     */
    FrameNavigated,
    /**
     * load 事件在加载整个页面时触发，包括所有依赖资源，例如样式表、脚本、iframe 和图像，但延迟加载的资源除外。 这与 DOMContentLoaded 相反，后者在页面 DOM 加载后立即触发，而无需等待资源完成加载。
     * <p>
     * 此事件不可取消，也不会冒泡。
     * <a href="https://developer.mozilla.org/en-US/docs/Web/Events/load">点击查看load</a>
     */
    Load,
    /**
     * 当 JavaScript 代码调用 `console.timeStamp` 时触发此事件。
     * metrics列表 见 {@link com.ruiyun.jvppeteer.api.core.Page#metrics}.
     * <p>
     * {@link  PageMetrics} 代表一个dialog事件的相关信息
     * <p>
     */
    Metrics,
    /**
     * 当页面内发生未捕获的异常时触发。包含 {@link PageEvents#Error}与未知类型的数据。
     */
    PageError,
    /**
     * 当页面打开新选项卡或窗口时触发。<p>
     * 包含与弹出窗口对应的页。<p>
     * {@link  com.ruiyun.jvppeteer.api.core.Page} 代表一个popup事件的相关信息
     */
    Popup,
    /**
     * 当页面发送请求并包含 HTTPRequest 时触发。
     * <p>
     * 该对象是只读的。请参阅 Page.setRequestInterception() 了解拦截和修改请求。
     */
    Request,
    /**
     * 当请求最终从缓存加载时触发。包含 HTTPRequest。
     * <p>
     * 对于某些请求，可能包含未定义。<a href="https://crbug.com/750469">具体见这里</a>
     */
    RequestServedFromCache,
    /**
     * 当请求失败时触发，例如超时。
     * {@link com.ruiyun.jvppeteer.api.core.Request}.代表一个requestfailed事件的相关信息
     * <p>
     * 包含 Request。
     * <p>
     * 从 HTTP 角度来看，HTTP 错误响应（例如 404 或 503）仍然是成功响应，因此请求将通过 requestfinished 事件完成，而不是通过 requestfailed 事件完成。
     */
    RequestFailed,
    /**
     * 当请求成功完成时触发。包含 Request。
     * <p>
     * {@link com.ruiyun.jvppeteer.api.core.Request}.代表一个requestfinished事件的相关信息
     */
    RequestFinished,
    /**
     * 收到响应时触发。包含 HTTPResponse。
     * {@link com.ruiyun.jvppeteer.api.core.Response}.代表一个response事件的相关信息
     */
    Response,
    /**
     * 当页面生成专用 WebWorker 时触发。
     * <p>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">了解WebWorker</a>
     */
    WorkerCreated,
    /**
     * 当页面生成专用 WebWorker 时触发。
     * <p>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">了解WebWorker</a>
     */
    WorkerDestroyed

}
