package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CallFunctionOptions {
    private List<LocalValue> arguments;
    private ResultOwnership resultOwnership;
    private SerializationOptions serializationOptions;
    @JsonProperty("this")
    private LocalValue this1;
    private boolean userActivation;

    public List<LocalValue> getArguments() {
        return arguments;
    }

    public void setArguments(List<LocalValue> arguments) {
        this.arguments = arguments;
    }

    public boolean getUserActivation() {
        return userActivation;
    }

    public void setUserActivation(boolean userActivation) {
        this.userActivation = userActivation;
    }

    public LocalValue getThis1() {
        return this1;
    }

    public void setThis1(LocalValue this1) {
        this.this1 = this1;
    }

    public SerializationOptions getSerializationOptions() {
        return serializationOptions;
    }

    public void setSerializationOptions(SerializationOptions serializationOptions) {
        this.serializationOptions = serializationOptions;
    }

    public ResultOwnership getResultOwnership() {
        return resultOwnership;
    }

    public void setResultOwnership(ResultOwnership resultOwnership) {
        this.resultOwnership = resultOwnership;
    }
}
