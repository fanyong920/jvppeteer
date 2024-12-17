package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.common.ARIAQueryHandler;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BidiFrameRealm extends BidiRealm {

    private final BidiFrame frame;
    final WindowRealm realm;
    private volatile boolean bindingsInstalled;
    final Object lock = new Object();

    private BidiFrameRealm(WindowRealm realm, BidiFrame frame) {
        super(realm, frame.timeoutSettings());
        this.frame = frame;
        this.realm = realm;
    }

    public static BidiFrameRealm from(WindowRealm realm, BidiFrame frame) {
        BidiFrameRealm frameRealm = new BidiFrameRealm(realm, frame);
        frameRealm.initialize();
        return frameRealm;
    }

    protected void initialize() {
        super.initialize();
        // This should run first.
        this.realm.on(BidiRealmCore.RealmCoreEvents.updated, (ignored) -> {
            this.frame().clearDocumentHandle();
            this.bindingsInstalled = false;
        });
    }

    @Override
    protected void initPuppeteerUtil() throws JsonProcessingException {
        if (!this.bindingsInstalled) {
            synchronized (lock) {
                if (!this.bindingsInstalled) {
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
                    BidiExposeableFunction.from(this.frame(), "__ariaQuerySelector", queryOneFunction, StringUtil.isNotEmpty(this.sandbox()));
                    BindingFunction queryAllFunction = (args) -> {
                        ElementHandle element = (ElementHandle) args.get(0);
                        String selector = (String) args.get(1);
                        ARIAQueryHandler ariaQueryHandler = new ARIAQueryHandler();
                        try {
                            List<ElementHandle> results = ariaQueryHandler.queryAll(element, selector);
                            List<Object> args2 = new ArrayList<>(results);
                            return element.realm().evaluateHandle("(...elements) => {\n" +
                                    "                return elements;\n" +
                                    "              }", args2);
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    };
                    BidiExposeableFunction.from(this.frame(), "__ariaQuerySelectorAll", queryAllFunction, StringUtil.isNotEmpty(this.sandbox()));
                    super.initPuppeteerUtil();
                    this.bindingsInstalled = true;
                }
            }
        }
    }

    public String sandbox() {
        return this.realm.sandbox;
    }

    public BidiFrame frame() {
        return this.frame;
    }

    @Override
    public ChromeEnvironment environment() {
        return null;
    }

    @Override
    public JSHandle adoptBackendNode(int backendNodeId) throws JsonProcessingException {
        double executionContextId = this.realm.resolveExecutionContextId();
        Map<String, Object> params = ParamsFactory.create();
        params.put("executionContextId", executionContextId);
        params.put("backendNodeId", backendNodeId);
        JsonNode response = this.frame.client().send("DOM.resolveNode", params);
        RemoteValue remoteValue = new RemoteValue();
        remoteValue.setHandle(response.at("/object/objectId").asText());
        remoteValue.setType("node");
        BidiElementHandle handle = BidiElementHandle.from(remoteValue, this);
        try {
            return handle.evaluateHandle("lement => {\n" +
                    "      return element;\n" +
                    "    }");
        } finally {
            handle.dispose();
        }
    }

}
