package com.ruiyun.jvppeteer.cdp.entities;

public class FocusState extends ActiveProperty {
    private boolean enabled;

    public FocusState(boolean active, boolean enabled) {
        super(active);
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
