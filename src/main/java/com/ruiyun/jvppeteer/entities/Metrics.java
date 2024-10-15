package com.ruiyun.jvppeteer.entities;

/**
 * Timestamp 时间点(when the metrics sample was taken)
 * Documents  页面的documents数量。
 * Frames  页面的iframe数量。
 * JSEventListeners  页面的js事件数量。
 * Nodes 页面的dom节点数量。
 * LayoutCount  整页面或部分页面的布局数量。
 * RecalcStyleCount  页面样式重新计算数量。
 * LayoutDuration 页面布局总时间。
 * RecalcStyleDuration  页面样式重新计算总时间。
 * ScriptDuration  页面js代码执行总时间。
 * TaskDuration 页面任务执行总时间。
 * JSHeapUsedSize 页面占用堆内存大小。
 * JSHeapTotalSize  总的页面堆内存大小。
 */
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

    @Override
    public String toString() {
        return "Metrics{" +
                "Timestamp=" + Timestamp +
                ", Documents=" + Documents +
                ", Frames=" + Frames +
                ", JSEventListeners=" + JSEventListeners +
                ", Nodes=" + Nodes +
                ", LayoutCount=" + LayoutCount +
                ", RecalcStyleCount=" + RecalcStyleCount +
                ", LayoutDuration=" + LayoutDuration +
                ", RecalcStyleDuration=" + RecalcStyleDuration +
                ", ScriptDuration=" + ScriptDuration +
                ", TaskDuration=" + TaskDuration +
                ", JSHeapUsedSize=" + JSHeapUsedSize +
                ", JSHeapTotalSize=" + JSHeapTotalSize +
                '}';
    }
}
