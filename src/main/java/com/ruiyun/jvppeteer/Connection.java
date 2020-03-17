package com.ruiyun.jvppeteer;

import com.ruiyun.jvppeteer.transport.ConnectionTransport;

public class Connection {
	
	/**websoket url */
	private String url;
	
	private ConnectionTransport transport;
	
	private int delay;

	public Connection( String url,ConnectionTransport transport, int delay) {
		super();
		this.transport = transport;
		this.url = url;
		this.delay = delay;
	}
	
	
}
