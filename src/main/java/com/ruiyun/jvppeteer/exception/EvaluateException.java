package com.ruiyun.jvppeteer.exception;

public class EvaluateException extends RuntimeException  {
    private String name;

    private String stack;

    public EvaluateException() {
        super();
    }

    public EvaluateException(Throwable cause) {
        super(cause);
    }

    public EvaluateException(String message) {
        super(message);
    }

    public EvaluateException(String message, Throwable cause) {
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
