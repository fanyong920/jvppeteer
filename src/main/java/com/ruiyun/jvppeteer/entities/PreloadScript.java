package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.Frame;

import java.util.WeakHashMap;

public class PreloadScript {
    private final String id;
    private final String source;
    private final WeakHashMap<Frame, String> frameToId = new WeakHashMap<>();

    public PreloadScript(Frame mainFrame, String id, String source) {
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

    public String getIdForFrame(Frame frame) {
        return frameToId.get(frame);
    }

    public void setIdForFrame(Frame frame, String identifier) {
        frameToId.put(frame, identifier);
    }
}
