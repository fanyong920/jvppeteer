package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.transport.ConnectionTransport;

import java.util.Map;

public class ConnectOptions extends BrowserConnectOptions {
    private String browserWSEndpoint;
    private String browserURL;
    private ConnectionTransport transport;
    private Map<String, String> headers;

    public String getBrowserWSEndpoint() {
        return browserWSEndpoint;
    }

    public void setBrowserWSEndpoint(String browserWSEndpoint) {
        this.browserWSEndpoint = browserWSEndpoint;
    }

    public String getBrowserURL() {
        return browserURL;
    }

    public void setBrowserURL(String browserURL) {
        this.browserURL = browserURL;
    }

    public ConnectionTransport getTransport() {
        return transport;
    }

    public void setTransport(ConnectionTransport transport) {
        this.transport = transport;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
