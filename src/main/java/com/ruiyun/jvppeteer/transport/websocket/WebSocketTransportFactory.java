package com.ruiyun.jvppeteer.transport.websocket;

import java.io.IOException;
import java.net.URI;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;

public class WebSocketTransportFactory implements Constant {
	
	public static WebSocketContainer WEB_SOCKET_CONTAINER = null;
	
	/**
	 * create websocket client
	 * @param url
	 * @return
	 */
	public static WebSocketTransport create(String url) {
		
		if(WEB_SOCKET_CONTAINER == null) {
			synchronized (WebSocketTransportFactory.class) {
				if(WEB_SOCKET_CONTAINER == null) {
					WEB_SOCKET_CONTAINER = WebsocketContainerFactory.create();
					try {
						Session session = WEB_SOCKET_CONTAINER.connectToServer(WebSocketTransport.class,URI.create(url));
						return WS_HASH_MAP.get(session.getId());
					} catch (DeploymentException e) {
						throw new RuntimeException("connect to websocket server fail : DeploymentException",e);
					} catch (IOException e) {
						throw new RuntimeException("connect to websocket server fail:IOException",e);
					}
				}
			}
		}else {
			try {
				Session session = WEB_SOCKET_CONTAINER.connectToServer(WebSocketTransport.class,URI.create(url));
				return WS_HASH_MAP.get(session.getId());
			} catch (DeploymentException e) {
				throw new RuntimeException("connect to websocket server fail : DeploymentException",e);
			} catch (IOException e) {
				throw new RuntimeException("connect to websocket server fail:IOException",e);
			}
		}
		throw new RuntimeException("wobsoket client ==null");
	}
	
}
