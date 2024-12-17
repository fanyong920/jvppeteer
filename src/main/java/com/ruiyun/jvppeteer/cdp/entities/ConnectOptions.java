package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.bidi.entities.SupportedWebDriverCapabilities;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;

import java.util.Map;

public class ConnectOptions extends BrowserConnectOptions {
    private String browserWSEndpoint;
    private String browserURL;
    private ConnectionTransport transport;
    private Map<String, String> headers;
    private SupportedWebDriverCapabilities capabilities;
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

    public SupportedWebDriverCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(SupportedWebDriverCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public String toString() {
        return "ConnectOptions{" +
                "browserWSEndpoint='" + browserWSEndpoint + '\'' +
                ", browserURL='" + browserURL + '\'' +
                ", transport=" + transport +
                ", headers=" + headers +
                ", capabilities=" + capabilities +
                '}';
    }
}
