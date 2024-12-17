package com.ruiyun.jvppeteer.common;

public class PierceQueryHandler extends QueryHandler{
    @Override
    public String querySelector() {
        return "(\n" +
                "  element,\n" +
                "  selector,\n" +
                "  { pierceQuerySelector }\n" +
                ") => {\n" +
                "  return pierceQuerySelector(element, selector);\n" +
                "}";
    }

    @Override
    public String querySelectorAll() {
        return "(\n" +
                "  element,\n" +
                "  selector,\n" +
                "  { pierceQuerySelectorAll }\n" +
                ") => {\n" +
                "  return pierceQuerySelectorAll(element, selector);\n" +
                "}";
    }
}
