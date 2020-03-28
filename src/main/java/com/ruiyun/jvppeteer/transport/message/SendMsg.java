package com.ruiyun.jvppeteer.transport.message;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * message that send to browser
 * @author fff
 *
 */
public class SendMsg {
	
	private long id;
	
	private Map<String,Object> params;
	
	private String method;

	private Semaphore semaphore = new Semaphore(1);

	private JsonNode result;//本次发送消息返回的结果

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

	public Semaphore getSemaphore() {
		return semaphore;
	}

	public void setSemaphore(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	public JsonNode getResult() {
		return result;
	}

	public void setResult(JsonNode result) {
		this.result = result;
	}
}
