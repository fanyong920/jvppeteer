package com.ruiyun.jvppeteer.bidi.entities;

public class HandleUserPromptOptions {
    private String userText;
    private boolean accept;

    public HandleUserPromptOptions() {
    }

    public HandleUserPromptOptions(String userText, boolean accept) {
        this.userText = userText;
        this.accept = accept;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public boolean getAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    @Override
    public String toString() {
        return "HandleUserPromptOptions{" +
                "userText='" + userText + '\'' +
                ", accept=" + accept +
                '}';
    }
}
