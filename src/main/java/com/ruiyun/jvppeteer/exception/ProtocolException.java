package com.ruiyun.jvppeteer.exception;

public class ProtocolException extends RuntimeException {
    private int code;
    private String originalMessage = "";
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

    public ProtocolException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }
}
