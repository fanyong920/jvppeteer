package com.ruiyun.jvppeteer.transport;

public class WebSocketTransport implements ConnectionTransport {
	
	private String url;
	
	
	public WebSocketTransport(String url) {
		super();
		this.url = url;
		//connect to websocket server
		
	}

	@Override
	public void send(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onmessage(String mess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onclose() {
		// TODO Auto-generated method stub

	}

}
