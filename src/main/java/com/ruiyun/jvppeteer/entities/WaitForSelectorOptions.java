package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.ElementHandle;

public class WaitForSelectorOptions {
    /**
     * 等待所选元素出现在 DOM 中并可见。有关元素可见性的定义，请参阅 ElementHandle.isVisible()。
     */
    private boolean visible;
    /**
     * 等待所选元素在 DOM 中不存在或被隐藏。有关元素不可见性的定义，请参阅 ElementHandle.isHidden()。
     */
    private boolean hidden;

    private Integer timeout;

    private String polling;

    private ElementHandle root;

    public WaitForSelectorOptions() {
        super();
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden) {
        super();
        this.visible = visible;
        this.hidden = hidden;
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, String polling) {
        super();
        this.visible = visible;
        this.hidden = hidden;
        this.polling = polling;
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, Integer timeout, String polling) {
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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getPolling() {
        return polling;
    }

    public void setPolling(String polling) {
        this.polling = polling;
    }

    public ElementHandle getRoot() {
        return root;
    }

    public void setRoot(ElementHandle root) {
        this.root = root;
    }
}
