package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class RealmInfo {
    private RealmType type;
    private String context;
    private String sandbox;
    private List<String> owners;
    private String realm;
    private String origin;

    public RealmType getType() {
        return type;
    }

    public void setType(RealmType type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSandbox() {
        return sandbox;
    }

    public void setSandbox(String sandbox) {
        this.sandbox = sandbox;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
