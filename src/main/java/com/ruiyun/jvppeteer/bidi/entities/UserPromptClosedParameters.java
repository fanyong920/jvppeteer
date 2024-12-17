package com.ruiyun.jvppeteer.bidi.entities;

public class UserPromptClosedParameters {
    private String context;
    private boolean accepted;
    private String userText;
    private UserPromptType type;


    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public UserPromptType getType() {
        return type;
    }

    public void setType(UserPromptType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "UserPromptClosedParameters{" +
                "context='" + context + '\'' +
                ", accepted=" + accepted +
                ", userText='" + userText + '\'' +
                ", type=" + type +
                '}';
    }
}
