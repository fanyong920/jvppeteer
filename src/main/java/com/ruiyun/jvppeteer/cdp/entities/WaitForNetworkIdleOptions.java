package com.ruiyun.jvppeteer.cdp.entities;

import static com.ruiyun.jvppeteer.common.Constant.NETWORK_IDLE_TIME;

public class WaitForNetworkIdleOptions {
    /**
     * 网络应空闲的时间（以毫秒为单位）。
     */
    private Integer idleTime = NETWORK_IDLE_TIME;
    /**
     * 被视为不活动的最大并发网络连接数。
     */
    private int concurrency;
    /**
     * 最长等待时间（以毫秒为单位）。传递 0 以禁用超时。
     * 可以使用 Page.setDefaultTimeout() 方法更改默认值。
     */
    private Integer timeout;

    public WaitForNetworkIdleOptions() {
    }

    public WaitForNetworkIdleOptions(Integer idleTime, int concurrency, Integer timeout) {
        this.idleTime = idleTime;
        this.concurrency = concurrency;
        this.timeout = timeout;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(Integer idleTime) {
        this.idleTime = idleTime;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "WaitForNetworkIdleOptions{" +
                "idleTime=" + idleTime +
                ", concurrency=" + concurrency +
                ", timeout=" + timeout +
                '}';
    }
}
