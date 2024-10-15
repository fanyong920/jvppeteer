package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class CoverageEntry {

    private String url;

    private List<Range> ranges;

    private String text;

    public CoverageEntry() {
        super();
    }

    public CoverageEntry(String url, List<Range> ranges, String text) {
        super();
        this.url = url;
        this.ranges = ranges;
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Range> getRangse() {
        return ranges;
    }

    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "CoverageEntry{" +
                "url='" + url + '\'' +
                ", ranges=" + ranges +
                ", text='" + text + '\'' +
                '}';
    }
}
