package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPromptHandler {
    private UserPromptHandlerType alert;
    private UserPromptHandlerType beforeUnload;
    private UserPromptHandlerType confirm;
    @JsonProperty("default")
    private UserPromptHandlerType default1;
    private UserPromptHandlerType prompt;

    public UserPromptHandlerType getAlert() {
        return alert;
    }

    public void setAlert(UserPromptHandlerType alert) {
        this.alert = alert;
    }

    public UserPromptHandlerType getBeforeUnload() {
        return beforeUnload;
    }

    public void setBeforeUnload(UserPromptHandlerType beforeUnload) {
        this.beforeUnload = beforeUnload;
    }

    public UserPromptHandlerType getConfirm() {
        return confirm;
    }

    public void setConfirm(UserPromptHandlerType confirm) {
        this.confirm = confirm;
    }

    public UserPromptHandlerType getDefault1() {
        return default1;
    }

    public void setDefault1(UserPromptHandlerType default1) {
        this.default1 = default1;
    }

    public UserPromptHandlerType getPrompt() {
        return prompt;
    }

    public void setPrompt(UserPromptHandlerType prompt) {
        this.prompt = prompt;
    }
}
