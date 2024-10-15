package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.RemoteObject;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

/**
 * 表示对 JavaScript 对象的引用。可以使用 Page.evaluateHandle() 创建实例。
 * <p>
 * 句柄可防止引用的 JavaScript 对象被垃圾回收，除非句柄特意为 disposed。当 JSHandles 关联的框架被导航离开或父上下文被破坏时，JSHandles 会被自动处置。
 * <p>
 * 句柄可用作任何评估函数（例如 Page.$eval()、Page.evaluate() 和 Page.evaluateHandle()）的参数。它们被解析为其引用的对象。
 */
public class JSHandle {

    private final RemoteObject remoteObject;
    private boolean disposed = false;
    private final IsolatedWorld world;

    JSHandle(IsolatedWorld world, RemoteObject remoteObject) {
        this.world = world;
        this.remoteObject = remoteObject;
    }

    public boolean disposed() {
        return disposed;
    }

    public Realm realm() {
        return this.world;
    }

    public CDPSession client() {
        return this.realm().environment().client();
    }

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

    /* This always returns null but children can define this and return an ElementHandle */
    public ElementHandle asElement() {
        return null;
    }


    public Object evaluate(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluate", pageFunction);
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        if (args != null) {
            argsArray.addAll(args);
        }
        return this.realm().evaluate(pageFunction, argsArray);
    }

    public JSHandle evaluateHandle(String pageFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluateHandle(pageFunction, null);
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pageFunction);
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        if (args != null) {
            argsArray.addAll(args);
        }
        return this.realm().evaluateHandle(pageFunction, argsArray);
    }

    public JSHandle getProperty(String propertyName) throws JsonProcessingException, EvaluateException {
        String pageFunction = "(object, propertyName) => {\n" +
                "      return object[propertyName];\n" +
                "    }";
        return this.evaluateHandle(pageFunction, Collections.singletonList(propertyName));
    }

    public Map<String, JSHandle> getProperties() throws JsonProcessingException {
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.remoteObject.getObjectId());
        params.put("ownProperties", true);
        JsonNode response = this.client().send("Runtime.getProperties", params);
        Map<String, JSHandle> result = new LinkedHashMap<>();
        Iterator<JsonNode> iterator = response.get(Constant.RESULT).iterator();
        while (iterator.hasNext()) {
            JsonNode property = iterator.next();
            if (!property.get("enumerable").asBoolean() || !property.hasNonNull("value")) {
                continue;
            }
            result.put(property.get("name").asText(), this.world.createJSHandle(Constant.OBJECTMAPPER.treeToValue(property.get("value"), RemoteObject.class)));
        }
        return result;
    }

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

    public RemoteObject getRemoteObject() {
        return this.remoteObject;
    }

    public String id() {
        return this.remoteObject.getObjectId();
    }

}
