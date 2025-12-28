package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.CLOSE_REASON;

/**
 * websocket client
 *
 * @author fff
 */

public class WebSocketTransport extends WebSocketClient implements ConnectionTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTransport.class);
    private Connection connection = null;

    public WebSocketTransport(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int timeout) {
        super(serverUri, protocolDraft, httpHeaders, timeout);
    }

    public static ConnectionTransport create(String browserWSEndpoint) throws Exception {
        return WebSocketTransportFactory.create(browserWSEndpoint);
    }

    @Override
    public void onMessage(String message) {
        Objects.requireNonNull(this.connection, "Connection may be closed!");
        this.connection.onMessage(message);
    }


    /**
     * 当websocket连接关闭时，调用此方法
     * <p>
     * 状态码解释如下
     * <blockquote><pre>
     *     1000 = 正常关闭
     *     1001 = 终端离开, 可能因为服务端错误, 也可能因为浏览器正从打开连接的页面跳转离开
     *     1002 = 由于协议错误而中断连接
     *     1003 = 由于接收到不允许的数据类型而断开连接 (如仅接收文本数据的终端接收到了二进制数据)
     *     1005 = 表示没有收到预期的状态码
     *     1006 = 用于期望收到状态码时连接非正常关闭 (也就是说, 没有发送关闭帧)
     *     1007 = 由于收到了格式不符的数据而断开连接 (如文本消息中包含了非 UTF-8 数据)
     *     1008 = 由于收到不符合约定的数据而断开连接。 这是一个通用状态码, 用于不适合使用 1003 和 1009 状态码的场景
     *     1009 = 由于收到过大的数据帧而断开连接
     *     1010 = 客户端期望服务器商定一个或多个拓展, 但服务器没有处理, 因此客户端断开连接
     *     1011 = 客户端由于遇到没有预料的情况阻止其完成请求, 因此服务端断开连接。
     *     1012 = 服务器由于重启而断开连接。 这是一个通用状态码, 用于不适合使用 1001 状态码的场景
     *     1013 = 服务器由于临时原因断开连接, 如服务器过载因此断开一部分客户端连接
     *     1015 = 表示连接由于无法完成 TLS 握手而关闭 (例如无法验证服务器证书)
     *     -1 = 表示连接尚未打开
     *     -2 = 表示连接由于内部错误而关闭
     *     -3 = 表示连接由于无法完成 flash 策略检查而关闭
     * </pre></blockquote>
     *
     * @param reason 连接关闭的原因
     * @param remote 远程
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {//这里是WebSocketClient的实现方法,当websocket closed的时候会调用onClose
        LOGGER.info("Websocket connection closed by {} Code: {} Reason: {}", remote ? "remote peer" : "us", code, StringUtil.isEmpty(reason) ? CLOSE_REASON.get(code) : reason);
        //浏览器意外关闭时候，connection不为空
        Optional.ofNullable(this.connection).map(Connection::closeRunner).ifPresent(Runnable::run);
    }

    @Override
    public void onError(Exception e) {
        LOGGER.error("Websocket error:", e);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Websocket serverHandshake status: {}", serverHandshake.getHttpStatusMessage());
        }
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Websocket connection receive pong response: {}", f);
        }
    }
}
