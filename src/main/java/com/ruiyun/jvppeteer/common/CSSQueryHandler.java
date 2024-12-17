package com.ruiyun.jvppeteer.common;

public class CSSQueryHandler extends QueryHandler{
    @Override
    public String querySelector() {
        return "(element, selector, { cssQuerySelector }\n" +
                ") => {\n" +
                "  return cssQuerySelector(element, selector);\n" +
                "}";
    }

    @Override
    public String querySelectorAll() {
        return "(element,\n" +
                "  selector,\n" +
                "  { cssQuerySelectorAll }\n" +
                ") => {\n" +
                "  return cssQuerySelectorAll(element, selector);\n" +
                "}";
    }
}
