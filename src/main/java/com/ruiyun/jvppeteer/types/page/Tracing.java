package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * You can use [`tracing.start`](#tracingstartoptions) and [`tracing.stop`](#tracingstop) to create a trace file which can be opened in Chrome DevTools or [timeline viewer](https://chromedevtools.github.io/timeline-viewer/)
 */
public class Tracing implements Constant {

    /**
     * 当前要trace的 chrome devtools protocol session
     */
    private  CDPSession client;

    /**
     * 判断是否已经在追踪中
     */
    private  boolean recording;

    /**
     * 追踪到的信息要保存的文件路径
     */
    private  String path;


    public  Tracing(CDPSession client) {
        this.client = client;
        this.recording = false;
        this.path = "";
    }

    /**
     * start tracing
     * @param path A path to write the trace file to.
     * @param screenshots captures screenshots in the trace
     * @param categories specify custom categories to use instead of default.
     */
    public void  start(String path,boolean screenshots, Set<String> categories) {
        ValidateUtil.assertBoolean(!this.recording, "Cannot start recording trace while already recording trace.");
       if(ValidateUtil.isEmpty(categories))
           categories = DEFAULTCATEGORIES;
        if (screenshots)
            categories.add("disabled-by-default-devtools.screenshot");
        this.path = path;
        this.recording = true;
        Map<String, Object> params = new HashMap<>();
        params.put("transferMode","ReturnAsStream");
        params.put("categories",String.join(",",categories));
        this.client.send("Tracing.start", params,true);
    }

    /**
     * stop tracing
     */
    public void stop() {
//        this.client.once('Tracing.tracingComplete', event => {
//                helper.readProtocolStream(this._client, event.stream, this._path).then(fulfill);
//    });
        DefaultBrowserListener<JsonNode> traceListener = new DefaultBrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                Tracing tracing = null;
                try {
                    tracing = (Tracing)this.getTarget();
                    Helper.readProtocolStream(tracing.getClient(),event.get(RECV_MESSAGE_STREAM_PROPERTY).asText(),tracing.getPath());
                } catch (IOException ignored) {
                }
            }
        };
        traceListener.setTarget(this);
        traceListener.setMothod("Tracing.tracingComplete");
        this.client.once(traceListener.getMothod(),traceListener);
        this.client.send("Tracing.end",null,true);
        this.recording = false;
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
