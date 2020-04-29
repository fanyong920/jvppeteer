package com.ruiyun.jvppeteer.transport.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import org.glassfish.tyrus.client.ClientManager;

public class WebSocketTransportFactory implements Constant {
	/**
	 * create websocket client
	 * @param url
	 * @return
	 */
	public static WebSocketTransport create(String url) throws InterruptedException {
		WebSocketTransport client = new WebSocketTransport(URI.create(url));
		client.connectBlocking();
		client.setConnectionLostTimeout(0);
		return client;
	}
	
}
