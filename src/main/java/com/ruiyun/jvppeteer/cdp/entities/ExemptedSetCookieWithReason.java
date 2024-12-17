package com.ruiyun.jvppeteer.cdp.entities;

public class ExemptedSetCookieWithReason {
    /**
     * The reason the cookie was exempted.
     */
    private String exemptionReason;
    /**
     * The string representing this individual cookie as it would appear in the header.
     */
    private String cookieLine;
    /**
     * The cookie object representing the cookie.
     */
   private Cookie cookie;

    public String getExemptionReason() {
        return exemptionReason;
    }

    public void setExemptionReason(String exemptionReason) {
        this.exemptionReason = exemptionReason;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public String getCookieLine() {
        return cookieLine;
    }

    public void setCookieLine(String cookieLine) {
        this.cookieLine = cookieLine;
    }
}
