package com.ruiyun.jvppeteer.transport.message;

import java.util.Map;

/**
 * message that send to browser
 * @author fff
 *
 */
public class SendMsg {
	
	private long id;
	
	private Map<String,Object> params;
	
	private String method;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
}
