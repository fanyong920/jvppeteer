package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.common.Constant;
import org.java_websocket.drafts.Draft_6455;

import java.net.URI;

public class WebSocketTransportFactory implements Constant {
	/**
	 * create websocket client
	 * @param url 连接websocket的地址
	 * @throws InterruptedException 被打断异常
	 * @return WebSocketTransport websocket客户端
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
