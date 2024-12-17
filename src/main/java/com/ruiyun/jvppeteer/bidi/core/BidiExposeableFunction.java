package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.AddPreloadScriptOptions;
import com.ruiyun.jvppeteer.bidi.entities.CallFunctionOptions;
import com.ruiyun.jvppeteer.bidi.entities.ChannelProperties;
import com.ruiyun.jvppeteer.bidi.entities.ChannelValue;
import com.ruiyun.jvppeteer.bidi.entities.LocalValue;
import com.ruiyun.jvppeteer.bidi.entities.MessageParameters;
import com.ruiyun.jvppeteer.bidi.entities.ResultOwnership;
import com.ruiyun.jvppeteer.bidi.entities.Source;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.common.DisposableStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidiExposeableFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidiExposeableFunction.class);
    private final BidiFrame frame;
    private final String name;
    private final String channel;
    private final BindingFunction apply;
    private final boolean isolate;
    private final List<DisposableStack<?>> disposables = new ArrayList<>();
    private final List<List<Object>> scripts = new ArrayList<>();
    private final List<BidiElementHandle> handles = new ArrayList<>();

    public BidiExposeableFunction(BidiFrame frame, String name, BindingFunction apply, boolean isolate) {
        this.frame = frame;
        this.name = name;
        this.apply = apply;
        this.isolate = isolate;
        this.channel = "__puppeteer__" + this.frame.id() + "_page_exposeFunction_" + this.name;
    }

    public static BidiExposeableFunction from(BidiFrame frame, String name, BindingFunction apply, boolean isolate) {
        BidiExposeableFunction func = new BidiExposeableFunction(frame, name, apply, isolate);
        func.initialize();
        return func;
    }

    private void initialize() {
        Connection connection = this.connection();
        ChannelProperties properties = new ChannelProperties();
        properties.setChannel(this.channel);
        properties.setOwnership(ResultOwnership.Root);
        LocalValue channelObject = new ChannelValue("channel", properties);
        Consumer<MessageParameters> handleMessage = this::handleMessage;
        connection.on(ConnectionEvents.script_message, handleMessage);
        this.disposables.add(new DisposableStack<>(connection, ConnectionEvents.script_message, handleMessage));
        String functionDeclaration = "(callback) => {\n" +
                "      Object.assign(globalThis, {\n" +
                "        [(\"" + this.name + "\") ]: function (...args) {\n" +
                "          return new Promise(\n" +
                "            (resolve, reject) => {\n" +
                "              callback([resolve, reject, args]);\n" +
                "            },\n" +
                "          );\n" +
                "        },\n" +
                "      });\n" +
                "    }";
        List<BidiFrame> frames = new CopyOnWriteArrayList<>();
        frames.add(this.frame);
        Iterator<BidiFrame> iterator = frames.iterator();
        while (iterator.hasNext()) {
            BidiFrame frame = iterator.next();
            frames.addAll(frame.childFrames());
        }

        for (BidiFrame frame : frames) {
            try {
                BidiFrameRealm realm = this.isolate ? frame.isolatedRealm() : frame.mainRealm();
                AddPreloadScriptOptions addPreloadScriptOptions = new AddPreloadScriptOptions();
                List<LocalValue> arguments = Collections.singletonList(channelObject);
                addPreloadScriptOptions.setArguments(arguments);
                addPreloadScriptOptions.setSandbox(realm.sandbox());
                String script = frame.browsingContext.addPreloadScript(functionDeclaration, addPreloadScriptOptions);
                CallFunctionOptions callFunctionOptions = new CallFunctionOptions();
                callFunctionOptions.setArguments(arguments);
                realm.realm.callFunction(functionDeclaration, false, callFunctionOptions);
                scripts.add(Arrays.asList(frame, script));
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
    }

    public Connection connection() {
        return this.frame.page().browser().connection();
    }

    public void handleMessage(MessageParameters params) {
        if (!Objects.equals(params.getChannel(), this.channel)) {
            return;
        }
        BidiRealm realm = this.getRealm(params.getSource());
        if (Objects.isNull(realm)) {
            // Unrelated message.
            return;
        }
        BidiJSHandle dataHandle = BidiJSHandle.from(params.getData(), realm);
        try {
            JSHandle argsHandle = dataHandle.evaluateHandle("([, , args]) => {\n" +
                    "      return args;\n" +
                    "    }");
            try {
                List<Object> args = new ArrayList<>();
                Iterator<JSHandle> iterator = argsHandle.getProperties().values().iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    JSHandle handle = iterator.next();
                    // Element handles are passed as is.
                    if (handle instanceof BidiElementHandle) {
                        args.add(i, handle);
                        this.handles.add((BidiElementHandle) handle);
                        continue;
                    }
                    // Everything else is passed as the JS value.
                    args.add(i, handle.jsonValue());
                    i++;
                }
                Object result;
                try {
                    result = this.apply.bind(args);
                } catch (Exception e) {
                    try {
                        dataHandle.evaluate("([, reject], message, stack) => {\n" +
                                "    const error = new Error(message);\n" +
                                "    if (stack) {\n" +
                                "      error.stack = stack;\n" +
                                "    }\n" +
                                "    reject(error);\n" +
                                "  }", Arrays.asList(e.getMessage(), Arrays.toString(e.getStackTrace())));
                    } catch (Exception ex) {
                        LOGGER.error("jvppeteer error", e);
                    }
                    return;
                }
                try {
                    dataHandle.evaluate("([resolve], result) => {\n" +
                            "        resolve(result);\n" +
                            "      }", Collections.singletonList(result));
                } catch (Exception e) {
                    LOGGER.error("jvppeteer error", e);
                }
            } finally {
                argsHandle.dispose();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dataHandle.disposed();
        }

    }

    private BidiRealm getRealm(Source source) {
        BidiFrame frame = this.findFrame(source.getContext());
        if (Objects.isNull(frame)) {
            // Unrelated message.
            return null;
        }
        return frame.realm(source.getRealm());
    }

    private BidiFrame findFrame(String id) {
        List<BidiFrame> frames = new ArrayList<>();
        frames.add(this.frame);
        Iterator<BidiFrame> iterator = frames.iterator();
        while (iterator.hasNext()) {
            BidiFrame frame = iterator.next();
            if (Objects.equals(frame.id(), id)) {
                return frame;
            }
            frames.addAll(frame.childFrames());
        }
        return null;
    }

    public void dispose() {
        for (DisposableStack stack : this.disposables) {
            stack.getEmitter().off(stack.getType(), stack.getConsumer());
        }
        this.handles.forEach(BidiElementHandle::dispose);
        for (List<Object> array : this.scripts) {
            BidiFrame frame = (BidiFrame) array.get(0);
            String script = (String) array.get(1);
            try {
                BidiFrameRealm realm = this.isolate ? frame.isolatedRealm() : frame.mainRealm();
                realm.evaluate("name => {\n" +
                        "              delete (globalThis)[name];\n" +
                        "            }", Collections.singletonList(this.name));
                for (BidiFrame childFrame : frame.childFrames()) {
                    try {
                        childFrame.evaluate("name => {\n" +
                                "                delete (globalThis)[name];\n" +
                                "              }", Collections.singletonList(this.name));
                    } catch (Exception e) {
                        LOGGER.error("jvppeteer error", e);
                    }
                }
                frame.browsingContext.removePreloadScript(script);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
    }

}
