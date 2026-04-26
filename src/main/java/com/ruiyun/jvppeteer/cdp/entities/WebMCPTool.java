package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.cdp.core.CdpFrame;
import com.ruiyun.jvppeteer.cdp.core.IsolatedWorld;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPTool;
import com.ruiyun.jvppeteer.cdp.events.WebMCPAnnotation;
import com.ruiyun.jvppeteer.cdp.events.WebMCPEvents;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.Objects;

import static com.ruiyun.jvppeteer.common.Constant.MAIN_WORLD;

/**
 * Represents a registered WebMCP tool available on the page.
 */
public class WebMCPTool extends EventEmitter<WebMCPEvents> {
    private Integer backendNodeId;
    private ElementHandle formElement;


    private String name;

    /**
     * Tool name.
     */
    public String name() {
        return name;
    }


    private String description;

    /**
     * Tool description.
     */
    public String description() {
        return description;
    }


    private Object inputSchema;

    /**
     * Schema for the tool's input parameters.
     */
    public Object inputSchema() {
        return inputSchema;
    }

    private WebMCPAnnotation annotations;

    /**
     * Optional annotations for the tool.
     */
    public WebMCPAnnotation annotations() {
        return annotations;
    }


    private Frame frame;

    /**
     * Frame the tool was defined for.
     */
    public Frame frame() {
        return frame;
    }

    private ConsoleMessageLocation location;

    /**
     * Source location that defined the tool (if available).
     */
    public ConsoleMessageLocation location() {
        return location;
    }

    private StackTrace rawStackTrace;

    public StackTrace rawStackTrace() {
        return rawStackTrace;
    }

    public WebMCPTool(ProtocolWebMCPTool tool, Frame frame) {
        super();
        this.name = tool.getName();
        this.description = tool.getDescription();
        this.inputSchema = tool.getInputSchema();
        this.annotations = tool.getAnnotations();
        this.frame = frame;
        this.backendNodeId = tool.getBackendNodeId();

        if (Objects.nonNull(tool.getStackTrace()) && ValidateUtil.isNotEmpty(tool.getStackTrace().getCallFrames())) {
            CallFrame firstFrame = tool.getStackTrace().getCallFrames().get(0);
            this.location = new ConsoleMessageLocation(
                    firstFrame.getUrl(),
                    firstFrame.getLineNumber(),
                    firstFrame.getColumnNumber()
            );
        }
        this.rawStackTrace = tool.getStackTrace();
    }

    /**
     * The corresponding ElementHandle when tool was registered via a form.
     */
    public ElementHandle formElement() throws JsonProcessingException {
        if (Objects.nonNull(this.formElement) && !this.formElement.disposed()) {
            return this.formElement;
        }
        if (this.backendNodeId == null) {
            return null;
        }

        // Note: This assumes CdpFrame and MAIN_WORLD exist in the Java implementation
        CdpFrame cdpFrame = (CdpFrame) this.frame;
        IsolatedWorld mainWorld = cdpFrame.worlds().get(MAIN_WORLD);

        // This would need to be implemented based on the specific Java implementation
        this.formElement = (ElementHandle) mainWorld.adoptBackendNode(this.backendNodeId);
        return this.formElement;
    }
}
