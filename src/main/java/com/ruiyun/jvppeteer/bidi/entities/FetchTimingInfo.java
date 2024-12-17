package com.ruiyun.jvppeteer.bidi.entities;

import java.math.BigDecimal;

public class FetchTimingInfo {
    private BigDecimal timeOrigin;
    private BigDecimal requestTime;
    private BigDecimal redirectStart;
    private BigDecimal redirectEnd;
    private BigDecimal fetchStart;
    private BigDecimal dnsStart;
    private BigDecimal dnsEnd;
    private BigDecimal connectStart;
    private BigDecimal connectEnd;
    private BigDecimal tlsStart;
    private BigDecimal requestStart;
    private BigDecimal responseStart;
    private BigDecimal responseEnd;

    public BigDecimal getTimeOrigin() {
        return timeOrigin;
    }

    public void setTimeOrigin(BigDecimal timeOrigin) {
        this.timeOrigin = timeOrigin;
    }

    public BigDecimal getResponseEnd() {
        return responseEnd;
    }

    public void setResponseEnd(BigDecimal responseEnd) {
        this.responseEnd = responseEnd;
    }

    public BigDecimal getResponseStart() {
        return responseStart;
    }

    public void setResponseStart(BigDecimal responseStart) {
        this.responseStart = responseStart;
    }

    public BigDecimal getRequestStart() {
        return requestStart;
    }

    public void setRequestStart(BigDecimal requestStart) {
        this.requestStart = requestStart;
    }

    public BigDecimal getTlsStart() {
        return tlsStart;
    }

    public void setTlsStart(BigDecimal tlsStart) {
        this.tlsStart = tlsStart;
    }

    public BigDecimal getConnectEnd() {
        return connectEnd;
    }

    public void setConnectEnd(BigDecimal connectEnd) {
        this.connectEnd = connectEnd;
    }

    public BigDecimal getConnectStart() {
        return connectStart;
    }

    public void setConnectStart(BigDecimal connectStart) {
        this.connectStart = connectStart;
    }

    public BigDecimal getDnsEnd() {
        return dnsEnd;
    }

    public void setDnsEnd(BigDecimal dnsEnd) {
        this.dnsEnd = dnsEnd;
    }

    public BigDecimal getDnsStart() {
        return dnsStart;
    }

    public void setDnsStart(BigDecimal dnsStart) {
        this.dnsStart = dnsStart;
    }

    public BigDecimal getFetchStart() {
        return fetchStart;
    }

    public void setFetchStart(BigDecimal fetchStart) {
        this.fetchStart = fetchStart;
    }

    public BigDecimal getRedirectEnd() {
        return redirectEnd;
    }

    public void setRedirectEnd(BigDecimal redirectEnd) {
        this.redirectEnd = redirectEnd;
    }

    public BigDecimal getRedirectStart() {
        return redirectStart;
    }

    public void setRedirectStart(BigDecimal redirectStart) {
        this.redirectStart = redirectStart;
    }

    public BigDecimal getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(BigDecimal requestTime) {
        this.requestTime = requestTime;
    }
}
