package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import java.util.List;

public interface ClientProvider {

    List<CDPSession> clients();

    void registerState(EmulatedState<?> state);
}
