package com.ruiyun.jvppeteer.entities;

public class ClickOptions extends MouseClickOptions {

    private Offset offset;

    /**
     * "left"|"right"|"middle" 三种选择
     *
     * @return offset
     */
    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }
}
