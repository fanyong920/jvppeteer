package com.ruiyun.jvppeteer.transport.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * message that send to browser
 * @author fff
 *
 */
public class SendMsg {
	
	private long id;
	
	private Map<String,Object> params;
	
	private String method;

	@JsonIgnore
	private CountDownLatch countDownLatch ;

	private JsonNode result;//本次发送消息返回的结果

	private String sessionId;

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

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	public JsonNode getResult() {
		return result;
	}

	public void setResult(JsonNode result) {
		this.result = result;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean waitForResult(long timeout, TimeUnit timeUnit) throws InterruptedException {
		if(countDownLatch != null){
			if(timeout == 0){
				countDownLatch.await();
				return true;
			}
			return countDownLatch.await(timeout,timeUnit);
		}
		return true;
	}
}
