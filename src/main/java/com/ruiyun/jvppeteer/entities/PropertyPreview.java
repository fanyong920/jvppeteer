package com.ruiyun.jvppeteer.entities;

public class PropertyPreview {
    /**
     * Property name.
     */
    private String name;
    /**
     * Object type. Accessor means that the property itself is an accessor property.
     * "object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"accessor"|"bigint"
     */
    private String type ;
    /**
     * User-friendly property value string.
     */
    private String value;
    /**
     * Nested value preview.
     */
    private ObjectPreview valuePreview;
    /**
     * Object subtype hint. Specified for `object` type values only.
     * "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"
     */
    private String subtype;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ObjectPreview getValuePreview() {
        return valuePreview;
    }

    public void setValuePreview(ObjectPreview valuePreview) {
        this.valuePreview = valuePreview;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}
