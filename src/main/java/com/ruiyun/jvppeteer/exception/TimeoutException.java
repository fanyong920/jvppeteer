package com.ruiyun.jvppeteer.exception;

public class TimeoutException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(String message) {
		super(message);
	}

}
