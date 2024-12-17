package com.ruiyun.jvppeteer.cdp.entities;

public class ResourceTiming {
    /**
     * Timing's requestTime is a baseline in seconds, while the other numbers are ticks in
     * milliseconds relatively to this requestTime.
     */
    private  long requestTime;

    /**
     * Started resolving proxy.
     */
    private  long proxyStart;

    /**
     * Finished resolving proxy.
     */
    private  long proxyEnd;

    /**
     * Started DNS address resolve.
     */
    private  long dnsStart;

    /**
     * Finished DNS address resolve.
     */
    private  long dnsEnd;

    /**
     * Started connecting to the remote host.
     */
    private  long connectStart;

    /**
     * Connected to the remote host.
     */
    private  long connectEnd;

    /**
     * Started SSL handshake.
     */
    private  long sslStart;

    /**
     * Finished SSL handshake.
     */
    private  long sslEnd;

    /**
     * Started running ServiceWorker.
     */
    private  long workerStart;

    /**
     * Finished Starting ServiceWorker.
     */
    private  long workerReady;

    /**
     * Started fetch event.
     */
    private  long workerFetchStart;

    /**
     * Settled fetch event respondWith promise.
     */
    private  long workerRespondWithSettled;

    /**
     * Started ServiceWorker static routing source evaluation.
     */
    private  long workerRouterEvaluationStart;

    /**
     * Started cache lookup when the source was evaluated to `cache`.
     */
    private  long workerCacheLookupStart;

    /**
     * Started sending request.
     */
    private  long sendStart;

    /**
     * Finished sending request.
     */
    private  long sendEnd;

    /**
     * Time the server started pushing request.
     */
    private  long pushStart;

    /**
     * Time the server finished pushing request.
     */
    private  long pushEnd;

    /**
     * Started receiving response headers.
     */
    private  long receiveHeadersStart;

    /**
     * Finished receiving response headers.
     */
    private  long receiveHeadersEnd;

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getProxyStart() {
        return proxyStart;
    }

    public void setProxyStart(long proxyStart) {
        this.proxyStart = proxyStart;
    }

    public long getProxyEnd() {
        return proxyEnd;
    }

    public void setProxyEnd(long proxyEnd) {
        this.proxyEnd = proxyEnd;
    }

    public long getDnsStart() {
        return dnsStart;
    }

    public void setDnsStart(long dnsStart) {
        this.dnsStart = dnsStart;
    }

    public long getDnsEnd() {
        return dnsEnd;
    }

    public void setDnsEnd(long dnsEnd) {
        this.dnsEnd = dnsEnd;
    }

    public long getConnectStart() {
        return connectStart;
    }

    public void setConnectStart(long connectStart) {
        this.connectStart = connectStart;
    }

    public long getConnectEnd() {
        return connectEnd;
    }

    public void setConnectEnd(long connectEnd) {
        this.connectEnd = connectEnd;
    }

    public long getSslStart() {
        return sslStart;
    }

    public void setSslStart(long sslStart) {
        this.sslStart = sslStart;
    }

    public long getSslEnd() {
        return sslEnd;
    }

    public void setSslEnd(long sslEnd) {
        this.sslEnd = sslEnd;
    }

    public long getWorkerReady() {
        return workerReady;
    }

    public void setWorkerReady(long workerReady) {
        this.workerReady = workerReady;
    }

    public long getWorkerFetchStart() {
        return workerFetchStart;
    }

    public void setWorkerFetchStart(long workerFetchStart) {
        this.workerFetchStart = workerFetchStart;
    }

    public long getWorkerStart() {
        return workerStart;
    }

    public void setWorkerStart(long workerStart) {
        this.workerStart = workerStart;
    }

    public long getWorkerRouterEvaluationStart() {
        return workerRouterEvaluationStart;
    }

    public void setWorkerRouterEvaluationStart(long workerRouterEvaluationStart) {
        this.workerRouterEvaluationStart = workerRouterEvaluationStart;
    }

    public long getWorkerCacheLookupStart() {
        return workerCacheLookupStart;
    }

    public void setWorkerCacheLookupStart(long workerCacheLookupStart) {
        this.workerCacheLookupStart = workerCacheLookupStart;
    }

    public long getSendStart() {
        return sendStart;
    }

    public void setSendStart(long sendStart) {
        this.sendStart = sendStart;
    }

    public long getSendEnd() {
        return sendEnd;
    }

    public void setSendEnd(long sendEnd) {
        this.sendEnd = sendEnd;
    }

    public long getPushStart() {
        return pushStart;
    }

    public void setPushStart(long pushStart) {
        this.pushStart = pushStart;
    }

    public long getPushEnd() {
        return pushEnd;
    }

    public void setPushEnd(long pushEnd) {
        this.pushEnd = pushEnd;
    }

    public long getWorkerRespondWithSettled() {
        return workerRespondWithSettled;
    }

    public void setWorkerRespondWithSettled(long workerRespondWithSettled) {
        this.workerRespondWithSettled = workerRespondWithSettled;
    }

    public long getReceiveHeadersStart() {
        return receiveHeadersStart;
    }

    public void setReceiveHeadersStart(long receiveHeadersStart) {
        this.receiveHeadersStart = receiveHeadersStart;
    }

    public long getReceiveHeadersEnd() {
        return receiveHeadersEnd;
    }

    public void setReceiveHeadersEnd(long receiveHeadersEnd) {
        this.receiveHeadersEnd = receiveHeadersEnd;
    }

    @Override
    public String toString() {
        return "ResourceTiming{" +
                "requestTime=" + requestTime +
                ", proxyStart=" + proxyStart +
                ", proxyEnd=" + proxyEnd +
                ", dnsStart=" + dnsStart +
                ", dnsEnd=" + dnsEnd +
                ", connectStart=" + connectStart +
                ", connectEnd=" + connectEnd +
                ", sslStart=" + sslStart +
                ", sslEnd=" + sslEnd +
                ", workerStart=" + workerStart +
                ", workerReady=" + workerReady +
                ", workerFetchStart=" + workerFetchStart +
                ", workerRespondWithSettled=" + workerRespondWithSettled +
                ", workerRouterEvaluationStart=" + workerRouterEvaluationStart +
                ", workerCacheLookupStart=" + workerCacheLookupStart +
                ", sendStart=" + sendStart +
                ", sendEnd=" + sendEnd +
                ", pushStart=" + pushStart +
                ", pushEnd=" + pushEnd +
                ", receiveHeadersStart=" + receiveHeadersStart +
                ", receiveHeadersEnd=" + receiveHeadersEnd +
                '}';
    }
}
