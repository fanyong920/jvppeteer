package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.concurrent.TimeUnit;

public class Callback {
    private final long id;
    private String errorMsg = null;
    public final String label;
    private final int timeout;
    final AwaitableResult<JsonNode> waitingResponse = AwaitableResult.create();
    private int code;

    public Callback(long id, String label, int timeout) {
        this.id = id;
        this.label = label;
        this.timeout = timeout;
    }

    public void resolve(JsonNode value) {
        this.waitingResponse.onSuccess(value);
    }

    public void reject() {
        this.waitingResponse.complete();
    }

    public void reject(String errorMsg, int code) {
        this.waitingResponse.complete();
        this.errorMsg = errorMsg;
        this.code = code;
    }

    public long id() {
        return this.id;
    }


    public String label() {
        return this.label;
    }

    public JsonNode waitForResponse() throws InterruptedException {
        if (this.timeout > 0) {
            boolean waiting = waitingResponse.waiting(this.timeout, TimeUnit.MILLISECONDS);
            if (!waiting) {
                throw new TimeoutException("Timeout waiting for response for " + this.label);
            }
            if (StringUtil.isNotEmpty(errorMsg)) {
                throw new ProtocolException(errorMsg, code);
            }
            return waitingResponse.get();
        } else if (this.timeout == 0) {
            waitingResponse.wait();
            if (StringUtil.isNotEmpty(errorMsg)) {
                throw new ProtocolException(errorMsg, code);
            }
            return waitingResponse.get();
        } else {
            throw new JvppeteerException("Timeout < 0");
        }
    }

    public String errorMsg() {
        return this.errorMsg;
    }
}
