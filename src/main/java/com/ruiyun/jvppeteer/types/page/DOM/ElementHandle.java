package com.ruiyun.jvppeteer.types.page.DOM;

import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.types.page.context.ExecutionContext;
import com.ruiyun.jvppeteer.types.page.js.JSHandle;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.List;

public class ElementHandle extends JSHandle {

    public ElementHandle(ExecutionContext context, CDPSession client, RemoteObject remoteObject) {
        super(context, client, remoteObject);
    }

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

    public List<ElementHandle> $$(String selector) {
        return null;
    }

    public void click(ClickOptions options) {
    }

    public void focus() {
    }

    public void hover() {
    }

    public List<String> select(List<String> values) {
        return null;
    }

    public void tap() {
    }

    public void type(String text, int delay) {
    }

    public void uploadFile(List<String> filePaths) {
    }

    public RemoteObject getRemoteObject() {
        return null;
    }
}
