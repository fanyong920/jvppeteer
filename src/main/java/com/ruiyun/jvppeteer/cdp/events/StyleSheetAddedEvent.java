package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.CSSStyleSheetHeader;

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
