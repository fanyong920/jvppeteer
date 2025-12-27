package com.ruiyun.jvppeteer.cdp.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestWillBeSentExtraInfoEvent {
    /**
     * Request identifier. Used to match this information to an existing requestWillBeSent event.
     */
    private String requestId;
    /**
     * A list of cookies potentially associated to the requested URL. This includes both cookies sent with
     * the request and the ones not sent; the latter are distinguished by having blockedReasons field set.
     */
    private List<AssociatedCookie> associatedCookies;
    /**
     * Raw request headers as they will be sent over the wire.
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * Connection timing information for the request.
     *
     */
    private ConnectTiming connectTiming;
    /**
     * The client security state set for the request.
     */
    private ClientSecurityState clientSecurityState;
    /**
     * Whether the site has partitioned cookies stored in a partition different than the current one.
     */
    private boolean siteHasCookieInOtherPartition;
    /**
     * The network conditions id if this request was affected by network conditions configured via
     * emulateNetworkConditionsByRule.
     */
    private String appliedNetworkConditionsId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAppliedNetworkConditionsId() {
        return appliedNetworkConditionsId;
    }

    public void setAppliedNetworkConditionsId(String appliedNetworkConditionsId) {
        this.appliedNetworkConditionsId = appliedNetworkConditionsId;
    }

    public boolean getSiteHasCookieInOtherPartition() {
        return siteHasCookieInOtherPartition;
    }

    public void setSiteHasCookieInOtherPartition(boolean siteHasCookieInOtherPartition) {
        this.siteHasCookieInOtherPartition = siteHasCookieInOtherPartition;
    }

    public ClientSecurityState getClientSecurityState() {
        return clientSecurityState;
    }

    public void setClientSecurityState(ClientSecurityState clientSecurityState) {
        this.clientSecurityState = clientSecurityState;
    }

    public ConnectTiming getConnectTiming() {
        return connectTiming;
    }

    public void setConnectTiming(ConnectTiming connectTiming) {
        this.connectTiming = connectTiming;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<AssociatedCookie> getAssociatedCookies() {
        return associatedCookies;
    }

    public void setAssociatedCookies(List<AssociatedCookie> associatedCookies) {
        this.associatedCookies = associatedCookies;
    }

    @Override
    public String toString() {
        return "RequestWillBeSentExtraInfoEvent{" +
                "requestId='" + requestId + '\'' +
                ", associatedCookies=" + associatedCookies +
                ", headers=" + headers +
                ", connectTiming=" + connectTiming +
                ", clientSecurityState=" + clientSecurityState +
                ", siteHasCookieInOtherPartition=" + siteHasCookieInOtherPartition +
                ", appliedNetworkConditionsId='" + appliedNetworkConditionsId + '\'' +
                '}';
    }
}
