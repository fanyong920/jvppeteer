package com.ruiyun.jvppeteer.common;

public class XPathQueryHandler extends QueryHandler{
    @Override
    public String querySelector() {
        return "(\n" +
                "  element,\n" +
                "  selector,\n" +
                "  {xpathQuerySelectorAll}\n" +
                ") => {\n" +
                "  for (const result of xpathQuerySelectorAll(element, selector, 1)) {\n" +
                "    return result;\n" +
                "  }\n" +
                "  return null;\n" +
                "}";
    }

    @Override
    public String querySelectorAll() {
        return "(\n" +
                "  element,\n" +
                "  selector,\n" +
                "  {xpathQuerySelectorAll},\n" +
                ") => {\n" +
                "  return xpathQuerySelectorAll(element, selector);\n" +
                "}";
    }
}
