package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.cdp.core.CdpFrame;

import java.util.WeakHashMap;

public class PreloadScript {
    private final String id;
    private final String source;
    private final WeakHashMap<CdpFrame, String> frameToId = new WeakHashMap<>();

    public PreloadScript(CdpFrame mainFrame, String id, String source) {
        this.id = id;
        this.source = source;
        this.frameToId.put(mainFrame, id);
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getIdForFrame(CdpFrame frame) {
        return frameToId.get(frame);
    }

    public void setIdForFrame(CdpFrame frame, String identifier) {
        frameToId.put(frame, identifier);
    }
}
