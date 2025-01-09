package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.bidi.entities.SupportedWebDriverCapabilities;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import java.util.Map;
import java.util.function.Function;

public class ConnectOptions  {
    /**
     * <br/>
     * Whether to ignore HTTPS errors during navigation.
     * <p>
     * 默认是false
     */
    private boolean acceptInsecureCerts = false;
    /**
     * Sets a consistent viewport for each page.
     */
    private Viewport defaultViewport ;
    /**
     * <br/>
     * Slows down Puppeteer operations by the specified amount of milliseconds.
     * Useful so that you can see what is going on.
     */
    private int slowMo;
    /**
     * Callback to decide if Puppeteer should connect to a given target or not.
     */
    private Function<Target, Boolean> targetFilter;

    private Function<Target, Boolean> isPageTarget;
    /**
     * 使用的通讯协议，CHROME 默认是 CDP，也仅支持 CDP
     * <p>
     * firefox 默认使用 WebDriverBiDi，也仅支持 WebDriverBiDi
     * <p>
     * 目前该字段作为保留字段，方便以后支持更多协议
     */
    private Protocol protocol;
    /**
     * Timeout setting for individual protocol (CDP) calls.
     */
    private int protocolTimeout = Constant.DEFAULT_TIMEOUT;

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

    public boolean getAcceptInsecureCerts() {
        return acceptInsecureCerts;
    }

    public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
        this.acceptInsecureCerts = acceptInsecureCerts;
    }

    public Viewport getDefaultViewport() {
        return defaultViewport;
    }

    public void setDefaultViewport(Viewport defaultViewport) {
        this.defaultViewport = defaultViewport;
    }

    public int getSlowMo() {
        return slowMo;
    }

    public void setSlowMo(int slowMo) {
        this.slowMo = slowMo;
    }

    public Function<Target, Boolean> getTargetFilter() {
        return targetFilter;
    }

    public void setTargetFilter(Function<Target, Boolean> targetFilter) {
        this.targetFilter = targetFilter;
    }

    public Function<Target, Boolean> getIsPageTarget() {
        return isPageTarget;
    }

    public void setIsPageTarget(Function<Target, Boolean> isPageTarget) {
        this.isPageTarget = isPageTarget;
    }

    public int getProtocolTimeout() {
        return protocolTimeout;
    }

    public void setProtocolTimeout(int protocolTimeout) {
        this.protocolTimeout = protocolTimeout;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
