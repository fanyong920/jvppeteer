package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;

import java.util.List;

public class ElementHandle extends JSHandle {
    public void dispose() {
    }

    public ElementHandle $(String selector) {
        return null;
    }

    public List<ElementHandle> $x(String expression) {
        return null;
    }

    public Object $eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) {
        return null;
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) {
        return null;
    }

    public ElementHandle $$(String selector) {
        return null;
    }
}
