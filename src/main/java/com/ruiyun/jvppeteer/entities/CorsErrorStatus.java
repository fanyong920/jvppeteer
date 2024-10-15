package com.ruiyun.jvppeteer.entities;

public class CorsErrorStatus {
    private String corsError;
    private String failedParameter;

    public String getCorsError() {
        return corsError;
    }

    public void setCorsError(String corsError) {
        this.corsError = corsError;
    }

    public String getFailedParameter() {
        return failedParameter;
    }

    public void setFailedParameter(String failedParameter) {
        this.failedParameter = failedParameter;
    }
}
