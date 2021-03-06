package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.protocol.CSS.CSSStyleSheetHeader;
import com.ruiyun.jvppeteer.protocol.CSS.Range;
import com.ruiyun.jvppeteer.protocol.CSS.StyleSheetAddedPayload;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CSSCoverage {

    private final CDPSession client;

    private boolean enabled;

    private HashMap<String, String> stylesheetURLs;

    private HashMap<String, String> stylesheetSources;

    private List<BrowserListenerWrapper> eventListeners;

    private boolean resetOnNavigation;

    public CSSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.stylesheetURLs = new HashMap<>();
        this.stylesheetSources = new HashMap();
        this.eventListeners = new ArrayList<>();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation) {
        ValidateUtil.assertArg(!this.enabled, "CSSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.enabled = true;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();

        DefaultBrowserListener<StyleSheetAddedPayload> addLis = new DefaultBrowserListener<StyleSheetAddedPayload>() {
            @Override
            public void onBrowserEvent(StyleSheetAddedPayload event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onStyleSheet(event);
            }
        };
        addLis.setMethod("CSS.styleSheetAdded");
        addLis.setTarget(this);

        DefaultBrowserListener<Object> clearLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onExecutionContextsCleared();
            }
        };
        clearLis.setMethod("Runtime.executionContextsCleared");
        clearLis.setTarget(this);

        this.eventListeners.add(Helper.addEventListener(this.client, addLis.getMethod(), addLis));
        this.eventListeners.add(Helper.addEventListener(this.client, clearLis.getMethod(), clearLis));

        this.client.send("DOM.enable", null, false);
        this.client.send("CSS.enable", null, false);
        this.client.send("CSS.startRuleUsageTracking", null, true);

    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation) return;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();
    }

    private void onStyleSheet(StyleSheetAddedPayload event) {
        CSSStyleSheetHeader header = event.getHeader();
        // Ignore anonymous scripts
        if (StringUtil.isEmpty(header.getSourceURL())) return;

        Helper.commonExecutor().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("styleSheetId", header.getStyleSheetId());
            JsonNode response = client.send("CSS.getStyleSheetText", params, true);
            stylesheetURLs.put(header.getStyleSheetId(), header.getSourceURL());
            stylesheetSources.put(header.getStyleSheetId(), response.get("text").asText());
        });

    }

    public List<CoverageEntry> stop() {
        ValidateUtil.assertArg(this.enabled, "CSSCoverage is not enabled");
        this.enabled = false;


        JsonNode ruleTrackingResponse = this.client.send("CSS.stopRuleUsageTracking", null, true);

        this.client.send("CSS.disable", null, false);
        this.client.send("DOM.disable", null, false);

        Helper.removeEventListeners(this.eventListeners);

        // aggregate by styleSheetId
        Map<String, List<CoverageRange>> styleSheetIdToCoverage = new HashMap<>();
        JsonNode ruleUsageNode = ruleTrackingResponse.get("ruleUsage");
        Iterator<JsonNode> elements = ruleUsageNode.elements();
        while (elements.hasNext()) {
            JsonNode entry = elements.next();
            List<CoverageRange> ranges = styleSheetIdToCoverage.get(entry.get("styleSheetId").asText());
            if (ranges == null) {
                ranges = new ArrayList<>();
                styleSheetIdToCoverage.put(entry.get("styleSheetId").asText(), ranges);
            }
            boolean used = entry.get("used").asBoolean();
            if (used)
                ranges.add(new CoverageRange(entry.get("startOffset").asInt(), entry.get("endOffset").asInt(), 1));
            else
                ranges.add(new CoverageRange(entry.get("startOffset").asInt(), entry.get("endOffset").asInt(), 0));
        }


        List<CoverageEntry> coverage = new ArrayList<>();
        for (String styleSheetId : this.stylesheetURLs.keySet()) {
            String url = this.stylesheetURLs.get(styleSheetId);
            String text = this.stylesheetSources.get(styleSheetId);
            List<Range> ranges = Coverage.convertToDisjointRanges(styleSheetIdToCoverage.get(styleSheetId));
            coverage.add(new CoverageEntry(url, ranges, text));
        }

        return coverage;
    }

}
