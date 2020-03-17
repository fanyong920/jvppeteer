package com.ruiyun.jvppeteer.transport;

public interface ConnectionTransport {
	
	void send(String message);
	
	void close();
	
	void onmessage(String mess);
	
	void onclose();
}
