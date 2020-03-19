package com.ruiyun.jvppeteer.websocket;

import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruiyun.jvppeteer.Constant;


public class WebSocketClient implements Constant  {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

	private Session session;

	public void onMessage(String message ) {
		// TODO Auto-generated method stub

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		// TODO Auto-generated method stub
		LOGGER.info("websocket url" + session.getRequestURI() + "is close");
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
//		WS_HASH_MAP.put(session.getId(), this);
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				LOGGER.debug("Received message {} on {}", message, session.getRequestURI());
				this.onMessage(message);
			}
		});
		LOGGER.info("websocket url" + session.getRequestURI() + "is open");

	}

	@OnError
	public void onError(Session session, Throwable error) {
		// TODO Auto-generated method stub
		LOGGER.info("websocket url" + session.getRequestURI() + "is onError");
	}

	
}
