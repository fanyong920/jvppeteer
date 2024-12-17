package com.ruiyun.jvppeteer.common;

public class TextQueryHandler extends QueryHandler{
    @Override
    public String querySelector() {
        return "";
    }

    @Override
    public String querySelectorAll() {
        return "(\n" +
                "  element,\n" +
                "  selector,\n" +
                "  {textQuerySelectorAll},\n" +
                ") => {\n" +
                "  return textQuerySelectorAll(element, selector);\n" +
                "}";
    }
}
