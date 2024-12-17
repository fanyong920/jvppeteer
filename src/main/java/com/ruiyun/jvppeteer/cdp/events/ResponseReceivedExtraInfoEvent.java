package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.BlockedSetCookieWithReason;
import com.ruiyun.jvppeteer.cdp.entities.ExemptedSetCookieWithReason;
import java.util.List;
import java.util.Map;

public class ResponseReceivedExtraInfoEvent {
    private String requestId;
    private List<BlockedSetCookieWithReason> blockedCookies;
    private Map<String, String> headers;
    private String resourceIPAddressSpace;
    private int statusCode;
    private String headersText;
    private boolean cookiePartitionKeyOpaque;
    private List<ExemptedSetCookieWithReason> exemptedCookies;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<ExemptedSetCookieWithReason> getExemptedCookies() {
        return exemptedCookies;
    }

    public void setExemptedCookies(List<ExemptedSetCookieWithReason> exemptedCookies) {
        this.exemptedCookies = exemptedCookies;
    }

    public boolean getCookiePartitionKeyOpaque() {
        return cookiePartitionKeyOpaque;
    }

    public void setCookiePartitionKeyOpaque(boolean cookiePartitionKeyOpaque) {
        this.cookiePartitionKeyOpaque = cookiePartitionKeyOpaque;
    }

    public String getHeadersText() {
        return headersText;
    }

    public void setHeadersText(String headersText) {
        this.headersText = headersText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResourceIPAddressSpace() {
        return resourceIPAddressSpace;
    }

    public void setResourceIPAddressSpace(String resourceIPAddressSpace) {
        this.resourceIPAddressSpace = resourceIPAddressSpace;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<BlockedSetCookieWithReason> getBlockedCookies() {
        return blockedCookies;
    }

    public void setBlockedCookies(List<BlockedSetCookieWithReason> blockedCookies) {
        this.blockedCookies = blockedCookies;
    }
}
