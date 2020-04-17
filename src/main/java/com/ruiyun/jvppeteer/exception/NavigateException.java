package com.ruiyun.jvppeteer.exception;

/**
 * 导航到新的页面出错跑出来的异常
 */
public class NavigateException extends RuntimeException {

    public NavigateException() {
        super();
    }

    public NavigateException(String message) {
        super(message);
    }

    public NavigateException(String message, Throwable cause) {
        super(message, cause);
    }

    public NavigateException(Throwable cause) {
        super(cause);
    }

    protected NavigateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
