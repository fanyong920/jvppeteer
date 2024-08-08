package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.options.Timestamp;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;


public class ExceptionThrownEvent {
    private Timestamp timestamp;
    private ExceptionDetails exceptionDetails;

    public ExceptionThrownEvent() {
    }

    public ExceptionThrownEvent(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    public void setExceptionDetails(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    @Override
    public String toString() {
        return "ExceptionThrownEvent{" +
                "timestamp=" + timestamp +
                ", exceptionDetails=" + exceptionDetails +
                '}';
    }
}
