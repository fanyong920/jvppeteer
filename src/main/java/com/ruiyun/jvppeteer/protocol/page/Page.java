package com.ruiyun.jvppeteer.protocol.page;

import com.ruiyun.jvppeteer.EmulationManager;
import com.ruiyun.jvppeteer.protocol.page.trace.Tracing;
import com.ruiyun.jvppeteer.events.application.impl.DefaultApplicationListener;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.accessbility.Accessibility;
import com.ruiyun.jvppeteer.protocol.coverage.Coverage;
import com.ruiyun.jvppeteer.protocol.page.frame.*;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.protocol.work.Worker;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Page extends DefaultApplicationListener {

    private boolean closed = true;

    private CDPSession client;

    private Target target;

    private Keyboard keyboard;

    private Mouse mouse;

    private TimeoutSettings timeoutSettings;

    private Touchscreen touchscreen;

    private Accessibility accessibility;

    private FrameManager frameManager;

    private EmulationManager emulationManager;

    private Tracing tracing;

    private Map<String, Function> pageBindings;

    private Coverage coverage;

    private  boolean javascriptEnabled;

    private Viewport viewport;

    private TaskQueue screenshotTaskQueue;

    private Map<String, Worker> workers;


    public  static final String  ABOUT_BLANK = "about:blank".intern();



    public Page(CDPSession client, Target target, boolean ignoreHTTPSErrors, TaskQueue screenshotTaskQueue) {
        super();
        this.closed = false;
        this.client = client;
        this.target = target;
        this.keyboard = new Keyboard(client);
        this.mouse = new Mouse(client,keyboard);
        this.timeoutSettings = new TimeoutSettings();
        this.touchscreen = new Touchscreen(client,keyboard);
        this.accessibility = new Accessibility(client);
        this.frameManager = new FrameManager(client,this,ignoreHTTPSErrors,timeoutSettings);
        this.emulationManager = new EmulationManager(client);
        this.tracing = new Tracing(client);
        this.pageBindings = new HashMap<>();
        this.coverage = new Coverage(client);
        this.javascriptEnabled = true;
        this.viewport = null;
        this.screenshotTaskQueue = screenshotTaskQueue;
        this.workers = new HashMap<>();
        //TODO

    }

    public static Page create(CDPSession client, Target target, boolean ignoreHTTPSErrors, Viewport viewport,TaskQueue screenshotTaskQueue){
        Page page = new Page(client,target,ignoreHTTPSErrors,screenshotTaskQueue);
        page.initialize();
        if(viewport != null){
            page.setViewport(viewport);
        }
        return page;
    }

    private void setViewport(Viewport viewport) {

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
