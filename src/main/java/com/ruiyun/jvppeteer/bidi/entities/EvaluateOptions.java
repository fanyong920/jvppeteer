package com.ruiyun.jvppeteer.bidi.entities;

public class EvaluateOptions {
    private ResultOwnership resultOwnership;
    private SerializationOptions serializationOptions;
    public boolean userActivation;

    public ResultOwnership getResultOwnership() {
        return resultOwnership;
    }

    public void setResultOwnership(ResultOwnership resultOwnership) {
        this.resultOwnership = resultOwnership;
    }

    public SerializationOptions getSerializationOptions() {
        return serializationOptions;
    }

    public void setSerializationOptions(SerializationOptions serializationOptions) {
        this.serializationOptions = serializationOptions;
    }

    public boolean getsUserActivation() {
        return userActivation;
    }

    public void setUserActivation(boolean userActivation) {
        this.userActivation = userActivation;
    }
}
