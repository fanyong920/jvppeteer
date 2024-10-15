package com.ruiyun.jvppeteer.transport;

@FunctionalInterface
public interface SessionFactory {
    CDPSession create(boolean isAutoAttachEmulated);
}
