package com.ruiyun.jvppeteer.options;

/**
 * @author sage.xue
 * @date 2022/6/14 17:49
 */
public class ConnectionOptions {
    /**
     * session waiting message result timeout. the unit is millisecond
     */
    private long sessionWaitingResultTimeout;

    public long getSessionWaitingResultTimeout() {
        return sessionWaitingResultTimeout;
    }

    public void setSessionWaitingResultTimeout(long sessionWaitingResultTimeout) {
        this.sessionWaitingResultTimeout = sessionWaitingResultTimeout;
    }
}
