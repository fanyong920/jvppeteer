package com.ruiyun.jvppeteer.types.page;

import com.ruiyun.jvppeteer.events.impl.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.exception.TerminateException;
import com.ruiyun.jvppeteer.types.page.frame.Frame;
import com.ruiyun.jvppeteer.types.page.frame.FrameManager;
import com.ruiyun.jvppeteer.types.page.frame.Request;
import com.ruiyun.jvppeteer.types.page.payload.Response;
import com.ruiyun.jvppeteer.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class LifecycleWatcher {


    private FrameManager frameManager;

    private Frame frame;

    private List<String> waitUntil;

    private int timeout;

    private String initialLoaderId;

    private Request navigationRequest;

    private List<BrowserListenerWrapper> eventListeners;

    private boolean hasSameDocumentNavigation;

    public LifecycleWatcher() {
        super();
    }

    public LifecycleWatcher(FrameManager frameManager, Frame frame, List<String> waitUntil, int timeout) {
        super();
        this.frameManager = frameManager;
        this.frame = frame;
        this.waitUntil = waitUntil;
        this.timeout = timeout;
        waitUntil.replaceAll(value -> {
            if("domcontentloaded".equals(value)){
                return "DOMContentLoaded";
            }else if("networkidle0".equals(value)){
                return "networkIdle";
            }else if("networkidle2".equals(value)){
                return "networkAlmostIdle";
            }else if("load".equals(value)){
                return "load";
            }
            throw new IllegalArgumentException("Unknown value for options.waitUntil: "+value);
        } );

        this.initialLoaderId =  frame.getLoaderId();
        this.navigationRequest = null;
        this.eventListeners = new ArrayList<>();

        DefaultBrowserListener<Object> disconnecteListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                LifecycleWatcher watcher = (LifecycleWatcher)this.getTarget();
                watcher.terminate(new TerminateException("Navigation failed because browser has disconnected!"));
            }
        };
        disconnecteListener.setTarget(this);
        disconnecteListener.setMothod(Events.CDPSESSION_DISCONNECTED.getName());

        DefaultBrowserListener<Object> lifecycleEventListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                LifecycleWatcher watcher = (LifecycleWatcher)this.getTarget();
                watcher.checkLifecycleComplete();
            }
        };
        lifecycleEventListener.setTarget(this);
        lifecycleEventListener.setMothod(Events.FRAME_MANAGER_LIFECYCLE_EVENT.getName());

        DefaultBrowserListener<Frame> documentListener = new DefaultBrowserListener<Frame>() {
            @Override
            public void onBrowserEvent(Frame event) {
                LifecycleWatcher watcher = (LifecycleWatcher)this.getTarget();
                watcher.navigatedWithinDocument(event);
            }
        };
        documentListener.setTarget(this);
        documentListener.setMothod(Events.FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT.getName());

        DefaultBrowserListener<Frame> detachedListener = new DefaultBrowserListener<Frame>() {
            @Override
            public void onBrowserEvent(Frame event) {
                LifecycleWatcher watcher = (LifecycleWatcher)this.getTarget();
                watcher.onFrameDetached(event);
            }
        };
        detachedListener.setTarget(this);
        detachedListener.setMothod(Events.FRAME_MANAGER_FRAME_DETACHED.getName());

        DefaultBrowserListener<Request> requestListener = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                LifecycleWatcher watcher = (LifecycleWatcher)this.getTarget();
                watcher.onRequest(event);
            }
        };
        requestListener.setTarget(this);
        requestListener.setMothod(Events.NETWORK_MANAGER_REQUEST.getName());
        eventListeners.add(Helper.addEventListener(this.frameManager.getClient(), disconnecteListener.getMothod(), disconnecteListener)) ;
        eventListeners.add(Helper.addEventListener(this.frameManager, lifecycleEventListener.getMothod(), lifecycleEventListener)) ;
        eventListeners.add(Helper.addEventListener(frameManager, documentListener.getMothod(), documentListener)) ;
        eventListeners.add(Helper.addEventListener(frameManager, detachedListener.getMothod(), detachedListener)) ;
        eventListeners.add(Helper.addEventListener(frameManager.getNetworkManager(), requestListener.getMothod(), requestListener)) ;

        //TODO many promise
    }
    public void lifecyclePromise() {

    }
    public void lifecycleCallback(){
        setNavigateResult("success");
    }

    private void onFrameDetached(Frame frame) {
        if (this.frame.equals(frame)) {
            setNavigateResult("termination");
            return;
        }
        this.checkLifecycleComplete();
    }

    private void onRequest(Request event) {
    }

    private void navigatedWithinDocument(Frame frame) {
        if(this.frame != frame)
            return;
        this.hasSameDocumentNavigation = true;

        this.checkLifecycleComplete();
    }

    private void checkLifecycleComplete() {
    }

    private void terminate(TerminateException e) {
    }

    public String timeoutOrTerminationPromise() {

        return null;
    }

    public String createTimeoutPromise(){

        return null;
    }

    public void dispose() {
        Helper.removeEventListeners(this.eventListeners);
    }

    public Response navigationResponse() {
        return this.navigationRequest != null ? this.navigationRequest.response() : null;
    }


    public void newDocumentNavigationPromise() {
        setNavigateResult("success");
    }


    public void sameDocumentNavigationPromise() {
         setNavigateResult("success");
    }


    private void setNavigateResult(String result) {
        if (this.frameManager.getLatch() != null && this.frameManager.getLatch().getCount() > 0) {
            this.frameManager.setNavigateResult(result);
            this.frameManager.getLatch().countDown();
        }
    }

}

