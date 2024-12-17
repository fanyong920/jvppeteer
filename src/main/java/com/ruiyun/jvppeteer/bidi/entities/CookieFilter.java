package com.ruiyun.jvppeteer.bidi.entities;

public class CookieFilter {
    private String name;
    private String value;
    private String domain;
    private String path;
    private long size;
    private boolean httpOnly;
    private boolean secure;
    private SameSite sameSite;
    private long expiry;

    public CookieFilter() {
    }

    public CookieFilter(String name, long expiry, SameSite sameSite, boolean secure, boolean httpOnly, long size, String path, String domain, String value) {
        this.name = name;
        this.expiry = expiry;
        this.sameSite = sameSite;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.size = size;
        this.path = path;
        this.domain = domain;
        this.value = value;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
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

    public SameSite getSameSite() {
        return sameSite;
    }

    public void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    @Override
    public String toString() {
        return "CookieFilter{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", httpOnly=" + httpOnly +
                ", secure=" + secure +
                ", sameSite=" + sameSite +
                ", expiry=" + expiry +
                '}';
    }
}
