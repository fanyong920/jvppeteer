package com.ruiyun.jvppeteer.options;

public class WaitForSelectorOptions {

    private boolean visible;

    private boolean hidden;

    private int timeout;

    private String polling;

    public WaitForSelectorOptions() {
        super();
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, String polling) {
        super();
        this.visible = visible;
        this.hidden = hidden;
        this.polling = polling;
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, int timeout, String polling) {
        super();
        this.visible = visible;
        this.hidden = hidden;
        this.timeout = timeout;
        this.polling = polling;
    }



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
