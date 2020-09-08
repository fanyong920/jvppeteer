package com.ruiyun.jvppeteer.util;


import com.ruiyun.jvppeteer.core.page.QueryHandler;
import com.ruiyun.jvppeteer.core.page.QuerySelector;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryHandlerUtil {

    private static final Map<String, QueryHandler> customQueryHandlers = new HashMap<>();

    public static void registerCustomQueryHandler(String name, QueryHandler handler) {
        if (customQueryHandlers.containsKey(name))
            throw new RuntimeException("A custom query handler named " + name + " already exists");
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher isValidName = pattern.matcher(name);
        if (!isValidName.matches())
            throw new IllegalArgumentException("Custom query handler names may only contain [a-zA-Z]");

        customQueryHandlers.put(name, handler);
    }

    public static final void unregisterCustomQueryHandler(String name) {
        customQueryHandlers.remove(name);
    }

    public static Map<String, QueryHandler> customQueryHandlers() {
        return customQueryHandlers;
    }

    public void clearQueryHandlers() {
        customQueryHandlers.clear();
    }

    public static QuerySelector getQueryHandlerAndSelector(String selector, String defaultQueryHandler) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]+\\/");
        Matcher hasCustomQueryHandler = pattern.matcher(selector);
        if (!hasCustomQueryHandler.find())
            return new QuerySelector(selector, new QueryHandler() {
                @Override
                public String queryOne() {
                    return "(element,selector) =>\n" +
                            "      element.querySelector(selector)";
                }

                @Override
                public String queryAll() {
                    return "(element,selector) =>\n" +
                            "      element.querySelectorAll(selector)";
                }
            });
        int index = selector.indexOf("/");
        String name = selector.substring(0, index);
        String updatedSelector = selector.substring(index + 1);
        QueryHandler queryHandler = customQueryHandlers().get(name);
        if (queryHandler == null)
            throw new RuntimeException("Query set to use " + name + ", but no query handler of that name was found");
        return new QuerySelector(updatedSelector, queryHandler);
    }
}
