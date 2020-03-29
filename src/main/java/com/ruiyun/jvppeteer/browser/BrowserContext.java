package com.ruiyun.jvppeteer.browser;

import com.ruiyun.jvppeteer.page.Page;
import com.ruiyun.jvppeteer.transport.Connection;

public class BrowserContext {
	
	private Connection connection;
	
	private Browser browser;
	
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
