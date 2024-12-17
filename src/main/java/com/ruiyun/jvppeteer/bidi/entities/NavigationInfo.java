package com.ruiyun.jvppeteer.bidi.entities;

import java.util.Date;

public class NavigationInfo {
    private String url;
    private Date timestamp;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
