package com.ruiyun.jvppeteer.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;
import com.ruiyun.jvppeteer.common.TaskManager;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.entities.WaitTaskOptions;
import com.ruiyun.jvppeteer.entities.EvaluateType;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.List;

public abstract class Realm {

    protected final TimeoutSettings timeoutSettings;
    public final TaskManager taskManager = new TaskManager();
    private boolean disposed = false;

    public Realm(TimeoutSettings timeoutSettings) {
        this.timeoutSettings = timeoutSettings;
    }

    public abstract ChromeEnvironment environment();

    public abstract JSHandle adoptHandle(JSHandle handle) throws JsonProcessingException, EvaluateException;

    public abstract <T extends JSHandle> T transferHandle(T handle) throws JsonProcessingException;

    public abstract JSHandle evaluateHandle(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException;

    public abstract Object evaluate(String pageFunction, List<Object> args) throws JsonProcessingException, EvaluateException;


    public JSHandle waitForFunction(String pageFunction, String predicateQueryHandlerBody, WaitForSelectorOptions options, List<?> args) {
        String polling = "raf";
        int timeout = this.timeoutSettings.timeout();

        if (StringUtil.isNotEmpty(options.getPolling())) {
            if (Helper.isNumber(options.getPolling())) {
                if (Integer.parseInt(options.getPolling()) < 0) {
                    throw new IllegalArgumentException("Cannot poll with non-positive interval");
                }
            } else {
                polling = options.getPolling();
            }
        }
        if (options.getTimeout() != null) {
            timeout = options.getTimeout();
        }
        return new WaitTask(this, new WaitTaskOptions(polling, timeout, options.getRoot(), true), pageFunction, predicateQueryHandlerBody, Helper.isFunction(pageFunction) ? EvaluateType.FUNCTION : EvaluateType.STRING, args).result();
    }

    protected IsolatedWorld toIsolatedWorld() {
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

}
