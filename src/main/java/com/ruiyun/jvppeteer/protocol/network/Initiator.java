package com.ruiyun.jvppeteer.protocol.network;

import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;

/**
 * Information about the request initiator.
 */
public class Initiator {

    /**
     * Type of this initiator."parser"|"script"|"preload"|"SignedExchange"|"other"
     */
    private String type;
    /**
     * Initiator JavaScript stack trace, set for Script only.
     */
    private StackTrace stack;
    /**
     * Initiator URL, set for Parser type or for Script type (when script is importing module) or for SignedExchange type.
     */
    private String url;
    /**
     * Initiator line number, set for Parser type or for Script type (when script is importing
     module) (0-based).
     */
    private int lineNumber;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StackTrace getStack() {
        return stack;
    }

    public void setStack(StackTrace stack) {
        this.stack = stack;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
