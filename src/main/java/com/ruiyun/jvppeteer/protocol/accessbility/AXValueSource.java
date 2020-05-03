package com.ruiyun.jvppeteer.protocol.accessbility;

/**
 * A single source for a computed AX property.
 */
public class AXValueSource {
    /**
     * What type of source this is.
     * "attribute"|"implicit"|"style"|"contents"|"placeholder"|"relatedElement";
     *
     */
    private String type;
    /**
     * The value of this property source.
     */
    private AXNode value;
    /**
     * The name of the relevant attribute, if any.
     */
    private String attribute;
    /**
     * The value of the relevant attribute, if any.
     */
    private AXValue attributeValue;
    /**
     * Whether this source is superseded by a higher priority source.
     */
    private boolean superseded;
    /**
     * The native markup source for this value, e.g. a <label> element.
     * "figcaption"|"label"|"labelfor"|"labelwrapped"|"legend"|"tablecaption"|"title"|"other";
     *
     */
    private String nativeSource;
    /**
     * The value, such as a node or node list, of the native source.
     */
    private AXValue nativeSourceValue;
    /**
     * Whether the value for this property is invalid.
     */
    private boolean invalid;
    /**
     * Reason for the value being invalid, if it is.
     */
    private String invalidReason;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AXNode getValue() {
        return value;
    }

    public void setValue(AXNode value) {
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public AXValue getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(AXValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    public boolean getSuperseded() {
        return superseded;
    }

    public void setSuperseded(boolean superseded) {
        this.superseded = superseded;
    }

    public String getNativeSource() {
        return nativeSource;
    }

    public void setNativeSource(String nativeSource) {
        this.nativeSource = nativeSource;
    }

    public AXValue getNativeSourceValue() {
        return nativeSourceValue;
    }

    public void setNativeSourceValue(AXValue nativeSourceValue) {
        this.nativeSourceValue = nativeSourceValue;
    }

    public boolean getInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
