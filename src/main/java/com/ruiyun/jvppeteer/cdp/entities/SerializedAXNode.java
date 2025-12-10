package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.ElementHandle;
import java.util.List;

public class SerializedAXNode {

    private String role;

    private String name;

    private String value;

    private String description;

    private String keyshortcuts;

    private String roledescription;

    private String valuetext;

    private Boolean disabled;

    private Boolean expanded;

    private Boolean focused;

    private Boolean modal;

    private Boolean multiline;

    private Boolean multiselectable;

    private Boolean readonly;

    private Boolean required;

    private Boolean selected;
    /**
     * Boolean|'mixed'
     */
    private String checked;
    /**
     * Boolean|'mixed'
     */
    private String pressed;

    private Number level;

    private Number valuemin;

    private Number valuemax;

    private String autocomplete;

    private String haspopup;

    private String invalid;

    private String orientation;

    private List<SerializedAXNode> children;

    private ElementHandle elementHandle;

    private Integer backendNodeId;

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

    @Override
    public String toString() {
        return "SerializedAXNode{" +
                "role='" + role + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                ", keyshortcuts='" + keyshortcuts + '\'' +
                ", roledescription='" + roledescription + '\'' +
                ", valuetext='" + valuetext + '\'' +
                ", disabled=" + disabled +
                ", expanded=" + expanded +
                ", focused=" + focused +
                ", modal=" + modal +
                ", multiline=" + multiline +
                ", multiselectable=" + multiselectable +
                ", readonly=" + readonly +
                ", required=" + required +
                ", selected=" + selected +
                ", checked='" + checked + '\'' +
                ", pressed='" + pressed + '\'' +
                ", level=" + level +
                ", valuemin=" + valuemin +
                ", valuemax=" + valuemax +
                ", autocomplete='" + autocomplete + '\'' +
                ", haspopup='" + haspopup + '\'' +
                ", invalid='" + invalid + '\'' +
                ", orientation='" + orientation + '\'' +
                ", children=" + children +
                ", elementHandle=" + elementHandle +
                ", backendNodeId=" + backendNodeId +
                '}';
    }


}
