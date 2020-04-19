package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.context.ExecutionContext;
import com.ruiyun.jvppeteer.protocol.page.frame.Frame;
import com.ruiyun.jvppeteer.protocol.page.frame.FrameManager;
import com.ruiyun.jvppeteer.protocol.target.TimeoutSettings;

public class DOMWorld {

    private FrameManager frameManager;

    private Frame frame;

    private TimeoutSettings timeoutSettings;

    private boolean detached;
    public DOMWorld() {
        super();
    }

    public DOMWorld(FrameManager frameManager, Frame frame, TimeoutSettings timeoutSettings) {
        super();
        this.frameManager = frameManager;
        this.frame = frame;
        this.timeoutSettings = timeoutSettings;
        this.detached = false;
    }

    public String content() {
//        return  this.evaluate();
        return null;
    }

//    private Object evaluate(String pageFunction, PageEvaluateType type Object ...args) {
//    const context =  this.executionContext();
//        return context.evaluate(pageFunction, ...args);
//    }
//
//    public ExecutionContext executionContext() {
//        if (this.detached)
//            throw new RuntimeException("Execution Context is not available in detached frame "+this.frame.getUrl()+" (are you trying to evaluate?)");
//        return this.contextPromise;
//    }
//    public void setContext(ExecutionContext context) {
//        if (context != null) {
//            this.contextResolveCallback.call(null, context);
//            this._contextResolveCallback = null;
//            for (const waitTask of this._waitTasks)
//            waitTask.rerun();
//        } else {
//            this._documentPromise = null;
//            this._contextPromise = new Promise(fulfill => {
//                    this._contextResolveCallback = fulfill;
//      });
//        }
//    }
//
//    public boolean hasContext() {
//        return false;
//    }
//
//    contextResolveCallback
}
