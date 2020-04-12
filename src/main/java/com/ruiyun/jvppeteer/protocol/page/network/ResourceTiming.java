package com.ruiyun.jvppeteer.protocol.page.network;

/**
 * Timing information for the request.
 */
public class ResourceTiming {
    /**
     * Timing's requestTime is a baseline in seconds, while the other numbers are ticks in
     milliseconds relatively to this requestTime.
     */
    private int requestTime;
    /**
     * Started resolving proxy.
     */
    private int proxyStart;
    /**
     * Finished resolving proxy.
     */
    private int proxyEnd;
    /**
     * Started DNS address resolve.
     */
    private int dnsStart;
    /**
     * Finished DNS address resolve.
     */
    private int dnsEnd;
    /**
     * Started connecting to the remote host.
     */
    private int connectStart;
    /**
     * Connected to the remote host.
     */
    private int connectEnd;
    /**
     * Started SSL handshake.
     */
    private int sslStart;
    /**
     * Finished SSL handshake.
     */
    private int sslEnd;
    /**
     * Started running ServiceWorker.
     */
    private int workerStart;
    /**
     * Finished Starting ServiceWorker.
     */
    private int workerReady;
    /**
     * Started sending request.
     */
    private int sendStart;
    /**
     * Finished sending request.
     */
    private int sendEnd;
    /**
     * Time the server started pushing request.
     */
    private int pushStart;
    /**
     * Time the server finished pushing request.
     */
    private int pushEnd;
    /**
     * Finished receiving response headers.
     */
    private int receiveHeadersEnd;

    public int getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(int requestTime) {
        this.requestTime = requestTime;
    }

    public int getProxyStart() {
        return proxyStart;
    }

    public void setProxyStart(int proxyStart) {
        this.proxyStart = proxyStart;
    }

    public int getProxyEnd() {
        return proxyEnd;
    }

    public void setProxyEnd(int proxyEnd) {
        this.proxyEnd = proxyEnd;
    }

    public int getDnsStart() {
        return dnsStart;
    }

    public void setDnsStart(int dnsStart) {
        this.dnsStart = dnsStart;
    }

    public int getDnsEnd() {
        return dnsEnd;
    }

    public void setDnsEnd(int dnsEnd) {
        this.dnsEnd = dnsEnd;
    }

    public int getConnectStart() {
        return connectStart;
    }

    public void setConnectStart(int connectStart) {
        this.connectStart = connectStart;
    }

    public int getConnectEnd() {
        return connectEnd;
    }

    public void setConnectEnd(int connectEnd) {
        this.connectEnd = connectEnd;
    }

    public int getSslStart() {
        return sslStart;
    }

    public void setSslStart(int sslStart) {
        this.sslStart = sslStart;
    }

    public int getSslEnd() {
        return sslEnd;
    }

    public void setSslEnd(int sslEnd) {
        this.sslEnd = sslEnd;
    }

    public int getWorkerStart() {
        return workerStart;
    }

    public void setWorkerStart(int workerStart) {
        this.workerStart = workerStart;
    }

    public int getWorkerReady() {
        return workerReady;
    }

    public void setWorkerReady(int workerReady) {
        this.workerReady = workerReady;
    }

    public int getSendStart() {
        return sendStart;
    }

    public void setSendStart(int sendStart) {
        this.sendStart = sendStart;
    }

    public int getSendEnd() {
        return sendEnd;
    }

    public void setSendEnd(int sendEnd) {
        this.sendEnd = sendEnd;
    }

    public int getPushStart() {
        return pushStart;
    }

    public void setPushStart(int pushStart) {
        this.pushStart = pushStart;
    }

    public int getPushEnd() {
        return pushEnd;
    }

    public void setPushEnd(int pushEnd) {
        this.pushEnd = pushEnd;
    }

    public int getReceiveHeadersEnd() {
        return receiveHeadersEnd;
    }

    public void setReceiveHeadersEnd(int receiveHeadersEnd) {
        this.receiveHeadersEnd = receiveHeadersEnd;
    }
}
