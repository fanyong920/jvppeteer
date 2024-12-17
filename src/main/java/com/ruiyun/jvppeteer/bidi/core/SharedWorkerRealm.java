package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.RealmDestroyedParameters;
import com.ruiyun.jvppeteer.bidi.entities.RealmInfo;
import com.ruiyun.jvppeteer.bidi.entities.RealmType;
import com.ruiyun.jvppeteer.common.DisposableStack;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SharedWorkerRealm extends BidiRealmCore {
    private final BrowserCore browser;
    private final Map<String, DedicatedWorkerRealm> workers = new ConcurrentHashMap<>();

    public SharedWorkerRealm(BrowserCore browser, String id, String origin) {
        super(id, origin);
        this.browser = browser;
    }

    public static SharedWorkerRealm from(BrowserCore browserCore, String id, String origin) {
        SharedWorkerRealm realm = new SharedWorkerRealm(browserCore, id, origin);
        realm.initialize();
        return realm;
    }

    private void initialize() {
        Session session = this.session();
        Consumer<RealmDestroyedParameters> realmDestroyedConsumer = info -> {
            if (!Objects.equals(info.getRealm(), this.id)) {
                return;
            }
            this.dispose("Realm already destroyed.");
        };
        session.on(ConnectionEvents.script_realmDestroyed, realmDestroyedConsumer);
        this.disposables.add(new DisposableStack<>(session, ConnectionEvents.script_realmDestroyed, realmDestroyedConsumer));
        Consumer<RealmInfo> realmCreatedEventConsumer = info -> {
            if (!Objects.equals(RealmType.DedicatedWorker, info.getType()) || !info.getOwners().contains(this.id)) {
                return;
            }
            DedicatedWorkerRealm dedicatedWorkerRealm = DedicatedWorkerRealm.from(this, info.getRealm(), info.getOrigin());
            this.workers.put(dedicatedWorkerRealm.id, dedicatedWorkerRealm);
            Consumer<Object> destroyedConsumer = ignored -> this.workers.remove(dedicatedWorkerRealm.id);
            dedicatedWorkerRealm.once(RealmCoreEvents.destroyed, destroyedConsumer);
            this.disposables.add(new DisposableStack<>(dedicatedWorkerRealm, RealmCoreEvents.destroyed, destroyedConsumer));
            this.emit(RealmCoreEvents.worker, dedicatedWorkerRealm);
        };
        session.on(ConnectionEvents.script_realmCreated, realmCreatedEventConsumer);
        this.disposables.add(new DisposableStack<>(session, ConnectionEvents.script_realmCreated, realmCreatedEventConsumer));
    }

    @Override
    public Session session() {
        return this.browser.session();
    }
}
