package com.ruiyun.jvppeteer.bidi.entities;

public class CreateBrowsingContextOptions {
    /**
     * id
     */
    private String referenceContext;
    private boolean background;
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

    public boolean getBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }
}
