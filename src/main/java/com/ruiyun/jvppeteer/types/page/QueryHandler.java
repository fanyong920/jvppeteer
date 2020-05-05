package com.ruiyun.jvppeteer.types.page;

import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.scene.input.KeyCode.Z;

public class QueryHandler {

    private static final Map<String, String> customQueryHandlers = new HashMap<>();

    //TODO 验证方法
    public static void registerCustomQueryHandler(String name, String handler) {
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

    public static Map<String, String> customQueryHandlers() {
        return customQueryHandlers;
    }

    public void clearQueryHandlers() {
        customQueryHandlers.clear();
    }

    public static QuerySelector getQueryHandlerAndSelector(String selector, String defaultQueryHandler) {
        //TODO 验证
        Pattern pattern = Pattern.compile("^[a-zA-Z]+/");
        Matcher hasCustomQueryHandler = pattern.matcher(selector);
        if (!hasCustomQueryHandler.find())
            return new QuerySelector(selector, defaultQueryHandler);
        int index = selector.indexOf("/");
        String name = selector.substring(0, index);
        String updatedSelector = selector.substring(index + 1);
        String queryHandler = customQueryHandlers().get(name);
        if (StringUtil.isEmpty(queryHandler))
            throw new RuntimeException("Query set to use " + name + ", but no query handler of that name was found");

        return new QuerySelector(updatedSelector, queryHandler);
    }
}
