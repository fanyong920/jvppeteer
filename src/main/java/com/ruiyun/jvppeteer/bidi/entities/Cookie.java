package com.ruiyun.jvppeteer.bidi.entities;

public class Cookie {
    /**
     * Cookie name.
     */
    private String name;
    /**
     * Cookie value.
     */
    private Value value;
    /**
     * Cookie domain.
     */
    private String domain;
    /**
     * Cookie path.
     */
    private String path;
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
     * Cookie expiration date as the number of seconds since the UNIX epoch.
     */
    private long expiry;
    /**
     * Cookie SameSite type.
     * "Strict"|"Lax"|"None";
     */
    private SameSite sameSite;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
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

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public SameSite getSameSite() {
        return sameSite;
    }

    public void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }
}
