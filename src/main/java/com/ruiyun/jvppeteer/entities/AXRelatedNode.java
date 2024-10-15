package com.ruiyun.jvppeteer.entities;

public class AXRelatedNode {

    /**
     * The BackendNodeId of the related DOM node.
     */
    private int backendDOMNodeId;
    /**
     * The IDRef value provided, if any.
     */
    private String idref;
    /**
     * The text alternative of this node in the current context.
     */
    private String text;

    public int getBackendDOMNodeId() {
        return backendDOMNodeId;
    }

    public void setBackendDOMNodeId(int backendDOMNodeId) {
        this.backendDOMNodeId = backendDOMNodeId;
    }

    public String getIdref() {
        return idref;
    }

    public void setIdref(String idref) {
        this.idref = idref;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
