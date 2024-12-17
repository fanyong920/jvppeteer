package com.ruiyun.jvppeteer.bidi.entities;

public class SerializationOptions {
    private Long maxDomDepth;
    private Long maxObjectDepth;
    private String includeShadowTree;

    public SerializationOptions() {
    }

    public SerializationOptions(Long maxDomDepth, String includeShadowTree, Long maxObjectDepth) {
        this.maxDomDepth = maxDomDepth;
        this.includeShadowTree = includeShadowTree;
        this.maxObjectDepth = maxObjectDepth;
    }

    public Long getMaxDomDepth() {
        return maxDomDepth;
    }

    public void setMaxDomDepth(Long maxDomDepth) {
        this.maxDomDepth = maxDomDepth;
    }

    public Long getMaxObjectDepth() {
        return maxObjectDepth;
    }

    public void setMaxObjectDepth(Long maxObjectDepth) {
        this.maxObjectDepth = maxObjectDepth;
    }

    public String getIncludeShadowTree() {
        return includeShadowTree;
    }

    public void setIncludeShadowTree(String includeShadowTree) {
        this.includeShadowTree = includeShadowTree;
    }
}
