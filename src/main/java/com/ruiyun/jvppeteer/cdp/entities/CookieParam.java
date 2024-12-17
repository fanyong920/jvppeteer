package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.databind.JsonNode;

public class CookieParam {
    /**
     * Cookie name.
     */
    private String name;
    /**
     * Cookie value.
     */
    private String value;
    /**
     * The request-URI to associate with the setting of the cookie. This value can affect
     * the default domain, path, and source scheme values of the created cookie.
     */
    private String url;
    /**
     * Cookie domain.
     */
    private String domain;
    /**
     * Cookie path.
     */
    private String path;
    /**
     * True if cookie is secure.
     */
    private Boolean secure;
    /**
     * True if cookie is http-only.
     */
    private Boolean httpOnly;
    /**
     * Cookie SameSite type.
     */
    private CookieSameSite sameSite;
    /**
     * Cookie expiration date, session cookie if not set
     */
    private Long expires;
    /**
     * Cookie Priority
     * "Low"|"Medium"|"High";
     */
    private CookiePriority priority;

    /**
     * True if cookie is SameParty.
     */
    private boolean sameParty;
    /**
     * Cookie source scheme type.
     */
    private CookieSourceScheme sourceScheme;
    /**
     * Cookie partition key. 用JsonNode 兼容String 和 拥有{@link CookiePartitionKey}对象属性 这两种情况
     */
    private JsonNode partitionKey;

    public CookieParam() {
    }

    public CookieParam(String name, String value, String url, String domain, String path, Boolean secure, Boolean httpOnly, CookieSameSite sameSite, Long expires, CookiePriority priority, boolean sameParty, CookieSourceScheme sourceScheme, JsonNode partitionKey) {
        this.name = name;
        this.value = value;
        this.url = url;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
        this.expires = expires;
        this.priority = priority;
        this.sameParty = sameParty;
        this.sourceScheme = sourceScheme;
        this.partitionKey = partitionKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public CookieSameSite getSameSite() {
        return sameSite;
    }

    public void setSameSite(CookieSameSite sameSite) {
        this.sameSite = sameSite;
    }


    public CookiePriority getPriority() {
        return priority;
    }

    public void setPriority(CookiePriority priority) {
        this.priority = priority;
    }

    public boolean getSameParty() {
        return sameParty;
    }

    public void setSameParty(boolean sameParty) {
        this.sameParty = sameParty;
    }

    public CookieSourceScheme getSourceScheme() {
        return sourceScheme;
    }

    public void setSourceScheme(CookieSourceScheme sourceScheme) {
        this.sourceScheme = sourceScheme;
    }

    public JsonNode getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(JsonNode partitionKey) {
        this.partitionKey = partitionKey;
    }
}
