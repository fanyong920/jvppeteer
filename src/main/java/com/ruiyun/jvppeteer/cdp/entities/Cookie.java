package com.ruiyun.jvppeteer.cdp.entities;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * Cookie object
 */
public class Cookie extends CookieData {
    /**
     * Cookie name.
     */
    private String name;
    /**
     * Cookie value.
     */
    private String value;
    /**
     * Cookie domain.
     */
    private String domain;
    /**
     * Cookie path.
     */
    private String path;
    /**
     * Cookie expiration date as the number of seconds since the UNIX epoch. Set to `-1` for
     * session cookies
     */
    private long expires;
    /**
     * Cookie size.
     */
    private int size;
    /**
     * True if cookie is http-only.
     */
    private boolean httpOnly;
    /**
     * True if cookie is secure.
     */
    private boolean secure;
    /**
     * True in case of session cookie.
     */
    private boolean session;
    /**
     * Cookie SameSite type.
     * "Strict"|"Lax"|"None";
     */
    private CookieSameSite sameSite;
    /**
     * Cookie Priority
     * "Low"|"Medium"|"High";
     */
    private CookiePriority priority;

    /**
     * Cookie source scheme type.
     */
    private CookieSourceScheme sourceScheme;
    /**
     * Cookie partition key.用JsonNode 兼容String 和 拥有{@link CookiePartitionKey}对象属性 这两种情况
     */
    private JsonNode partitionKey;
    /**
     * True if cookie partition key is opaque.
     */
    private boolean partitionKeyOpaque;

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

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean getSession() {
        return session;
    }

    public void setSession(boolean session) {
        this.session = session;
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

    public boolean getPartitionKeyOpaque() {
        return partitionKeyOpaque;
    }

    public void setPartitionKeyOpaque(boolean partitionKeyOpaque) {
        this.partitionKeyOpaque = partitionKeyOpaque;
    }

    @Override
    public String toString() {
        return "Cookie{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", expires=" + expires +
                ", size=" + size +
                ", httpOnly=" + httpOnly +
                ", secure=" + secure +
                ", session=" + session +
                ", sameSite='" + sameSite + '\'' +
                ", priority='" + priority + '\'' +
                ", sourceScheme='" + sourceScheme + '\'' +
                ", partitionKey=" + partitionKey +
                ", partitionKeyOpaque=" + partitionKeyOpaque +
                '}';
    }
}
