package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.transport.CDPSession;

import java.util.List;

public interface ClientProvider {

    List<CDPSession> clients();

    void registerState(EmulatedState<?> state);
}
