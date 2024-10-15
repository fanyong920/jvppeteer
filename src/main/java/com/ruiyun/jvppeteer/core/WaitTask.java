package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.entities.EvaluateType;
import com.ruiyun.jvppeteer.entities.WaitTaskOptions;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ruiyun.jvppeteer.util.Helper.throwError;

public class WaitTask {
    private final AtomicInteger runCount;
    private boolean terminated;
    private final AwaitableResult<JSHandle> result = AwaitableResult.create();
    private final Realm world;
    private final String polling;
    private List<?> args = new ArrayList<>();
    private final int timeout;
    private final String predicateBody;

    public WaitTask(Realm world, WaitTaskOptions options, String predicateBody, String predicateQueryHandlerBody, EvaluateType type, List<?> args) {

        this.polling = options.getPolling();
        if (Helper.isNumber(this.polling)) {
            ValidateUtil.assertArg(new BigDecimal(this.polling).compareTo(new BigDecimal(0)) > 0, "Cannot poll with non-positive interval: " + this.polling);
        } else {
            ValidateUtil.assertArg("raf".equals(this.polling) || "mutation".equals(this.polling), "Unknown polling option: " + this.polling);
        }
        this.world = world;
//        this.root = options.getRoot();
        this.timeout = options.getTimeout();


        if (EvaluateType.STRING.equals(type)) {
            this.predicateBody = "return (" + predicateBody + ");";
        } else {
            if (StringUtil.isNotEmpty(predicateQueryHandlerBody)) {
                this.predicateBody = "\n" +
                        "          return (function wrapper(args) {\n" +
                        "            const predicateQueryHandler = " + predicateQueryHandlerBody + ";\n" +
                        "            return (" + predicateBody + ")(...args);\n" +
                        "          })(args);";
            } else {
                this.predicateBody = "return (" + predicateBody + ")(...args);";
            }
        }
        Optional.ofNullable(args).ifPresent(args1 -> this.args = args1);
        this.runCount = new AtomicInteger(0);
        this.world.taskManager.add(this);
        try {
            long start = System.currentTimeMillis();
            this.rerun();
            long end = System.currentTimeMillis();
            if (timeout > 0 && (end - start) > timeout) {
                this.terminate(new RuntimeException(MessageFormat.format("waitForFunction failed: timeout {0}ms exceeded", timeout)));
            }
        } catch (Exception e) {
            this.terminate(e);
        }

    }

    public void rerun() {
        int count = runCount.incrementAndGet();
        Exception error = null;
        JSHandle success = null;
        try {
            List<Object> args = new ArrayList<>();
            args.add(this.predicateBody);
            args.add(this.polling);
            args.add(this.timeout);
            args.addAll(this.args);
            success = this.world.evaluateHandle(waitForPredicatePageFunction(), args);

            if (this.terminated || count != this.runCount.get()) {
                if (success != null)
                    success.dispose();
                return;
            }
        } catch (Exception e) {
            error = e;
        }
        // Ignore timeouts in pageScript - we track timeouts ourselves.
        // If the frame's execution context has already changed, `frame.evaluate` will
        // throw an error - ignore this predicate run altogether.
        boolean isChanged = false;
        try {
            if (success != null) {
                this.world.evaluate("s => !s", Collections.singletonList(success));
            }
        } catch (Exception e) {
            isChanged = true;
        }

        if (error == null && isChanged) {
            success.dispose();
            return;
        }
        // When the page is navigated, the promise is rejected.
        // Try again right away.
        if (error != null && error.getMessage().contains("Execution context was destroyed")) {
            return;
        }
        if (error != null && error.getMessage().contains("Cannot find context with specified id"))
            return;
        if (error != null) {
            throwError(error);
        } else {
            this.result.onSuccess(success);
        }

        this.terminate(null);
    }

    private String waitForPredicatePageFunction() {

        return "async function waitForPredicatePageFunction(predicateBody, polling, timeout, ...args) {\n" +
                "  const predicate = new Function('...args', predicateBody);\n" +
                "  let timedOut = false;\n" +
                "  if (timeout)\n" +
                "    setTimeout(() => timedOut = true, timeout);\n" +
                "  if (polling === 'raf')\n" +
                "    return await pollRaf();\n" +
                "  if (polling === 'mutation')\n" +
                "    return await pollMutation();\n" +
                "  if (typeof polling === 'number')\n" +
                "    return await pollInterval(polling);\n" +
                "\n" +
                "  /**\n" +
                "   * @return {!Promise<*>}\n" +
                "   */\n" +
                "  function pollMutation() {\n" +
                "    const success = predicate.apply(null, args);\n" +
                "    if (success)\n" +
                "      return Promise.resolve(success);\n" +
                "\n" +
                "    let fulfill;\n" +
                "    const result = new Promise(x => fulfill = x);\n" +
                "    const observer = new MutationObserver(mutations => {\n" +
                "      if (timedOut) {\n" +
                "        observer.disconnect();\n" +
                "        fulfill();\n" +
                "      }\n" +
                "      const success = predicate.apply(null, args);\n" +
                "      if (success) {\n" +
                "        observer.disconnect();\n" +
                "        fulfill(success);\n" +
                "      }\n" +
                "    });\n" +
                "    observer.observe(document, {\n" +
                "      childList: true,\n" +
                "      subtree: true,\n" +
                "      attributes: true\n" +
                "    });\n" +
                "    return result;\n" +
                "  }\n" +
                "\n" +
                "  /**\n" +
                "   * @return {!Promise<*>}\n" +
                "   */\n" +
                "  function pollRaf() {\n" +
                "    let fulfill;\n" +
                "    const result = new Promise(x => fulfill = x);\n" +
                "    onRaf();\n" +
                "    return result;\n" +
                "\n" +
                "    function onRaf() {\n" +
                "      if (timedOut) {\n" +
                "        fulfill();\n" +
                "        return;\n" +
                "      }\n" +
                "      const success = predicate.apply(null, args);\n" +
                "      if (success)\n" +
                "        fulfill(success);\n" +
                "      else\n" +
                "        requestAnimationFrame(onRaf);\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  /**\n" +
                "   * @param {number} pollInterval\n" +
                "   * @return {!Promise<*>}\n" +
                "   */\n" +
                "  function pollInterval(pollInterval) {\n" +
                "    let fulfill;\n" +
                "    const result = new Promise(x => fulfill = x);\n" +
                "    onTimeout();\n" +
                "    return result;\n" +
                "\n" +
                "    function onTimeout() {\n" +
                "      if (timedOut) {\n" +
                "        fulfill();\n" +
                "        return;\n" +
                "      }\n" +
                "      const success = predicate.apply(null, args);\n" +
                "      if (success)\n" +
                "        fulfill(success);\n" +
                "      else\n" +
                "        setTimeout(onTimeout, pollInterval);\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public void terminate(Exception error) {
        this.terminated = true;
        this.world.taskManager.delete(this);
        if (error != null && !this.result.isDone()) {
            this.result.complete();
            throwError(error);
        }
    }

    public JSHandle result() {
        return this.result.waitingGetResult(this.timeout, TimeUnit.MILLISECONDS);
    }

}
