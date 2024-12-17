package com.ruiyun.jvppeteer.bidi.events;

import com.ruiyun.jvppeteer.bidi.core.SharedWorkerRealm;

public class SharedworkerEvent {
    private SharedWorkerRealm realm;

    public SharedWorkerRealm getRealm() {
        return realm;
    }

    public void setRealm(SharedWorkerRealm realm) {
        this.realm = realm;
    }
}
