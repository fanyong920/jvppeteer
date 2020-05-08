package com.ruiyun.jvppeteer.transport.websocket;

import com.ruiyun.jvppeteer.transport.ConnectionTransport;
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

	private Consumer<String> messageConsumer = null;

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
		ValidateUtil.notNull(this.messageConsumer,"MessageConsumer must be initialized");
		messageConsumer.accept(message);
	}

	@Override
	public void onClose() {

	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		LOGGER.info("Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
		// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		this.onClose();
		System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
	}

	@Override
	public void onError(Exception e) {

	}



	@Override
	public void onOpen(ServerHandshake ServerHandshake) {

	}


	public void addMessageConsumer(Consumer<String> consumer) {
		this.messageConsumer = consumer;
	}



}
