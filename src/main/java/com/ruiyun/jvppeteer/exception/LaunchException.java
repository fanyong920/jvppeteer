package com.ruiyun.jvppeteer.exception;

public class LaunchException extends RuntimeException {

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
