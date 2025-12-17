package com.ruiyun.jvppeteer.exception;

public class ConnectionClosedException extends ProtocolException{
    public ConnectionClosedException(String message) {
        super(message);
    }
}
