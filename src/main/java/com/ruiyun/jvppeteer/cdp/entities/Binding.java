package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.cdp.core.ExecutionContext;
import com.ruiyun.jvppeteer.common.BindingFunction;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Binding {
    private static final Logger LOGGER = LoggerFactory.getLogger(Binding.class);
    private final String name;
    private final BindingFunction fn;
    private final String initSource;

    public Binding(String name, BindingFunction fn, String initSource) {
        this.name = name;
        this.fn = fn;
        this.initSource = initSource;
    }

    public String name() {
        return this.name;
    }

    public String initSource() {
        return this.initSource;
    }

    public void run(ExecutionContext context, int id, List<Object> args, boolean isTrivial) {
        try {
            if (!isTrivial) {
                List<Object> params = new ArrayList<>();
                params.add(this.name);
                params.add(id);
                JSHandle handles = context.evaluateHandle("(name, seq) => {\n" +
                        "            // @ts-expect-error Code is evaluated in a different context.\n" +
                        "            return globalThis[name].args.get(seq);\n" +
                        "          }", params);
                try {
                    Map<String, JSHandle> properties = handles.getProperties();
                    AtomicInteger count = new AtomicInteger();
                    properties.forEach((key, handle) -> {
                        if (count.get() <= args.size()) {
                            if (Objects.equals(handle.remoteObject().getSubtype(), "node")) {
                                args.set(count.get(), handle);
                            } else {
                                handle.dispose();
                            }
                        }
                        count.getAndIncrement();
                    });
                } finally {
                    if (handles != null) {
                        handles.dispose();
                    }
                }
            }
            List<Object> params = new ArrayList<>();
            params.add(this.name);
            params.add(id);
            params.add(this.fn.bind(args));
            context.evaluate("(name, seq, result) => {\n" +
                    "            // @ts-expect-error Code is evaluated in a different context.\n" +
                    "          const callbacks = globalThis[name].callbacks;\n" +
                    "            callbacks.get(seq).resolve(result);\n" +
                    "            callbacks.delete(seq);\n" +
                    "        }", params);

            for (Object arg : args) {
                if (arg instanceof JSHandle) {
                    ((JSHandle) arg).dispose();
                }
            }
        } catch (Exception e) {
            LOGGER.error("jvppeteer error: ", e);
            if (e instanceof EvaluateException) {
                try {
                    List<Object> params = new ArrayList<>();
                    params.add(this.name);
                    params.add(id);
                    params.add(e.getMessage());
                    context.evaluate("(name, seq, message) => {\n" +
                            "              const error = new Error(message);\n" +
                            "                // @ts-expect-error Code is evaluated in a different context.\n" +
                            "              const callbacks = globalThis[name].callbacks;\n" +
                            "                callbacks.get(seq).reject(error);\n" +
                            "                callbacks.delete(seq);\n" +
                            "            }", params);
                } catch (Exception ex) {
                    LOGGER.error("jvppeteer error: ", e);
                }
            } else {
                try {
                    List<Object> params = new ArrayList<>();
                    params.add(this.name);
                    params.add(id);
                    context.evaluate(" (name, seq) => {\n" +
                            "             const error = new Error();\n" +
                            "                    // @ts-expect-error Code is evaluated in a different context.\n" +
                            "              const callbacks = globalThis[name].callbacks;\n" +
                            "                    callbacks.get(seq).reject(error);\n" +
                            "                    callbacks.delete(seq);\n" +
                            "                }", params);
                } catch (Exception ex) {
                    LOGGER.error("jvppeteer error: ", e);
                }
            }
        }
    }
}
