package com.ruiyun.jvppeteer.cdp.entities;

public class JavascriptEnabledState extends ActiveProperty {

    public boolean javaScriptEnabled;

    public JavascriptEnabledState(boolean active, boolean javaScriptEnabled) {
        super(active);
        this.javaScriptEnabled = javaScriptEnabled;
    }

    public boolean getJavaScriptEnabled() {
        return javaScriptEnabled;
    }

    public void setJavaScriptEnabled(boolean javaScriptEnabled) {
        this.javaScriptEnabled = javaScriptEnabled;
    }

    @Override
    public String toString() {
        return "JavascriptEnabledState{" +
                "javaScriptEnabled=" + javaScriptEnabled +
                ", active=" + active +
                '}';
    }
}
