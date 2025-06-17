package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.events.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.entities.RealmInfo;
import com.ruiyun.jvppeteer.bidi.entities.RealmType;
import com.ruiyun.jvppeteer.bidi.entities.Target;
import com.ruiyun.jvppeteer.common.DisposableStack;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WindowRealm extends BidiRealmCore {

    private final BrowsingContext browsingContext;
    String sandbox;
    private final Map<String, DedicatedWorkerRealm> workers = new ConcurrentHashMap<>();

    protected WindowRealm(BrowsingContext context, String sandbox) {
        super("", "");
        this.browsingContext = context;
        this.sandbox = sandbox;
    }

    public static WindowRealm from(BrowsingContext browsingContext, String sandbox) {
        WindowRealm realm = new WindowRealm(browsingContext, sandbox);
        realm.initialize();
        return realm;
    }

    private void initialize() {
        Consumer<ClosedEvent> closedEventConsumer = event -> {
            this.dispose(event.getReason());
        };
        this.browsingContext.on(BrowsingContext.BrowsingContextEvents.closed, closedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.browsingContext,BrowsingContext.BrowsingContextEvents.closed, closedEventConsumer));
        Consumer<RealmInfo> realmCreatedEventConsumer1 = info -> {
            if (!Objects.equals(RealmType.Window, info.getType()) || !Objects.equals(info.getContext(), this.browsingContext.id()) || !Objects.equals(info.getSandbox(), this.sandbox)) {
                return;
            }
            this.id = info.getRealm();
            this.origin = info.getOrigin();
            this.executionContextId = null;
            this.emit(RealmCoreEvents.updated, this);
        };
        this.session().on(ConnectionEvents.script_realmCreated, realmCreatedEventConsumer1);
        this.disposables.add(new DisposableStack<>(this.session(),ConnectionEvents.script_realmCreated, realmCreatedEventConsumer1));
        Consumer<RealmInfo> realmCreatedEventConsumer2 = info -> {
            if (!Objects.equals(RealmType.DedicatedWorker, info.getType()) || !info.getOwners().contains(this.id)) {
                return;
            }
            DedicatedWorkerRealm dedicatedWorkerRealm = DedicatedWorkerRealm.from(this, info.getRealm(), info.getOrigin());
            this.workers.put(dedicatedWorkerRealm.id, dedicatedWorkerRealm);
            Consumer<Object> destroyedConsumer = ignored -> {
                dedicatedWorkerRealm.removeAllListeners(null);
                this.workers.remove(dedicatedWorkerRealm.id);
            };
            this.emit(RealmCoreEvents.worker,dedicatedWorkerRealm);
            dedicatedWorkerRealm.once(RealmCoreEvents.destroyed, destroyedConsumer);
            this.disposables.add(new DisposableStack<>(dedicatedWorkerRealm,RealmCoreEvents.destroyed, destroyedConsumer));
        };
        this.session().on(ConnectionEvents.script_realmCreated, realmCreatedEventConsumer2);
        this.disposables.add(new DisposableStack<>(this.session(),ConnectionEvents.script_realmCreated, realmCreatedEventConsumer2));
    }

    public Session session() {
        return this.browsingContext.userContext.session();
    }

    @Override
    public Target target() {
        Target target = new Target();
        target.setContext(this.browsingContext.id());
        target.setSandbox(this.sandbox);
        return target;
    }
}
