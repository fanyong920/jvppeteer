package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSHandle {

    private ExecutionContext context;

    private CDPSession client;

    private RemoteObject remoteObject;

    private boolean disposed = false;

    public JSHandle(ExecutionContext context, CDPSession client, RemoteObject remoteObject) {
        this.context = context;
        this.client = client;
        this.remoteObject = remoteObject;
    }

    public ExecutionContext executionContext() {
        return this.context;
    }

    public Object evaluate(String pageFunction, List<Object> args) {
        if(args != null){
            args = new ArrayList<>();
        }
        args.add(this);
        return this.executionContext().evaluate(pageFunction, args);
    }

    public Object evaluateHandle(String pageFunction, List<Object> args) {
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        argsArray.addAll(args);
        return this.executionContext().evaluateHandle(pageFunction,argsArray);
    }

    public JSHandle getProperty(String propertyName) {
        String pageFunction = "(object, propertyName) => {\n" +
                "            const result = { __proto__: null };\n" +
                "            result[propertyName] = object[propertyName];\n" +
                "            return result;\n" +
                "        }";
        JSHandle objectHandle = (JSHandle) this.evaluateHandle(pageFunction, Arrays.asList(propertyName));
        Map<String, JSHandle> properties = objectHandle.getProperties();
        JSHandle result = properties.get(propertyName);
        objectHandle.dispose();
        return result;
    }

    public Map<String, JSHandle> getProperties() {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", this.remoteObject.getObjectId());
        params.put("ownProperties", true);
        JsonNode response = this.client.send("Runtime.getProperties", params, true);
        Map<String,JSHandle> result = new HashMap<>();
        Iterator<JsonNode> iterator = response.get("result").iterator();
        while (iterator.hasNext()) {
            JsonNode property = iterator.next();
            if (!property.get("enumerable").asBoolean())
                continue;
            try {
                result.put(property.get("name").asText(), createJSHandle(this.context, Constant.OBJECTMAPPER.treeToValue(property.get("value"), RemoteObject.class)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Object jsonValue()  {
        if (StringUtil.isNotEmpty(this.remoteObject.getObjectId())) {
            Map<String, Object> params = new HashMap<>();
            params.put("functionDeclaration", "function() { return this; }");
            params.put("objectId", this.remoteObject.getObjectId());
            params.put("returnByValue", true);
            params.put("awaitPromise", true);
            JsonNode response = this.client.send("Runtime.callFunctionOn", params, true);
            try {
                return Helper.valueFromRemoteObject(Constant.OBJECTMAPPER.treeToValue(response.get("result"), RemoteObject.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return Helper.valueFromRemoteObject(this.remoteObject);
    }

    /* This always returns null but children can define this and return an ElementHandle */
    public ElementHandle asElement() {
        return null;
    }

    /**
     * 阻塞释放elementHandle
     */
    public void dispose() {
        this.dispose(true);
    }

    /**
     * 释放elementhandle
     * 当在websocket信息回调中处理时需要isBlock=false
     * @param isBlock 是否是异步
     */
    public void dispose(boolean isBlock) {
        if (this.disposed)
            return;
        this.disposed = true;
        Helper.releaseObject(this.client, this.remoteObject,isBlock);
    }
    public String toString() {
        if (StringUtil.isNotEmpty(this.remoteObject.getObjectId())) {
            String type = StringUtil.isNotEmpty(this.remoteObject.getSubtype()) ? this.remoteObject.getSubtype() : this.remoteObject.getType();
            return "JSHandle@" + type;
        }
        return "JSHandle:" + Helper.valueFromRemoteObject(this.remoteObject);
    }

    public static JSHandle createJSHandle(ExecutionContext context, RemoteObject remoteObject) {
        Frame frame = context.frame();
        if ("node".equals(remoteObject.getSubtype()) && frame != null) {
            FrameManager frameManager = frame.getFrameManager();
            return new ElementHandle(context, context.getClient(), remoteObject, frameManager.getPage(), frameManager);
        }
        return new JSHandle(context, context.getClient(), remoteObject);
    }

    protected ExecutionContext getContext() {
        return context;
    }

    protected void setContext(ExecutionContext context) {
        this.context = context;
    }

    protected boolean getDisposed() {
        return disposed;
    }

    protected void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }

    protected RemoteObject getRemoteObject() {
        return remoteObject;
    }
}
