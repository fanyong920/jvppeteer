package com.ruiyun.jvppeteer.options;

public class WaitForOptions {

    private boolean visible;

    private boolean hidden;

    private int timeout;

    private String polling;

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPolling() {
        return polling;
    }

    public void setPolling(String polling) {
        this.polling = polling;
    }
}
