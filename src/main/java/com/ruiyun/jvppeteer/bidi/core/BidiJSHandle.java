package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.Collections;

public class BidiJSHandle extends JSHandle {
    private final RemoteValue remoteValue;
    private final BidiRealm realm;
    private boolean disposed = false;

    public BidiJSHandle(RemoteValue value, BidiRealm realm) {
        super();
        this.remoteValue = value;
        this.realm = realm;
    }

    public static BidiJSHandle from(RemoteValue value, BidiRealm realm) {
        return new BidiJSHandle(value, realm);
    }

    @Override
    public boolean disposed() {
        return this.disposed;
    }

    @Override
    public Object jsonValue() throws JsonProcessingException, EvaluateException {
        return this.evaluate("value => {\n" +
                "      return value;\n" +
                "    }");
    }

    @Override
    public ElementHandle asElement() {
        return null;
    }

    @Override
    public Realm realm() {
        return this.realm;
    }

    @Override
    public void dispose() {
        if (disposed) return;
        this.disposed = true;
        this.realm.destroyHandles(Collections.singletonList(this));
    }

    public boolean isPrimitiveValue() {
        switch (this.remoteValue.getType()) {
            case "string":
            case "number":
            case "bigint":
            case "boolean":
            case "undefined":
            case "null":
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        if (this.isPrimitiveValue()) {
            return "JSHandle:" + BidiDeserializer.deserialize(this.remoteValue);
        }

        return "JSHandle@" + this.remoteValue.getType();
    }

    @Override
    public String id() {
        if (StringUtil.isNotEmpty(this.remoteValue.getHandle())) {
            return this.remoteValue.getHandle();
        }
        return null;
    }

    public RemoteValue remoteValue() {
        return this.remoteValue;
    }

    @Override
    public RemoteObject remoteObject() {
        throw new UnsupportedOperationException("Not available in WebDriver BiDi");
    }
}
