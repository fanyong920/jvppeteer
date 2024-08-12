package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.transport.CDPSession;

@FunctionalInterface
public  interface Updater<T>{
    void update(CDPSession client, T state );
}