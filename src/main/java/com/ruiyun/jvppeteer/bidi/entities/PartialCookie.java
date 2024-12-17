package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruiyun.jvppeteer.cdp.entities.CookiePriority;
import com.ruiyun.jvppeteer.cdp.entities.CookieSourceScheme;


import static com.ruiyun.jvppeteer.common.Constant.CDP_SPECIFIC_PREFIX;

public class PartialCookie {
    private String name;
    private BytesValue value;
    private String domain;
    private String path;
    private long size;
    private Boolean httpOnly;
    private Boolean secure;
    private SameSite sameSite;
    private Long expiry;
    @JsonProperty(CDP_SPECIFIC_PREFIX + "sameParty")
    private boolean sameParty;
    @JsonProperty(CDP_SPECIFIC_PREFIX + "sourceScheme")
    private CookieSourceScheme sourceScheme;
    @JsonProperty(CDP_SPECIFIC_PREFIX + "priority")
    private CookiePriority priority;
    @JsonProperty(CDP_SPECIFIC_PREFIX + "url")
    private String url;

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public SameSite getSameSite() {
        return sameSite;
    }

    public void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public BytesValue getValue() {
        return value;
    }

    public void setValue(BytesValue value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CookiePriority getPriority() {
        return priority;
    }

    public void setPriority(CookiePriority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "PartialCookie{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", httpOnly=" + httpOnly +
                ", secure=" + secure +
                ", sameSite=" + sameSite +
                ", expiry=" + expiry +
                ", sameParty=" + sameParty +
                ", sourceScheme=" + sourceScheme +
                ", priority=" + priority +
                ", url='" + url + '\'' +
                '}';
    }
}
