package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.transport.CDPSession;

@FunctionalInterface
public  interface Updater<T>{
    void update(CDPSession client, T state );
}