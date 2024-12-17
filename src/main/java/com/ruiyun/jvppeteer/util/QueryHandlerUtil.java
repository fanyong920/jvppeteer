package com.ruiyun.jvppeteer.util;


import com.ruiyun.jvppeteer.cdp.entities.SelectorParseResult;
import com.ruiyun.jvppeteer.cdp.entities.Token;
import com.ruiyun.jvppeteer.common.ARIAQueryHandler;
import com.ruiyun.jvppeteer.common.CSSQueryHandler;
import com.ruiyun.jvppeteer.common.PierceQueryHandler;
import com.ruiyun.jvppeteer.common.QueryHandler;
import com.ruiyun.jvppeteer.common.QuerySelector;
import com.ruiyun.jvppeteer.common.TextQueryHandler;
import com.ruiyun.jvppeteer.common.XPathQueryHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryHandlerUtil {

    private static final Map<String, QueryHandler> customQueryHandlers = new HashMap<>();

    private static final List<String> QUERY_SEPARATORS = Arrays.asList("=", "/");

    private static final Pattern attribute = Pattern.compile("\\[\\s*(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?(?<name>[-\\w\\P{ASCII}]+)\\s*(?:(?<operator>\\W?=)\\s*(?<value>.+?)\\s*(\\s(?<caseSensitive>[iIsS]))?\\s*)?\\]", Pattern.UNICODE_CASE);// /gu
    private static final Pattern clazz = Pattern.compile("\\.(?<name>[-\\w\\P{ASCII}]+)", Pattern.UNICODE_CASE);// /gu
    private static final Pattern combinator = Pattern.compile("\\s*(>>>>?|[\\s>+~])\\s*", Pattern.UNICODE_CASE);// /gu
    private static final Pattern comma = Pattern.compile("\\s*,\\s*");// /g
    private static final Pattern id = Pattern.compile("#(?<name>[-\\w\\P{ASCII}]+)", Pattern.UNICODE_CASE);// /gu
    private static final Pattern nesting = Pattern.compile("&");// /g
    private static final Pattern pseudo_class = Pattern.compile(":(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?", Pattern.UNICODE_CASE);// /gu
    private static final Pattern pseudo_element = Pattern.compile("::(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?", Pattern.UNICODE_CASE);// /gu
    private static final Pattern type = Pattern.compile("(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?(?<name>[-\\w\\P{ASCII}]+)", Pattern.UNICODE_CASE);// /gu
    private static final Pattern universal = Pattern.compile("(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?\\*", Pattern.UNICODE_CASE);// /gu
    private static final Map<String, Pattern> patterns = new HashMap<>();

    static {
        patterns.put("attribute", attribute);
        patterns.put("class", clazz);
        patterns.put("combinator", combinator);
        patterns.put("comma", comma);
        patterns.put("id", id);
        patterns.put("nesting", nesting);
        patterns.put("pseudo-class", pseudo_class);
        patterns.put("pseudo-element", pseudo_element);
        patterns.put("type", type);
        patterns.put("universal", universal);
        customQueryHandlers.put("aria", new ARIAQueryHandler());
        customQueryHandlers.put("pierce", new PierceQueryHandler());
        customQueryHandlers.put("xpath", new XPathQueryHandler());
        customQueryHandlers.put("text", new TextQueryHandler());
        customQueryHandlers.put("css", new CSSQueryHandler());

    }

    public static void registerCustomQueryHandler(String name, QueryHandler handler) {
        if (customQueryHandlers.containsKey(name))
            throw new RuntimeException("A custom query handler named " + name + " already exists");
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher isValidName = pattern.matcher(name);
        if (!isValidName.matches())
            throw new IllegalArgumentException("Custom query handler names may only contain [a-zA-Z]");

        customQueryHandlers.put(name, handler);
    }

    public static void unregisterCustomQueryHandler(String name) {
        customQueryHandlers.remove(name);
    }

    public static Map<String, QueryHandler> customQueryHandlers() {
        return customQueryHandlers;
    }

    public void clearQueryHandlers() {
        customQueryHandlers.clear();
    }

    public static QuerySelector getQueryHandlerAndSelector(String selector) {
        for (Map.Entry<String, QueryHandler> entry : customQueryHandlers.entrySet()) {
            String name = entry.getKey();
            QueryHandler queryHandler = entry.getValue();
            for (String separator : QUERY_SEPARATORS) {
                String prefix = name + separator;
                if (selector.startsWith(prefix)) {
                    int index = selector.indexOf(separator);
                    selector = selector.substring(index + 1);
                    return new QuerySelector(selector, queryHandler, Objects.equals("aria", name) ? "raf" : "mutation");
                }
            }
        }
        boolean hasPseudoClasses;
        Pattern pattern = Pattern.compile(":(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?", Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(selector);
        hasPseudoClasses = matcher.find();
        if (hasPseudoClasses) {
            return new QuerySelector(selector, customQueryHandlers.get("css"), "raf");
        } else {
            return new QuerySelector(selector, customQueryHandlers.get("css"), "mutation");
        }
    }

    //todo
    public static SelectorParseResult parsePSelectors(String selector) {
        SelectorParseResult result = new SelectorParseResult();

        List<Token> tokens = tokenize(selector);
        return result;
    }

    public static List<Token> tokenize(String selector) {
        Map<Integer, Token> tokens = new TreeMap<>();
        StringTokenizer st = new StringTokenizer(selector);
        int i = 0;
        while (st.hasMoreElements()) {
            String nextSelector = st.nextToken();
            i++;
            for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
                String type = entry.getKey();
                Pattern pattern = entry.getValue();
                if (Objects.equals("attribute", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        String content = matcher.group();
                        token.setContent(content);
                        token.setType(type);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("class", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setName(content.substring(1));
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("combinator", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("comma", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("id", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        token.setName(content.substring(1));
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("nesting", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("pseudo-class", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("pseudo-element", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        token.setName(content.replace("::", ""));
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("type", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        token.setName(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                } else if (Objects.equals("universal", type)) {
                    Matcher matcher = pattern.matcher(nextSelector);
                    boolean matches = matcher.matches();
                    if (matches) {
                        Token token = new Token();
                        token.setType(type);
                        String content = matcher.group();
                        token.setContent(content);
                        tokens.put(i, token);
                        System.out.println("第" + i + "个： " + nextSelector + " " + token);
                    }
                }

            }
        }

        return new ArrayList<>(tokens.values());
    }

}
