package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.WebWorker;

public class BidiWebWorker extends WebWorker {

    private BidiFrame frame;
    private BidiWorkerRealm realm;

    public BidiWebWorker(String url) {
        super(url);
    }

    public BidiWebWorker(BidiFrame frame, DedicatedWorkerRealm realm) {
        super(realm.origin);
        this.frame = frame;
        this.realm = BidiWorkerRealm.from(realm, this);
    }

    public BidiWebWorker(BidiFrame frame, SharedWorkerRealm realm) {
        super(realm.origin);
        this.frame = frame;
        this.realm = BidiWorkerRealm.from(realm, this);
    }

    public static BidiWebWorker from(BidiFrame frame, DedicatedWorkerRealm realm) {
        return new BidiWebWorker(frame, realm);
    }

    public static BidiWebWorker from(BidiFrame frame, SharedWorkerRealm realm) {
        return new BidiWebWorker(frame, realm);
    }

    public BidiFrame frame() {
        return this.frame;
    }

    @Override
    public BidiWorkerRealm mainRealm() {
        return this.realm;
    }

    @Override
    public CDPSession client() {
        throw new UnsupportedOperationException();
    }


}
