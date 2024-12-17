package com.ruiyun.jvppeteer.bidi.entities;


public class EvaluateResult {
    private String type;
    private ExceptionDetails exceptionDetails;
    private String realm;
    private RemoteValue result;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    public void setExceptionDetails(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public RemoteValue getResult() {
        return result;
    }

    public void setResult(RemoteValue result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "EvaluateResult{" +
                "type='" + type + '\'' +
                ", exceptionDetails=" + exceptionDetails +
                ", realm='" + realm + '\'' +
                ", result=" + result +
                '}';
    }
}
