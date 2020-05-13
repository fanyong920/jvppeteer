package com.ruiyun.jvppeteer.core.page;

public class QuerySelector {

    private String updatedSelector;

    private String queryHandler;

    public QuerySelector() {
        super();
    }

    public QuerySelector(String updatedSelector, String queryHandler) {
        super();
        this.updatedSelector = updatedSelector;
        this.queryHandler = queryHandler;
    }

    public String getUpdatedSelector() {
        return updatedSelector;
    }

    public void setUpdatedSelector(String updatedSelector) {
        this.updatedSelector = updatedSelector;
    }

    public String getQueryHandler() {
        return queryHandler;
    }

    public void setQueryHandler(String queryHandler) {
        this.queryHandler = queryHandler;
    }
}
