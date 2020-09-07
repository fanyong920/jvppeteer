package com.ruiyun.jvppeteer.core.page;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ExecutionContext {

    public static final String EVALUATION_SCRIPT_URL = "__puppeteer_evaluation_script__";

    public static final Pattern SOURCE_URL_REGEX = Pattern.compile("^[\\040\\t]*//[@#] sourceURL=\\s*(\\S*?)\\s*$", Pattern.MULTILINE);

    private CDPSession client;

    private DOMWorld world;

    private int contextId;

    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, DOMWorld world) {
        this.client = client;
        this.world = world;
        this.contextId = contextPayload.getId();
    }

    public Frame frame() {
        return this.world != null ? this.world.frame() : null;
    }

    public DOMWorld getWorld() {
        return world;
    }

    public void setWorld(DOMWorld world) {
        this.world = world;
    }

    public ElementHandle adoptElementHandle(ElementHandle elementHandle) {
        ValidateUtil.assertArg(elementHandle.executionContext() != this, "Cannot adopt handle that already belongs to this execution context");
        ValidateUtil.assertArg(this.world != null, "Cannot adopt handle without DOMWorld");
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", elementHandle.getRemoteObject().getObjectId());
        JsonNode nodeInfo = this.client.send("DOM.describeNode", params, true);
        return this.adoptBackendNodeId(nodeInfo.get("node").get("backendNodeId").asInt());
    }

    public Object evaluateHandle(String pageFunction, PageEvaluateType type, List<Object> args) {
        return this.evaluateInternal(false, pageFunction, type, args);
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, List<Object> args) {
        return this.evaluateInternal(true, pageFunction, type, args);
    }

    public Object evaluateInternal(boolean returnByValue, String pageFunction, PageEvaluateType type, List<Object> args) {
        String suffix = "//# sourceURL=" + ExecutionContext.EVALUATION_SCRIPT_URL;
        if (PageEvaluateType.STRING.equals(type)) {
            int contextId = this.contextId;
            String expression = pageFunction;
            String expressionWithSourceUrl = ExecutionContext.SOURCE_URL_REGEX.matcher(expression).find() ? expression : expression + "\n" + suffix;
            Map<String, Object> params = new HashMap<>();
            params.put("expression", expressionWithSourceUrl);
            params.put("contextId", contextId);
            params.put("returnByValue", returnByValue);
            params.put("awaitPromise", true);
            params.put("userGesture", true);
            JsonNode result = this.client.send("Runtime.evaluate", params, true);
            JsonNode exceptionDetails = result.get("exceptionDetails");
            try {
                if (exceptionDetails != null)
                    throw new RuntimeException("Evaluation failed: " + Helper.getExceptionMessage(Constant.OBJECTMAPPER.treeToValue(exceptionDetails, ExceptionDetails.class)));
                RemoteObject remoteObject = Constant.OBJECTMAPPER.treeToValue(result.get("result"), RemoteObject.class);
                return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : createJSHandle(this, remoteObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!PageEvaluateType.FUNCTION.equals(type))
            throw new IllegalArgumentException("Expected to get |string| or |function| as the first argument, but got " + type.name() + " instead.");
        String functionText = pageFunction;
        Map<String, Object> params = new HashMap<>();
        List<Object> argList = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(args)) {
            for (Object arg : args) {
                try {
                    argList.add(convertArgument(this, arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        params.put("functionDeclaration", functionText + "\n" + suffix + "\n");
        params.put("executionContextId", this.contextId);
        params.put("arguments", argList);
        params.put("returnByValue", returnByValue);
        params.put("awaitPromise", true);
        params.put("userGesture", true);
        JsonNode callFunctionOnPromise;
        try {
            callFunctionOnPromise = this.client.send("Runtime.callFunctionOn", params, true);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Converting circular structure to JSON"))
                throw new RuntimeException(e.getMessage() + " Are you passing a nested JSHandle?");
            else
                throw new RuntimeException(e);
        }
        if (callFunctionOnPromise == null) {
            return null;
        }
        JsonNode exceptionDetails = callFunctionOnPromise.get("exceptionDetails");
        RemoteObject remoteObject;
        try {
            if (exceptionDetails != null)
                throw new ProtocolException("Evaluation failed: " + Helper.getExceptionMessage(Constant.OBJECTMAPPER.treeToValue(exceptionDetails, ExceptionDetails.class)));
            remoteObject = Constant.OBJECTMAPPER.treeToValue(callFunctionOnPromise.get("result"), RemoteObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : createJSHandle(this, remoteObject);
    }

    public JSHandle queryObjects(JSHandle prototypeHandle) {
        ValidateUtil.assertArg(!prototypeHandle.getDisposed(), "Prototype JSHandle is disposed!");
        ValidateUtil.assertArg(StringUtil.isNotEmpty(prototypeHandle.getRemoteObject().getObjectId()), "Prototype JSHandle must not be referencing primitive value");
        Map<String, Object> params = new HashMap<>();
        params.put("prototypeObjectId", prototypeHandle.getRemoteObject().getObjectId());
        JsonNode response = this.client.send("Runtime.queryObjects", params, true);
        try {
            return createJSHandle(this, Constant.OBJECTMAPPER.treeToValue(response.get("objects"), RemoteObject.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Object convertArgument(ExecutionContext th, Object arg) throws JsonProcessingException {
        ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
        if (arg == null) {
            return null;
        }
        if (arg instanceof BigInteger) // eslint-disable-line valid-typeof
            return objectNode.put("unserializableValue", arg.toString() + "n");
        if ("-0".equals(arg))
            return objectNode.put("unserializableValue", "-0");

        if ("Infinity".equals(arg))
            return objectNode.put("unserializableValue", "Infinity");

        if ("-Infinity".equals(arg))
            return objectNode.put("unserializableValue", "-Infinity");

        if ("NaN".equals(arg))
            return objectNode.put("unserializableValue", "NaN");
        JSHandle objectHandle = arg instanceof JSHandle ? (JSHandle) arg : null;
        if (objectHandle != null) {
            if (objectHandle.getContext() != this)
                throw new IllegalArgumentException("JSHandles can be evaluated only in the context they were created!");
            if (objectHandle.getDisposed())
                throw new IllegalArgumentException("JSHandle is disposed!");
            if (objectHandle.getRemoteObject().getUnserializableValue() != null)
                return objectNode.put("unserializableValue", objectHandle.getRemoteObject().getUnserializableValue());
            if (StringUtil.isEmpty(objectHandle.getRemoteObject().getObjectId()))
                return objectNode.putPOJO("value", objectHandle.getRemoteObject().getValue());
            return objectNode.put("objectId", objectHandle.getRemoteObject().getObjectId());
        }
        return objectNode.putPOJO("value", arg);
    }

    private JSHandle createJSHandle(ExecutionContext executionContext, RemoteObject remoteObject) {
        return JSHandle.createJSHandle(executionContext, remoteObject);
    }

    public ElementHandle adoptBackendNodeId(int backendNodeId)  {
        Map<String, Object> params = new HashMap<>();
        params.put("backendNodeId", backendNodeId);
        params.put("executionContextId", this.contextId);
        JsonNode object = this.client.send("DOM.resolveNode", params, true);
        try {
            return (ElementHandle) createJSHandle(this, Constant.OBJECTMAPPER.treeToValue(object.get("object"), RemoteObject.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public int getContextId() {
        return contextId;
    }
}
