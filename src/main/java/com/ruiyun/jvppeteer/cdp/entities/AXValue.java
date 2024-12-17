package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

/**
 * A single computed AX property.
 */
public class AXValue {

    public AXValue() {
    }

    /**
     * The type of this value.
     * Enum of possible property types.
     * "boolean"|"tristate"|"booleanOrUndefined"|"idref"|"idrefList"|"integer"|"node"|"nodeList"|"number"|"string"|"computedString"|"token"|"tokenList"|"domRelation"|"role"|"internalRole"|"valueUndefined";
     *
     */
    private String type;
    /**
     * The computed value of this property.
     */
    private Object value;
    /**
     * One or more related nodes, if applicable.
     */
    private List<AXRelatedNode> relatedNodes;
    /**
     * The sources which contributed to the computation of this property.
     */
    private List<AXValueSource> sources;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<AXRelatedNode> getRelatedNodes() {
        return relatedNodes;
    }

    public void setRelatedNodes(List<AXRelatedNode> relatedNodes) {
        this.relatedNodes = relatedNodes;
    }

    public List<AXValueSource> getSources() {
        return sources;
    }

    public void setSources(List<AXValueSource> sources) {
        this.sources = sources;
    }
}
