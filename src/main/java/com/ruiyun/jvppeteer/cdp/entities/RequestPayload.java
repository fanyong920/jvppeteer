package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;
import java.util.Map;

/**
 * HTTP request data.
 */
public class RequestPayload {
    /**
     * Request URL (without fragment).
     */
    private String url;
    /**
     * Fragment of the requested URL starting with hash, if present.
     */
    private String urlFragment;
    /**
     * HTTP request method.
     */
    private String method;
    /**
     * HTTP request headers.
     */
    private Map<String,String> headers;
    /**
     * HTTP POST request data.
     */
    private String postData;
    /**
     * True when the request has POST data. Note that postData might still be omitted when this flag is true when the data is too long.
     */
    private boolean hasPostData;
    /**
     * Request body elements (post data broken into individual entries).
     */
    private List<PostDataEntry> postDataEntries;
    /**
     * The mixed content type of the request.
     * "blockable"|"optionally-blockable"|"none"
     */
    private String mixedContentType;
    /**
     * Priority of the resource request at the time request is sent.
     * "VeryLow"|"Low"|"Medium"|"High"|"VeryHigh"
     */
    private String initialPriority;
    /**
     * The referrer policy of the request, as defined in https://www.w3.org/TR/referrer-policy/
     * "unsafe-url"|"no-referrer-when-downgrade"|"no-referrer"|"origin"|"origin-when-cross-origin"|"same-origin"|"strict-origin"|"strict-origin-when-cross-origin";
     */
    private String referrerPolicy;
    /**
     * Whether is loaded via link preload.
     */
    private boolean isLinkPreload;
    /**
     * Set for requests when the TrustToken API is used. Contains the parameters
     * passed by the developer (e.g. via "fetch") as understood by the backend.
     */
    private TrustTokenParams trustTokenParams;
    /**
     * True if this resource request is considered to be the 'same site' as the
     * request corresponding to the main frame.
     */
    private boolean isSameSite;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlFragment() {
        return urlFragment;
    }

    public void setUrlFragment(String urlFragment) {
        this.urlFragment = urlFragment;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public boolean getHasPostData() {
        return hasPostData;
    }

    public void setHasPostData(boolean hasPostData) {
        this.hasPostData = hasPostData;
    }

    public String getMixedContentType() {
        return mixedContentType;
    }

    public void setMixedContentType(String mixedContentType) {
        this.mixedContentType = mixedContentType;
    }

    public String getInitialPriority() {
        return initialPriority;
    }

    public void setInitialPriority(String initialPriority) {
        this.initialPriority = initialPriority;
    }

    public String getReferrerPolicy() {
        return referrerPolicy;
    }

    public void setReferrerPolicy(String referrerPolicy) {
        this.referrerPolicy = referrerPolicy;
    }

    public boolean getIsLinkPreload() {
        return isLinkPreload;
    }

    public void setIsLinkPreload(boolean linkPreload) {
        isLinkPreload = linkPreload;
    }

    public boolean getIsSameSite() {
        return isSameSite;
    }

    public void setIsSameSite(boolean sameSite) {
        isSameSite = sameSite;
    }

    public TrustTokenParams getTrustTokenParams() {
        return trustTokenParams;
    }

    public void setTrustTokenParams(TrustTokenParams trustTokenParams) {
        this.trustTokenParams = trustTokenParams;
    }

    public List<PostDataEntry> getPostDataEntries() {
        return postDataEntries;
    }

    public void setPostDataEntries(List<PostDataEntry> postDataEntries) {
        this.postDataEntries = postDataEntries;
    }
}
