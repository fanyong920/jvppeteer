package com.ruiyun.jvppeteer.protocol.accessbility;

import java.util.List;

public class SerializedAXNode {

    private String role;

    private String name;

    private String value;

    private String description;

    private String keyshortcuts;

    private String roledescription;

    private String valuetext;

    private boolean disabled;

    private boolean expanded;

    private boolean focused;

    private boolean modal;

    private boolean multiline;

    private boolean multiselectable;

    private boolean readonly;

    private boolean required;

    private boolean selected;
    /**
     * boolean|'mixed'
     */
    private String checked;
    /**
     * boolean|'mixed'
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

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean getFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
    }

    public boolean getMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean getMultiselectable() {
        return multiselectable;
    }

    public void setMultiselectable(boolean multiselectable) {
        this.multiselectable = multiselectable;
    }

    public boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
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
}
