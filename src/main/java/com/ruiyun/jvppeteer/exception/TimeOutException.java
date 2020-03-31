package com.ruiyun.jvppeteer.exception;

public class TimeOutException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public TimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeOutException(String message) {
		super(message);
	}

}
