package com.ruiyun.jvppeteer.events.browser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEventPublisher;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultBrowserPublisher implements BrowserEventPublisher, Constant {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBrowserPublisher.class);

	private Map<String, Set<DefaultBrowserListener>> listenerMap = new ConcurrentHashMap<>();

	@Override
	public void publishEvent(String method, Object event) {
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);
		synchronized (listenerMap){
			browserListeners = new LinkedHashSet(browserListeners);
		}
		for (DefaultBrowserListener listener : browserListeners) {
			executor.execute(() -> invokeListener(listener, event));
		}
	}

	public void addListener(String method,DefaultBrowserListener listener){
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);
		if (browserListeners == null) {
			Set<DefaultBrowserListener> listeners = getConcurrentSet();
			listenerMap.putIfAbsent(method,listeners);
			listeners.add(listener);
		}else{
			browserListeners.add(listener);
		}

	}

	public void invokeListener(BrowserListener listener, Object event){
		listener.onBrowserEvent(event);
	}

	public void publishEvent2(String method, JsonNode params) {
		ValidateUtil.notNull(method, "method must not be null");
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);

		if (ValidateUtil.isNotEmpty(browserListeners)) {
			try {
				Class<?> resolveType = browserListeners.stream().findFirst().get().getResolveType();
				Object event = readJsonObject(resolveType, params);
				this.publishEvent(method, event);
			} catch (IOException e) {
				LOGGER.error("publish event error:", e);
			}
		}
	}
	private <T> T readJsonObject(Class<T> clazz, JsonNode jsonNode) throws IOException {
		if (jsonNode == null) {
			throw new IllegalArgumentException(
					"Failed converting null response to clazz " + clazz.getName());
		}
		return OBJECTMAPPER.readerFor(clazz).readValue(jsonNode);
	}

	private Set<DefaultBrowserListener> getConcurrentSet() {
		return new CopyOnWriteArraySet<DefaultBrowserListener>();
	}

	public void  removeListener(String mothod ,DefaultBrowserListener listener){
		Set<DefaultBrowserListener> defaultBrowserListeners = listenerMap.get(mothod);
		if (ValidateUtil.isNotEmpty(defaultBrowserListeners)) {
			defaultBrowserListeners.remove(listener);
		}
	}

}
