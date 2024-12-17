package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;

public abstract class CDPSession extends EventEmitter<ConnectionEvents> {

    public abstract Connection getConnection();

    public CDPSession parentSession() {
        return null;
    }

    public abstract String id();

    public abstract void detach();

    public abstract void onClosed();

    public JsonNode send(String method) {
        return this.send(method, null);
    }

    public JsonNode send(String method, Object params) {
        return this.send(method, params, null, true);
    }

    public abstract JsonNode send(String method, Object params, Integer timeout, boolean isBlocking);
}
