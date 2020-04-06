package com.ruiyun.jvppeteer.events.browser.impl;

import com.ruiyun.jvppeteer.events.browser.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;
import com.ruiyun.jvppeteer.events.browser.definition.EventHandler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public abstract  class DefaultBrowserListener<T> implements BrowserListener<T> {
	
	private String mothod;

	private Class<?> resolveType;

	private EventHandler handler;

	private Object target;

	private boolean isOnce;

	public void setResolveType(Class<?> resolveType) {
		this.resolveType = resolveType;
	}

	public Class<?> getResolveType() {
		return resolveType;
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


	public boolean isOnce() {
		return isOnce;
	}

	public void setOnce(boolean once) {
		isOnce = once;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

}
