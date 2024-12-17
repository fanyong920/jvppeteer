package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.common.Constant;
import java.util.function.Function;

public class BrowserConnectOptions extends BrowserLaunchArgumentOptions {

    /**
     * <br/>
     * Whether to ignore HTTPS errors during navigation.
     * <p>
     * 默认是false
     */
    private boolean acceptInsecureCerts = false;
    /**
     * 800x600
     * <br/>
     * Sets a consistent viewport for each page. Defaults to an 800x600 viewport. null disables the default viewport.
     */
    private Viewport defaultViewport = new Viewport();
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
     * Timeout setting for individual protocol (CDP) calls.
     */
    private int protocolTimeout = Constant.DEFAULT_TIMEOUT;
    /**
     * 连接协议
     * firefox -- webdriver bidi
     * chrome -- cdp
     */
    private Protocol protocolType;

    public BrowserConnectOptions() {
        super();
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

    public Protocol getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(Protocol protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public String toString() {
        return "BrowserConnectOptions{" +
                "acceptInsecureCerts=" + acceptInsecureCerts +
                ", defaultViewport=" + defaultViewport +
                ", slowMo=" + slowMo +
                ", targetFilter=" + targetFilter +
                ", isPageTarget=" + isPageTarget +
                ", protocolTimeout=" + protocolTimeout +
                ", protocolType=" + protocolType +
                '}';
    }
}
