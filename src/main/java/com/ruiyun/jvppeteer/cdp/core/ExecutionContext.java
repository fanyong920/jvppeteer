package com.ruiyun.jvppeteer.cdp.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.Binding;
import com.ruiyun.jvppeteer.cdp.entities.BindingPayload;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateResponse;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.ExceptionDetails;
import com.ruiyun.jvppeteer.cdp.entities.ExecutionContextDescription;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.common.ARIAQueryHandler;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.LazyArg;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.CDP_BINDING_PREFIX;
import static com.ruiyun.jvppeteer.common.Constant.Infinity;
import static com.ruiyun.jvppeteer.common.Constant.NaN;
import static com.ruiyun.jvppeteer.common.Constant.Navigate_Infinity;
import static com.ruiyun.jvppeteer.common.Constant.Navigate_Zero;
import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.common.Constant.Source;
import static com.ruiyun.jvppeteer.util.Helper.setSourceUrlComment;
import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class ExecutionContext extends EventEmitter<ExecutionContext.ExecutionContextEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);
    private final CDPSession client;
    private String name;
    private final int id;
    private final IsolatedWorld world;
    private final Map<ConnectionEvents, Consumer<?>> listener = new HashMap<>();
    private final Map<String, Binding> bindings = new HashMap<>();
    private volatile JSHandle puppeteerUtil;

    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, IsolatedWorld world) {
        this.client = client;
        this.world = world;
        this.id = contextPayload.getId();
        if (StringUtil.isNotEmpty(contextPayload.getName())) {
            this.name = contextPayload.getName();
        }
        setListener(client);
    }

    private void setListener(CDPSession client) {
        Consumer<BindingCalledEvent> bindingCalled = this::onBindingCalled;
        client.on(ConnectionEvents.Runtime_bindingCalled, bindingCalled);
        this.listener.put(ConnectionEvents.Runtime_bindingCalled, bindingCalled);

        Consumer<ExecutionContextDestroyedEvent> executionContextDestroyed = event -> {
            if (event.getExecutionContextId() == this.id) {
                this.dispose();
            }
        };
        client.on(ConnectionEvents.Runtime_executionContextDestroyed, executionContextDestroyed);
        this.listener.put(ConnectionEvents.Runtime_executionContextDestroyed, executionContextDestroyed);

        Consumer<Boolean> executionContextsCleared = event -> this.dispose();
        client.on(ConnectionEvents.Runtime_executionContextsCleared, executionContextsCleared);
        this.listener.put(ConnectionEvents.Runtime_executionContextsCleared, executionContextsCleared);

        Consumer<ConsoleAPICalledEvent> consoleAPICalled = ExecutionContext.this::onConsoleAPI;
        client.on(ConnectionEvents.Runtime_consoleAPICalled, consoleAPICalled);
        this.listener.put(ConnectionEvents.Runtime_consoleAPICalled, consoleAPICalled);

        Consumer<Boolean> disconnected = event -> this.dispose();
        client.on(ConnectionEvents.CDPSession_Disconnected, disconnected);
        this.listener.put(ConnectionEvents.CDPSession_Disconnected, disconnected);
    }

    public void addBinding(Binding binding) {
        if (this.bindings.containsKey(binding.name())) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("name", CDP_BINDING_PREFIX + binding.name());
        if (StringUtil.isNotEmpty(this.name)) {
            params.put("executionContextName", this.name);
        } else {
            params.put("executionContextId", this.id);
        }
        synchronized (this) {
            try {
                this.client.send("Runtime.addBinding", params);
                List<Object> args = new ArrayList<>();
                args.add("internal");
                args.add(binding.name());
                args.add(CDP_BINDING_PREFIX);
                this.evaluate(addPageBinding(), args);
                this.bindings.put(binding.name(), binding);
            } catch (Exception e) {
                if (e.getMessage().contains("Execution context was destroyed")) {
                    return;
                }
                if (e.getMessage().contains("Cannot find context with specified id'")) {
                    return;
                }
                LOGGER.error("addBinding error:", e);
            }
        }
    }

    private String addPageBinding() {
        return "function addPageBinding(type, name, prefix) {\n" +
                "  // Depending on the frame loading state either Runtime.evaluate or\n" +
                "  // Page.addScriptToEvaluateOnNewDocument might succeed. Let's check that we   \n" +
                "  // don't re-wrap Puppeteer's binding.    \n" +
                "  // @ts-expect-error: In a different context.    \n" +
                "  if (globalThis[name]) {\n" +
                "    return;\n" +
                "  }\n" +
                "  // We replace the CDP binding with a Puppeteer binding.\n" +
                "  Object.assign(globalThis, {\n" +
                "    [name](...args) {\n" +
                "      // This is the Puppeteer binding.\n" +
                "      // @ts-expect-error: In a different context.\n" +
                "      const callPuppeteer = globalThis[name];\n" +
                "      callPuppeteer.args ??= new Map();\n" +
                "      callPuppeteer.callbacks ??= new Map();\n" +
                "      const seq = (callPuppeteer.lastSeq ?? 0) + 1;\n" +
                "      callPuppeteer.lastSeq = seq;\n" +
                "      callPuppeteer.args.set(seq, args);\n" +
                "      // @ts-expect-error: In a different context.\n" +
                "      // Needs to be the same as CDP_BINDING_PREFIX.\n" +
                "      globalThis[prefix + name](JSON.stringify({\n" +
                "        type,\n" +
                "        name,\n" +
                "        seq,\n" +
                "        args,\n" +
                "        isTrivial: !args.some(value => {\n" +
                "          return value instanceof Node;\n" +
                "        }),\n" +
                "      }));\n" +
                "      return new Promise((resolve, reject) => {\n" +
                "        callPuppeteer.callbacks.set(seq, {\n" +
                "          resolve(value) {\n" +
                "            callPuppeteer.args.delete(seq);\n" +
                "            resolve(value);\n" +
                "          },\n" +
                "          reject(value) {\n" +
                "            callPuppeteer.args.delete(seq);\n" +
                "            reject(value);\n" +
                "          },\n" +
                "        });\n" +
                "      });\n" +
                "    },\n" +
                "  })\n" +
                "}";
    }

    private void onBindingCalled(BindingCalledEvent event) {
        if (event.getExecutionContextId() != this.id) {
            return;
        }
        String payloadStr = event.getPayload();
        BindingPayload payload;
        try {
            payload = OBJECTMAPPER.readValue(payloadStr, BindingPayload.class);
        } catch (JsonProcessingException e) {
            return;
        }
        if (!"internal".equals(payload.getType())) {
            this.emit(ExecutionContextEvent.Bindingcalled, event);
            return;
        }
        if (!this.bindings.containsKey(payload.getName())) {
            this.emit(ExecutionContextEvent.Bindingcalled, event);
            return;
        }
        Binding binding = this.bindings.get(payload.getName());
        try {
            if (binding != null) {
                binding.run(this, payload.getSeq(), payload.getArgs(), payload.getIsTrivial());
            }
        } catch (Exception e) {
            LOGGER.error("onBindingCalled error", e);
        }
    }

    public int getId() {
        return id;
    }

    private void onConsoleAPI(ConsoleAPICalledEvent event) {
        if (event.getExecutionContextId() != this.id) {
            return;
        }
        this.emit(ExecutionContextEvent.Consoleapicalled, event);
    }

    public CdpJSHandle evaluateHandle(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        Object handle = this.evaluateInternal(false, pptrFunction, type, args);
        if (handle == null) {
            return null;
        }
        return (CdpJSHandle) handle;
    }

    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException {
        Object handle = this.evaluateInternal(false, pptrFunction, Helper.isFunction(pptrFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING, args);
        if (handle == null) {
            return null;
        }
        return (JSHandle) handle;
    }

    public Object evaluate(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        if (type == null) {
            type = Helper.isFunction(pptrFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING;
        }
        return this.evaluateInternal(true, pptrFunction, type, args);
    }

    public Object evaluate(String pptrFunction, List<Object> args) throws JsonProcessingException {
        return this.evaluate(pptrFunction, null, args);
    }

    private EvaluateResponse rewriteError(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("Object reference chain is too long")) {
            RemoteObject remoteObject = new RemoteObject();
            remoteObject.setType("undefined");
            EvaluateResponse response = new EvaluateResponse();
            response.setResult(remoteObject);
            return response;
        }
        if (e.getMessage() != null && e.getMessage().contains("Object couldn't be returned by value")) {
            RemoteObject remoteObject = new RemoteObject();
            remoteObject.setType("undefined");
            EvaluateResponse response = new EvaluateResponse();
            response.setResult(remoteObject);
            return response;
        }

        if (e.getMessage() != null && (e.getMessage().endsWith("Cannot find context with specified id") || e.getMessage().endsWith("Inspected target navigated or closed"))) {
            throw new JvppeteerException("Execution context was destroyed, most likely because of a navigation.");
        }
        throwError(e);
        return null;
    }

    /**
     * 这里的EvaluateType有时候是明确指定为String的，不指定的情况下，会自动判断是字符串还是函数
     * <p>
     * {@link CdpFrame#addExposedFunctionBinding(Binding)}就指定了是String
     */
    private Object evaluateInternal(boolean returnByValue, String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        pptrFunction = setSourceUrlComment(pptrFunction);
        if (EvaluateType.STRING.equals(type)) {
            Map<String, Object> params = new HashMap<>();
            params.put("expression", pptrFunction);
            params.put("contextId", this.id);
            params.put("returnByValue", returnByValue);
            params.put("awaitPromise", true);
            params.put("userGesture", true);
            EvaluateResponse result;
            try {
                result = OBJECTMAPPER.treeToValue(this.client.send("Runtime.evaluate", params), EvaluateResponse.class);
            } catch (Exception e) {
                result = rewriteError(e);
            }
            ExceptionDetails exceptionDetails = result.getExceptionDetails();
            if (exceptionDetails != null) {
                Object evaluationError = Helper.createCdpEvaluationError(exceptionDetails);
                if (evaluationError instanceof EvaluateException) {
                    throw (EvaluateException) evaluationError;
                } else {
                    throw new EvaluateException(OBJECTMAPPER.writeValueAsString(evaluationError));
                }
            }
            RemoteObject remoteObject = result.getResult();
            return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : this.world.createJSHandle(remoteObject);
        }
        Map<String, Object> params = new HashMap<>();
        List<JsonNode> argList = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                if (Objects.nonNull(arg) && arg instanceof LazyArg) {
                    initPuppeteerUtil();
                    argList.add(convertArgument(this, this.puppeteerUtil));
                } else {
                    argList.add(convertArgument(this, arg));
                }
            }
        }
        params.put("functionDeclaration", pptrFunction);
        params.put("executionContextId", this.id);
        params.put("arguments", argList);
        params.put("returnByValue", returnByValue);
        params.put("awaitPromise", true);
        params.put("userGesture", true);
        EvaluateResponse callFunctionOnPromise;
        try {
            try {//第一个try用来添加message,第二个try是重写错误，返回结果
                callFunctionOnPromise = OBJECTMAPPER.treeToValue(this.client.send("Runtime.callFunctionOn", params), EvaluateResponse.class);
            } catch (Exception e) {
                if (e.getMessage().startsWith("Converting circular structure to JSON"))
                    throw new JvppeteerException(e.getMessage() + " Recursive objects are not allowed.");
                else
                    throw e;
            }
        } catch (Exception e) {
            callFunctionOnPromise = rewriteError(e);
        }
        if (callFunctionOnPromise == null) {
            return null;
        }
        ExceptionDetails exceptionDetails = callFunctionOnPromise.getExceptionDetails();
        if (exceptionDetails != null) {
            Object evaluationError = Helper.createCdpEvaluationError(exceptionDetails);
            if (evaluationError instanceof EvaluateException) {
                throw (EvaluateException) evaluationError;
            } else {
                throw new EvaluateException(OBJECTMAPPER.writeValueAsString(evaluationError));
            }
        }
        RemoteObject remoteObject = callFunctionOnPromise.getResult();
        return returnByValue ? Helper.valueFromRemoteObject(remoteObject) : this.world.createJSHandle(remoteObject);
    }

    private void initPuppeteerUtil() throws JsonProcessingException {
        if (this.puppeteerUtil == null) {
            synchronized (this) {
                if (this.puppeteerUtil == null) {
                    BindingFunction queryOneFunction = (args) -> {
                        ElementHandle element = (ElementHandle) args.get(0);
                        String selector = (String) args.get(1);
                        try {
                            ARIAQueryHandler ariaQueryHandler = new ARIAQueryHandler();
                            return ariaQueryHandler.queryOne(element, selector);
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    };
                    Binding ariaQuerySelectorBinding = new Binding("__ariaQuerySelector", queryOneFunction, "");
                    BindingFunction queryAllFunction = (args) -> {
                        ElementHandle element = (ElementHandle) args.get(0);
                        String selector = (String) args.get(1);
                        ARIAQueryHandler ariaQueryHandler = new ARIAQueryHandler();
                        try {
                            return ariaQueryHandler.queryAll(element, selector);
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    };
                    Binding ariaQuerySelectorAllBinding = new Binding("__ariaQuerySelectorAll", queryAllFunction, "");
                    this.addBinding(ariaQuerySelectorBinding);
                    this.addBinding(ariaQuerySelectorAllBinding);
                    this.puppeteerUtil = this.evaluateHandle(Source, EvaluateType.STRING, null);
                }
            }
        }
    }

    private JsonNode convertArgument(ExecutionContext context, Object arg) {
        ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
        if (Objects.isNull(arg)) {
            return objectNode;
        }
        if (arg instanceof BigInteger) { // eslint-disable-line valid-typeof
            objectNode.put("unserializableValue", arg + "n");
            return objectNode;
        }
        if (Navigate_Zero.equals(arg)) {
            objectNode.put("unserializableValue", Navigate_Zero);
            return objectNode;
        }
        if (Infinity.equals(arg)) {
            objectNode.put("unserializableValue", Infinity);
            return objectNode;
        }
        if (Navigate_Infinity.equals(arg)) {
            objectNode.put("unserializableValue", Navigate_Infinity);
            return objectNode;
        }
        if (NaN.equals(arg)) {
            objectNode.put("unserializableValue", NaN);
            return objectNode;
        }
        CdpJSHandle objectHandle = arg instanceof CdpJSHandle ? (CdpJSHandle) arg : null;
        if (objectHandle != null) {
            if (objectHandle.realm() != context.world()) {
                throw new JvppeteerException("JSHandles can be evaluated only in the context they were created!");
            }
            if (objectHandle.disposed()) {
                throw new JvppeteerException("JSHandle is disposed!" + objectHandle.remoteObject().getObjectId());

            }
            if (objectHandle.remoteObject().getUnserializableValue() != null) {
                objectNode.put("unserializableValue", objectHandle.remoteObject().getUnserializableValue());
                return objectNode;
            }
            if (StringUtil.isEmpty(objectHandle.remoteObject().getObjectId())) {
                return objectNode.putPOJO("value", objectHandle.remoteObject().getValue());
            }
            return objectNode.put("objectId", objectHandle.remoteObject().getObjectId());
        }
        return objectNode.putPOJO("value", arg);
    }

    private IsolatedWorld world() {
        return this.world;
    }


    public void dispose() {
        this.listener.forEach(this.client::off);
        this.emit(ExecutionContextEvent.Disposed, true);
    }

    public enum ExecutionContextEvent {
        Disposed("disposed"),
        Consoleapicalled("consoleapicalled"),
        Bindingcalled("bindingcalled");
        private String eventType;

        ExecutionContextEvent(String eventType) {

        }

        public String getEventType() {
            return eventType;
        }
    }
}
