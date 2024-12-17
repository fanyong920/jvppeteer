package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

/**
 * Object containing abbreviated remote object value.
 */
public class ObjectPreview {

    /**
     *对象类型."object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"bigint"
     */
    private String type;
    /**
     * 对象子类型 hint. Specified for `object` type values only.
     * "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"
     */
    private String subtype;
    /**
     * 对象的字符串表示形式。
     */
    private String description;
    /**
     * 如果原始对象的某些属性或条目不合适，则为 True。
     */
    private boolean overflow;
    /**
     * 属性列表。
     */
    private List<PropertyPreview> properties;
    /**
     * 条目列表。仅对映射和设置子类型值指定。
     */
    private List<EntryPreview> entries;

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String subtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean overflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    public List<PropertyPreview> properties() {
        return properties;
    }

    public void setProperties(List<PropertyPreview> properties) {
        this.properties = properties;
    }

    public List<EntryPreview> entries() {
        return entries;
    }

    public void setEntries(List<EntryPreview> entries) {
        this.entries = entries;
    }
}
