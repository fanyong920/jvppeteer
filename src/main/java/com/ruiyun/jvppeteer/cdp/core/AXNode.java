package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.cdp.entities.AXProperty;
import com.ruiyun.jvppeteer.cdp.entities.SerializedAXNode;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


public class AXNode {

    private com.ruiyun.jvppeteer.cdp.entities.AXNode payload;
    private List<AXNode> children = new ArrayList<>();
    private SerializedAXNode iframeSnapshot;

    private boolean richlyEditable = false;
    private boolean editable = false;
    private boolean focusable = false;
    private boolean hidden = false;
    private boolean busy = false;
    private boolean modal = false;
    private boolean hasErrormessage = false;
    private boolean hasDetails = false;
    private String name;
    private String role;
    private String description;
    private String roledescription;
    private String live;
    private boolean ignored = false;
    private Boolean cachedHasFocusableChild;
    private Realm realm;

    private static final String[] userStringProperties = new String[]{
            "name",
            "value",
            "description",
            "keyshortcuts",
            "roledescription",
            "valuetext",
            "url"
    };
    private static final String[] booleanProperties = new String[]{
            "disabled",
            "expanded",
            "focused",
            "modal",
            "multiline",
            "multiselectable",
            "readonly",
            "required",
            "selected",
            "busy",
            "atomic",
    };

    public static final String[] tristateProperties = new String[]{
            "checked",
            "pressed"
    };
    public static final String[] tokenProperties = new String[]{
            "autocomplete",
            "haspopup",
            "invalid",
            "orientation",
            "live",
            "relevant",
            "errormessage",
            "details",
    };

    public static final String[] numericalProperties = new String[]{
            "level",
            "valuemax",
            "valuemin"
    };

    public AXNode() {
    }

    public AXNode(Realm realm, com.ruiyun.jvppeteer.cdp.entities.AXNode payload) {
        this.payload = payload;
        this.role = this.payload.getRole() != null ? String.valueOf(this.payload.getRole().getValue()) : "Unknown";
        this.ignored = payload.getIgnored();
        this.name = Objects.nonNull(this.payload.getName()) ? String.valueOf(this.payload.getName().getValue()) : "";
        this.description = Objects.nonNull(this.payload.getDescription()) ? String.valueOf(this.payload.getDescription().getValue()) : null;
        this.realm = realm;
        List<AXProperty> properties = this.payload.getProperties();
        if (ValidateUtil.isNotEmpty(properties)) {
            for (AXProperty property : properties) {
                if ("editable".equals(property.getName())) {
                    this.richlyEditable = "richtext".equals(property.getValue().getValue());
                    this.editable = true;
                }
                if ("focusable".equals(property.getName())) {
                    this.focusable = (Boolean) property.getValue().getValue();
                }
                if ("hidden".equals(property.getName())) {
                    this.hidden = (Boolean) property.getValue().getValue();
                }
                if ("busy".equals(property.getName())) {
                    Object busyValue = property.getValue().getValue();

                    if (busyValue instanceof Boolean) {
                        this.busy = (Boolean) busyValue;
                    } else if (busyValue instanceof String) {
                        String strValue = ((String) busyValue).toLowerCase().trim();
                        // 根据ARIA规范，只有"true"才表示忙碌状态
                        this.busy = "true".equals(strValue);
                    } else if (busyValue instanceof Number) {
                        // 在Chrome DevTools Protocol中，1通常表示true，0表示false
                        this.busy = ((Number) busyValue).intValue() != 0;
                    } else {
                        // 对于其他类型，转换为字符串再处理
                        this.busy = "true".equalsIgnoreCase(busyValue.toString());
                    }

                }
                if ("live".equals(property.getName())) {
                    this.live = (String) property.getValue().getValue();
                }
                if ("modal".equals(property.getName())) {
                    this.modal = (Boolean) property.getValue().getValue();
                }
                if ("roledescription".equals(property.getName())) {
                    this.roledescription = (String) property.getValue().getValue();
                }
                if ("errormessage".equals(property.getName())) {
                    this.hasErrormessage = true;
                }
                if ("details".equals(property.getName())) {
                    this.hasDetails = true;
                }
            }
        }

    }

    private boolean isPlainTextField() {
        if (this.richlyEditable)
            return false;
        if (this.editable)
            return true;
        return "textbox".equals(this.role) || "searchbox".equals(this.role);
    }

    private boolean isTextOnlyObject() {
        return ("LineBreak".equals(this.role) || "text".equals(this.role) || "InlineTextBox".equals(this.role) || "StaticText".equals(this.role));
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
        if (ValidateUtil.isEmpty(this.children)) {
            return true;
        }
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
            case "image":
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
        return Objects.equals(this.role, "heading") && StringUtil.isNotEmpty(this.name);
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
            case "treeitem":
                return true;
            default:
                return false;
        }
    }

    public boolean isLandmark() {
        switch (this.role) {
            case "banner":
            case "complementary":
            case "contentinfo":
            case "form":
            case "main":
            case "navigation":
            case "region":
            case "search":
                return true;
            default:
                return false;
        }
    }

    public boolean isInteresting(boolean insideControl) {
        if ("Ignored".equals(this.role) || this.hidden || this.ignored)
            return false;

        if (this.isLandmark())
            return true;

        if (
                this.focusable ||
                        this.richlyEditable ||
                        this.busy ||
                        (StringUtil.isNotEmpty(this.live) && !"off".equals(this.live)) ||
                        this.modal ||
                        this.hasErrormessage ||
                        this.hasDetails ||
                        StringUtil.isNotEmpty(this.roledescription)
        )
            return true;

        // If it"s not focusable but has a control role, then it"s interesting.
        if (this.isControl())
            return true;

        // A non-focusable child of a control is not interesting
        if (insideControl)
            return false;

        return this.isLeafNode() && (StringUtil.isNotEmpty(this.name) || StringUtil.isNotEmpty(this.description));
    }

    public SerializedAXNode serialize() throws IntrospectionException, InvocationTargetException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> properties = new HashMap<>();
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
        if (this.payload.getBackendDOMNodeId() == null) {
            node.setElementHandle(null);
        }
        ElementHandle handle = this.realm.adoptBackendNode(this.payload.getBackendDOMNodeId()).asElement();
        node.setElementHandle(handle.evaluateHandle("node => {\n" +
                "  return node.nodeType === Node.TEXT_NODE ? node.parentElement : node;\n" +
                "}").asElement());
        node.setBackendNodeId(this.payload.getBackendDOMNodeId());
        // LoaderId is an experimental mechanism to establish unique IDs across
        // navigations.
        IsolatedWorld world = (IsolatedWorld) this.realm;
        node.setLoadId(world.frame().loaderId());
        for (String userStringProperty : userStringProperties) {
            if (!properties.containsKey(userStringProperty))
                continue;
            PropertyDescriptor propDesc = new PropertyDescriptor(userStringProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(userStringProperty));
        }

        for (String booleanProperty : booleanProperties) {
            // WebArea"s treat focus differently than other nodes. They report whether their frame  has focus,
            // not whether focus is specifically on the root node.
            if ("focused".equals(booleanProperty) && "RootWebArea".equals(this.role))
                continue;
            boolean containsProperty = properties.containsKey(booleanProperty);
            if (!containsProperty)
                continue;
            PropertyDescriptor propDesc = new PropertyDescriptor(booleanProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, containsProperty);
        }

        for (String tristateProperty : tristateProperties) {
            if (!properties.containsKey(tristateProperty))
                continue;
            Object value = properties.get(tristateProperty);
            if ("mixed".equals(value)) {
                PropertyDescriptor propDesc = new PropertyDescriptor(tristateProperty, SerializedAXNode.class);
                propDesc.getWriteMethod().invoke(node, value);
            } else {
                if ("true".equals(value)) {
                    PropertyDescriptor propDesc = new PropertyDescriptor(tristateProperty, SerializedAXNode.class);
                    propDesc.getWriteMethod().invoke(node, true);
                } else {
                    PropertyDescriptor propDesc = new PropertyDescriptor(tristateProperty, SerializedAXNode.class);
                    propDesc.getWriteMethod().invoke(node, false);
                }
            }
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

    public static AXNode createTree(Realm realm, List<com.ruiyun.jvppeteer.cdp.entities.AXNode> payloads) {
        Map<String, AXNode> nodeById = new LinkedHashMap<>();
        for (com.ruiyun.jvppeteer.cdp.entities.AXNode payload : payloads) {
            nodeById.put(payload.getNodeId(), new AXNode(realm, payload));
        }
        for (AXNode node : nodeById.values()) {
            List<String> childIds = node.getPayload().getChildIds();
            if (ValidateUtil.isNotEmpty(childIds)) {
                for (String childId : childIds) {
                    AXNode child = nodeById.get(childId);
                    if (child != null) {
                        node.getChildren().add(child);
                    }
                }
            }

        }
        // Return the first node (root) or null if map is empty
        Iterator<AXNode> iterator = nodeById.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public com.ruiyun.jvppeteer.cdp.entities.AXNode getPayload() {
        return payload;
    }

    public void setPayload(com.ruiyun.jvppeteer.cdp.entities.AXNode payload) {
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

    public SerializedAXNode getIframeSnapshot() {
        return iframeSnapshot;
    }

    public void setIframeSnapshot(SerializedAXNode iframeSnapshot) {
        this.iframeSnapshot = iframeSnapshot;
    }
}
