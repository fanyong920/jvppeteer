package com.ruiyun.jvppeteer.entities;

/**
 * Mirror object referencing original JavaScript object.
 */
public class RemoteObject {
    /**
     * Object 类型。. "object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"bigint";
     */
    private String type;
    /**
     * Object 子类型提示。仅对对象类型值指定。 注意：如果您在此处更改了任何内容，请确保同时更新下面的 ObjectPreview 和 PropertyPreview 中的子类型。
     *  "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"|"proxy"|"promise"|"typedarray"|"arraybuffer"|"dataview";
     */
    private String subtype;
    /**
     * 对象类 （constructor） 名称。仅对对象类型值指定。
     */
    private String className;
    /**
     * Remote object value in case of primitive values or JSON values (if it was requested).
     */
    private Object value;
    /**
     * 深度序列化值
     */
    private String unserializableValue;
    /**
     * 对象的字符串表示形式。
     */
    private String description;
    /**
     * 唯一对象标识符（对于非原始值）
     */
    private String objectId;
    /**
     * 包含缩写属性值的预览。仅对对象类型值指定。
     */
    private ObjectPreview preview;

    private CustomPreview customPreview;

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

    public String getUnserializableValue() {
        return unserializableValue;
    }

    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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

    @Override
    public String toString() {
        return "RemoteObject{" +
                "type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", className='" + className + '\'' +
                ", value=" + value +
                ", unserializableValue='" + unserializableValue + '\'' +
                ", description='" + description + '\'' +
                ", objectId='" + objectId + '\'' +
                ", preview=" + preview +
                ", customPreview=" + customPreview +
                '}';
    }
}
