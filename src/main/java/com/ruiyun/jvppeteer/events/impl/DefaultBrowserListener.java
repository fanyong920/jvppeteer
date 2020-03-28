package com.ruiyun.jvppeteer.events.impl;

import com.ruiyun.jvppeteer.events.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.definition.BrowserListener;

public  class DefaultBrowserListener implements BrowserListener<BrowserEvent> {
	
	private String mothod;

	private Class<?> resolveType;

	public Class<?> getResolveType() {
		return resolveType;
	}

	public void setResolveType(Class<?> resolveType) {
		this.resolveType = resolveType;
	}

	public String getMothod() {
		return mothod;
	}

	public void setMothod(String mothod) {
		this.mothod = mothod;
	}


	@Override
	public void onBrowserEvent(BrowserEvent event) {

	}
}
