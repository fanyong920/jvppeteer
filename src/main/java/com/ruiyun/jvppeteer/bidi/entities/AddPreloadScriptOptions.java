package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class AddPreloadScriptOptions {
    private List<LocalValue> arguments;
    private List<String> contexts;
    private String sandbox;

    public List<LocalValue> getArguments() {
        return arguments;
    }

    public void setArguments(List<LocalValue> arguments) {
        this.arguments = arguments;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public void setContexts(List<String> contexts) {
        this.contexts = contexts;
    }

    public String getSandbox() {
        return sandbox;
    }

    public void setSandbox(String sandbox) {
        this.sandbox = sandbox;
    }
}
