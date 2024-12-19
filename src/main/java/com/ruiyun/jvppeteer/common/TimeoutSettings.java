package com.ruiyun.jvppeteer.common;

import java.util.Objects;

public class TimeoutSettings implements Constant {

    private Integer defaultNavigationTimeout;

    private Integer defaultTimeout;

    public TimeoutSettings() {
        this.defaultTimeout = 0;
        this.defaultNavigationTimeout = 0;
    }

    public int navigationTimeout() {
        if (Objects.nonNull(this.defaultNavigationTimeout))
            return this.defaultNavigationTimeout;
        if (Objects.nonNull(this.defaultTimeout))
            return this.defaultTimeout;
        return DEFAULT_TIMEOUT;
    }

    public int timeout() {
        if (Objects.nonNull(this.defaultTimeout))
            return this.defaultTimeout;
        return DEFAULT_TIMEOUT;
    }

    public int getDefaultNavigationTimeout() {
        return defaultNavigationTimeout;
    }

    public void setDefaultNavigationTimeout(int defaultNavigationTimeout) {
        this.defaultNavigationTimeout = defaultNavigationTimeout;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }
}
