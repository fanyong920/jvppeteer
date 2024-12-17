package com.ruiyun.jvppeteer.common;

public class QuerySelector {

    private String updatedSelector;

    private QueryHandler queryHandler;

    private String polling;


    public QuerySelector(String updatedSelector, QueryHandler queryHandler, String polling) {
        super();
        this.updatedSelector = updatedSelector;
        this.queryHandler = queryHandler;
        this.polling = polling;
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

    public String getPolling() {
        return polling;
    }

    public void setPolling(String polling) {
        this.polling = polling;
    }
}
