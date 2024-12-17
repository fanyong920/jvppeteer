package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class AddInterceptOptions {
    private List<String> phases;
    private List<UrlPattern> urlPatterns;

    public AddInterceptOptions(List<String> phases) {
        this.phases = phases;
    }

    public List<String> getPhases() {
        return phases;
    }

    public void setPhases(List<String> phases) {
        this.phases = phases;
    }

    public List<UrlPattern> getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(List<UrlPattern> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }
}
