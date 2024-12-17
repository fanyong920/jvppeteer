package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;

/**
 * 表示对 JavaScript 对象的引用。可以使用 Page.evaluateHandle() 创建实例。
 * <p>
 * 句柄可防止引用的 JavaScript 对象被垃圾回收，除非句柄特意为 disposed。当 JSHandles 关联的框架被导航离开或父上下文被破坏时，JSHandles 会被自动处置。
 * <p>
 * 句柄可用作任何评估函数（例如 Page.$eval()、Page.evaluate() 和 Page.evaluateHandle()）的参数。它们被解析为其引用的对象。
 */
public class CdpJSHandle extends JSHandle {

    private final RemoteObject remoteObject;
    private boolean disposed = false;
    private final IsolatedWorld world;

    CdpJSHandle(IsolatedWorld world, RemoteObject remoteObject) {
        super();
        this.world = world;
        this.remoteObject = remoteObject;
    }

    @Override
    public boolean disposed() {
        return disposed;
    }

    @Override
    public Realm realm() {
        return this.world;
    }

    public CDPSession client() {
        return this.realm().environment().client();
    }

    @Override
    public Object jsonValue() throws JsonProcessingException, EvaluateException {
        if (StringUtil.isNotEmpty(this.remoteObject.getObjectId())) {
            Object value = this.evaluate("object => {\n" +
                    "      return object;\n" +
                    "    }", new ArrayList<>());
            if (value == null) {
                throw new JvppeteerException("Could not serialize referenced object");
            }
            return value;
        }
        return Helper.valueFromRemoteObject(this.remoteObject);
    }

    @Override
    /* This always returns null but children can define this and return an ElementHandle */
    public ElementHandle asElement() {
        return null;
    }


    @Override
    public void dispose() {
        if (this.disposed)
            return;
        this.disposed = true;
        Helper.releaseObject(this.client(), this.remoteObject);
    }

    public String toString() {
        if (StringUtil.isNotEmpty(this.remoteObject.getObjectId())) {
            String type = StringUtil.isNotEmpty(this.remoteObject.getSubtype()) ? this.remoteObject.getSubtype() : this.remoteObject.getType();
            return "JSHandle@" + type;
        }
        return "JSHandle:" + Helper.valueFromRemoteObject(this.remoteObject);
    }

    @Override
    public RemoteObject remoteObject() {
        return this.remoteObject;
    }

    @Override
    public String id() {
        return this.remoteObject.getObjectId();
    }

}
