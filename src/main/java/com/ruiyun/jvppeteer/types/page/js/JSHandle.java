package com.ruiyun.jvppeteer.types.page.js;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.types.page.context.ExecutionContext;
import com.ruiyun.jvppeteer.types.page.DOM.ElementHandle;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.HashMap;
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

    public  ExecutionContext executionContext() {
        return this.context;
    }

     public Object evaluate(String pageFunction , PageEvaluateType type,Object... args) {
        return  this.executionContext().evaluate(pageFunction, type,this, args);
    }

    public JSHandle evaluateHandle(String pageFunction , PageEvaluateType type,Object... args){
        return this.executionContext().evaluateInternal(false, pageFunction,type ,args);
    }
    //TODO
    public JSHandle  getProperty(String propertyName){
//    const objectHandle =  this.evaluateHandle((object: HTMLElement, propertyName: string) => {
//      const result = {__proto__: null};
//            result[propertyName] = object[propertyName];
//            return result;
//        }, propertyName);
//    const properties = await objectHandle.getProperties();
//    const result = properties.get(propertyName) || null;
//        await objectHandle.dispose();
//        return result;
        return  null;
    }

    public Object jsonValue() throws JsonProcessingException {
        if (StringUtil.isNotEmpty(this.remoteObject.getObjectId())) {
            Map<String,Object> params = new HashMap<>();
            params.put("functionDeclaration","function() { return this; }");
            params.put("objectId",this.remoteObject.getObjectId());
            params.put("returnByValue",true);
            params.put("awaitPromise",true);
            JsonNode response =  this.client.send("Runtime.callFunctionOn", params,true);
            return Helper.valueFromRemoteObject(Constant.OBJECTMAPPER.treeToValue(response.get("result"),RemoteObject.class));
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
        String type =  StringUtil.isNotEmpty(this.remoteObject.getSubtype()) ? this.remoteObject.getSubtype() : this.remoteObject.getType();
            return "JSHandle@" + type;
        }
        return "JSHandle:" + Helper.valueFromRemoteObject(this.remoteObject);
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
}
