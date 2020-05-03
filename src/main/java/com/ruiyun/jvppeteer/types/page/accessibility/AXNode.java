package com.ruiyun.jvppeteer.types.page.accessibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.protocol.accessbility.AXProperty;
import com.ruiyun.jvppeteer.protocol.accessbility.SerializedAXNode;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AXNode {

    private com.ruiyun.jvppeteer.protocol.accessbility.AXNode payload;

    private List<AXNode> children = new ArrayList<>();

    private boolean richlyEditable = false;

    private boolean editable = false;

    private boolean focusable = false;

    private boolean expanded = false;

    private boolean hidden = false;

    private String name;

    private String role;

    private Boolean cachedHasFocusableChild;

    private static final String[] userStringProperties = new String[]{
            "name",
            "value",
            "description",
            "keyshortcuts",
            "roledescription",
            "valuetext"
    };
    private static final String[] booleanProperties = new String[]{"disabled",
            "expanded",
            "focused",
            "modal",
            "multiline",
            "multiselectable",
            "readonly",
            "required",
            "selected"
    };

    public static final String[] tristateProperties = new String[]{
            "checked",
            "pressed"
    };
    public static final String[] tokenProperties = new String[]{
            "autocomplete",
            "haspopup",
            "invalid",
            "orientation"
    };

    public static final String[] numericalProperties = new String[]{
            "level",
            "valuemax",
            "valuemin"
    };

    public AXNode() {
    }

    public AXNode(com.ruiyun.jvppeteer.protocol.accessbility.AXNode payload) {
        this.payload = payload;
        this.name = this.payload.getName() != null ? String.valueOf(this.payload.getName().getValue()) : "";
        this.role = this.payload.getRole() != null ? String.valueOf(this.payload.getRole().getValue()) : "Unknown";
        List<AXProperty> properties = this.payload.getProperties();
        if (ValidateUtil.isNotEmpty(properties)) {
            for (AXProperty property : properties) {
                if ("editable".equals(property.getName())) {
                    this.richlyEditable = "richtext".equals(property.getValue().getValue());
                    this.editable = true;
                }
                if ("focusable".equals(property.getName()))
                    this.focusable = (Boolean) property.getValue().getValue();
                if ("expanded".equals(property.getName()))
                    this.expanded = (Boolean) property.getValue().getValue();
                if ("hidden".equals(property.getName()))
                    this.hidden = (Boolean) property.getValue().getValue();
            }
        }

    }

    private boolean isPlainTextField() {
        if (this.richlyEditable)
            return false;
        if (this.editable)
            return true;
        return "textbox".equals(this.role) || "ComboBox".equals(this.role) || "searchbox".equals(this.role);
    }

    private boolean isTextOnlyObject() {
        return ("LineBreak".equals(this.role) || "text".equals(this.role) || "InlineTextBox".equals(this.role));
    }

    private boolean hasFocusableChild() {
        if (this.cachedHasFocusableChild == null) {
            this.cachedHasFocusableChild = false;
            for (AXNode child : this.children) {
                if (child.getFocusable() || child.hasFocusableChild()) {
                    this.cachedHasFocusableChild = true;
                    break;
                }
            }
        }
        return this.cachedHasFocusableChild;
    }

    public AXNode find(Predicate<AXNode> predicate) {
        if (predicate.test(this))
            return this;
        for (AXNode child : this.children) {
            AXNode result = child.find(predicate);
            if (result != null)
                return result;
        }
        return null;
    }

    public boolean isLeafNode() {
        if (ValidateUtil.isNotEmpty(this.children))
            return true;

        // These types of objects may have children that we use as internal
        // implementation details, but we want to expose them as leaves to platform
        // accessibility APIs because screen readers might be confused if they find
        // any children.
        if (this.isPlainTextField() || this.isTextOnlyObject())
            return true;

        // Roles whose children are only presentational according to the ARIA and
        // HTML5 Specs should be hidden from screen readers.
        // (Note that whilst ARIA buttons can have only presentational children, HTML5
        // buttons are allowed to have content.)
        switch (this.role) {
            case "doc-cover":
            case "graphics-symbol":
            case "img":
            case "Meter":
            case "scrollbar":
            case "slider":
            case "separator":
            case "progressbar":
                return true;
            default:
                break;
        }

        // Here and below: Android heuristics
        if (this.hasFocusableChild())
            return false;
        if (this.focusable && StringUtil.isNotEmpty(this.name))
            return true;
        if ("heading".equals(this.role) && StringUtil.isNotEmpty(this.name))
            return true;
        return false;
    }

    public boolean isControl() {
        switch (this.role) {
            case "button":
            case "checkbox":
            case "ColorWell":
            case "combobox":
            case "DisclosureTriangle":
            case "listbox":
            case "menu":
            case "menubar":
            case "menuitem":
            case "menuitemcheckbox":
            case "menuitemradio":
            case "radio":
            case "scrollbar":
            case "searchbox":
            case "slider":
            case "spinbutton":
            case "switch":
            case "tab":
            case "textbox":
            case "tree":
                return true;
            default:
                return false;
        }
    }

    public boolean isInteresting(boolean insideControl) {

        if ("Ignored".equals(this.role) || this.hidden)
            return false;

        if (this.focusable || this.richlyEditable)
            return true;

        // If it's not focusable but has a control role, then it's interesting.
        if (this.isControl())
            return true;

        // A non focusable child of a control is not interesting
        if (insideControl)
            return false;

        return this.isLeafNode() && StringUtil.isNotEmpty(this.name);
    }

    public SerializedAXNode serialize() throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> properties = new HashMap<String, Object>();
        List<AXProperty> properties1 = this.payload.getProperties();
        if (ValidateUtil.isNotEmpty(properties1)) {
            for (AXProperty property : properties1)
                properties.put(property.getName().toLowerCase(), property.getValue().getValue());
        }

        if (this.payload.getName() != null)
            properties.put("name", this.payload.getName().getValue());
        if (this.payload.getValue() != null)
            properties.put("value", this.payload.getValue().getValue());
        if (this.payload.getDescription() != null)
            properties.put("description", this.payload.getDescription().getValue());

        SerializedAXNode node = new SerializedAXNode();
        node.setRole(this.role);


        BeanInfo beanInfo = Introspector.getBeanInfo(node.getClass());

        for (String userStringProperty : userStringProperties) {
            if (!properties.containsKey(userStringProperty))
                continue;
            PropertyDescriptor propDesc = new PropertyDescriptor(userStringProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(userStringProperty));
        }


        for (String booleanProperty : booleanProperties) {
            // WebArea's treat focus differently than other nodes. They report whether their frame  has focus,
            // not whether focus is specifically on the root node.
            if ("focused".equals(booleanProperty) && "WebArea".equals(this.role))
                continue;
            boolean value = (Boolean) properties.get(booleanProperty);
            if (!value)
                continue;

            PropertyDescriptor propDesc = new PropertyDescriptor(booleanProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, value);
        }


        for (String tristateProperty : tristateProperties) {
            if (!properties.containsKey(tristateProperty))
                continue;

            PropertyDescriptor propDesc = new PropertyDescriptor(tristateProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(tristateProperty));
        }


        for (String numericalProperty : numericalProperties) {
            if (!properties.containsKey(numericalProperty))
                continue;
            PropertyDescriptor propDesc = new PropertyDescriptor(numericalProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(numericalProperty));
        }


        for (String tokenProperty : tokenProperties) {
            Object value = properties.get(tokenProperty);

            if (value == null || "false".equals(value))
                continue;
            PropertyDescriptor propDesc = new PropertyDescriptor(tokenProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, value);
        }
        return node;
    }

    public static AXNode createTree(List<com.ruiyun.jvppeteer.protocol.accessbility.AXNode> payloads) {
        /** @type {!Map<string, !AXNode>} */
    Map<String,AXNode> nodeById = new HashMap<>();
        for (com.ruiyun.jvppeteer.protocol.accessbility.AXNode payload : payloads)
        nodeById.put(payload.getNodeId(), new AXNode(payload));
        for (AXNode node : nodeById.values()) {
            List<String> childIds = node.getPayload().getChildIds();
            if(ValidateUtil.isNotEmpty(childIds)){
                for (String childId : childIds) {
                    node.getChildren().add(nodeById.get(childId));
                }
            }

        }
        return nodeById.values().iterator().next();
    }
    public com.ruiyun.jvppeteer.protocol.accessbility.AXNode getPayload() {
        return payload;
    }

    public void setPayload(com.ruiyun.jvppeteer.protocol.accessbility.AXNode payload) {
        this.payload = payload;
    }

    public List<AXNode> getChildren() {
        return children;
    }

    public void setChildren(List<AXNode> children) {
        this.children = children;
    }

    public boolean getRichlyEditable() {
        return richlyEditable;
    }

    public void setRichlyEditable(boolean richlyEditable) {
        this.richlyEditable = richlyEditable;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean getFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getCachedHasFocusableChild() {
        return cachedHasFocusableChild;
    }

    public void setCachedHasFocusableChild(boolean cachedHasFocusableChild) {
        this.cachedHasFocusableChild = cachedHasFocusableChild;
    }
}
