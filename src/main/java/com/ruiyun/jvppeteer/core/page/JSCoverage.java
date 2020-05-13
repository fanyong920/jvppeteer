package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.BrowserListenerWrapper;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;
import com.ruiyun.jvppeteer.protocol.profiler.FunctionCoverage;
import com.ruiyun.jvppeteer.protocol.CSS.Range;
import com.ruiyun.jvppeteer.protocol.profiler.ScriptCoverage;
import com.ruiyun.jvppeteer.protocol.debugger.ScriptParsedPayload;
import com.ruiyun.jvppeteer.protocol.profiler.TakePreciseCoverageReturnValue;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSCoverage {


    private CDPSession client;

    private boolean enabled;

    private Map<String, String> scriptSources;

    private Map<String, String> scriptURLs;

    private List<BrowserListenerWrapper> eventListeners;

    private boolean resetOnNavigation;

    private boolean reportAnonymousScripts;

    public JSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.scriptURLs = new HashMap<>();
        this.scriptSources = new HashMap<>();
        this.eventListeners = new ArrayList<>();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation, boolean reportAnonymousScripts) {
        ValidateUtil.assertBoolean(!this.enabled, "JSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.reportAnonymousScripts = reportAnonymousScripts;
        this.enabled = true;
        this.scriptURLs.clear();
        this.scriptSources.clear();
        DefaultBrowserListener<ScriptParsedPayload> scriptParsedLis = new DefaultBrowserListener<ScriptParsedPayload>() {
            @Override
            public void onBrowserEvent(ScriptParsedPayload event) {
                JSCoverage jsCoverage = (JSCoverage) this.getTarget();
                jsCoverage.onScriptParsed(event);
            }
        };
        scriptParsedLis.setTarget(this);
        scriptParsedLis.setMothod("Debugger.scriptParsed");
        this.eventListeners.add(Helper.addEventListener(this.client, scriptParsedLis.getMothod(), scriptParsedLis));

        DefaultBrowserListener<Object> clearedLis = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                JSCoverage jsCoverage = (JSCoverage) this.getTarget();
                jsCoverage.onExecutionContextsCleared();
            }
        };
        clearedLis.setTarget(this);
        clearedLis.setMothod("Runtime.executionContextsCleared");
        this.eventListeners.add(Helper.addEventListener(this.client, clearedLis.getMothod(), clearedLis));


        this.client.send("Profiler.enable", null, false);
        Map<String, Object> params = new HashMap<>();
        params.put("callCount", false);
        params.put("detailed", true);
        this.client.send("Profiler.startPreciseCoverage", params, false);
        this.client.send("Debugger.enable", null, false);
        params.clear();
        params.put("skip", true);
        this.client.send("Debugger.setSkipAllPauses", params, true);

    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation)
            return;
        this.scriptURLs.clear();
        this.scriptSources.clear();
    }

    private void onScriptParsed(ScriptParsedPayload event) {
        // Ignore puppeteer-injected scripts
        if (ExecutionContext.EVALUATION_SCRIPT_URL.equals(event.getUrl()))
            return;
        // Ignore other anonymous scripts unless the reportAnonymousScripts option is true.
        if (StringUtil.isEmpty(event.getUrl()) && !this.reportAnonymousScripts)
            return;
        Map<String, Object> params = new HashMap<>();
        params.put("scriptId", event.getScriptId());
        JsonNode response = this.client.send("Debugger.getScriptSource", params, true);
        this.scriptURLs.put(event.getScriptId(), event.getUrl());
        this.scriptSources.put(event.getScriptId(), response.get("scriptSource").asText());
    }

    public List<CoverageEntry> stop() throws JsonProcessingException {
        ValidateUtil.assertBoolean(this.enabled, "JSCoverage is not enabled");
        this.enabled = false;

        JsonNode result = this.client.send("Profiler.takePreciseCoverage", null, true);
        this.client.send("Profiler.stopPreciseCoverage", null, false);
        this.client.send("Profiler.disable", null, false);
        this.client.send("Debugger.disable", null, false);


        Helper.removeEventListeners(this.eventListeners);

        List<CoverageEntry> coverage = new ArrayList<>();
        /** @type Protocol.Profiler.takePreciseCoverageReturnValue */
        TakePreciseCoverageReturnValue profileResponse = Constant.OBJECTMAPPER.treeToValue(result, TakePreciseCoverageReturnValue.class);
        if (ValidateUtil.isEmpty(profileResponse.getResult())) {
            return coverage;
        }
        for (ScriptCoverage entry : profileResponse.getResult()) {
            String url = this.scriptURLs.get(entry.getScriptId());
            if (StringUtil.isEmpty(url) && this.reportAnonymousScripts)
                url = "debugger://VM" + entry.getScriptId();
            String text = this.scriptSources.get(entry.getScriptId());
            if (StringUtil.isEmpty(url) || StringUtil.isEmpty(text))
                continue;
            List<CoverageRange> flattenRanges = new ArrayList<>();
            for (FunctionCoverage func : entry.getFunctions())
                flattenRanges.addAll(func.getRanges());
            List<Range> ranges = Coverage.convertToDisjointRanges(flattenRanges);
            coverage.add(createCoverageEntry(url, ranges, text));
        }
        return coverage;
    }


    private CoverageEntry createCoverageEntry(String url, List<Range> ranges, String text) {
        CoverageEntry coverageEntity = new CoverageEntry();
        coverageEntity.setUrl(url);
        coverageEntity.setRanges(ranges);
        coverageEntity.setText(text);
        return coverageEntity;
    }

}
