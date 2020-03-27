package com.ruiyun.jvppeteer.exception;

public class ProtocolException extends RuntimeException {

	private static final long serialVersionUID = -2264436485477679192L;

	public ProtocolException() {
		super();
	}

	public ProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}
	
	
}
