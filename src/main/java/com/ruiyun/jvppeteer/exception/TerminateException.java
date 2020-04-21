package com.ruiyun.jvppeteer.exception;

public class TerminateException extends RuntimeException {

    private static final long serialVersionUID = -4407392184588405652L;

    public TerminateException() {
        super();
    }

    public TerminateException(String message) {
        super(message);
    }

    public TerminateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TerminateException(Throwable cause) {
        super(cause);
    }

    protected TerminateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
