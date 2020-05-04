package com.ruiyun.jvppeteer.types.page;

import com.ruiyun.jvppeteer.Constant;

public class TimeoutSettings implements Constant {

    private int defaultNavigationTimeout;

    private  int defaultTimeout;

    public TimeoutSettings() {
        this.defaultTimeout = 0;
        this.defaultNavigationTimeout = 0;
    }


    public int navigationTimeout() {
        if (this.defaultNavigationTimeout != 0)
            return this.defaultNavigationTimeout;
        if (this.defaultTimeout != 0)
            return this.defaultTimeout;
        return DEFAULT_TIMEOUT;
    }

    public int timeout() {
        if (this.defaultTimeout != 0)
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
