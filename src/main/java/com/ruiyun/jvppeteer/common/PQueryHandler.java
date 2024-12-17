package com.ruiyun.jvppeteer.common;

public class PQueryHandler extends QueryHandler{
    @Override
    public String querySelector() {
        return "(\n" +
                "    element,\n" +
                "    selector,\n" +
                "    {pQuerySelector},\n" +
                "  ) => {\n" +
                "    return pQuerySelector(element, selector);\n" +
                "  }";
    }

    @Override
    public String querySelectorAll() {
        return "(\n" +
                "    element,\n" +
                "    selector,\n" +
                "    {pQuerySelectorAll},\n" +
                "  ) => {\n" +
                "    return pQuerySelectorAll(element, selector);\n" +
                "  }";
    }
}
