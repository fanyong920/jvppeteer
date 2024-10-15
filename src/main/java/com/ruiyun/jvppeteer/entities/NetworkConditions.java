package com.ruiyun.jvppeteer.entities;

public class NetworkConditions {

    /**
     * 下载速度（字节/秒） (bytes/s)
     */
    private double download = - 1;
    /**
     * 上传速度（字节/秒）
     */
    private double upload = -1;
    /**
     * 延迟（毫秒）
     */
    private double latency = 0;

    public NetworkConditions(double download, double upload, double latency) {
        this.download = download;
        this.upload = upload;
        this.latency = latency;
    }

    public NetworkConditions() {
    }

    public double getDownload() {
        return download;
    }

    public void setDownload(double download) {
        this.download = download;
    }

    public double getUpload() {
        return upload;
    }

    public void setUpload(double upload) {
        this.upload = upload;
    }

    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return "NetworkConditions [download=" + download + ", upload=" + upload + ", latency=" + latency + "]";
    }
}
