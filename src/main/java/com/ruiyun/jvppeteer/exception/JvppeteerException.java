package com.ruiyun.jvppeteer.exception;

public class JvppeteerException extends RuntimeException{

    private String name;

    private String stack;

    public JvppeteerException() {
        super();
    }

    public JvppeteerException(Throwable cause) {
        super(cause);
    }

    public JvppeteerException(String message) {
        super(message);
    }

    public JvppeteerException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }
}
