package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.RemoteAddress;
import com.ruiyun.jvppeteer.cdp.entities.ResourceTiming;
import com.ruiyun.jvppeteer.cdp.entities.ResponseSecurityDetails;
import java.io.IOException;
import java.util.List;

public abstract class Response {
    public Response() {
    }

    /**
     * 用于连接到远程服务器的 IP 地址和端口号。
     *
     * @return 用于连接到远程服务器的 IP 地址和端口号。
     */
    public abstract RemoteAddress remoteAddress();

    /**
     * 响应的 URL。
     *
     * @return 响应的 URL。
     */
    public abstract String url();

    /**
     * 如果响应成功（状态范围为 200-299），则为 true。
     *
     * @return 如果响应成功（状态范围为 200-299），则为 true。
     */
    public boolean ok() {
        return this.status() == 0 || (this.status() >= 200 && this.status() <= 299);
    }

    /**
     * 响应的状态代码（例如，200 表示成功）。
     *
     * @return 响应的状态代码（例如，200 表示成功）。
     */
    public abstract int status();

    /**
     * 响应的状态文本（例如，通常为 "OK" 表示成功）。
     *
     * @return 响应的状态文本（例如，通常为 "OK" 表示成功）。
     */
    public abstract String statusText();

    /**
     * 具有与响应关联的 HTTP 标头的对象。所有标头名称均为小写。
     *
     * @return 具有与响应关联的 HTTP 标头的对象。
     */
    public abstract List<HeaderEntry> headers();

    /**
     * 如果通过安全连接收到响应，则为 SecurityDetails，否则为 null。
     *
     * @return 如果通过安全连接收到响应，则为 SecurityDetails，否则为 null。
     */
    public abstract ResponseSecurityDetails securityDetails();

    /**
     * 与响应相关的时间信息。
     *
     * @return 与响应相关的时间信息。
     */
    public abstract ResourceTiming timing();

    /**
     * 响应正文的字节数组。
     *
     * @return 响应正文的字节数组。
     */
    public abstract byte[] content();

    /**
     * 响应正文的字符串
     * @return 响应正文的字符串
     */
    public String text() {
        return new String(content());
    }

    /**
     * 响应正文的 JSON 表示形式。
     *
     * @return 响应正文的 JSON 表示形式。
     * @throws IOException IO异常
     */
    public JsonNode json() throws IOException {
        return Constant.OBJECTMAPPER.readTree(this.content());
    }

    /**
     * 匹配的 Request 对象。
     *
     * @return 匹配的 Request 对象。
     */
    public abstract Request request();

    /**
     * 如果响应是从浏览器的磁盘缓存或内存缓存提供的，则为 true。
     *
     * @return 如果响应是从浏览器的磁盘缓存或内存缓存提供的，则为 true。
     */
    public abstract boolean fromCache();

    /**
     * 如果响应是由服务工作进程提供的，则为 true。
     *
     * @return 如果响应是由服务工作进程提供的，则为 true。
     */
    public abstract boolean fromServiceWorker();

    /**
     * 启动此响应的 框架，如果导航到错误页面，则为 null
     *
     * @return 启动此响应的 框架
     */
    public abstract Frame frame();
}
