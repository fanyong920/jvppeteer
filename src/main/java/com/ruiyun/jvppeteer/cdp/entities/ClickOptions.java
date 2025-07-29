package com.ruiyun.jvppeteer.cdp.entities;

public class ClickOptions extends MouseClickOptions {

    private Offset offset;
    /**
     * An experimental debugging feature. If true, inserts an element into the
     * page to highlight the click location for 10 seconds. Might not work on all
     * pages and does not persist across navigations.
     */
    private boolean debugHighlight;
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

    public boolean getDebugHighlight() {
        return debugHighlight;
    }

    public void setDebugHighlight(boolean debugHighlight) {
        this.debugHighlight = debugHighlight;
    }
}
