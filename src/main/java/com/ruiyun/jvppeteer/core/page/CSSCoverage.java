package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.events.StyleSheetAddedEvent;
import com.ruiyun.jvppeteer.protocol.CSS.CSSStyleSheetHeader;
import com.ruiyun.jvppeteer.protocol.CSS.Range;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class CSSCoverage {

    private final CDPSession client;

    private boolean enabled;

    private HashMap<String, String> stylesheetURLs;

    private HashMap<String, String> stylesheetSources;

    private final List<Disposable> disposables = new ArrayList<>();

    private boolean resetOnNavigation;

    public CSSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.stylesheetURLs = new HashMap<>();
        this.stylesheetSources = new HashMap();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation) {
        ValidateUtil.assertArg(!this.enabled, "CSSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.enabled = true;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();
        this.disposables.add(Helper.fromEmitterEvent(this.client, CDPSession.CDPSessionEvent.CSS_styleSheetAdded).subscribe((event) -> this.onStyleSheet((StyleSheetAddedEvent) event)));
        this.disposables.add(Helper.fromEmitterEvent(this.client, CDPSession.CDPSessionEvent.Runtime_executionContextsCleared).subscribe((ignore)->this.onExecutionContextsCleared()));
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

        ForkJoinPool.commonPool().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("styleSheetId", header.getStyleSheetId());
            JsonNode response = client.send("CSS.getStyleSheetText", params);
            stylesheetURLs.put(header.getStyleSheetId(), header.getSourceURL());
            stylesheetSources.put(header.getStyleSheetId(), response.get("text").asText());
        });

    }

    public List<CoverageEntry> stop() {
        ValidateUtil.assertArg(this.enabled, "CSSCoverage is not enabled");
        this.enabled = false;


        JsonNode ruleTrackingResponse = this.client.send("CSS.stopRuleUsageTracking");

        this.client.send("CSS.disable", null, null,false);
        this.client.send("DOM.disable", null,null, false);


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
