package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.RealmDestroyedParameters;
import com.ruiyun.jvppeteer.bidi.entities.RealmInfo;
import com.ruiyun.jvppeteer.bidi.entities.RealmType;
import com.ruiyun.jvppeteer.common.DisposableStack;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DedicatedWorkerRealm extends BidiRealmCore {
    private final Set<BidiRealmCore> owners;
    private final Map<String, DedicatedWorkerRealm> workers = new ConcurrentHashMap<>();

    protected DedicatedWorkerRealm(BidiRealmCore owner, String id, String origin) {
        super(id, origin);
        this.owners = new HashSet<>();
        this.owners.add(owner);
    }

    public static DedicatedWorkerRealm from(BidiRealmCore owner, String id, String origin) {
        DedicatedWorkerRealm dedicatedWorkerRealm = new DedicatedWorkerRealm(owner, id, origin);
        dedicatedWorkerRealm.initialize();
        return dedicatedWorkerRealm;
    }

    private void initialize() {
        Consumer<RealmDestroyedParameters> realmDestroyedParametersConsumer = info -> {
            if (!Objects.equals(info.getRealm(), this.id)) {
                return;
            }
            this.dispose("Realm already destroyed.");
        };
        this.session().on(ConnectionEvents.script_realmDestroyed, realmDestroyedParametersConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.script_realmDestroyed, realmDestroyedParametersConsumer));
        Consumer<RealmInfo> realmCreatedEventConsumer = info -> {
            if (!Objects.equals(RealmType.DedicatedWorker, info.getType()) || !info.getOwners().contains(this.id)) {
                return;
            }
            DedicatedWorkerRealm dedicatedWorkerRealm = DedicatedWorkerRealm.from(this, info.getRealm(), info.getOrigin());
            this.workers.put(dedicatedWorkerRealm.id, dedicatedWorkerRealm);
            Consumer<Object> destroyedConsumer = ignored -> {
                this.workers.remove(dedicatedWorkerRealm.id);
            };
            dedicatedWorkerRealm.once(RealmCoreEvents.destroyed, destroyedConsumer);
            this.disposables.add(new DisposableStack<>(dedicatedWorkerRealm, RealmCoreEvents.destroyed, destroyedConsumer));
            this.emit(RealmCoreEvents.worker, dedicatedWorkerRealm);
        };
        this.session().on(ConnectionEvents.script_realmCreated, realmCreatedEventConsumer);
        this.disposables.add(new DisposableStack<>(this.session(), ConnectionEvents.script_realmCreated, realmCreatedEventConsumer));
    }

    public Session session() {
        return this.owners.iterator().next().session();
    }
}
