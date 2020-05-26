package com.ruiyun.jvppeteer.transport.factory;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import org.java_websocket.drafts.Draft_6455;

import java.net.URI;

public class WebSocketTransportFactory implements Constant {
	/**
	 * create websocket client
	 * @param url
	 * @return
	 */
	public static WebSocketTransport create(String url) throws InterruptedException {
		WebSocketTransport client = new WebSocketTransport(URI.create(url),new Draft_6455());
		/*保持websokcet连接*/
		client.setConnectionLostTimeout(0);
		client.connectBlocking();
//		client.setConnectionLostTimeout(0);
		return client;
	}
	
}
