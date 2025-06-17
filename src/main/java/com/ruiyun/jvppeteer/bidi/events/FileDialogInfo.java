package com.ruiyun.jvppeteer.bidi.events;

import com.ruiyun.jvppeteer.bidi.core.BrowsingContext;
import com.ruiyun.jvppeteer.bidi.entities.SharedReference;

public class FileDialogInfo {
    private String context;
    private SharedReference element;
    private boolean multiple;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public SharedReference getElement() {
        return element;
    }

    public void setElement(SharedReference element) {
        this.element = element;
    }

    public boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
}
