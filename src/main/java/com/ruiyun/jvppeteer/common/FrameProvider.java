package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.core.Frame;

@FunctionalInterface
public interface FrameProvider {
    Frame frame(String id);
}
