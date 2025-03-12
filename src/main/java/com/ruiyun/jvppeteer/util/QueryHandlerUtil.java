package com.ruiyun.jvppeteer.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.entities.CompoundPSelector;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.SelectorParseResult;
import com.ruiyun.jvppeteer.cdp.entities.Token;
import com.ruiyun.jvppeteer.common.ARIAQueryHandler;
import com.ruiyun.jvppeteer.common.CSSQueryHandler;
import com.ruiyun.jvppeteer.common.PQueryHandler;
import com.ruiyun.jvppeteer.common.PierceQueryHandler;
import com.ruiyun.jvppeteer.common.QueryHandler;
import com.ruiyun.jvppeteer.common.QuerySelector;
import com.ruiyun.jvppeteer.common.TextQueryHandler;
import com.ruiyun.jvppeteer.common.XPathQueryHandler;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.ruiyun.jvppeteer.common.Constant.OBJECTMAPPER;
import static java.util.regex.Pattern.MULTILINE;

public class QueryHandlerUtil {

    private static final Map<String, QueryHandler> customQueryHandlers = new HashMap<>();

    private static final List<String> QUERY_SEPARATORS = Arrays.asList("=", "/");

    static {
        customQueryHandlers.put("aria", new ARIAQueryHandler());
        customQueryHandlers.put("pierce", new PierceQueryHandler());
        customQueryHandlers.put("xpath", new XPathQueryHandler());
        customQueryHandlers.put("text", new TextQueryHandler());
        customQueryHandlers.put("css", new CSSQueryHandler());
        customQueryHandlers.put("p", new PQueryHandler());

    }

    public static void registerCustomQueryHandler(String name, QueryHandler handler) {
        if (customQueryHandlers.containsKey(name))
            throw new JvppeteerException("A custom query handler named " + name + " already exists");
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

    public static QuerySelector getQueryHandlerAndSelector(String selector, Frame frame) {
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
        try {
            SelectorParseResult selectorParseResult = parsePSelectors(selector, frame);
            if (selectorParseResult.getIsPureCSS()) {
                return new QuerySelector(selector, customQueryHandlers.get("css"), selectorParseResult.getHasPseudoClasses() ? "raf" : "mutation");
            }
            return new QuerySelector(OBJECTMAPPER.writeValueAsString(selectorParseResult.getSelectors()), customQueryHandlers.get("p"), selectorParseResult.getHasAria() ? "raf" : "mutation");
        } catch (Exception e) {
            return new QuerySelector(selector, customQueryHandlers.get("css"), "mutation");
        }
    }

    public static SelectorParseResult parsePSelectors(String selector, Frame frame) throws JsonProcessingException {
        SelectorParseResult selectorParseResult = new SelectorParseResult();
        selectorParseResult.setIsPureCSS(true);
        List<Token> storage = new ArrayList<>();
        List<Object> compoundSelector = new ArrayList<>();
        List<Object> complexSelector = new ArrayList<>();
        complexSelector.add(compoundSelector);
        List<Object> selectors = new ArrayList<>();
        selectors.add(complexSelector);
        Page page = frame.page();
        String id = "parsel-js";
        boolean hasParselJsScript = (boolean) page.evaluate(" () => {\n" +
                "  return !!document.querySelector(\"#" + id + "\")\n" +
                "}");

        try {
            if (!hasParselJsScript) {
                FrameAddScriptTagOptions scriptTagOptions = new FrameAddScriptTagOptions();
                scriptTagOptions.setId(id);
                scriptTagOptions.setUrl("https://parsel.verou.me/dist/nomodule/parsel.js");
                page.addScriptTag(scriptTagOptions);
            }
            Object results = page.evaluate("() => {\n" +
                    "parsel.TOKENS.nesting = /&/g;\n" +
                    "parsel.TOKENS.combinator = /\\s*(>>>>?|[\\s>+~])\\s*/g;" +
                    "  return parsel.tokenize('" + selector + "');\n" +
                    "}");
            if (Objects.nonNull(results)) {
                ArrayList<Token> tokens = OBJECTMAPPER.convertValue(results, new TypeReference<ArrayList<Token>>() {
                });
                for (Token token : tokens) {
                    switch (token.getType()) {
                        case "combinator":
                            switch (token.getContent()) {
                                case ">>>":
                                    selectorParseResult.setIsPureCSS(false);
                                    if (!storage.isEmpty()) {
                                        compoundSelector.add(page.evaluate("(storage) => {\n" +
                                                "  return  parsel.stringify(storage);\n" +
                                                "}", storage));
                                        storage.clear();
                                    }
                                    compoundSelector = new ArrayList<>();
                                    complexSelector.add(">>>");
                                    complexSelector.add(compoundSelector);
                                    continue;
                                case ">>>>":
                                    selectorParseResult.setIsPureCSS(false);
                                    if (!storage.isEmpty()) {
                                        compoundSelector.add(page.evaluate("(storage) => {\n" +
                                                "  return  parsel.stringify(storage);\n" +
                                                "}", storage));
                                        storage.clear();
                                    }
                                    compoundSelector = new ArrayList<>();
                                    complexSelector.add(">>>>");
                                    complexSelector.add(compoundSelector);
                                    continue;
                            }
                            break;
                        case "pseudo-element":
                            if (!token.getName().startsWith("-p-")) {
                                break;
                            }
                            selectorParseResult.setIsPureCSS(false);
                            if (!storage.isEmpty()) {
                                compoundSelector.add(page.evaluate("(storage) => {\n" +
                                        "  return  parsel.stringify(storage);\n" +
                                        "}", storage));
                                storage.clear();
                            }
                            String name = token.getName().substring(3);
                            if ("aria".equals(name)) {
                                selectorParseResult.setHasAria(true);
                            }
                            compoundSelector.add(new CompoundPSelector(name, unquote(token.getArgument())));
                            continue;
                        case "pseudo-class":
                            selectorParseResult.setHasPseudoClasses(true);
                            break;
                        case "comma":
                            if (!storage.isEmpty()) {
                                compoundSelector.add(page.evaluate("(storage) => {\n" +
                                        "  return  parsel.stringify(storage);\n" +
                                        "}", storage));
                                storage.clear();
                            }
                            compoundSelector = new ArrayList<>();
                            complexSelector = new ArrayList<>();
                            selectors.add(complexSelector);
                            continue;
                    }
                    storage.add(token);
                }
                if (!storage.isEmpty()) {
                    compoundSelector.add(page.evaluate("(storage) => {\n" +
                            "  return  parsel.stringify(storage);\n" +
                            "}", storage));
                }
            }
        } catch (IOException e) {
            throw new JvppeteerException(e);
        }
        selectorParseResult.setSelectors(selectors);
        return selectorParseResult;
    }

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\[\\s\\S]", MULTILINE);

    private static String unquote(String text) {
        if (Objects.isNull(text)) {
            text = "";
        }
        if (text.length() <= 1) {
            return text;
        }
        if ((text.startsWith("'") || text.startsWith("\"") && text.endsWith(text.substring(0, 1)))) {
            text = text.substring(1, text.length() - 1);
        }
        return unescape(text);
    }

    private static String unescape(String text) {
        Matcher matcher = ESCAPE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
