package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.ExceptionDetails;
import java.math.BigDecimal;


public class ExceptionThrownEvent {
    private BigDecimal timestamp;
    private ExceptionDetails exceptionDetails;

    public ExceptionThrownEvent() {
    }

    public ExceptionThrownEvent(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
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
