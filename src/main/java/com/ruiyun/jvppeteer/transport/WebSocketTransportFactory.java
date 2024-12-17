package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.exception.LaunchException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.java_websocket.drafts.Draft_6455;

public class WebSocketTransportFactory implements Constant {

    /**
     * create websocket client
     *
     * @param url 连接websocket的地址
     * @return WebSocketTransport websocket客户端
     * @throws InterruptedException 被打断异常
     */
    public static ConnectionTransport create(String url) throws Exception {
        return create(url, null, Constant.DEFAULT_TIMEOUT);
    }

    /**
     * create websocket client
     *
     * @param url         连接websocket的地址
     * @param httpHeaders 请求头
     * @return WebSocketTransport websocket客户端
     * @throws InterruptedException 被打断异常
     */
    public static ConnectionTransport create(String url, Map<String, String> httpHeaders, int timeout) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Puppeteer 2.2.3");
        if (Objects.nonNull(httpHeaders)) {
            headers.putAll(httpHeaders);
        }
        // 默认是60s的心跳机制
        WebSocketTransport client = new WebSocketTransport(URI.create(url), new Draft_6455(), headers, timeout);
        //30s 连接的超时时间
        boolean connected = client.connectBlocking(timeout, TimeUnit.MILLISECONDS);
        if (!connected) {
            throw new LaunchException("Websocket connection was not successful, please check if the URL(" + url + ") is effective.");
        }
        return client;
    }

}
