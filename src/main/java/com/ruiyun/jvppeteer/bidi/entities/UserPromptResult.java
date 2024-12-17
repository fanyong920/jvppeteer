package com.ruiyun.jvppeteer.bidi.entities;

public class UserPromptResult {
    private boolean accepted;
    private UserPromptType type;
    private String userText;

    public boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public UserPromptType getType() {
        return type;
    }

    public void setType(UserPromptType type) {
        this.type = type;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }
}
