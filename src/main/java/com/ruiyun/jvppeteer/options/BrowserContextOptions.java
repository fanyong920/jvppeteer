package com.ruiyun.jvppeteer.options;

import java.util.List;

public class BrowserContextOptions {
    private String proxyServer;
    private List<String> proxyBypassList;
    public BrowserContextOptions() {
    }
    public BrowserContextOptions(List<String> proxyBypassList, String proxyServer) {
        this.proxyBypassList = proxyBypassList;
        this.proxyServer = proxyServer;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public List<String> getProxyBypassList() {
        return proxyBypassList;
    }

    public void setProxyBypassList(List<String> proxyBypassList) {
        this.proxyBypassList = proxyBypassList;
    }

    @Override
    public String toString() {
        return "BrowserContextOptions{" +
                "proxyServer='" + proxyServer + '\'' +
                ", proxyBypassList=" + proxyBypassList +
                '}';
    }
}
