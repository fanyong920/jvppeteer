package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.bidi.entities.CallFunctionOptions;
import com.ruiyun.jvppeteer.bidi.entities.EvaluateOptions;
import com.ruiyun.jvppeteer.bidi.entities.EvaluateResult;
import com.ruiyun.jvppeteer.bidi.entities.LocalValue;
import com.ruiyun.jvppeteer.bidi.entities.RemoteReference;
import com.ruiyun.jvppeteer.bidi.entities.ResultOwnership;
import com.ruiyun.jvppeteer.bidi.entities.SerializationOptions;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.LazyArg;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.Source;
import static com.ruiyun.jvppeteer.util.Helper.createBidiEvaluationError;
import static com.ruiyun.jvppeteer.util.Helper.setSourceUrlComment;

public abstract class BidiRealm extends Realm {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BidiRealm.class);
    protected final BidiRealmCore realm;
    private JSHandle internalPuppeteerUtil;

    public BidiRealm(BidiRealmCore realm, TimeoutSettings timeoutSettings) {
        super(timeoutSettings);
        this.realm = realm;
    }

    protected void initialize() {
        this.realm.on(BidiRealmCore.RealmCoreEvents.destroyed, (Consumer<String>) reason -> {
            this.taskManager.terminateAll(new JvppeteerException(reason));
            this.dispose();
        });
        this.realm.on(BidiRealmCore.RealmCoreEvents.updated, (ignored) -> {
            this.taskManager.rerunAll();
        });
    }

    public JSHandle evaluateHandle(String pptrFunction) throws JsonProcessingException {
        return this.evaluateHandle(pptrFunction, null);
    }

    public JSHandle evaluateHandle(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        return (JSHandle) this.evaluate(false, type, pptrFunction, args);
    }

    @Override
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException {
        return this.evaluateHandle(pptrFunction, Helper.isFunction(pptrFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING, args);
    }

    @Override
    public Object evaluate(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        if (type == null) {
            type = Helper.isFunction(pptrFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING;
        }
        return this.evaluate(true, type, pptrFunction, args);
    }

    private Object evaluate(boolean returnByValue, EvaluateType type, String pptrFunction, List<Object> args) throws JsonProcessingException {
        String sourceUrlComment = setSourceUrlComment(pptrFunction);
        ResultOwnership resultOwnership = returnByValue ? ResultOwnership.None : ResultOwnership.Root;
        SerializationOptions serializationOptions = returnByValue ? new SerializationOptions() : new SerializationOptions(0L, null, 0L);
        EvaluateResult responsePromise;
        if (EvaluateType.STRING.equals(type)) {
            EvaluateOptions evaluateOptions = new EvaluateOptions();
            evaluateOptions.setUserActivation(true);
            evaluateOptions.setResultOwnership(resultOwnership);
            evaluateOptions.setSerializationOptions(serializationOptions);
            responsePromise = this.realm.evaluate(sourceUrlComment, true, evaluateOptions);
        } else {
            List<LocalValue> argList = new ArrayList<>();
            if (ValidateUtil.isNotEmpty(args)) {
                for (Object arg : args) {
                    if (Objects.nonNull(arg) && arg instanceof LazyArg) {
                        initPuppeteerUtil();
                        argList.add(this.serialize(this.internalPuppeteerUtil));
                    } else {
                        argList.add(this.serialize(arg));
                    }
                }
            }
            CallFunctionOptions callFunctionOptions = new CallFunctionOptions();
            callFunctionOptions.setArguments(argList);
            callFunctionOptions.setResultOwnership(resultOwnership);
            callFunctionOptions.setSerializationOptions(serializationOptions);
            callFunctionOptions.setUserActivation(true);
            responsePromise = this.realm.callFunction(pptrFunction, true, callFunctionOptions);
        }
        if (Objects.equals("exception", responsePromise.getType())) {
            createBidiEvaluationError(responsePromise.getExceptionDetails());
        }
        return returnByValue ? BidiDeserializer.deserialize(responsePromise.getResult()) : this.createHandle(responsePromise.getResult());
    }

    protected void initPuppeteerUtil() throws JsonProcessingException {
        if (Objects.isNull(this.internalPuppeteerUtil)) {
            synchronized (this) {
                if (Objects.isNull(this.internalPuppeteerUtil)) {
                    this.internalPuppeteerUtil = this.evaluateHandle(Source, EvaluateType.STRING, null);
                }
            }
        }
    }

    private LocalValue serialize(Object arg) {
        if (arg instanceof BidiJSHandle || arg instanceof BidiElementHandle) {
            JSHandle handle = (JSHandle) arg;
            if (handle.realm() != this) {
                if (!(handle.realm() instanceof BidiFrameRealm) || !(this instanceof BidiFrameRealm)) {
                    throw new JvppeteerException("Trying to evaluate JSHandle from different global types. Usually this means you're using a handle from a worker in a page or vice versa.");
                }
                if (((BidiFrameRealm) handle.realm()).frame() != ((BidiFrameRealm) this).frame()) {
                    throw new JvppeteerException("Trying to evaluate JSHandle from different frames. Usually this means you're using a handle from a page on a different page.");
                }
            }
            if (handle.disposed()) {
                throw new JvppeteerException("JSHandle is disposed!");
            }
            if (handle instanceof BidiElementHandle) {
                return Constant.OBJECTMAPPER.convertValue(((BidiElementHandle) handle).remoteValue(), RemoteReference.class);
            } else {
                return Constant.OBJECTMAPPER.convertValue(((BidiJSHandle) handle).remoteValue(), RemoteReference.class);
            }

        }
        return BidiSerializer.serialize(arg);
    }

    public void destroyHandles(List<BidiJSHandle> handles) {
        if (this.disposed()) {
            return;
        }
        if (ValidateUtil.isEmpty(handles)) {
            return;
        }
        List<String> handleIds = handles.stream().map(BidiJSHandle::id).filter(StringUtil::isNotEmpty).collect(Collectors.toList());
        if (ValidateUtil.isEmpty(handleIds)) {
            return;
        }
        try {
            this.realm.disown(handleIds);
        } catch (Exception e) {
            // Exceptions might happen in case of a page been navigated or closed.
            // Swallow these since they are harmless and we don't leak anything in this case.
            LOGGER.error("jvppeteer error", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JSHandle> T adoptHandle(T handle) throws JsonProcessingException {
        return (T) this.evaluateHandle("node => {\n" +
                "      return node;\n" +
                "    }", Collections.singletonList(handle));
    }

    @Override
    public <T extends JSHandle> T transferHandle(T handle) throws JsonProcessingException {
        if (handle.realm() == this) {
            return handle;
        }
        T transferredHandle = this.adoptHandle(handle);
        handle.dispose();
        return transferredHandle;
    }
}
