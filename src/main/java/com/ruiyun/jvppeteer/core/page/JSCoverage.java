package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.CSS.Range;
import com.ruiyun.jvppeteer.protocol.debugger.ScriptParsedEvent;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;
import com.ruiyun.jvppeteer.protocol.profiler.FunctionCoverage;
import com.ruiyun.jvppeteer.protocol.profiler.ScriptCoverage;
import com.ruiyun.jvppeteer.protocol.profiler.TakePreciseCoverageReturnValue;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class JSCoverage {


    private CDPSession client;

    private boolean enabled;

    private Map<String, String> scriptSources;

    private Map<String, String> scriptURLs;

    private final List<Disposable> disposables = new ArrayList<>();

    private boolean resetOnNavigation;

    private boolean reportAnonymousScripts;

    public JSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.scriptURLs = new HashMap<>();
        this.scriptSources = new HashMap<>();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation, boolean reportAnonymousScripts) {
        ValidateUtil.assertArg(!this.enabled, "JSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.reportAnonymousScripts = reportAnonymousScripts;
        this.enabled = true;
        this.scriptURLs.clear();
        this.scriptSources.clear();
        this.disposables.add(Helper.fromEmitterEvent(this.client, CDPSession.CDPSessionEvent.Debugger_scriptparsed).subscribe((event) -> this.onScriptParsed((ScriptParsedEvent) event)));
        this.disposables.add(Helper.fromEmitterEvent(this.client, CDPSession.CDPSessionEvent.Runtime_executionContextsCleared).subscribe((ignore)->this.onExecutionContextsCleared()));
        this.client.send("Profiler.enable", null,null, false);
        Map<String, Object> params = new HashMap<>();
        params.put("callCount", false);
        params.put("detailed", true);
        this.client.send("Profiler.startPreciseCoverage", params, null,false);
        this.client.send("Debugger.enable", null, null,false);
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
        if (ExecutionContext.EVALUATION_SCRIPT_URL.equals(event.getUrl()))
            return;
        // Ignore other anonymous scripts unless the reportAnonymousScripts option is true.
        if (StringUtil.isEmpty(event.getUrl()) && !this.reportAnonymousScripts)
            return;
        ForkJoinPool.commonPool().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("scriptId", event.getScriptId());
            JsonNode response = client.send("Debugger.getScriptSource", params);
            scriptURLs.put(event.getScriptId(), event.getUrl());
            scriptSources.put(event.getScriptId(), response.get("scriptSource").asText());
        });
    }

    public List<CoverageEntry> stop() throws JsonProcessingException {
        ValidateUtil.assertArg(this.enabled, "JSCoverage is not enabled");
        this.enabled = false;
        JsonNode result = this.client.send("Profiler.takePreciseCoverage");
        this.client.send("Profiler.stopPreciseCoverage");
        this.client.send("Profiler.disable" );
        this.client.send("Debugger.disable");
        this.disposables.forEach(Disposable::dispose);
        List<CoverageEntry> coverage = new ArrayList<>();
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
