package com.ruiyun.jvppeteer.exception;

public class LaunchException extends RuntimeException {

    private static final long serialVersionUID = -8116119409970166589L;

    public LaunchException() {
        super();
    }

    public LaunchException(String message) {
        super(message);
    }

    public LaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}
