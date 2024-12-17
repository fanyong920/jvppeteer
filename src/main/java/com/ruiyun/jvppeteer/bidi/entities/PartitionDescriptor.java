package com.ruiyun.jvppeteer.bidi.entities;

public class PartitionDescriptor {

    private String type;
    private String context;
    private String userContext;
    private String sourceOrigin;

    public PartitionDescriptor() {
    }

    public PartitionDescriptor(String type, String context, String userContext, String sourceOrigin) {
        this.type = type;
        this.context = context;
        this.userContext = userContext;
        this.sourceOrigin = sourceOrigin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceOrigin() {
        return sourceOrigin;
    }

    public void setSourceOrigin(String sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "PartitionDescriptor{" +
                "type='" + type + '\'' +
                ", context='" + context + '\'' +
                ", userContext='" + userContext + '\'' +
                ", sourceOrigin='" + sourceOrigin + '\'' +
                '}';
    }
}
