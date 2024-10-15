package com.ruiyun.jvppeteer.entities;

public class AXProperty {
    /**
     * The name of this property.
     * "busy"|"disabled"|"editable"|"focusable"|"focused"|"hidden"|"hiddenRoot"|"invalid"|"keyshortcuts"|"settable"|"roledescription"|"live"|"atomic"|"relevant"|"root"|"autocomplete"|"hasPopup"|"level"|"multiselectable"|"orientation"|"multiline"|"readonly"|"required"|"valuemin"|"valuemax"|"valuetext"|"checked"|"expanded"|"modal"|"pressed"|"selected"|"activedescendant"|"controls"|"describedby"|"details"|"errormessage"|"flowto"|"labelledby"|"owns";
     *
     */
    private String name;
    /**
     * The value of this property.
     */
    private AXValue value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AXValue getValue() {
        return value;
    }

    public void setValue(AXValue value) {
        this.value = value;
    }
}
