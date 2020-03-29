package com.ruiyun.jvppeteer.events.browser.impl;

import com.ruiyun.jvppeteer.events.browser.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;
import com.ruiyun.jvppeteer.events.browser.definition.EventHandler;

public  class DefaultBrowserListener implements BrowserListener<BrowserEvent> {
	
	private String mothod;

	private Class<?> resolveType;

	public Class<?> getResolveType() {
		return resolveType;
	}

	private EventHandler handler;

	public void setResolveType(Class<?> resolveType) {
		this.resolveType = resolveType;
	}

	public String getMothod() {
		return mothod;
	}

	public void setMothod(String mothod) {
		this.mothod = mothod;
	}

	public EventHandler getHandler() {
		return handler;
	}

	public void setHandler(EventHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onBrowserEvent(BrowserEvent event) {
		getHandler().onEvent(event);
	}

}
