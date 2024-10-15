package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.AuxData;
import com.ruiyun.jvppeteer.entities.StackTrace;

/**
 * Fired when virtual machine parses script. This event is also fired for all known and uncollected scripts upon enabling debugger.
 */
public class ScriptParsedEvent {
    /**
     * Identifier of the script parsed.
     */
    private String scriptId;
    /**
     * URL or name of the script parsed (if any).
     */
    private String url;
    /**
     * Line offset of the script within the resource with given URL (for script tags).
     */
    private int startLine;
    /**
     * Column offset of the script within the resource with given URL.
     */
    private int startColumn;
    /**
     * Last line of the script.
     */
    private int endLine;
    /**
     * Length of the last line of the script.
     */
    private int endColumn;
    /**
     * Specifies script creation context.
     */
    private int executionContextId;
    /**
     * Content hash of the script.
     */
    private String hash;
    /**
     * Embedder-specific auxiliary data.
     */
    private AuxData executionContextAuxData;
    /**
     * True, if this script is generated as a result of the live edit operation.
     */
    private boolean isLiveEdit;
    /**
     * URL of source map associated with script (if any).
     */
    private String sourceMapURL;
    /**
     * True, if this script has sourceURL.
     */
    private boolean hasSourceURL;
    /**
     * True, if this script is ES6 module.
     */
    private boolean isModule;
    /**
     * This script length.
     */
    private int length;
    /**
     * JavaScript top stack frame of where the script parsed event was triggered if available.
     */
    private StackTrace stackTrace;

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public AuxData getExecutionContextAuxData() {
        return executionContextAuxData;
    }

    public void setExecutionContextAuxData(AuxData executionContextAuxData) {
        this.executionContextAuxData = executionContextAuxData;
    }

    public boolean getIsLiveEdit() {
        return isLiveEdit;
    }

    public void setIsLiveEdit(boolean isLiveEdit) {
        this.isLiveEdit = isLiveEdit;
    }

    public String getSourceMapURL() {
        return sourceMapURL;
    }

    public void setSourceMapURL(String sourceMapURL) {
        this.sourceMapURL = sourceMapURL;
    }

    public boolean getHasSourceURL() {
        return hasSourceURL;
    }

    public void setHasSourceURL(boolean hasSourceURL) {
        this.hasSourceURL = hasSourceURL;
    }

    public boolean getIsModule() {
        return isModule;
    }

    public void setIsModule(boolean isModule) {
        this.isModule = isModule;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return "ScriptParsedEvent{" +
                "scriptId='" + scriptId + '\'' +
                ", url='" + url + '\'' +
                ", startLine=" + startLine +
                ", startColumn=" + startColumn +
                ", endLine=" + endLine +
                ", endColumn=" + endColumn +
                ", executionContextId=" + executionContextId +
                ", hash='" + hash + '\'' +
                ", executionContextAuxData=" + executionContextAuxData +
                ", isLiveEdit=" + isLiveEdit +
                ", sourceMapURL='" + sourceMapURL + '\'' +
                ", hasSourceURL=" + hasSourceURL +
                ", isModule=" + isModule +
                ", length=" + length +
                ", stackTrace=" + stackTrace +
                '}';
    }
}
