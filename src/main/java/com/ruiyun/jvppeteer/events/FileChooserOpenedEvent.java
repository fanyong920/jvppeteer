package com.ruiyun.jvppeteer.events;

/**
 * Emitted only when `page.interceptFileChooser` is enabled.
 */
public class FileChooserOpenedEvent {

    /**
     * Id of the frame containing input node.
     */
    private String frameId;
    /**
     * Input node id.
     */
    private int backendNodeId;
    /**
     * Input mode.
     * "selectSingle"|"selectMultiple"
     */
    private String mode;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public int getBackendNodeId() {
        return backendNodeId;
    }

    public void setBackendNodeId(int backendNodeId) {
        this.backendNodeId = backendNodeId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
