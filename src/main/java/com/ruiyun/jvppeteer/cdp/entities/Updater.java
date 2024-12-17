package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.CDPSession;

@FunctionalInterface
public  interface Updater<T>{
    void update(CDPSession client, T state );
}