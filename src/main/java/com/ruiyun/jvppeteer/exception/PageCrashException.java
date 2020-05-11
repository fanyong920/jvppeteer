package com.ruiyun.jvppeteer.exception;

public class PageCrashException extends RuntimeException{


    public PageCrashException() {
        super();
    }

    public PageCrashException(String message) {
        super(message);
    }

    public PageCrashException(String message, Throwable cause) {
        super(message, cause);
    }

    public PageCrashException(Throwable cause) {
        super(cause);
    }

    protected PageCrashException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
