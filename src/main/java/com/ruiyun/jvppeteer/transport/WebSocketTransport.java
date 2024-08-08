package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.transport.factory.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 * websocket client 
 * @author fff
 *
 */

public class WebSocketTransport extends WebSocketClient implements ConnectionTransport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTransport.class);

	private Connection connection = null;

	public WebSocketTransport(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public WebSocketTransport(URI serverURI) {
		super( serverURI );
	}

	public WebSocketTransport( URI serverUri, Map<String, String> httpHeaders) {
		super(serverUri, httpHeaders);
	}

	public static WebSocketTransport create(String browserWSEndpoint) throws InterruptedException {
		return WebSocketTransportFactory.create(browserWSEndpoint);
	}

	@Override
	public void onMessage(String message) {
		ValidateUtil.notNull(this.connection,"MessageConsumer must be initialized");
		this.connection.accept(message);
	}
	/**
	 *
	 * @param code  NORMAL = 1000;
	 *     GOING_AWAY = 1001;
	 *     PROTOCOL_ERROR = 1002;
	 *     REFUSE = 1003;
	 *     NOCODE = 1005;
	 *     ABNORMAL_CLOSE = 1006;
	 *     NO_UTF8 = 1007;
	 *     POLICY_VALIDATION = 1008;
	 *     TOOBIG = 1009;
	 *     EXTENSION = 1010;
	 *     UNEXPECTED_CONDITION = 1011;
	 *     SERVICE_RESTART = 1012;
	 *     TRY_AGAIN_LATER = 1013;
	 *     BAD_GATEWAY = 1014;
	 *     TLS_ERROR = 1015;
	 *     NEVER_CONNECTED = -1;
	 *     BUGGYCLOSE = -2;
	 *     FLASHPOLICY = -3;
	 * @param reason 原因
	 * @param remote 远程
	 */
	@Override
	public void onClose( int code, String reason, boolean remote) {//这里是WebSocketClient的实现方法,当websocket closed的时候会调用onClose
        LOGGER.info("Connection closed by {} Code: {} Reason: {}", remote ? "remote peer" : "us", code, reason);
		if(this.connection != null){//浏览器以外关闭时候，connection不为空
			this.connection.dispose();
		}
	}

	@Override
	public void onError(Exception e) {
		LOGGER.error("Websocket error:",e);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
        LOGGER.info("Websocket serverHandshake status: {}", serverHandshake.getHttpStatus());
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}
