package com.ruiyun.jvppeteer.protocol.runtime;

import java.util.List;

/**
 * Object containing abbreviated remote object value.
 */
public class ObjectPreview {

    /**
     * Object type."object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"bigint"
     */
    private String type;
    /**
     * Object subtype hint. Specified for `object` type values only.
     * "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"
     */
    private String subtype;
    /**
     * String representation of the object.
     */
    private String description;
    /**
     * True iff some of the properties or entries of the original object did not fit.
     */
    private boolean overflow;
    /**
     * List of the properties.
     */
    private List<PropertyPreview> properties;
    /**
     * List of the entries. Specified for `map` and `set` subtype values only.
     */
    private List<EntryPreview> entries;
}
