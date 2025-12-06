package com.ruiyun.jvppeteer.cdp.entities;

public class ConnectTiming {
    /**
     * Timing's requestTime is a baseline in seconds, while the other numbers are ticks in
     * milliseconds relatively to this requestTime. Matches ResourceTiming's requestTime for
     * the same request (but not for redirected requests).
     */
    private double requestTime;

    public double getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(double requestTime) {
        this.requestTime = requestTime;
    }

    @Override
    public String toString() {
        return "ConnectTiming{" +
                "requestTime=" + requestTime +
                '}';
    }
}
