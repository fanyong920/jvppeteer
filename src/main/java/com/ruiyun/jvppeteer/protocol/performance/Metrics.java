package com.ruiyun.jvppeteer.protocol.performance;

public class Metrics {

    private int Timestamp;

    private int Documents;

    private int  Frames;

    private int JSEventListeners;

    private int Nodes;

    private int LayoutCount;

    private int RecalcStyleCount;

    private int LayoutDuration;

    private int RecalcStyleDuration;

    private int ScriptDuration;

    private int  TaskDuration;

    private int JSHeapUsedSize;

    private int  JSHeapTotalSize;

    public int getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(int timestamp) {
        Timestamp = timestamp;
    }

    public int getDocuments() {
        return Documents;
    }

    public void setDocuments(int documents) {
        Documents = documents;
    }

    public int getFrames() {
        return Frames;
    }

    public void setFrames(int frames) {
        Frames = frames;
    }

    public int getJSEventListeners() {
        return JSEventListeners;
    }

    public void setJSEventListeners(int JSEventListeners) {
        this.JSEventListeners = JSEventListeners;
    }

    public int getNodes() {
        return Nodes;
    }

    public void setNodes(int nodes) {
        Nodes = nodes;
    }

    public int getLayoutCount() {
        return LayoutCount;
    }

    public void setLayoutCount(int layoutCount) {
        LayoutCount = layoutCount;
    }

    public int getRecalcStyleCount() {
        return RecalcStyleCount;
    }

    public void setRecalcStyleCount(int recalcStyleCount) {
        RecalcStyleCount = recalcStyleCount;
    }

    public int getLayoutDuration() {
        return LayoutDuration;
    }

    public void setLayoutDuration(int layoutDuration) {
        LayoutDuration = layoutDuration;
    }

    public int getRecalcStyleDuration() {
        return RecalcStyleDuration;
    }

    public void setRecalcStyleDuration(int recalcStyleDuration) {
        RecalcStyleDuration = recalcStyleDuration;
    }

    public int getScriptDuration() {
        return ScriptDuration;
    }

    public void setScriptDuration(int scriptDuration) {
        ScriptDuration = scriptDuration;
    }

    public int getTaskDuration() {
        return TaskDuration;
    }

    public void setTaskDuration(int taskDuration) {
        TaskDuration = taskDuration;
    }

    public int getJSHeapUsedSize() {
        return JSHeapUsedSize;
    }

    public void setJSHeapUsedSize(int JSHeapUsedSize) {
        this.JSHeapUsedSize = JSHeapUsedSize;
    }

    public int getJSHeapTotalSize() {
        return JSHeapTotalSize;
    }

    public void setJSHeapTotalSize(int JSHeapTotalSize) {
        this.JSHeapTotalSize = JSHeapTotalSize;
    }
}
