package com.ruiyun.jvppeteer.events.browser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEventPublisher;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 默认事件发布这，其唯一实例存放在{@link com.ruiyun.jvppeteer.util.Factory}
 */
public class DefaultBrowserPublisher implements BrowserEventPublisher, Constant {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBrowserPublisher.class);

	private Map<String, Set<DefaultBrowserListener>> listenerMap = new ConcurrentHashMap<>();

	/**
	 * 发布事件
	 * @param method 事件对应的方法
	 * @param event 要发布的事件
	 */
	@Override
	public void publishEvent(String method, Object event) {
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);
		synchronized (DefaultBrowserPublisher.class){
			browserListeners = new LinkedHashSet(browserListeners);
		}
		for (DefaultBrowserListener listener : browserListeners) {
			executor.execute(() -> invokeListener(listener, event));
		}
	}

	/**
	 * 增加一个监听器
	 * @param method 监听器对应的方法
	 * @param listener 要增加的监听器
	 */
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

	/**
	 * 执行监听器，如果是用户的监听，则用用户的处理器去处理，不然执行onBrowserEvent方法
	 * @param listener 监听器
	 * @param event 事件
	 */
	public void invokeListener(DefaultBrowserListener listener, Object event){
		if(listener.getHandler() != null){
			listener.getHandler().onEvent(event);
		}else{
			listener.onBrowserEvent(event);
		}
	}

	/**
	 * 发布事件
	 * @param method 方法
	 * @param params 事件 event
	 */
	public void publishEvent2(String method, JsonNode params) {
		ValidateUtil.notNull(method, "method must not be null");
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);
		if (ValidateUtil.isNotEmpty(browserListeners)) {
			try {
				Class<?> resolveType = null;
				DefaultBrowserListener listener = browserListeners.stream().findFirst().get();
				Type genericSuperclass = listener.getClass().getGenericSuperclass();
				if(genericSuperclass instanceof ParameterizedType){
					ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
					Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
					if(actualTypeArguments.length == 1){
						resolveType = (Class)actualTypeArguments[0];
					}
				}else{
					resolveType = listener.getResolveType();
				}
				Object event = readJsonObject(resolveType, params);
				this.publishEvent(method, event);
			} catch (IOException e) {
				LOGGER.error("publish event error:", e);
			}
		}
	}

	/**
	 * 如果clazz属于JsonNode.class则不用转换类型，如果不是，则将jsonNode转化成clazz类型对象
	 * @param clazz 目标类型
	 * @param jsonNode event
	 * @param <T>  具体类型
	 * @return T
	 * @throws IOException 转化失败抛出的异常
	 */
	private <T> T readJsonObject(Class<T> clazz, JsonNode jsonNode) throws IOException {
		if (jsonNode == null) {
			throw new IllegalArgumentException(
					"Failed converting null response to clazz " + clazz.getName());
		}
		if(JsonNode.class.isAssignableFrom(clazz)){
			return (T)jsonNode;
		}
		return OBJECTMAPPER.readerFor(clazz).readValue(jsonNode);
	}

	private Set<DefaultBrowserListener> getConcurrentSet() {
		return new CopyOnWriteArraySet<>();
	}

	/**
	 * 移除具体某个监听器
	 * @param mothod 监听器对应的方法
	 * @param listener 要移除的监听器
	 */
	public void  removeListener(String mothod ,DefaultBrowserListener listener){
		Set<DefaultBrowserListener> defaultBrowserListeners = listenerMap.get(mothod);
		if (ValidateUtil.isNotEmpty(defaultBrowserListeners)) {
			defaultBrowserListeners.remove(listener);
		}
	}

}
