package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
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

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        return this.executionContext().evaluate(pageFunction, type, this, args);
    }

    public Object evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        Object[] argsArray = new Object[args.length + 1];
        argsArray[0] = this;
        System.arraycopy(args,0,argsArray,1,args.length);
        return this.executionContext().evaluateInternal(false, pageFunction, type,argsArray);
    }

    public JSHandle getProperty(String propertyName) throws JsonProcessingException {
        String pageFunction = "(object, propertyName) => {\n" +
                "            const result = { __proto__: null };\n" +
                "            result[propertyName] = object[propertyName];\n" +
                "            return result;\n" +
                "        }";
        JSHandle objectHandle = (JSHandle) this.evaluateHandle(pageFunction, PageEvaluateType.FUNCTION, propertyName);
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
        Map result = new HashMap<>();
        Iterator<JsonNode> iterator = response.get("result").iterator();
        while (iterator.hasNext()) {
            JsonNode property = iterator.next();

            if (!property.get("enumerable").asBoolean())
                continue;
            try {
                result.put(property.get("name").asText(), createJSHandle(this.context, Constant.OBJECTMAPPER.treeToValue(property.get("value"), RemoteObject.class)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        return Helper.valueFromRemoteObject(this.remoteObject);
    }

    /* This always returns null but children can define this and return an ElementHandle */
    public ElementHandle asElement() {
        return null;
    }

    public void dispose() {
        if (this.disposed)
            return;
        this.disposed = true;
        Helper.releaseObject(this.client, this.remoteObject);
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

    public ExecutionContext getContext() {
        return context;
    }

    public void setContext(ExecutionContext context) {
        this.context = context;
    }

    public boolean getDisposed() {
        return disposed;
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }

    public RemoteObject getRemoteObject() {
        return remoteObject;
    }
}
