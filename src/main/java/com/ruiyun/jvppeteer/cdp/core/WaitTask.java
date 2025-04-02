package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.WaitTaskOptions;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.LazyArg;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.java_websocket.util.NamedThreadFactory;


import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class WaitTask {
    private final Realm world;
    private final String polling;
    private final List<Object> args = new ArrayList<>();
    private final String fn;
    private final ElementHandle root;
    public static final ExecutorService waitTaskService = Executors.newCachedThreadPool(new NamedThreadFactory("jvppeteer-waitTask-service"));
    private final int timeout;
    private volatile JSHandle poller;
    private final AwaitableResult<JSHandle> result = AwaitableResult.create();
    private final CompletableFuture<JSHandle> rerunFuture;

    public WaitTask(Realm world, WaitTaskOptions options, String pptrFunction, EvaluateType type, Object... args) {
        this.world = world;
        this.polling = options.getPolling();
        this.root = options.getRoot();
        this.timeout = options.getTimeout();
        if (EvaluateType.STRING.equals(type)) {
            this.fn = "() => {return (" + pptrFunction + ");}";
        } else {
            this.fn = pptrFunction;
        }
        Optional.ofNullable(args).ifPresent(args1 -> this.args.addAll(Arrays.asList(args1)));
        this.world.taskManager.add(this);
        this.rerunFuture = CompletableFuture.supplyAsync(this::rerun, waitTaskService);
    }

    public JSHandle rerun() {
        try {
            switch (this.polling) {
                case "raf":
                    List<Object> args = new ArrayList<>();
                    args.add(new LazyArg());
                    args.add(this.fn);
                    args.addAll(this.args);
                    this.poller = this.world.evaluateHandle("({RAFPoller, createFunction}, fn, ...args) => {\n" +
                            "  const fun = createFunction(fn);\n" +
                            "  return new RAFPoller(() => {\n" +
                            "    return fun(...args);\n" +
                            "  });\n" +
                            "}", args);
                    break;
                case "mutation":
                    List<Object> args1 = new ArrayList<>();
                    args1.add(new LazyArg());
                    args1.add(this.root);
                    args1.add(this.fn);
                    args1.addAll(this.args);
                    this.poller = this.world.evaluateHandle("({MutationPoller, createFunction}, root, fn, ...args) => {\n" +
                            "  const fun = createFunction(fn);\n" +
                            "  return new MutationPoller(() => {\n" +
                            "    return fun(...args);\n" +
                            "  }, root || document);\n" +
                            "}", args1);
                    break;
                default:
                    List<Object> args2 = new ArrayList<>();
                    args2.add(new LazyArg());
                    args2.add(this.polling);
                    args2.add(this.fn);
                    args2.addAll(this.args);
                    this.poller = this.world.evaluateHandle("({IntervalPoller, createFunction}, ms, fn, ...args) => {\n" +
                            "  const fun = createFunction(fn);\n" +
                            "  return new IntervalPoller(() => {\n" +
                            "    return fun(...args);\n" +
                            "  }, ms);\n" +
                            "}", args2);

            }
            this.poller.evaluate("poller => {\n" +
                    "  void poller.start();\n" +
                    "}");

            JSHandle result = this.poller.evaluateHandle("poller => {\n" +
                    "        return poller.result();\n" +
                    "      }");
            this.terminate(null);
            return result;
        } catch (Exception error) {
            Throwable badError = this.getBadError(error);
            if (Objects.nonNull(badError)) {
                this.terminate(badError);
            }
            return null;
        }

    }

    public void terminate(Throwable error) {
        this.world.taskManager.delete(this);
        if (Objects.nonNull(this.poller)) {
            try {
                this.poller.evaluate("async poller => {\n" +
                        "          await poller.stop();\n" +
                        "        }");
                if (Objects.nonNull(this.poller)) {
                    this.poller.dispose();
                    this.poller = null;
                }
            } catch (Exception ignored) {
                // Ignore errors since they most likely come from low-level cleanup.
            }
        }
        if (error != null && !this.result.isDone()) {
            this.result.complete();
            throwError(error);
        }
    }

    public JSHandle result() throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        if (this.timeout == 0) {
            return this.rerunFuture.get();
        } else {
            return this.rerunFuture.get(this.timeout, TimeUnit.MILLISECONDS);
        }
    }

    private Throwable getBadError(Throwable error) {
        if (error instanceof EvaluateException) {
            // When frame is detached the task should have been terminated by the IsolatedWorld.
            // This can fail if we were adding this task while the frame was detached,
            // so we terminate here instead.
            if (StringUtil.isNotEmpty(error.getMessage()) && error.getMessage().contains("Execution context is not available in detached frame")) {
                return new JvppeteerException("Waiting failed: Frame detached");
            }
            // When the page is navigated, the promise is rejected.
            // We will try again in the new execution context.
            if (StringUtil.isNotEmpty(error.getMessage()) && error.getMessage().contains("Execution context was destroyed")) {
                return null;
            }

            // We could have tried to evaluate in a context which was already
            // destroyed.
            if (StringUtil.isNotEmpty(error.getMessage()) && error.getMessage().contains("Cannot find context with specified id")) {
                return null;
            }

            // Errors coming from WebDriver BiDi. TODO: Adjust messages after
            // https://github.com/w3c/webdriver-bidi/issues/540 is resolved.
            if (StringUtil.isNotEmpty(error.getMessage()) && error.getMessage().contains("AbortError: Actor 'MessageHandlerFrame' destroyed")) {
                return null;
            }
            return error;
        }
        return new JvppeteerException("WaitTask failed with an error: " + error.getMessage(), error);
    }

}
