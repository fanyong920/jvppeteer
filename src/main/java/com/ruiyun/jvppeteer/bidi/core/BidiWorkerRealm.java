package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;

public class BidiWorkerRealm extends BidiRealm {
    private final BidiWebWorker worker;

    public static BidiWorkerRealm from(SharedWorkerRealm realm, BidiWebWorker worker) {
        BidiWorkerRealm workerRealm = new BidiWorkerRealm(realm, worker);
        workerRealm.initialize();
        return workerRealm;
    }

    public static BidiWorkerRealm from(DedicatedWorkerRealm realm, BidiWebWorker worker) {
        BidiWorkerRealm workerRealm = new BidiWorkerRealm(realm, worker);
        workerRealm.initialize();
        return workerRealm;
    }

    public BidiWorkerRealm(DedicatedWorkerRealm realm, BidiWebWorker worker) {
        super(realm, worker.timeoutSettings);
        this.worker = worker;
    }

    public BidiWorkerRealm(SharedWorkerRealm realm, BidiWebWorker worker) {
        super(realm, worker.timeoutSettings);
        this.worker = worker;
    }

    public BidiWebWorker worker() {
        return this.worker;
    }

    @Override
    public ChromeEnvironment environment() {
        return null;
    }

    @Override
    public JSHandle adoptBackendNode(int backendNodeId) throws JsonProcessingException {
        throw new UnsupportedOperationException("Cannot adopt DOM nodes into a worker.");
    }
}
