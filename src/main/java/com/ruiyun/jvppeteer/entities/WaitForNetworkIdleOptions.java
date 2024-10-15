package com.ruiyun.jvppeteer.entities;

public class WaitForNetworkIdleOptions {
    /**
     * 网络应空闲的时间（以毫秒为单位）。
     */
    public int  idleTime;
    /**
     * 被视为不活动的最大并发网络连接数。
     */
    public int  concurrency;
    /**
     * 最长等待时间（以毫秒为单位）。传递 0 以禁用超时。
     * 可以使用 Page.setDefaultTimeout() 方法更改默认值。
     */
    public int  timeout;

    public WaitForNetworkIdleOptions() {
    }

    public WaitForNetworkIdleOptions(int idleTime, int concurrency, int timeout) {
        this.idleTime = idleTime;
        this.concurrency = concurrency;
        this.timeout = timeout;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
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
