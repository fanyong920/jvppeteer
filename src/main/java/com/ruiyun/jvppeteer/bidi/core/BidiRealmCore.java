package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.bidi.entities.CallFunctionOptions;
import com.ruiyun.jvppeteer.bidi.entities.EvaluateOptions;
import com.ruiyun.jvppeteer.bidi.entities.EvaluateResult;
import com.ruiyun.jvppeteer.bidi.entities.Target;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.DisposableStack;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class BidiRealmCore extends EventEmitter<BidiRealmCore.RealmCoreEvents> {
    protected volatile String id;
    protected volatile String origin;
    protected volatile Double executionContextId;
    private String reason;
    protected final List<DisposableStack<?>> disposables = new ArrayList<>();

    protected BidiRealmCore(String id, String origin) {
        super();
        this.id = id;
        this.origin = origin;
    }

    public boolean disposed() {
        return Objects.nonNull(this.reason);
    }

    public abstract Session session();

    public Target target() {
        Target target = new Target();
        target.setRealm(this.id);
        return target;
    }

    protected void dispose(String reason) {
        this.reason = reason;
        this.disposeSymbol();
    }

    public void disown(List<String> handles) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("handles", handles);
        params.put("target", this.target());
        this.session().send("script.disown", params);
    }

    public EvaluateResult callFunction(String functionDeclaration, boolean awaitPromise, CallFunctionOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("functionDeclaration", functionDeclaration);
        params.put("awaitPromise", awaitPromise);
        params.put("target", this.target());
        params.put("arguments", options.getArguments());
        params.put("resultOwnership", options.getResultOwnership());
        params.put("serializationOptions", options.getSerializationOptions());
        params.put("this", options.getThis1());
        params.put("userActivation", options.getUserActivation());
        JsonNode response = this.session().send("script.callFunction", params);
        return Constant.OBJECTMAPPER.convertValue(response.get(Constant.RESULT), EvaluateResult.class);
    }

    public EvaluateResult evaluate(String expression, boolean awaitPromise, EvaluateOptions options) {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        Map<String, Object> params = ParamsFactory.create();
        params.put("expression", expression);
        params.put("awaitPromise", awaitPromise);
        params.put("target", this.target());
        params.put("resultOwnership", options.getResultOwnership());
        params.put("serializationOptions", options.getSerializationOptions());
        params.put("userActivation", options.getsUserActivation());
        JsonNode response = this.session().send("script.evaluate", params);
        return Constant.OBJECTMAPPER.convertValue(response.get(Constant.RESULT), EvaluateResult.class);
    }

    public Double resolveExecutionContextId() {
        ValidateUtil.assertArg(StringUtil.isEmpty(this.reason), this.reason);
        if (Objects.isNull(this.executionContextId)) {
            Map<String, Object> params = ParamsFactory.create();
            params.put("realm", this.id);
            JsonNode response = this.session().connection().send("goog:cdp.resolveRealm", params);
            this.executionContextId = response.at("/result/executionContextId").asDouble();
        }
        return this.executionContextId;
    }

    public void disposeSymbol() {
        if (Objects.isNull(this.reason)) {
            this.reason = "Realm already destroyed, probably because all associated browsing contexts closed.";
            this.emit(RealmCoreEvents.destroyed, this.reason);
            for (DisposableStack stack : this.disposables) {
                stack.getEmitter().off(stack.getType(), stack.getConsumer());
            }
            super.disposeSymbol();
        }
    }

    public enum RealmCoreEvents {
        /**
         * 当领域更新时发出。
         * Realm.class
         */
        updated,
        /**
         * 当领域被销毁时发出。
         * string
         */
        destroyed,
        /**
         * 当领域中创建了一个专用工作线程时发出。
         * DedicatedWorkerRealm.class
         */
        worker,
        /**
         * 当领域中创建了一个共享工作线程时发出。
         * SharedWorkerRealm.class
         */
        sharedworker
    }
}
