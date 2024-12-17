package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;


public class IsolatedWorld extends Realm {

    private volatile ExecutionContext context;
    private final IsolatedWorldEmitter emitter = new IsolatedWorldEmitter();
    private final CdpFrame frame;
    private final CdpWebWorker webWorker;
    private ChromeEnvironment chromeEnvironment;

    public IsolatedWorld(CdpFrame frame, CdpWebWorker webWorker, TimeoutSettings timeoutSettings) {
        super(timeoutSettings);
        this.frame = frame;
        this.webWorker = webWorker;
    }

    @Override
    public ChromeEnvironment environment() {
        if (this.chromeEnvironment == null) {
            Realm realm;
            CDPSession client;
            if (this.webWorker != null) {
                client = this.webWorker.client();
                realm = this.webWorker.mainRealm();
            } else {
                client = this.frame.client();
                realm = this.frame.mainRealm();
            }
            this.chromeEnvironment = new ChromeEnvironment(client, realm);
        }
        return this.chromeEnvironment;
    }

    public CDPSession client() {
        return this.environment().client();
    }

    public IsolatedWorldEmitter emitter() {
        return this.emitter;
    }

    public IsolatedWorld toIsolatedWorld() {
        return this;
    }

    public void setContext(ExecutionContext context) {
        Optional.ofNullable(this.context).ifPresent(ExecutionContext::dispose);
        context.once(ExecutionContext.ExecutionContextEvent.Disposed, (ignore) -> this.onContextDisposed());
        context.on(ExecutionContext.ExecutionContextEvent.Consoleapicalled, (event) -> this.onContextConsoleApiCalled((ConsoleAPICalledEvent) event));
        context.on(ExecutionContext.ExecutionContextEvent.Bindingcalled, (event) -> this.onContextBindingCalled((BindingCalledEvent) event));
        this.context = context;
        this.emitter.emit(IsolatedWorldEmitter.IsolatedWorldEventType.Context, context);
        this.taskManager.rerunAll();
    }

    private void onContextDisposed() {
        this.context = null;
        if (this.frame != null) {
            this.frame.clearDocumentHandle();
        }
    }

    private void onContextConsoleApiCalled(ConsoleAPICalledEvent event) {
        this.emitter.emit(IsolatedWorldEmitter.IsolatedWorldEventType.Consoleapicalled, event);
    }

    private void onContextBindingCalled(BindingCalledEvent event) {
        this.emitter.emit(IsolatedWorldEmitter.IsolatedWorldEventType.Bindingcalled, event);
    }

    public boolean hasContext() {
        return this.context != null;
    }

    public ExecutionContext context() {
        return this.context;
    }

    private ExecutionContext executionContext() {
        if (this.disposed()) {
            throw new JvppeteerException("Execution context is not available in detached frame or worker" + (this.webWorker == null ? this.frame.url() : this.webWorker.url()) + "(are you trying to evaluate?)");
        }
        return this.context;
    }

    private ExecutionContext waitForExecutionContext() {
        final ExecutionContext[] result = {null};
        this.emitter.once(IsolatedWorldEmitter.IsolatedWorldEventType.Context, (Consumer<ExecutionContext>) context -> result[0] = context);
        final JvppeteerException[] destroyedError = {null};
        this.emitter.once(IsolatedWorldEmitter.IsolatedWorldEventType.Disposed, ignore -> destroyedError[0] = new JvppeteerException("Execution context was destroyed"));
        Supplier<Boolean> conditionChecker = () -> {
            if (destroyedError[0] != null) {
                throw destroyedError[0];
            }
            if (result[0] != null) {
                return true;
            }
            return null;
        };
        Helper.waitForCondition(conditionChecker, this.timeoutSettings.timeout(), "waiting for target failed: timeout " + this.timeoutSettings.timeout() + "ms exceeded");
        return result[0];
    }

    public JSHandle evaluateHandle(String pptrFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluateHandle(pptrFunction, null);
    }

    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        ExecutionContext context = this.executionContext();
        if (context == null) {
            context = this.waitForExecutionContext();
        }
        return context.evaluateHandle(pptrFunction, args);
    }

    public Object evaluate(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluate", pptrFunction);
        ExecutionContext context = this.executionContext();
        if (context == null) {
            context = this.waitForExecutionContext();
        }
        return context.evaluate(pptrFunction, type, args);
    }



    public JSHandle adoptBackendNode(int backendNodeId) throws JsonProcessingException {
        ExecutionContext executionContext = this.executionContext();
        if (executionContext == null) {
            executionContext = this.waitForExecutionContext();
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("backendNodeId", backendNodeId);
        params.put("executionContextId", executionContext.getId());
        JsonNode result = this.client().send("DOM.resolveNode", params);
        return this.createJSHandle(OBJECTMAPPER.treeToValue(result.get("object"), RemoteObject.class));
    }

    @SuppressWarnings("unchecked")
    public  <T extends JSHandle> T adoptHandle(T handle) throws JsonProcessingException {
        if (handle.realm() == this) {
            // If the context has already adopted this handle, clone it so downstream
            // disposal doesn't become an issue.
            return (T) handle.evaluateHandle("value => {\n" +
                    "            return value;\n" +
                    "      }");
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", handle.id());
        JsonNode nodeInfo = this.client().send("DOM.describeNode", params);
        return (T) this.adoptBackendNode(nodeInfo.get("node").get("backendNodeId").asInt());
    }

    @SuppressWarnings("unchecked")
    public <T extends JSHandle> T transferHandle(T handle) throws JsonProcessingException {
        if (handle.realm() == this) {
            return handle;
        }
        // Implies it's a primitive value, probably.
        RemoteObject remoteObject = handle.remoteObject();
        if (StringUtil.isEmpty(remoteObject.getObjectId())) {
            return handle;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", remoteObject.getObjectId());
        JsonNode info = this.client().send("DOM.describeNode", params);
        return (T) this.adoptBackendNode(info.get("node").get("backendNodeId").asInt());
    }


    public JSHandle createJSHandle(RemoteObject remoteObject) {
        if ("node".equals(remoteObject.getSubtype())) {
            return new CdpElementHandle(this, remoteObject);
        }
        return new CdpJSHandle(this, remoteObject);
    }

    public void dispose() {
        Optional.ofNullable(this.context).ifPresent(ExecutionContext::dispose);
        this.emitter.emit(IsolatedWorldEmitter.IsolatedWorldEventType.Disposed, true);
        super.dispose();
        this.emitter.removeAllListeners(null);
    }

    public CdpFrame getFrame() {
        return this.frame;
    }

}
