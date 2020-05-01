package com.ruiyun.jvppeteer.protocol.js;

import com.ruiyun.jvppeteer.protocol.runtime.CustomPreview;
import com.ruiyun.jvppeteer.protocol.runtime.ObjectPreview;

/**
 * Mirror object referencing original JavaScript object.
 */
public class RemoteObject {

    /**
     * Allowed values: object, function, undefined, string, number, boolean, symbol, bigint
     */
    private String type;

    /**
     * Allowed values: array, null, node, regexp, date, map, set, weakmap, weakset, error, proxy, promise, typedarray
     */
    private String subtype;

     /**
     * Object class (constructor) name. Specified for `object` type values only.
     */
    private String className;

    /**
     * Remote object value in case of primitive values or JSON values (if it was requested).
     */
    private Object value;

    /**
     * Allowed values: Infinity, -Infinity, -0, NaN
     */
    private String unserializableValue;

    /**
     * String representation of the object.
     */
    private String description;
    /**
     *Unique object identifier (for non-primitive values).
     */
    private String objectId;

    /**
     * Preview containing abbreviated property values. Specified for `object` type values only.
     */
    private ObjectPreview preview;

    private CustomPreview customPreview;



    public RemoteObject() {
        super();
    }

    public RemoteObject(String type, String subtype, String className, Object value, String unserializableValue, String description, String objectId, ObjectPreview preview, CustomPreview customPreview) {
        super();
        this.type = type;
        this.subtype = subtype;
        this.className = className;
        this.value = value;
        this.unserializableValue = unserializableValue;
        this.description = description;
        this.objectId = objectId;
        this.preview = preview;
        this.customPreview = customPreview;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObjectPreview getPreview() {
        return preview;
    }

    public void setPreview(ObjectPreview preview) {
        this.preview = preview;
    }

    public CustomPreview getCustomPreview() {
        return customPreview;
    }

    public void setCustomPreview(CustomPreview customPreview) {
        this.customPreview = customPreview;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUnserializableValue() {
        return unserializableValue;
    }

    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
    }

}
