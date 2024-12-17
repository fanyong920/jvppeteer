package com.ruiyun.jvppeteer.bidi.events;

import java.util.List;

public class ContextCreatedEvent {
    private List<ContextCreatedEvent> children;
    private String clientWindow;
    private String context;
    private  String originalOpener;
    private  String url;
    private  String userContext;
    private  String parent;

    public List<ContextCreatedEvent> getChildren() {
        return children;
    }

    public void setChildren(List<ContextCreatedEvent> children) {
        this.children = children;
    }

    public String getClientWindow() {
        return clientWindow;
    }

    public void setClientWindow(String clientWindow) {
        this.clientWindow = clientWindow;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getOriginalOpener() {
        return originalOpener;
    }

    public void setOriginalOpener(String originalOpener) {
        this.originalOpener = originalOpener;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "ContextCreatedEvent{" +
                "children=" + children +
                ", clientWindow='" + clientWindow + '\'' +
                ", context='" + context + '\'' +
                ", originalOpener='" + originalOpener + '\'' +
                ", url='" + url + '\'' +
                ", userContext='" + userContext + '\'' +
                ", parent='" + parent + '\'' +
                '}';
    }
}
