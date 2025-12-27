package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;
/**
 * A cookie associated with the request which may or may not be sent with it.
 * Includes the cookies itself and reasons for blocking or exemption.
 */
public class AssociatedCookie {
    /**
     * The cookie object representing the cookie which was not sent.
     */
    private Cookie cookie;
    /**
     * The reason(s) the cookie was blocked. If empty means the cookie is included.
     */
    private List<CookieBlockedReason> blockedReasons;
    /**
     * The reason the cookie should have been blocked by 3PCD but is exempted. A cookie could
     * only have at most one exemption reason.
     */
    private CookieExemptionReason exemptionReason;

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public List<CookieBlockedReason> getBlockedReasons() {
        return blockedReasons;
    }

    public void setBlockedReasons(List<CookieBlockedReason> blockedReasons) {
        this.blockedReasons = blockedReasons;
    }

    public CookieExemptionReason getExemptionReason() {
        return exemptionReason;
    }

    public void setExemptionReason(CookieExemptionReason exemptionReason) {
        this.exemptionReason = exemptionReason;
    }

    @Override
    public String toString() {
        return "AssociatedCookie{" +
                "cookie=" + cookie +
                ", blockedReasons=" + blockedReasons +
                ", exemptionReason=" + exemptionReason +
                '}';
    }
}
