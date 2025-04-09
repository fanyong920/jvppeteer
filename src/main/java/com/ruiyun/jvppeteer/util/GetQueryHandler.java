package com.ruiyun.jvppeteer.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.entities.CompoundPSelector;
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

public class GetQueryHandler {

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
        if (!name.matches("^[a-zA-Z]+$"))
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

    public static QuerySelector getQueryHandlerAndSelector(String selector, Frame frame) throws JsonProcessingException {
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
        SelectorParseResult selectorParseResult = parsePSelectors(selector, frame);
        if (selectorParseResult.getIsPureCSS()) {
            return new QuerySelector(selector, customQueryHandlers.get("css"), selectorParseResult.getHasPseudoClasses() ? "raf" : "mutation");
        }
        return new QuerySelector(OBJECTMAPPER.writeValueAsString(selectorParseResult.getSelectors()), customQueryHandlers.get("p"), selectorParseResult.getHasAria() ? "raf" : "mutation");

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
        try {
            Object results = page.evaluate(parselJsContent, selector);
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

    private static String parselJsContent = "(selector) => {\n" +
            "  var parsel = (function (exports) {\n" +
            "    'use strict';\n" +
            "\n" +
            "    const TOKENS = {\n" +
            "      attribute: /\\[\\s*(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?(?<name>[-\\w\\P{ASCII}]+)\\s*(?:(?<operator>\\W?=)\\s*(?<value>.+?)\\s*(\\s(?<caseSensitive>[iIsS]))?\\s*)?\\]/gu,\n" +
            "      id: /#(?<name>[-\\w\\P{ASCII}]+)/gu,\n" +
            "      class: /\\.(?<name>[-\\w\\P{ASCII}]+)/gu,\n" +
            "      comma: /\\s*,\\s*/g,\n" +
            "      combinator: /\\s*[\\s>+~]\\s*/g,\n" +
            "      'pseudo-element': /::(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?/gu,\n" +
            "      'pseudo-class': /:(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?/gu,\n" +
            "      universal: /(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?\\*/gu,\n" +
            "      type: /(?:(?<namespace>\\*|[-\\w\\P{ASCII}]*)\\|)?(?<name>[-\\w\\P{ASCII}]+)/gu, // this must be last\n" +
            "    };\n" +
            "    const TRIM_TOKENS = new Set(['combinator', 'comma']);\n" +
            "    const RECURSIVE_PSEUDO_CLASSES = new Set([\n" +
            "      'not',\n" +
            "      'is',\n" +
            "      'where',\n" +
            "      'has',\n" +
            "      'matches',\n" +
            "      '-moz-any',\n" +
            "      '-webkit-any',\n" +
            "      'nth-child',\n" +
            "      'nth-last-child',\n" +
            "    ]);\n" +
            "    const nthChildRegExp = /(?<index>[\\dn+-]+)\\s+of\\s+(?<subtree>.+)/;\n" +
            "    const RECURSIVE_PSEUDO_CLASSES_ARGS = {\n" +
            "      'nth-child': nthChildRegExp,\n" +
            "      'nth-last-child': nthChildRegExp,\n" +
            "    };\n" +
            "    const getArgumentPatternByType = (type) => {\n" +
            "      switch (type) {\n" +
            "        case 'pseudo-element':\n" +
            "        case 'pseudo-class':\n" +
            "          return new RegExp(TOKENS[type].source.replace('(?<argument>¶*)', '(?<argument>.*)'), 'gu');\n" +
            "        default:\n" +
            "          return TOKENS[type];\n" +
            "      }\n" +
            "    };\n" +
            "    function gobbleParens(text, offset) {\n" +
            "      let nesting = 0;\n" +
            "      let result = '';\n" +
            "      for (; offset < text.length; offset++) {\n" +
            "        const char = text[offset];\n" +
            "        switch (char) {\n" +
            "          case '(':\n" +
            "            ++nesting;\n" +
            "            break;\n" +
            "          case ')':\n" +
            "            --nesting;\n" +
            "            break;\n" +
            "        }\n" +
            "        result += char;\n" +
            "        if (nesting === 0) {\n" +
            "          return result;\n" +
            "        }\n" +
            "      }\n" +
            "      return result;\n" +
            "    }\n" +
            "    function tokenizeBy(text, grammar = TOKENS) {\n" +
            "      if (!text) {\n" +
            "        return [];\n" +
            "      }\n" +
            "      const tokens = [text];\n" +
            "      for (const [type, pattern] of Object.entries(grammar)) {\n" +
            "        for (let i = 0; i < tokens.length; i++) {\n" +
            "          const token = tokens[i];\n" +
            "          if (typeof token !== 'string') {\n" +
            "            continue;\n" +
            "          }\n" +
            "          pattern.lastIndex = 0;\n" +
            "          const match = pattern.exec(token);\n" +
            "          if (!match) {\n" +
            "            continue;\n" +
            "          }\n" +
            "          const from = match.index - 1;\n" +
            "          const args = [];\n" +
            "          const content = match[0];\n" +
            "          const before = token.slice(0, from + 1);\n" +
            "          if (before) {\n" +
            "            args.push(before);\n" +
            "          }\n" +
            "          args.push({\n" +
            "            ...match.groups,\n" +
            "            type,\n" +
            "            content,\n" +
            "          });\n" +
            "          const after = token.slice(from + content.length + 1);\n" +
            "          if (after) {\n" +
            "            args.push(after);\n" +
            "          }\n" +
            "          tokens.splice(i, 1, ...args);\n" +
            "        }\n" +
            "      }\n" +
            "      let offset = 0;\n" +
            "      for (const token of tokens) {\n" +
            "        switch (typeof token) {\n" +
            "          case 'string':\n" +
            "            throw new Error(`Unexpected sequence ${token} found at index ${offset}`);\n" +
            "          case 'object':\n" +
            "            offset += token.content.length;\n" +
            "            token.pos = [offset - token.content.length, offset];\n" +
            "            if (TRIM_TOKENS.has(token.type)) {\n" +
            "              token.content = token.content.trim() || ' ';\n" +
            "            }\n" +
            "            break;\n" +
            "        }\n" +
            "      }\n" +
            "      return tokens;\n" +
            "    }\n" +
            "    const STRING_PATTERN = /(['\"])([^\\\\\\n]+?)\\1/g;\n" +
            "    const ESCAPE_PATTERN = /\\\\./g;\n" +
            "    function tokenize(selector, grammar = TOKENS) {\n" +
            "      // Prevent leading/trailing whitespaces from being interpreted as combinators\n" +
            "      selector = selector.trim();\n" +
            "      if (selector === '') {\n" +
            "        return [];\n" +
            "      }\n" +
            "      const replacements = [];\n" +
            "      // Replace escapes with placeholders.\n" +
            "      selector = selector.replace(ESCAPE_PATTERN, (value, offset) => {\n" +
            "        replacements.push({ value, offset });\n" +
            "        return '\\uE000'.repeat(value.length);\n" +
            "      });\n" +
            "      // Replace strings with placeholders.\n" +
            "      selector = selector.replace(STRING_PATTERN, (value, quote, content, offset) => {\n" +
            "        replacements.push({ value, offset });\n" +
            "        return `${quote}${'\\uE001'.repeat(content.length)}${quote}`;\n" +
            "      });\n" +
            "      // Replace parentheses with placeholders.\n" +
            "      {\n" +
            "        let pos = 0;\n" +
            "        let offset;\n" +
            "        while ((offset = selector.indexOf('(', pos)) > -1) {\n" +
            "          const value = gobbleParens(selector, offset);\n" +
            "          replacements.push({ value, offset });\n" +
            "          selector = `${selector.substring(0, offset)}(${'¶'.repeat(value.length - 2)})${selector.substring(offset + value.length)}`;\n" +
            "          pos = offset + value.length;\n" +
            "        }\n" +
            "      }\n" +
            "      // Now we have no nested structures and we can parse with regexes\n" +
            "      const tokens = tokenizeBy(selector, grammar);\n" +
            "      // Replace placeholders in reverse order.\n" +
            "      const changedTokens = new Set();\n" +
            "      for (const replacement of replacements.reverse()) {\n" +
            "        for (const token of tokens) {\n" +
            "          const { offset, value } = replacement;\n" +
            "          if (!(token.pos[0] <= offset &&\n" +
            "            offset + value.length <= token.pos[1])) {\n" +
            "            continue;\n" +
            "          }\n" +
            "          const { content } = token;\n" +
            "          const tokenOffset = offset - token.pos[0];\n" +
            "          token.content =\n" +
            "            content.slice(0, tokenOffset) +\n" +
            "            value +\n" +
            "            content.slice(tokenOffset + value.length);\n" +
            "          if (token.content !== content) {\n" +
            "            changedTokens.add(token);\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "      // Update changed tokens.\n" +
            "      for (const token of changedTokens) {\n" +
            "        const pattern = getArgumentPatternByType(token.type);\n" +
            "        if (!pattern) {\n" +
            "          throw new Error(`Unknown token type: ${token.type}`);\n" +
            "        }\n" +
            "        pattern.lastIndex = 0;\n" +
            "        const match = pattern.exec(token.content);\n" +
            "        if (!match) {\n" +
            "          throw new Error(`Unable to parse content for ${token.type}: ${token.content}`);\n" +
            "        }\n" +
            "        Object.assign(token, match.groups);\n" +
            "      }\n" +
            "      return tokens;\n" +
            "    }\n" +
            "    /**\n" +
            "     *  Convert a flat list of tokens into a tree of complex & compound selectors\n" +
            "     */\n" +
            "    function nestTokens(tokens, { list = true } = {}) {\n" +
            "      if (list && tokens.find((t) => t.type === 'comma')) {\n" +
            "        const selectors = [];\n" +
            "        const temp = [];\n" +
            "        for (let i = 0; i < tokens.length; i++) {\n" +
            "          if (tokens[i].type === 'comma') {\n" +
            "            if (temp.length === 0) {\n" +
            "              throw new Error('Incorrect comma at ' + i);\n" +
            "            }\n" +
            "            selectors.push(nestTokens(temp, { list: false }));\n" +
            "            temp.length = 0;\n" +
            "          }\n" +
            "          else {\n" +
            "            temp.push(tokens[i]);\n" +
            "          }\n" +
            "        }\n" +
            "        if (temp.length === 0) {\n" +
            "          throw new Error('Trailing comma');\n" +
            "        }\n" +
            "        else {\n" +
            "          selectors.push(nestTokens(temp, { list: false }));\n" +
            "        }\n" +
            "        return { type: 'list', list: selectors };\n" +
            "      }\n" +
            "      for (let i = tokens.length - 1; i >= 0; i--) {\n" +
            "        let token = tokens[i];\n" +
            "        if (token.type === 'combinator') {\n" +
            "          let left = tokens.slice(0, i);\n" +
            "          let right = tokens.slice(i + 1);\n" +
            "          if (left.length === 0) {\n" +
            "            return {\n" +
            "              type: 'relative',\n" +
            "              combinator: token.content,\n" +
            "              right: nestTokens(right),\n" +
            "            };\n" +
            "          }\n" +
            "          return {\n" +
            "            type: 'complex',\n" +
            "            combinator: token.content,\n" +
            "            left: nestTokens(left),\n" +
            "            right: nestTokens(right),\n" +
            "          };\n" +
            "        }\n" +
            "      }\n" +
            "      switch (tokens.length) {\n" +
            "        case 0:\n" +
            "          throw new Error('Could not build AST.');\n" +
            "        case 1:\n" +
            "          // If we're here, there are no combinators, so it's just a list.\n" +
            "          return tokens[0];\n" +
            "        default:\n" +
            "          return {\n" +
            "            type: 'compound',\n" +
            "            list: [...tokens], // clone to avoid pointers messing up the AST\n" +
            "          };\n" +
            "      }\n" +
            "    }\n" +
            "    /**\n" +
            "     * Traverse an AST in depth-first order\n" +
            "     */\n" +
            "    function* flatten(node,\n" +
            "      /**\n" +
            "       * @internal\n" +
            "       */\n" +
            "      parent) {\n" +
            "      switch (node.type) {\n" +
            "        case 'list':\n" +
            "          for (let child of node.list) {\n" +
            "            yield* flatten(child, node);\n" +
            "          }\n" +
            "          break;\n" +
            "        case 'complex':\n" +
            "          yield* flatten(node.left, node);\n" +
            "          yield* flatten(node.right, node);\n" +
            "          break;\n" +
            "        case 'relative':\n" +
            "          yield* flatten(node.right, node);\n" +
            "          break;\n" +
            "        case 'compound':\n" +
            "          yield* node.list.map((token) => [token, node]);\n" +
            "          break;\n" +
            "        default:\n" +
            "          yield [node, parent];\n" +
            "      }\n" +
            "    }\n" +
            "    /**\n" +
            "     * Traverse an AST (or part thereof), in depth-first order\n" +
            "     */\n" +
            "    function walk(node, visit,\n" +
            "      /**\n" +
            "       * @internal\n" +
            "       */\n" +
            "      parent) {\n" +
            "      if (!node) {\n" +
            "        return;\n" +
            "      }\n" +
            "      for (const [token, ast] of flatten(node, parent)) {\n" +
            "        visit(token, ast);\n" +
            "      }\n" +
            "    }\n" +
            "    /**\n" +
            "     * Parse a CSS selector\n" +
            "     *\n" +
            "     * @param selector - The selector to parse\n" +
            "     * @param options.recursive - Whether to parse the arguments of pseudo-classes like :is(), :has() etc. Defaults to true.\n" +
            "     * @param options.list - Whether this can be a selector list (A, B, C etc). Defaults to true.\n" +
            "     */\n" +
            "    function parse(selector, { recursive = true, list = true } = {}) {\n" +
            "      const tokens = tokenize(selector);\n" +
            "      if (!tokens) {\n" +
            "        return;\n" +
            "      }\n" +
            "      const ast = nestTokens(tokens, { list });\n" +
            "      if (!recursive) {\n" +
            "        return ast;\n" +
            "      }\n" +
            "      for (const [token] of flatten(ast)) {\n" +
            "        if (token.type !== 'pseudo-class' || !token.argument) {\n" +
            "          continue;\n" +
            "        }\n" +
            "        if (!RECURSIVE_PSEUDO_CLASSES.has(token.name)) {\n" +
            "          continue;\n" +
            "        }\n" +
            "        let argument = token.argument;\n" +
            "        const childArg = RECURSIVE_PSEUDO_CLASSES_ARGS[token.name];\n" +
            "        if (childArg) {\n" +
            "          const match = childArg.exec(argument);\n" +
            "          if (!match) {\n" +
            "            continue;\n" +
            "          }\n" +
            "          Object.assign(token, match.groups);\n" +
            "          argument = match.groups['subtree'];\n" +
            "        }\n" +
            "        if (!argument) {\n" +
            "          continue;\n" +
            "        }\n" +
            "        Object.assign(token, {\n" +
            "          subtree: parse(argument, {\n" +
            "            recursive: true,\n" +
            "            list: true,\n" +
            "          }),\n" +
            "        });\n" +
            "      }\n" +
            "      return ast;\n" +
            "    }\n" +
            "    /**\n" +
            "     * Converts the given list or (sub)tree to a string.\n" +
            "     */\n" +
            "    function stringify(listOrNode) {\n" +
            "      if (Array.isArray(listOrNode)) {\n" +
            "        return listOrNode.map((token) => token.content).join(\"\");\n" +
            "      }\n" +
            "      switch (listOrNode.type) {\n" +
            "        case \"list\":\n" +
            "          return listOrNode.list.map(stringify).join(\",\");\n" +
            "        case \"relative\":\n" +
            "          return (listOrNode.combinator +\n" +
            "            stringify(listOrNode.right));\n" +
            "        case \"complex\":\n" +
            "          return (stringify(listOrNode.left) +\n" +
            "            listOrNode.combinator +\n" +
            "            stringify(listOrNode.right));\n" +
            "        case \"compound\":\n" +
            "          return listOrNode.list.map(stringify).join(\"\");\n" +
            "        default:\n" +
            "          return listOrNode.content;\n" +
            "      }\n" +
            "    }\n" +
            "    /**\n" +
            "     * To convert the specificity array to a number\n" +
            "     */\n" +
            "    function specificityToNumber(specificity, base) {\n" +
            "      base = base || Math.max(...specificity) + 1;\n" +
            "      return (specificity[0] * (base << 1) + specificity[1] * base + specificity[2]);\n" +
            "    }\n" +
            "    /**\n" +
            "     * Calculate specificity of a selector.\n" +
            "     *\n" +
            "     * If the selector is a list, the max specificity is returned.\n" +
            "     */\n" +
            "    function specificity(selector) {\n" +
            "      let ast = selector;\n" +
            "      if (typeof ast === 'string') {\n" +
            "        ast = parse(ast, { recursive: true });\n" +
            "      }\n" +
            "      if (!ast) {\n" +
            "        return [];\n" +
            "      }\n" +
            "      if (ast.type === 'list' && 'list' in ast) {\n" +
            "        let base = 10;\n" +
            "        const specificities = ast.list.map((ast) => {\n" +
            "          const sp = specificity(ast);\n" +
            "          base = Math.max(base, ...specificity(ast));\n" +
            "          return sp;\n" +
            "        });\n" +
            "        const numbers = specificities.map((ast) => specificityToNumber(ast, base));\n" +
            "        return specificities[numbers.indexOf(Math.max(...numbers))];\n" +
            "      }\n" +
            "      const ret = [0, 0, 0];\n" +
            "      for (const [token] of flatten(ast)) {\n" +
            "        switch (token.type) {\n" +
            "          case 'id':\n" +
            "            ret[0]++;\n" +
            "            break;\n" +
            "          case 'class':\n" +
            "          case 'attribute':\n" +
            "            ret[1]++;\n" +
            "            break;\n" +
            "          case 'pseudo-element':\n" +
            "          case 'type':\n" +
            "            ret[2]++;\n" +
            "            break;\n" +
            "          case 'pseudo-class':\n" +
            "            if (token.name === 'where') {\n" +
            "              break;\n" +
            "            }\n" +
            "            if (!RECURSIVE_PSEUDO_CLASSES.has(token.name) ||\n" +
            "              !token.subtree) {\n" +
            "              ret[1]++;\n" +
            "              break;\n" +
            "            }\n" +
            "            const sub = specificity(token.subtree);\n" +
            "            sub.forEach((s, i) => (ret[i] += s));\n" +
            "            // :nth-child() & :nth-last-child() add (0, 1, 0) to the specificity of their most complex selector\n" +
            "            if (token.name === 'nth-child' ||\n" +
            "              token.name === 'nth-last-child') {\n" +
            "              ret[1]++;\n" +
            "            }\n" +
            "        }\n" +
            "      }\n" +
            "      return ret;\n" +
            "    }\n" +
            "\n" +
            "    exports.RECURSIVE_PSEUDO_CLASSES = RECURSIVE_PSEUDO_CLASSES;\n" +
            "    exports.RECURSIVE_PSEUDO_CLASSES_ARGS = RECURSIVE_PSEUDO_CLASSES_ARGS;\n" +
            "    exports.TOKENS = TOKENS;\n" +
            "    exports.TRIM_TOKENS = TRIM_TOKENS;\n" +
            "    exports.flatten = flatten;\n" +
            "    exports.gobbleParens = gobbleParens;\n" +
            "    exports.parse = parse;\n" +
            "    exports.specificity = specificity;\n" +
            "    exports.specificityToNumber = specificityToNumber;\n" +
            "    exports.stringify = stringify;\n" +
            "    exports.tokenize = tokenize;\n" +
            "    exports.tokenizeBy = tokenizeBy;\n" +
            "    exports.walk = walk;\n" +
            "\n" +
            "    Object.defineProperty(exports, '__esModule', { value: true });\n" +
            "\n" +
            "    return exports;\n" +
            "\n" +
            "  })({});\n" +
            "  //扩展 TOKENS\n" +
            "  parsel.TOKENS.nesting = /&/g;\n" +
            "  parsel.TOKENS.combinator = /\\s*(>>>>?|[\\s>+~])\\s*/g;\n" +
            "  window.parsel = parsel;\n" +
            "  return parsel.tokenize(selector)\n" +
            "}";

}
