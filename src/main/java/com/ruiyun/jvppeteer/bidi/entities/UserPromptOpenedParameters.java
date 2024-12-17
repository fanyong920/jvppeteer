package com.ruiyun.jvppeteer.bidi.entities;

public class UserPromptOpenedParameters {
    private String context;
    private UserPromptHandlerType handler;
    private String message;
    private UserPromptType type;
    private String defaultValue;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public UserPromptHandlerType getHandler() {
        return handler;
    }

    public void setHandler(UserPromptHandlerType handler) {
        this.handler = handler;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserPromptType getType() {
        return type;
    }

    public void setType(UserPromptType type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "UserPromptOpenedParameters{" +
                "context='" + context + '\'' +
                ", handler=" + handler +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
