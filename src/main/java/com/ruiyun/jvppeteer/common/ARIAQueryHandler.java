package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.core.ElementHandle;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ARIAQueryHandler {

    private final static String ATTRIBUTE_REGEXP = "\\[\\s*(?<attribute>\\w+)\\s*=\\s*(?<quote>\"|')(?<value>\\\\.|.*?(?=\\k<quote>))\\k<quote>\\s*\\]";
    private final ElementHandle element;
    private final String selector;

    public ARIAQueryHandler(ElementHandle element, String selector) {
        this.element = element;
        this.selector = selector;
    }

    public ElementHandle queryOne() throws JsonProcessingException {
        return queryAll().stream().findFirst().get();
    }

    public List<ElementHandle> queryAll() throws JsonProcessingException {
        ARIASelector ariaSelector = parseARIASelector(this.selector);
        return element.queryAXTree(ariaSelector.getName(), ariaSelector.getRole());
    }

    private ARIASelector parseARIASelector(String selector) {
        ARIASelector queryOptions = new ARIASelector();
        Pattern selectorPattern = Pattern.compile(ATTRIBUTE_REGEXP, Pattern.MULTILINE);
        Matcher matcher = selectorPattern.matcher(selector);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            builder.append(matcher.group());
            String attribute = matcher.group("attribute");
            String value = matcher.group("value");
            if (!isKnownAttribute(attribute)) {
                throw new IllegalArgumentException("Unknown aria attribute \"" + attribute + "\" in selector");
            }
            if (attribute.equals("role")) {
                queryOptions.setRole(value);
            } else if (attribute.equals("name")) {
                queryOptions.setName(value);
            }
        }
        String defaultName = selector.replace(builder, "");
        if (StringUtil.isBlank(queryOptions.getName()) && StringUtil.isNotBlank(defaultName)) {
            queryOptions.setName(defaultName);
        }
        return queryOptions;
    }

    private static boolean isKnownAttribute(String attribute) {
        return Arrays.asList("name", "role").contains(attribute);
    }
}
