package com.ruiyun.jvppeteer.cdp.entities;

import java.math.BigDecimal;

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

    private BigDecimal Timestamp;

    private BigDecimal Documents;

    private BigDecimal  Frames;

    private BigDecimal JSEventListeners;

    private BigDecimal Nodes;

    private BigDecimal LayoutCount;

    private BigDecimal RecalcStyleCount;

    private BigDecimal LayoutDuration;

    private BigDecimal RecalcStyleDuration;

    private BigDecimal ScriptDuration;

    private BigDecimal  TaskDuration;

    private BigDecimal JSHeapUsedSize;

    private BigDecimal  JSHeapTotalSize;

    public BigDecimal getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        Timestamp = timestamp;
    }

    public BigDecimal getDocuments() {
        return Documents;
    }

    public void setDocuments(BigDecimal documents) {
        Documents = documents;
    }

    public BigDecimal getFrames() {
        return Frames;
    }

    public void setFrames(BigDecimal frames) {
        Frames = frames;
    }

    public BigDecimal getJSEventListeners() {
        return JSEventListeners;
    }

    public void setJSEventListeners(BigDecimal JSEventListeners) {
        this.JSEventListeners = JSEventListeners;
    }

    public BigDecimal getNodes() {
        return Nodes;
    }

    public void setNodes(BigDecimal nodes) {
        Nodes = nodes;
    }

    public BigDecimal getLayoutCount() {
        return LayoutCount;
    }

    public void setLayoutCount(BigDecimal layoutCount) {
        LayoutCount = layoutCount;
    }

    public BigDecimal getRecalcStyleCount() {
        return RecalcStyleCount;
    }

    public void setRecalcStyleCount(BigDecimal recalcStyleCount) {
        RecalcStyleCount = recalcStyleCount;
    }

    public BigDecimal getLayoutDuration() {
        return LayoutDuration;
    }

    public void setLayoutDuration(BigDecimal layoutDuration) {
        LayoutDuration = layoutDuration;
    }

    public BigDecimal getRecalcStyleDuration() {
        return RecalcStyleDuration;
    }

    public void setRecalcStyleDuration(BigDecimal recalcStyleDuration) {
        RecalcStyleDuration = recalcStyleDuration;
    }

    public BigDecimal getScriptDuration() {
        return ScriptDuration;
    }

    public void setScriptDuration(BigDecimal scriptDuration) {
        ScriptDuration = scriptDuration;
    }

    public BigDecimal getTaskDuration() {
        return TaskDuration;
    }

    public void setTaskDuration(BigDecimal taskDuration) {
        TaskDuration = taskDuration;
    }

    public BigDecimal getJSHeapUsedSize() {
        return JSHeapUsedSize;
    }

    public void setJSHeapUsedSize(BigDecimal JSHeapUsedSize) {
        this.JSHeapUsedSize = JSHeapUsedSize;
    }

    public BigDecimal getJSHeapTotalSize() {
        return JSHeapTotalSize;
    }

    public void setJSHeapTotalSize(BigDecimal JSHeapTotalSize) {
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
