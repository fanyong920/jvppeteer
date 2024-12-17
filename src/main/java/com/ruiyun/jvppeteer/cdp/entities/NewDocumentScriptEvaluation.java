package com.ruiyun.jvppeteer.cdp.entities;

public class NewDocumentScriptEvaluation {
    private String identifier;

    public NewDocumentScriptEvaluation() {
    }

    public NewDocumentScriptEvaluation(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
