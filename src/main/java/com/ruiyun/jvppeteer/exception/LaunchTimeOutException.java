package com.ruiyun.jvppeteer.exception;

public class LaunchTimeOutException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public LaunchTimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public LaunchTimeOutException(String message) {
		super(message);
	}


	
	
}
