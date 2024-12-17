package com.ruiyun.jvppeteer.api.events;

public enum ConnectionEvents {
    //cdp
    CDPSession_Disconnected("CDPSession.Disconnected"),
    CDPSession_Swapped("CDPSession.Swapped"),
    CDPSession_Ready("CDPSession.Ready"),
    sessionAttached("sessionattached"),
    sessionDetached("sessiondetached"),
    Page_domContentEventFired("Page.domContentEventFired"),
    Page_loadEventFired("Page.loadEventFired"),
    Page_javascriptDialogOpening("Page.javascriptDialogOpening"),
    Page_fileChooserOpened("Page.fileChooserOpened"),
    Page_frameStartedLoading("Page.frameStartedLoading"),
    Page_frameAttached("Page.frameAttached"),
    Page_frameNavigated("Page.frameNavigated"),
    Page_navigatedWithinDocument("Page.navigatedWithinDocument"),
    Page_frameDetached("Page.frameDetached"),
    Page_frameStoppedLoading("Page.frameStoppedLoading"),
    Page_lifecycleEvent("Page.lifecycleEvent"),
    Page_screencastFrame("Page.screencastFrame"),
    Runtime_executionContextCreated("Runtime.executionContextCreated"),
    Runtime_executionContextDestroyed("Runtime.executionContextDestroyed"),
    Runtime_executionContextsCleared("Runtime.executionContextsCleared"),
    Runtime_exceptionThrown("Runtime.exceptionThrown"),
    Runtime_consoleAPICalled("Runtime.consoleAPICalled"),
    Runtime_bindingCalled("Runtime.bindingCalled"),
    Inspector_targetCrashed("Inspector.targetCrashed"),
    Performance_metrics("Performance.metrics"),
    Log_entryAdded("Log.entryAdded"),
    Target_targetCreated("Target.targetCreated"),
    Target_targetDestroyed("Target.targetDestroyed"),
    Target_targetInfoChanged("Target.targetInfoChanged"),
    Target_attachedToTarget("Target.attachedToTarget"),
    Target_detachedFromTarget("Target.detachedFromTarget"),
    Debugger_scriptParsed("Debugger.scriptParsed"),
    CSS_styleSheetAdded("CSS.styleSheetAdded"),
    DeviceAccess_deviceRequestPrompted("DeviceAccess.deviceRequestPrompted"),
    targetcreated("targetcreated"),
    targetdestroyed("targetdestroyed"),
    targetchanged("targetchanged"),
    disconnected("disconnected"),
    Fetch_requestPaused("Fetch.requestPaused"),
    Fetch_authRequired("Fetch.authRequired"),
    Network_requestWillBeSent("Network.requestWillBeSent"),
    Network_requestServedFromCache("Network.requestServedFromCache"),
    Network_responseReceived("Network.responseReceived"),
    Network_loadingFinished("Network.loadingFinished"),
    Network_loadingFailed("Network.loadingFailed"),
    Network_responseReceivedExtraInfo("Network.responseReceivedExtraInfo"),
    Tracing_tracingComplete("Tracing.tracingComplete"),
    Input_dragIntercepted("Input.dragIntercepted"),
    /**
     * 下载进度时触发
     */
    Browser_downloadProgress("Browser.downloadProgress"),
    /**
     * 当页面准备开始下载时促发
     */
    Browser_downloadWillBegin("Browser.downloadWillBegin"),

    //bidi
    browsingContext_contextCreated("browsingContext.contextCreated"),
    browsingContext_contextDestroyed("browsingContext.contextDestroyed"),
    ended("ended"),
    browsingContext_fragmentNavigated("browsingContext.fragmentNavigated"),
    browsingContext_navigationStarted("browsingContext.navigationStarted"),
    browsingContext_navigationFailed("browsingContext.navigationFailed"),
    browsingContext_navigationAborted("browsingContext.navigationAborted"),
    browsingContext_domContentLoaded("browsingContext.domContentLoaded"),
    browsingContext_load("browsingContext.load"),
    network_beforeRequestSent("network.beforeRequestSent"),
    network_authRequired("network.authRequired"),
    network_fetchError("network.fetchError"),
    network_responseCompleted("network.responseCompleted"),
    network_responseStarted("network.responseStarted"),
    log_entryAdded("log.entryAdded"),
    browsingContext_userPromptOpened("browsingContext.userPromptOpened"),
    script_realmCreated("script.realmCreated"),
    script_realmDestroyed("script.realmDestroyed"),
    script_message("script.message"),
    browsingContext_userPromptClosed("browsingContext.userPromptClosed");
    private final String eventName;

    ConnectionEvents(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
