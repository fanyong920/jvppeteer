package com.ruiyun.jvppeteer.events;


public class DefaultBrowserListener<T> implements BrowserListener<T> {
	
	private String method;

	private Class<T> resolveType;

	private EventHandler<T> handler;

	private Object target;

	private boolean isOnce;

	private boolean isAvaliable = true;

	//是否异步
	private boolean isSync;

	public void setResolveType(Class<T> resolveType) {
		this.resolveType = resolveType;
	}

	public Class<?> getResolveType() {
		return resolveType;
}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public EventHandler<T> getHandler() {
		return handler;
	}

	public void setHandler(EventHandler<T> handler) {
		this.handler = handler;
	}

	public boolean getIsOnce() {
		return isOnce;
	}

	public void setIsOnce(boolean isOnce) {
		this.isOnce = isOnce;
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
		this.isAvaliable = isAvaliable;
	}

	public boolean getIsSync() {
		return isSync;
	}

	public void setIsSync(boolean isSync) {
		this.isSync = isSync;
	}

	@Override
	public void onBrowserEvent(T event) {
		this.getHandler().onEvent(event);
	}
}
