package com.ruiyun.jvppeteer.core.page;

public class QuerySelector {

    private String updatedSelector;

    private QueryHandler queryHandler;

    public QuerySelector() {
        super();
    }

    public QuerySelector(String updatedSelector, QueryHandler queryHandler) {
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

    public QueryHandler getQueryHandler() {
        return queryHandler;
    }

    public void setQueryHandler(QueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }
}
