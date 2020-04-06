package com.ruiyun.jvppeteer.protocol.coverage;

import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSCoverage {

    private  CDPSession client;

    private  boolean enabled;

    private  Map<Object,Object> scriptSources;

    private  Map<Object,Object> scriptURLs;

    private List<Object> eventListeners;

    private  boolean resetOnNavigation;

    public JSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.scriptURLs = new HashMap<>();
        this.scriptSources = new HashMap();
        this.eventListeners = new ArrayList<>();
        this.resetOnNavigation = false;
    }
}
