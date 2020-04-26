package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.options.ClickOptions;
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

    public void click(ClickOptions options) {
    }

    public void focus() {
    }

    public void hover() {
    }

    public List<String> select(String[] values) {
        return null;
    }

    public void tap() {
    }

    public void type(String text, int delay) {
    }
}
