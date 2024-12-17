package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class TrustTokenParams {
    private String operation;
    private String refreshPolicy;
    private List<String> issuers;

    public String getRefreshPolicy() {
        return refreshPolicy;
    }

    public void setRefreshPolicy(String refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }

    public List<String> getIssuers() {
        return issuers;
    }

    public void setIssuers(List<String> issuers) {
        this.issuers = issuers;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
