package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ARIAQueryHandler extends QueryHandler {

    private final static String ATTRIBUTE_REGEXP = "\\[\\s*(?<attribute>\\w+)\\s*=\\s*(?<quote>\"|')(?<value>\\\\.|.*?(?=\\k<quote>))\\k<quote>\\s*\\]";

    public ElementHandle queryOne(ElementHandle element, String selector) throws JsonProcessingException {
        return this.queryAll(element, selector).stream().findFirst().orElse(null);
    }

    public List<ElementHandle> queryAll(ElementHandle element, String selector) throws JsonProcessingException {
        ARIASelector ariaSelector = parseARIASelector(selector);
        return element.queryAXTree(ariaSelector.getName(), ariaSelector.getRole());
    }

    private static ARIASelector parseARIASelector(String selector) {
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

    @Override
    public String querySelector() {
        return "async (\n" +
                "  node,\n" +
                "  selector,\n" +
                "  {ariaQuerySelector},\n" +
                ") => {\n" +
                "  return await ariaQuerySelector(node, selector);\n" +
                "}";
    }

    @Override
    public String querySelectorAll() {
        return "";
    }

}
