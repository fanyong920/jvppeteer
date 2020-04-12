package com.ruiyun.jvppeteer.protocol.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.EmulationManager;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.accessbility.Accessibility;
import com.ruiyun.jvppeteer.protocol.coverage.Coverage;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.protocol.page.frame.*;
import com.ruiyun.jvppeteer.protocol.page.network.NetworkManager;
import com.ruiyun.jvppeteer.protocol.page.network.Response;
import com.ruiyun.jvppeteer.protocol.page.payload.*;
import com.ruiyun.jvppeteer.protocol.page.trace.Tracing;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;
import com.ruiyun.jvppeteer.protocol.target.Target;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;
import com.ruiyun.jvppeteer.protocol.work.Worker;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Page extends EventEmitter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Page.class);

    private Set<Object> fileChooserInterceptors;

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
        this.client.getConnection();
        System.out.println("this.client.getConnection();"+this.client.getConnection());
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
        DefaultBrowserListener<Target> attachedListener = new DefaultBrowserListener<Target>(){
            @Override
            public void onBrowserEvent(Target event) {
                Page page = (Page)this.getTarget();
                    if(!"worker".equals(event.getTargetInfo().getType())){
                        Map<String,Object> params= new HashMap<>();
                        params.put("sessionId",event.getSessionId());
                        /**
                         * If we don't detach from service workers, they will never die
                         */
                        client.send("Target.detachFromTarget",params,false);
                        return;
                    }
                CDPSession session = Connection.fromSession(page.getClient()).session(event.getSessionId());
                Worker worker = new Worker(session,event.getTargetInfo().getUrl(),page::addConsoleMessage,page::handleException);
                page.getWorkers().putIfAbsent(event.getSessionId(),worker);
                page.emit(Events.PAGE_WORKERCREATED.getName(),worker);
            }
        };
        attachedListener.setMothod("Target.attachedToTarget");
        attachedListener.setTarget(this);
        attachedListener.setResolveType(Target.class);
        this.client.on(attachedListener.getMothod(),attachedListener);

        DefaultBrowserListener<Target> detachedListener = new DefaultBrowserListener<Target>(){
            @Override
            public void onBrowserEvent(Target event) {
                Page page = (Page)this.getTarget();
                Worker worker = page.getWorkers().get(event.getSessionId());
                if(worker == null){
                    return;
                }
                page.emit(Events.PAGE_WORKERDESTROYED.getName(),worker);
                page.getWorkers().remove(event.getSessionId());
            }
        };
        detachedListener.setMothod("Target.detachedFromTarget");
        detachedListener.setTarget(this);
        detachedListener.setResolveType(Target.class);
        this.client.on(detachedListener.getMothod(),detachedListener);

        DefaultBrowserListener<Object> frameAttachedListener = new DefaultBrowserListener<Object>(){
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_FRAMEATTACHED.getName(),event);
            }
        };
        frameAttachedListener.setMothod(Events.FRAMEMANAGER_FRAMEATTACHED.getName());
        frameAttachedListener.setTarget(this);
        this.frameManager.on(frameAttachedListener.getMothod(),frameAttachedListener);

        DefaultBrowserListener<Object> frameDetachedListener = new DefaultBrowserListener<Object>(){
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_FRAMEDETACHED.getName(),event);
            }
        };
        frameDetachedListener.setMothod(Events.FRAMEMANAGER_FRAMEDETACHED.getName());
        frameDetachedListener.setTarget(this);
        this.frameManager.on(frameDetachedListener.getMothod(),frameDetachedListener);

        DefaultBrowserListener<Object> frameNavigatedListener = new DefaultBrowserListener<Object>(){
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_FRAMENAVIGATED.getName(),event);
            }
        };
        frameNavigatedListener.setMothod(Events.FRAMEMANAGER_FRAMENAVIGATED.getName());
        frameNavigatedListener.setTarget(this);
        this.frameManager.on(frameNavigatedListener.getMothod(),frameNavigatedListener);

        NetworkManager networkManager = this.frameManager.getNetworkManager();

        DefaultBrowserListener<Request> requestLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_REQUEST.getName(),event);
            }
        };
        requestLis.setMothod(Events.NETWORKMANAGER_REQUEST.getName());
        requestLis.setTarget(this);
        networkManager.on(requestLis.getMothod(),requestLis);

        DefaultBrowserListener<Response> responseLis = new DefaultBrowserListener<Response>() {
            @Override
            public void onBrowserEvent(Response event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_RESPONSE.getName(),event);
            }
        };
        responseLis.setMothod(Events.NETWORKMANAGER_RESPONSE.getName());
        responseLis.setTarget(this);
        networkManager.on(responseLis.getMothod(),responseLis);

        DefaultBrowserListener<Request> requestFailedLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_REQUESTFAILED.getName(),event);
            }
        };
        requestFailedLis.setMothod(Events.NETWORKMANAGER_REQUESTFAILED.getName());
        requestFailedLis.setTarget(this);
        networkManager.on(requestFailedLis.getMothod(),requestFailedLis);

        DefaultBrowserListener<Request> requestFinishedLis = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_REQUESTFINISHED.getName(),event);
            }
        };
        requestFinishedLis.setMothod(Events.NETWORKMANAGER_REQUESTFINISHED.getName());
        requestFinishedLis.setTarget(this);
        networkManager.on(requestFinishedLis.getMothod(),requestFinishedLis);

        //TODO ${fileChooserInterceptors}
        this.fileChooserInterceptors = new HashSet<>();

        DefaultBrowserListener<Object> domContentEventFiredLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_DOMContentLoaded.getName(),event);
            }
        };
        domContentEventFiredLis.setMothod("Page.domContentEventFired");
        domContentEventFiredLis.setTarget(this);
        this.client.on(domContentEventFiredLis.getMothod(),domContentEventFiredLis);

        DefaultBrowserListener<Object> loadEventFiredLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.emit(Events.PAGE_LOAD.getName(),event);
            }
        };
        loadEventFiredLis.setMothod("Page.loadEventFired");
        loadEventFiredLis.setTarget(this);
        this.client.on(loadEventFiredLis.getMothod(),loadEventFiredLis);

        DefaultBrowserListener<ConsoleAPICalledPayload> consoleAPICalledLis = new DefaultBrowserListener<ConsoleAPICalledPayload>() {
            @Override
            public void onBrowserEvent(ConsoleAPICalledPayload event) {
                Page page = (Page)this.getTarget();
                page.onConsoleAPI(event);
            }
        };
        consoleAPICalledLis.setMothod("Runtime.consoleAPICalled");
        consoleAPICalledLis.setTarget(this);
        this.client.on(consoleAPICalledLis.getMothod(),consoleAPICalledLis);

        DefaultBrowserListener<BindingCalledPayload> bindingCalledLis = new DefaultBrowserListener<BindingCalledPayload>() {
            @Override
            public void onBrowserEvent(BindingCalledPayload event) {
                Page page = (Page)this.getTarget();
                page.onBindingCalled(event);
            }
        };
        bindingCalledLis.setMothod("Runtime.bindingCalled");
        bindingCalledLis.setTarget(this);
        this.client.on(bindingCalledLis.getMothod(),bindingCalledLis);

        DefaultBrowserListener<JavascriptDialogOpeningPayload> javascriptDialogOpeningLis = new DefaultBrowserListener<JavascriptDialogOpeningPayload>() {
            @Override
            public void onBrowserEvent(JavascriptDialogOpeningPayload event) {
                Page page = (Page)this.getTarget();
                page.onDialog(event);
            }
        };
        javascriptDialogOpeningLis.setMothod("Page.javascriptDialogOpening");
        javascriptDialogOpeningLis.setTarget(this);
        this.client.on(javascriptDialogOpeningLis.getMothod(),javascriptDialogOpeningLis);

        DefaultBrowserListener<JsonNode> exceptionThrownLis = new DefaultBrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                Page page = (Page)this.getTarget();
                JsonNode exceptionDetails = event.get("exceptionDetails");
                try {
                    if (exceptionDetails == null) {
                        return;
                    }
                    ExceptionDetails value = OBJECTMAPPER.treeToValue(exceptionDetails,ExceptionDetails.class);
                    page.handleException(value);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        exceptionThrownLis.setMothod("Runtime.exceptionThrown");
        exceptionThrownLis.setTarget(this);
        this.client.on(exceptionThrownLis.getMothod(),exceptionThrownLis);

        DefaultBrowserListener<Object> targetCrashedLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                Page page = (Page)this.getTarget();
                page.onTargetCrashed();
            }
        };
        targetCrashedLis.setMothod("Inspector.targetCrashed");
        targetCrashedLis.setTarget(this);
        this.client.on(targetCrashedLis.getMothod(),targetCrashedLis);

        DefaultBrowserListener<MetricsPayload> metricsLis = new DefaultBrowserListener<MetricsPayload>() {
            @Override
            public void onBrowserEvent(MetricsPayload event) {
                Page page = (Page)this.getTarget();
                page.emitMetrics(event);
            }
        };
        metricsLis.setMothod("Inspector.targetCrashed");
        metricsLis.setTarget(this);
        this.client.on(metricsLis.getMothod(),metricsLis);

        DefaultBrowserListener<EntryAddedPayload> entryAddedLis = new DefaultBrowserListener<EntryAddedPayload>() {
            @Override
            public void onBrowserEvent(EntryAddedPayload event) {
                Page page = (Page)this.getTarget();
                page.onLogEntryAdded(event);
            }
        };
        entryAddedLis.setMothod("Log.entryAdded");
        entryAddedLis.setTarget(this);
        this.client.on(entryAddedLis.getMothod(),entryAddedLis);

        DefaultBrowserListener<FileChooserOpenedPayload> fileChooserOpenedLis = new DefaultBrowserListener<FileChooserOpenedPayload>() {
            @Override
            public void onBrowserEvent(FileChooserOpenedPayload event) {
                Page page = (Page)this.getTarget();
                page.onFileChooser(event);
            }
        };
        fileChooserOpenedLis.setMothod("Page.fileChooserOpened");
        fileChooserOpenedLis.setTarget(this);
        this.client.on(fileChooserOpenedLis.getMothod(),fileChooserOpenedLis);

    }

    private void onFileChooser(FileChooserOpenedPayload event){

    }
    private void onLogEntryAdded(EntryAddedPayload event){}

    private void emitMetrics(MetricsPayload event){

    }
    private void onTargetCrashed() {

    }

    public static Page create(CDPSession client, Target target, boolean ignoreHTTPSErrors, Viewport viewport,TaskQueue screenshotTaskQueue){
        Page page = new Page(client,target,ignoreHTTPSErrors,screenshotTaskQueue);
        page.initialize();
        if(viewport != null){
            page.setViewport(viewport);
        }
        return page;
    }


    private void onDialog(JavascriptDialogOpeningPayload event){

    }
    private void onConsoleAPI(ConsoleAPICalledPayload event){

    }

    private void onBindingCalled(BindingCalledPayload event){

    }
    private void setViewport(Viewport viewport) {

    }

    public void initialize(){
        frameManager.initialize();
        Map<String,Object> params = new HashMap<>();
        params.put("autoAttach",true);
        params.put("waitForDebuggerOnStart",false);
        params.put("flatten",true);
        this.client.send("Target.setAutoAttach",params,true);
        this.client.send("Target.setAutoAttach",params,true);
        params.clear();
        this.client.send("Performance.enable",params,true);
        this.client.send("Log.enable",params,true);
    }

    private void addConsoleMessage(String type, List<JSHandle> args, StackTrace stackTrace) {

    }

    private void handleException(ExceptionDetails exceptionDetails) {
        String  message = Helper.getExceptionMessage(exceptionDetails);

    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Map<String, Worker> getWorkers() {
        return workers;
    }

    public boolean getClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
