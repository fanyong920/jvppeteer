package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.CoverageRange;
import com.ruiyun.jvppeteer.cdp.entities.FunctionCoverage;
import com.ruiyun.jvppeteer.cdp.entities.JSCoverageEntry;
import com.ruiyun.jvppeteer.cdp.entities.JSCoverageOptions;
import com.ruiyun.jvppeteer.cdp.entities.Range;
import com.ruiyun.jvppeteer.cdp.entities.ScriptCoverage;
import com.ruiyun.jvppeteer.cdp.entities.TakePreciseCoverageResponse;
import com.ruiyun.jvppeteer.cdp.events.ScriptParsedEvent;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.Helper.isPuppeteerURL;

public class JSCoverage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSCoverage.class);
    private CDPSession client;
    private volatile boolean enabled;
    private final Map<String, String> scriptSources = new HashMap<>();
    private final Map<String, String> scriptURLs = new HashMap<>();
    private final Map<ConnectionEvents, Consumer<?>> listeners = new HashMap<>();
    private boolean resetOnNavigation;
    private boolean reportAnonymousScripts;
    private boolean includeRawScriptCoverage;

    public JSCoverage(CDPSession client) {
        this.client = client;
    }

    public void start(JSCoverageOptions options) {
        ValidateUtil.assertArg(!this.enabled, "JSCoverage is already enabled");
        this.resetOnNavigation = options.getResetOnNavigation();
        this.reportAnonymousScripts = options.getReportAnonymousScripts();
        this.includeRawScriptCoverage = options.getIncludeRawScriptCoverage();
        this.enabled = true;
        this.scriptURLs.clear();
        this.scriptSources.clear();
        Consumer<ScriptParsedEvent> onScriptParsed = this::onScriptParsed;
        this.client.on(ConnectionEvents.Debugger_scriptParsed, onScriptParsed);
        this.listeners.put(ConnectionEvents.Debugger_scriptParsed, onScriptParsed);

        Consumer<Object> executionContextsCleared = (event) -> this.onExecutionContextsCleared();
        this.client.on(ConnectionEvents.Runtime_executionContextsCleared, executionContextsCleared);
        this.listeners.put(ConnectionEvents.Runtime_executionContextsCleared, executionContextsCleared);

        this.client.send("Profiler.enable", null, null, false);
        Map<String, Object> params = ParamsFactory.create();
        params.put("callCount", this.includeRawScriptCoverage);
        params.put("detailed", options.getUseBlockCoverage());
        this.client.send("Profiler.startPreciseCoverage", params, null, false);
        this.client.send("Debugger.enable", null, null, false);
        params.clear();
        params.put("skip", true);
        this.client.send("Debugger.setSkipAllPauses", params);

    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation)
            return;
        this.scriptURLs.clear();
        this.scriptSources.clear();
    }

    private void onScriptParsed(ScriptParsedEvent event) {
        // Ignore puppeteer-injected scripts
        if (isPuppeteerURL(event.getUrl()))
            return;
        // Ignore other anonymous scripts unless the reportAnonymousScripts option is true.
        if (StringUtil.isEmpty(event.getUrl()) && !this.reportAnonymousScripts)
            return;
        if (!this.enabled)
            return;
        Map<String, Object> params = ParamsFactory.create();
        params.put("scriptId", event.getScriptId());
        try {
            JsonNode response = this.client.send("Debugger.getScriptSource", params);
            scriptURLs.put(event.getScriptId(), event.getUrl());
            scriptSources.put(event.getScriptId(), response.get("scriptSource").asText());
        } catch (Exception e) {
            LOGGER.error("onScriptParsed error: ", e);
        }
    }

    public List<JSCoverageEntry> stop() throws JsonProcessingException {
        ValidateUtil.assertArg(this.enabled, "JSCoverage is not enabled");
        this.enabled = false;
        JsonNode result = this.client.send("Profiler.takePreciseCoverage");
        this.client.send("Profiler.stopPreciseCoverage", null, null, false);
        this.client.send("Profiler.disable", null, null, false);
        this.client.send("Debugger.disable");
        this.listeners.forEach((eventType, listener) -> this.client.off(eventType, listener));
        List<JSCoverageEntry> coverage = new ArrayList<>();
        TakePreciseCoverageResponse profileResponse = Constant.OBJECTMAPPER.treeToValue(result, TakePreciseCoverageResponse.class);
        if (ValidateUtil.isEmpty(profileResponse.getResult())) {
            return coverage;
        }
        for (ScriptCoverage entry : profileResponse.getResult()) {
            String url = this.scriptURLs.get(entry.getScriptId());
            if (StringUtil.isEmpty(url) && this.reportAnonymousScripts)
                url = "debugger://VM" + entry.getScriptId();
            String text = this.scriptSources.get(entry.getScriptId());
            if (StringUtil.isEmpty(text) || StringUtil.isEmpty(url))
                continue;
            List<CoverageRange> flattenRanges = new ArrayList<>();
            for (FunctionCoverage func : entry.getFunctions())
                flattenRanges.addAll(func.getRanges());
            List<Range> ranges = Coverage.convertToDisjointRanges(flattenRanges);
            if (!this.includeRawScriptCoverage) {
                coverage.add(new JSCoverageEntry(url, ranges, text, null));
            } else {
                coverage.add(new JSCoverageEntry(url, ranges, text, entry));
            }
        }
        return coverage;
    }

    public void updateClient(CdpCDPSession client) {
        this.client = client;
    }
}
