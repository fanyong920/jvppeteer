package com.ruiyun.jvppeteer.events.browser.impl;

import com.ruiyun.jvppeteer.events.browser.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;
import com.ruiyun.jvppeteer.events.browser.definition.EventHandler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;


public abstract class DefaultBrowserListener<T> implements BrowserListener<T> {
	
	private String mothod;

	private Class<T> resolveType;

	private EventHandler<T> handler;

	private Object target;

	private boolean isOnce;

	private boolean isAvaliable = true;

	public void setResolveType(Class<T> resolveType) {
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

	public boolean getIsOnce() {
		return isOnce;
	}

	public void setIsOnce(boolean isOnce) {
		isOnce = isOnce;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public boolean getIsAvaliable() {
		return isAvaliable;
	}

	public void setIsAvaliable(boolean isAvaliable) {
		isAvaliable = isAvaliable;
	}
}
