package com.ruiyun.jvppeteer.cdp.entities;

public class FilterEntry {

    private Boolean exclude;
    private String type;

    public FilterEntry() {
    }

    public FilterEntry(boolean exclude, String type) {
        this.exclude = exclude;
        this.type = type;
    }

    public Boolean getExclude() {
        return this.exclude;
    }

    public void setExclude(Boolean exclude) {
        this.exclude = exclude;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
