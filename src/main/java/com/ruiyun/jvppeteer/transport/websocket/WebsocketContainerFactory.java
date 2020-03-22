package com.ruiyun.jvppeteer.transport.websocket;

import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientContainer;

import com.ruiyun.jvppeteer.Constant;

public class WebsocketContainerFactory implements Constant {

	public static WebSocketContainer create() {
		ClientManager client = ClientManager.createClient(GrizzlyClientContainer.class.getCanonicalName());
		client.getProperties().put(INCOMING_BUFFER_SIZE_PROPERTY, DEFAULT_PAYLOAD);
		return client;
	}
}
