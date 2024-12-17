package com.ruiyun.jvppeteer.bidi.events;

public class NavigationInfoEvent {

    private String context;
    private String navigation;
    private long timestamp;
    private String url;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "NavigationInfoEvent{" +
                "context='" + context + '\'' +
                ", navigation='" + navigation + '\'' +
                ", timestamp=" + timestamp +
                ", url='" + url + '\'' +
                '}';
    }
}
