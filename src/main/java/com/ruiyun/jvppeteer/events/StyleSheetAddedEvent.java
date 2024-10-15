package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.CSSStyleSheetHeader;

public class StyleSheetAddedEvent {
    /**
     * Added stylesheet metainfo.
     */
    private CSSStyleSheetHeader header;

    public CSSStyleSheetHeader getHeader() {
        return header;
    }

    public void setHeader(CSSStyleSheetHeader header) {
        this.header = header;
    }
}
