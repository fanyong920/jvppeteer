package com.ruiyun.jvppeteer.protocol.CSS;

public class StyleSheetAddedPayload {
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
