package com.ruiyun.jvppeteer.bidi.entities;

public class Source {
    private String realm;
    private String context;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Source{" +
                "realm='" + realm + '\'' +
                ", context='" + context + '\'' +
                '}';
    }
}
