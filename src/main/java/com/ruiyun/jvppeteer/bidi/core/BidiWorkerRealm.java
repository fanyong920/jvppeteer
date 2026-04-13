package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.events.WebWorkerEvent;
import com.ruiyun.jvppeteer.bidi.entities.LogEntry;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.common.ChromeEnvironment;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.util.Helper.getConsoleMessage;
import static com.ruiyun.jvppeteer.util.Helper.isConsoleLogEntry;

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

    @Override
    protected void initialize() {
        super.initialize();
        this.realm.on(BidiRealmCore.RealmCoreEvents.log, (Consumer<LogEntry>) entry -> {
            if (isConsoleLogEntry(entry) &&
                    this.worker.listenerCount(WebWorkerEvent.Console) > 0) {
                List<JSHandle> args = entry.getArgs().stream().map(arg -> this.createHandle(arg)).collect(Collectors.toList());
                ConsoleMessage message = getConsoleMessage(entry, args, null, this.realm.id);
                this.worker.emit(WebWorkerEvent.Console, message);
            }
        });
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
