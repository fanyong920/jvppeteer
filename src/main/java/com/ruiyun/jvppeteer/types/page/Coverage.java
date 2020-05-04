package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.protocol.profiler.CoverageEntry;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.List;

/**
 * Coverage gathers information about parts of JavaScript and CSS that were used by the page.
 */
public class Coverage {

    private CSSCoverage cssCoverage;

    private JSCoverage jsCoverage;

    public Coverage(CDPSession client) {
        this.cssCoverage = new CSSCoverage(client);
        this.jsCoverage = new JSCoverage(client);
    }

    public void startJSCoverage(boolean resetOnNavigation,boolean reportAnonymousScripts){
        this.jsCoverage.start(resetOnNavigation,reportAnonymousScripts);
    }

    public List<CoverageEntry> stopJSCoverage() throws JsonProcessingException {
        return  this.jsCoverage.stop();
    }

    public void startCSSCoverage(boolean resetOnNavigation) {
        this.cssCoverage.start(resetOnNavigation);
    }

    /**
     * @return {!Promise<!Array<!CoverageEntry>>}
     */
    public List<CoverageEntry> stopCSSCoverage() {
        return  this.cssCoverage.stop();
    }
}
