package com.ruiyun.jvppeteer.bidi.entities;

public class CreateBrowsingContextOptions {
    /**
     * id
     */
    private String referenceContext;
    private Boolean background;
    /**
     * id
     */
    private String userContext;

    public String getReferenceContext() {
        return referenceContext;
    }

    public void setReferenceContext(String referenceContext) {
        this.referenceContext = referenceContext;
    }

    public Boolean getBackground() {
        return background;
    }

    public void setBackground(Boolean background) {
        this.background = background;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }
}
