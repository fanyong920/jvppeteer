package com.ruiyun.jvppeteer.protocol.profiler;

import com.ruiyun.jvppeteer.protocol.CSS.Range;

import java.util.List;

public class CoverageEntry {

    private String url;

    private List<Range> ranges;

    private String text;

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
}
