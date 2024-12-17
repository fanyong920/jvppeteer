package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.api.core.Frame;

@FunctionalInterface
public interface FrameProvider {
    Frame frame(String id);
}
