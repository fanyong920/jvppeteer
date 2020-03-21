package com.ruiyun.jvppeteer.transport;

import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * websocket client 
 * @author fff
 *
 */
@ClientEndpoint
public class WebSocketTransport implements ConnectionTransport,Consumer<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTransport.class);
	
	private Session session;
	
	public WebSocketTransport() {

	}
	
	@Override
	public void send(String message) {
		session.getAsyncRemote().sendText(message);
	}

	@Override
	public boolean close() {
		return session == null || !session.isOpen();
	}

	@Override
	public void onMessage(String message) {
		
	}
	
	@OnClose
	@Override
	public void onClose() {
		LOGGER.info("websocket url" + session.getRequestURI() + "is close");
	}
	
	@OnOpen
	@Override
	public void onOpen(Session session) {
		System.out.println("has connected to browser websocket sever:" + session.getRequestURI());
		this.session = session;
//		System.err.println(session.getMaxTextMessageBufferSize());
//		System.err.println(session.getMaxIdleTimeout());
//		System.err.println(session.getContainer().getDefaultMaxSessionIdleTimeout());
//		System.err.println(session.getContainer().getDefaultMaxTextMessageBufferSize());
		WS_HASH_MAP.put(session.getId(), this);
		
		LOGGER.info("has connected to browser websocket sever:" + session.getRequestURI());
		
	}

	@Override
	public void onError(Session session, Throwable error) {
		LOGGER.error("websocket url" + session.getRequestURI() + "is onError");
	}

	@Override
	public void accept(String t) {
		onMessage(t);
	}

	public void addMessageHandler(Consumer<String> consumer) {
		if (session == null) {
			throw new RuntimeException("You first must connect to ws server in order to receive messages.");
		}

		if (!session.getMessageHandlers().isEmpty()) {
			throw new RuntimeException("You are already subscribed to this web socket service.");
		}

		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				LOGGER.debug("Received message {} on {}", message, session.getRequestURI());
				consumer.accept(message);
			}
		});
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	
}
