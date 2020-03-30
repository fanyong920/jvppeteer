package com.ruiyun.jvppeteer.page;

import com.ruiyun.jvppeteer.events.application.impl.DefaultApplicationListener;
import com.ruiyun.jvppeteer.options.DefaultViewport;
import com.ruiyun.jvppeteer.page.frame.FrameManager;
import com.ruiyun.jvppeteer.protocol.target.Target;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashMap;
import java.util.Map;

public class Page extends DefaultApplicationListener {

    private CDPSession client;

    private Target target;

    private boolean ignoreHTTPSErrors;

    private TaskQueue screenshotTaskQueue;

    private FrameManager frameManager;

    public Page(CDPSession client, Target target, boolean ignoreHTTPSErrors, TaskQueue screenshotTaskQueue) {
        super();
        this.client = client;
        this.target = target;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.screenshotTaskQueue = screenshotTaskQueue;
    }

    public static Page create(CDPSession client, Target target, boolean ignoreHTTPSErrors, DefaultViewport defaultViewport,TaskQueue screenshotTaskQueue){
        Page page = new Page(client,target,ignoreHTTPSErrors,screenshotTaskQueue);
        page.initialize();
        if(defaultViewport != null){
            page.setViewport(defaultViewport);
        }
        return page;
    }

    private void setViewport(DefaultViewport defaultViewport) {

    }

    public void initialize(){
        frameManager.initialize();
        Map<String,Object> params = new HashMap<>();
        params.put("autoAttach",true);
        params.put("waitForDebuggerOnStart",false);
        params.put("flatten",true);
        this.client.send("Target.setAutoAttach",params);
        params.clear();
        this.client.send("Performance.enable",params);
        this.client.send("Log.enable",params);
    }
}
