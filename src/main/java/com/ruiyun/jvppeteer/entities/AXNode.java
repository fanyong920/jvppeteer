package com.ruiyun.jvppeteer.entities;

import java.util.List;

/**
 * A node in the accessibility tree.
 */
public class AXNode {
    public AXNode() {
    }

    /**
     * Unique identifier for this node.
     */
    private String nodeId;
    /**
     * Whether this node is ignored for accessibility
     */
    private boolean ignored;
    /**
     * Collection of reasons why this node is hidden.
     */
    private List<AXProperty> ignoredReasons;
    /**
     * This `Node`'s role, whether explicit or implicit.
     */
    private AXValue role;
    /**
     * The accessible name for this `Node`.
     */
    private AXValue name;
    /**
     * The accessible description for this `Node`.
     */
    private AXValue description;
    /**
     * The value for this `Node`.
     */
    private AXValue value;
    /**
     * All other properties
     */
    private List<AXProperty> properties;
    /**
     * ID for this node's parent.
     */
    private String parentId;
    /**
     * IDs for each of this node's child nodes.
     */
    private List<String> childIds;
    /**
     * The backend ID for the associated DOM node, if any.
     */
    private Integer backendDOMNodeId;
    /**
     * The frame ID for the frame associated with this nodes document.
     */
    private String frameId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean getIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public List<AXProperty> getIgnoredReasons() {
        return ignoredReasons;
    }

    public void setIgnoredReasons(List<AXProperty> ignoredReasons) {
        this.ignoredReasons = ignoredReasons;
    }

    public AXValue getRole() {
        return role;
    }

    public void setRole(AXValue role) {
        this.role = role;
    }

    public AXValue getName() {
        return name;
    }

    public void setName(AXValue name) {
        this.name = name;
    }

    public AXValue getDescription() {
        return description;
    }

    public void setDescription(AXValue description) {
        this.description = description;
    }

    public AXValue getValue() {
        return value;
    }

    public void setValue(AXValue value) {
        this.value = value;
    }

    public List<AXProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<AXProperty> properties) {
        this.properties = properties;
    }

    public List<String> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<String> childIds) {
        this.childIds = childIds;
    }

    public Integer getBackendDOMNodeId() {
        return backendDOMNodeId;
    }

    public void setBackendDOMNodeId(Integer backendDOMNodeId) {
        this.backendDOMNodeId = backendDOMNodeId;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
