package com.ruiyun.jvppeteer.browser;

import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.transport.Connection;

/**
 * 浏览器上下文
 */
public class BrowserContext extends EventEmitter {

	/**
	 *  浏览器对应的websocket client包装类，用于发送和接受消息
	 */
	private Connection connection;

	/**
	 * 浏览器上下文对应的浏览器，一个上下文只有一个浏览器，但是一个浏览器可能有多个上下文
	 */
	private Browser browser;

	/**
	 *浏览器上下文id
	 */
	private String contextId;
	
	

	public BrowserContext(Connection connection, Browser browser, String contextId) {
		super();
		this.connection = connection;
		this.browser = browser;
		this.contextId = contextId;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Browser getBrowser() {
		return browser;
	}

	public void setBrowser(Browser browser) {
		this.browser = browser;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public Page newPage(){
		return browser.createPageInContext(this.contextId);
	}
	
}
