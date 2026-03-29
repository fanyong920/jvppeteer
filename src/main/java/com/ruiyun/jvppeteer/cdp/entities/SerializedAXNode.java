package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruiyun.jvppeteer.api.core.ElementHandle;

import java.util.List;

/**
 * Represents a Node and the properties of it that are relevant to Accessibility.
 *
 */
public class SerializedAXNode {
    /**
     * The {@link <a href="https://www.w3.org/TR/wai-aria/#usage_intro">role</a>} of the node.
     */
    private String role;
    /**
     * A human readable name for the node.
     */
    private String name;
    /**
     * The current value of the node.
     */
    private String value;
    /**
     * An additional human readable description of the node.
     */
    private String description;
    /**
     * Any keyboard shortcuts associated with this node.
     */
    private String keyshortcuts;
    /**
     * A human readable alternative to the role.
     */
    private String roledescription;
    /**
     * A description of the current value.
     */
    private String valuetext;

    private Boolean disabled;

    private Boolean expanded;

    private Boolean focused;

    private Boolean modal;

    private Boolean multiline;
    /**
     * Whether more than one child can be selected.
     */
    private Boolean multiselectable;

    private Boolean readonly;

    private Boolean required;

    private Boolean selected;
    /**
     * Whether the checkbox is checked, or in a
     * {@link <a href="https://www.w3.org/TR/wai-aria-practices/examples/checkbox/checkbox-2/checkbox-2.html">mixed state</a>}.
     */
    private String checked;
    /**
     * Boolean|'mixed'
     */
    private String pressed;
    /**
     * The level of a heading.
     */
    private Number level;

    private Number valuemin;

    private Number valuemax;

    private String autocomplete;

    private String haspopup;
    /**
     * Whether and in what way this node's value is invalid.
     */
    private String invalid;

    private String orientation;
    /**
     * Whether the node is {@link <a href="https://www.w3.org/TR/wai-aria/#aria-busy">busy</a>}.
     */
    private Boolean busy;
    /**
     * The {@link <a href="https://www.w3.org/TR/wai-aria/#aria-live">live</a>} status of the
     * node.
     */
    private String live;
    /**
     * Whether the live region is
     * {@link <a href="https://www.w3.org/TR/wai-aria/#aria-atomic">atomic</a>}.
     */
    private Boolean atomic;
    /**
     * The {@link <a href="https://www.w3.org/TR/wai-aria/#aria-relevant">relevant</a>}
     * changes for the live region.
     */
    private String relevant;
    /**
     * The {@link <a href="https://www.w3.org/TR/wai-aria/#aria-errormessage">error message</a>}
     * for the node.
     */
    private String errormessage;
    /**
     * The {@link <a href="https://www.w3.org/TR/wai-aria/#aria-details">details</a>} for the
     * node.
     */
    private String details;
    /**
     * Url for link elements.
     */
    private String url;

    private List<SerializedAXNode> children;
    @JsonIgnore
    private ElementHandle elementHandle;
    /**
     * CDP-specific ID to reference the DOM node.
     */
    private Integer backendNodeId;
    /**
     * CDP-specific documentId.
     */
    private String loadId;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyshortcuts() {
        return keyshortcuts;
    }

    public void setKeyshortcuts(String keyshortcuts) {
        this.keyshortcuts = keyshortcuts;
    }

    public String getRoledescription() {
        return roledescription;
    }

    public void setRoledescription(String roledescription) {
        this.roledescription = roledescription;
    }

    public String getValuetext() {
        return valuetext;
    }

    public void setValuetext(String valuetext) {
        this.valuetext = valuetext;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public Boolean getFocused() {
        return focused;
    }

    public void setFocused(Boolean focused) {
        this.focused = focused;
    }

    public Boolean getModal() {
        return modal;
    }

    public void setModal(Boolean modal) {
        this.modal = modal;
    }

    public Boolean getMultiline() {
        return multiline;
    }

    public void setMultiline(Boolean multiline) {
        this.multiline = multiline;
    }

    public Boolean getMultiselectable() {
        return multiselectable;
    }

    public void setMultiselectable(Boolean multiselectable) {
        this.multiselectable = multiselectable;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public String getPressed() {
        return pressed;
    }

    public void setPressed(String pressed) {
        this.pressed = pressed;
    }

    public Number getLevel() {
        return level;
    }

    public void setLevel(Number level) {
        this.level = level;
    }

    public Number getValuemin() {
        return valuemin;
    }

    public void setValuemin(Number valuemin) {
        this.valuemin = valuemin;
    }

    public Number getValuemax() {
        return valuemax;
    }

    public void setValuemax(Number valuemax) {
        this.valuemax = valuemax;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }

    public String getHaspopup() {
        return haspopup;
    }

    public void setHaspopup(String haspopup) {
        this.haspopup = haspopup;
    }

    public String getInvalid() {
        return invalid;
    }

    public void setInvalid(String invalid) {
        this.invalid = invalid;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public List<SerializedAXNode> getChildren() {
        return children;
    }

    public void setChildren(List<SerializedAXNode> children) {
        this.children = children;
    }

    public void setElementHandle(ElementHandle elementHandle) {
        this.elementHandle = elementHandle;
    }

    public ElementHandle getElementHandle() {
        return elementHandle;
    }

    public Integer getBackendNodeId() {
        return backendNodeId;
    }

    public void setBackendNodeId(Integer backendNodeId) {
        this.backendNodeId = backendNodeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }

    public Boolean getBusy() {
        return busy;
    }

    public void setBusy(Boolean busy) {
        this.busy = busy;
    }

    public String getLive() {
        return live;
    }

    public void setLive(String live) {
        this.live = live;
    }

    public Boolean getAtomic() {
        return atomic;
    }

    public void setAtomic(Boolean atomic) {
        this.atomic = atomic;
    }

    public String getRelevant() {
        return relevant;
    }

    public void setRelevant(String relevant) {
        this.relevant = relevant;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
