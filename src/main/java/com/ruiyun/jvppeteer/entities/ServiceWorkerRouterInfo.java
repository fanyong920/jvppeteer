package com.ruiyun.jvppeteer.entities;

public class ServiceWorkerRouterInfo {
    private int ruleIdMatched;
    private String matchedSourceType;
    private String actualSourceType;

    public int getRuleIdMatched() {
        return ruleIdMatched;
    }

    public void setRuleIdMatched(int ruleIdMatched) {
        this.ruleIdMatched = ruleIdMatched;
    }

    public String getActualSourceType() {
        return actualSourceType;
    }

    public void setActualSourceType(String actualSourceType) {
        this.actualSourceType = actualSourceType;
    }

    public String getMatchedSourceType() {
        return matchedSourceType;
    }

    public void setMatchedSourceType(String matchedSourceType) {
        this.matchedSourceType = matchedSourceType;
    }
}
