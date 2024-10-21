package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.CSSCoverageOptions;
import com.ruiyun.jvppeteer.entities.CSSStyleSheetHeader;
import com.ruiyun.jvppeteer.entities.CoverageEntry;
import com.ruiyun.jvppeteer.entities.CoverageRange;
import com.ruiyun.jvppeteer.entities.Range;
import com.ruiyun.jvppeteer.events.StyleSheetAddedEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CSSCoverage {

    private CDPSession client;
    private boolean enabled;
    private final HashMap<String, String> stylesheetURLs = new HashMap<>();
    private final HashMap<String, String> stylesheetSources = new HashMap<>();
    private final Map<CDPSession.CDPSessionEvent, Consumer<?>> listeners = new HashMap<>();
    private boolean resetOnNavigation;
    public CSSCoverage(CDPSession client) {
        this.client = client;
    }

    public void updateClient(CDPSession client) {
        this.client = client;
    }

    public void start(CSSCoverageOptions options) {
        ValidateUtil.assertArg(!this.enabled, "CSSCoverage is already enabled");
        this.resetOnNavigation = options.getResetOnNavigation();
        this.enabled = true;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();

        Consumer<StyleSheetAddedEvent> styleSheetAdded = this::onStyleSheet;
        this.client.on(CDPSession.CDPSessionEvent.CSS_styleSheetAdded, styleSheetAdded);
        this.listeners.put(CDPSession.CDPSessionEvent.CSS_styleSheetAdded, styleSheetAdded);

        Consumer<Object> executionContextsCleared = (ignore) -> this.onExecutionContextsCleared();
        this.client.on(CDPSession.CDPSessionEvent.Runtime_executionContextsCleared, executionContextsCleared);
        this.listeners.put(CDPSession.CDPSessionEvent.Runtime_executionContextsCleared, executionContextsCleared);

        this.client.send("DOM.enable");
        this.client.send("CSS.enable");
        this.client.send("CSS.startRuleUsageTracking");
    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation) return;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();
    }

    private void onStyleSheet(StyleSheetAddedEvent event) {
        CSSStyleSheetHeader header = event.getHeader();
        // Ignore anonymous scripts
        if (StringUtil.isEmpty(header.getSourceURL())) return;
        Map<String, Object> params = ParamsFactory.create();
        params.put("styleSheetId", header.getStyleSheetId());
        JsonNode response = client.send("CSS.getStyleSheetText", params);
        this.stylesheetURLs.put(header.getStyleSheetId(), header.getSourceURL());
        this.stylesheetSources.put(header.getStyleSheetId(), response.get("text").asText());

    }

    public List<CoverageEntry> stop() {
        ValidateUtil.assertArg(this.enabled, "CSSCoverage is not enabled");
        this.enabled = false;
        JsonNode ruleTrackingResponse = this.client.send("CSS.stopRuleUsageTracking");
        this.client.send("CSS.disable");
        this.client.send("DOM.disable");
        this.listeners.forEach(this.client::off);
        // aggregate by styleSheetId
        Map<String, List<CoverageRange>> styleSheetIdToCoverage = new HashMap<>();
        JsonNode ruleUsageNode = ruleTrackingResponse.get("ruleUsage");
        Iterator<JsonNode> elements = ruleUsageNode.elements();
        while (elements.hasNext()) {
            JsonNode entry = elements.next();
            List<CoverageRange> ranges = styleSheetIdToCoverage.computeIfAbsent(entry.get("styleSheetId").asText(), k -> new ArrayList<>());
            boolean used = entry.get("used").asBoolean();
            if (used)
                ranges.add(new CoverageRange(entry.get("startOffset").asDouble(), entry.get("endOffset").asDouble(), 1));
            else
                ranges.add(new CoverageRange(entry.get("startOffset").asDouble(), entry.get("endOffset").asDouble(), 0));
        }

        List<CoverageEntry> coverage = new ArrayList<>();
        for (String styleSheetId : this.stylesheetURLs.keySet()) {
            String url = this.stylesheetURLs.get(styleSheetId);
            ValidateUtil.assertArg(url != null, "Stylesheet URL is undefined (styleSheetId=" + styleSheetId + ")");
            String text = this.stylesheetSources.get(styleSheetId);
            ValidateUtil.assertArg(text != null, "Stylesheet text is undefined (styleSheetId=" + styleSheetId + ")");
            List<Range> ranges = Coverage.convertToDisjointRanges(styleSheetIdToCoverage.get(styleSheetId));
            coverage.add(new CoverageEntry(url, ranges, text));
        }
        return coverage;
    }

}
