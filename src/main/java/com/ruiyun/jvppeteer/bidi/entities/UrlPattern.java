package com.ruiyun.jvppeteer.bidi.entities;

public class UrlPattern {
    private String type;
    private String protocol;
    private String hostname;
    private String port;
    private String pathname;
    private String search;

    public UrlPattern(String type, String search, String pathname, String port, String hostname, String protocol) {
        this.type = type;
        this.search = search;
        this.pathname = pathname;
        this.port = port;
        this.hostname = hostname;
        this.protocol = protocol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
