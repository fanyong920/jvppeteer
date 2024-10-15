package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.core.Realm;
import com.ruiyun.jvppeteer.transport.CDPSession;

public class ChromeEnvironment {
    final CDPSession client;
    final Realm mainRealm;
    public ChromeEnvironment( CDPSession client, Realm mainRealm) {
        this.client = client;
        this.mainRealm = mainRealm;
    }

    public CDPSession client(){
        return client;
    }
    Realm mainRealm(){
        return mainRealm;
    }
}
