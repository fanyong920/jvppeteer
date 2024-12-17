package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class BrowserContextOptions {
    /**
     * 具有用于所有请求的可选端口的代理服务器。用户名和密码可以在 Page.authenticate 中设置。
     */
    private String proxyServer;
    /**
     * 绕过给定主机列表的代理。
     */
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
