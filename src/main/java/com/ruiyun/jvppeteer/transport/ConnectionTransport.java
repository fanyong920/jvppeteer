package com.ruiyun.jvppeteer.transport;

import java.util.function.Consumer;

import javax.websocket.Session;

import com.ruiyun.jvppeteer.Constant;

public interface ConnectionTransport extends Constant {
	
	void send(String message);
	
	boolean close();
	
	void onMessage(String message);
	
	void onClose();
	
	void onOpen(Session session);
	
	void onError(Session session, Throwable error);
	
	void addMessageHandler(Consumer<String> consumer);
}
