package com.ruiyun.jvppeteer.options;

public class FilterEntry {
    private boolean exclude;
    private String type;

    public FilterEntry() {
    }
    public FilterEntry(boolean exclude, String type) {
        this.exclude = exclude;
        this.type = type;
    }
    public boolean getExclude() {
        return this.exclude;
    }
    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
