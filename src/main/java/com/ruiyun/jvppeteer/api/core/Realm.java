package com.ruiyun.jvppeteer.api.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.bidi.core.BidiElementHandle;
import com.ruiyun.jvppeteer.bidi.core.BidiFrameRealm;
import com.ruiyun.jvppeteer.bidi.core.BidiJSHandle;
import com.ruiyun.jvppeteer.bidi.core.BidiRealm;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.cdp.core.IsolatedWorld;
import com.ruiyun.jvppeteer.cdp.core.WaitTask;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.cdp.entities.WaitTaskOptions;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;
import com.ruiyun.jvppeteer.common.TaskManager;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class Realm {

    protected final TimeoutSettings timeoutSettings;
    public final TaskManager taskManager = new TaskManager();
    private boolean disposed = false;

    public Realm(TimeoutSettings timeoutSettings) {
        this.timeoutSettings = timeoutSettings;
    }

    /**
     * Returns the origin that created the Realm.
     * For example, if the realm was created by an extension content script,
     * this will return the origin of the extension
     * (e.g., `chrome-extension://<extension-id>`).
     * <p>
     * `chrome-extension://<chrome-extension-id>`
     */
    public abstract String getOrigin();

    /**
     * Returns the {@link Extension} that created this realm, if applicable.
     * This is typically populated when the realm was created by an extension
     * content script injected into a page.
     * <p>
     *
     * @returns The {@link Extension} or `null` if not created by an extension.
     *
     */
    public abstract Extension extension();

    public abstract ChromeEnvironment environment();

    public abstract <T extends JSHandle> T adoptHandle(T handle) throws JsonProcessingException;

    public abstract <T extends JSHandle> T transferHandle(T handle) throws JsonProcessingException;

    /**
     * Evaluates a function in the realm's context and returns a
     * {@link JSHandle} to the result.
     * <p>
     * If the function passed to `realm.evaluateHandle` returns a Promise,
     * the method will wait for the promise to resolve and return its value.
     * <p>
     * {@link JSHandle} instances can be passed as arguments to the function.
     * <p>
     * <p>
     * ```ts
     * const aHandle = await realm.evaluateHandle(() => document.body);
     * const resultHandle = await realm.evaluateHandle(
     * body => body.innerHTML,
     * aHandle,
     * );
     * ```
     *
     * @param pptrFunction - A function to be evaluated in the realm.
     * @param args         - Arguments to be passed to the `pageFunction`.
     * @returns A {@link JSHandle} containing the result.
     *
     */
    public abstract JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException;

    public Object evaluate(String pptrFunction) throws JsonProcessingException {
        return this.evaluate(pptrFunction, null);
    }

    public Object evaluate(String pptrFunction, List<Object> args) throws JsonProcessingException {
        return this.evaluate(pptrFunction, null, args);
    }

    public abstract Object evaluate(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException;

    /**
     * Waits for a function to return a truthy value when evaluated in
     * the realm's context.
     * <p>
     * Arguments can be passed from Node.js to `pageFunction`.
     *
     * @param pptrFunction - A function to evaluate in the realm.
     * @param options      - Options for polling and timeouts.
     * @param args         - Arguments to pass to the function.
     * @example ```ts
     * const selector = '.foo';
     * await realm.waitForFunction(
     * selector => !!document.querySelector(selector),
     * {},
     * selector,
     * );
     * ```
     * @returns A promise that resolves when the function returns a truthy value.
     *
     */
    public JSHandle waitForFunction(String pptrFunction, WaitForSelectorOptions options, EvaluateType type, Object... args) throws ExecutionException, InterruptedException, TimeoutException {
        String polling = "raf";
        int timeout = Objects.isNull(options.getTimeout()) ? this.timeoutSettings.timeout() : options.getTimeout();
        if (Objects.nonNull(options.getPolling())) {
            polling = options.getPolling();
        }
        if (Helper.isNumber(polling)) {
            ValidateUtil.assertArg(new BigDecimal(polling).compareTo(new BigDecimal(0)) > 0, "Cannot poll with non-positive interval: " + polling);
        } else {
            ValidateUtil.assertArg("raf".equals(polling) || "mutation".equals(polling), "Unknown polling option: " + polling);
        }
        return new WaitTask(this, new WaitTaskOptions(polling, options.getRoot(), timeout), pptrFunction, type, args).result();
    }

    public IsolatedWorld toIsolatedWorld() {
        return null;
    }

    public boolean disposed() {
        return this.disposed;
    }

    public void dispose() {
        this.disposed = true;
        this.taskManager.terminateAll(new JvppeteerException("waitForFunction failed: frame got detached."));
    }

    public abstract JSHandle adoptBackendNode(int backendNodeId) throws JsonProcessingException;

    public JSHandle createHandle(RemoteValue result) {
        if ((Objects.equals("node", result.getType()) || Objects.equals("window", result.getType())) && this instanceof BidiFrameRealm) {
            return BidiElementHandle.from(result, (BidiFrameRealm) this);
        }
        return BidiJSHandle.from(result, (BidiRealm) this);
    }
}
