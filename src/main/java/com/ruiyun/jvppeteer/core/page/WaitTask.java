package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitTask.class);

    private AtomicInteger runCount;

    private boolean terminated;

    private JSHandle promise;

    private DOMWorld domWorld;

    private String polling;

    private int timeout;

    private String predicateBody;

    private List<Object> args;

    private CountDownLatch waitPromiseLatch;

    public WaitTask(DOMWorld domWorld, String predicateBody, String predicateQueryHandlerBody, PageEvaluateType type, String title, String polling, int timeout, List<Object> args) {
        if (Helper.isNumber(polling)) {
            ValidateUtil.assertArg(new BigDecimal(polling).compareTo(new BigDecimal(0)) > 0, "Cannot poll with non-positive interval: " + polling);
        } else {
            ValidateUtil.assertArg("raf".equals(polling) || "mutation".equals(polling), "Unknown polling option: " + polling);
        }
        this.domWorld = domWorld;
        this.polling = polling;
        this.timeout = timeout;
        if (PageEvaluateType.STRING.equals(type)) {
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
        this.args = args;
        this.runCount = new AtomicInteger(0);
        domWorld.getWaitTasks().add(this);
        // Since page navigation requires us to re-install the pageScript, we should track
        // timeout on our end.

        long start = System.currentTimeMillis();
        this.rerun();
        long end = System.currentTimeMillis();
        if (timeout > 0 && (end - start) > timeout) {
            this.terminate(new RuntimeException(MessageFormat.format("waiting for {0} failed: timeout {1}ms exceeded", title, timeout)));
        }
    }

    public void rerun() {
        int runcount = runCount.incrementAndGet();
        RuntimeException error = null;
        JSHandle success = null;
        try {
            List<Object> args = new ArrayList<>();
            args.add(this.predicateBody);
            args.add(this.polling)  ;
            args.add(this.timeout)  ;
            args.addAll(this.args);
            ExecutionContext context = this.domWorld.executionContext();
            success = (JSHandle) context.evaluateHandle(waitForPredicatePageFunction(),args);
            System.out.println(success);

            if (this.terminated || runcount != this.runCount.get()) {
                if (success != null)
                    success.dispose();
                return;
            }
        } catch (RuntimeException e) {
            error = e;
        }
        // Ignore timeouts in pageScript - we track timeouts ourselves.
        // If the frame's execution context has already changed, `frame.evaluate` will
        // throw an error - ignore this predicate run altogether.
        boolean isChanged = false;
        try {
            this.domWorld.evaluate("s => !s", Arrays.asList(success));
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
            throw error;
        } else {
            this.promise = success;
        }
        if (waitPromiseLatch != null) {
            waitPromiseLatch.countDown();
        }
        this.cleanup();
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

    public void terminate(RuntimeException e) {
        this.terminated = true;
        LOGGER.error("", e);
        this.cleanup();
    }

    private void cleanup() {
        this.domWorld.getWaitTasks().remove(this);
    }

    public JSHandle getPromise() throws InterruptedException {
        if (promise != null) {
            return promise;
        }
        waitPromiseLatch = new CountDownLatch(1);
        waitPromiseLatch.await(this.timeout, TimeUnit.MILLISECONDS);
        return promise;
    }

    public void setPromise(ElementHandle promise) {
        this.promise = promise;
    }
}
