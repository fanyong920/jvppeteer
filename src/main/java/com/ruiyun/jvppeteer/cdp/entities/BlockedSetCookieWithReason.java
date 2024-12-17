package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class BlockedSetCookieWithReason {
    private List<String> blockedReasons;
    private String cookieLine;
    private Cookie cookie;

    public BlockedSetCookieWithReason() {
    }

    public List<String> getBlockedReasons() {
        return this.blockedReasons;
    }

    public void setBlockedReasons(List<String> blockedReasons) {
        this.blockedReasons = blockedReasons;
    }

    public String getCookieLine() {
        return this.cookieLine;
    }

    public void setCookieLine(String cookieLine) {
        this.cookieLine = cookieLine;
    }

    public Cookie getCookie() {
        return this.cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }
}
