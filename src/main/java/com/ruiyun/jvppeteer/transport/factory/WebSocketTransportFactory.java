package com.ruiyun.jvppeteer.transport.factory;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.transport.websocket.WebSocketTransport;

import java.net.URI;

public class WebSocketTransportFactory implements Constant {
	/**
	 * create websocket client
	 * @param url
	 * @return
	 */
	public static WebSocketTransport create(String url) throws InterruptedException {
		WebSocketTransport client = new WebSocketTransport(URI.create(url));
		/*保持websokcet连接*/
		client.setConnectionLostTimeout(0);
		client.connectBlocking();
		client.setConnectionLostTimeout(0);
		return client;
	}
	
}
