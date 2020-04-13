package com.ruiyun.jvppeteer.events.browser.definition;

public enum Events {
    PAGE_CLOSE("close"),
    PAGE_CONSOLE("console"),
    PAGE_DIALOG("dialog"),
    PAGE_DOMContentLoaded("domcontentloaded"),
    PAGE_ERROR("error"),
    PAGE_PageError("pageerror"),
    PAGE_REQUEST( "request"),
    PAGE_RESPONSE( "response"),
    PAGE_REQUESTFAILED( "requestfailed"),
    PAGE_REQUESTFINISHED( "requestfinished"),
    PAGE_FRAMEATTACHED( "frameattached"),
    PAGE_FRAMEDETACHED( "framedetached"),
    PAGE_FRAMENAVIGATED( "framenavigated"),
    PAGE_LOAD( "load"),
    PAGE_METRICS( "metrics"),
    PAGE_POPUP( "popup"),
    PAGE_WORKERCREATED( "workercreated"),
    PAGE_WORKERDESTROYED( "workerdestroyed"),

    BROWSER_TARGETCREATED ("targetcreated"),
    BROWSER_TARGETDESTROYED ("targetdestroyed"),
    BROWSER_TARGETCHANGED ("targetchanged"),
    BROWSER_DISCONNECTED ("disconnected"),

    BROWSERCONTEXT_TARGETCREATED ("targetcreated"),
    BROWSERCONTEXT_TARGETDESTROYED ("targetdestroyed"),
    BrowserContext_TargetChanged ("targetchanged"),

    NETWORK_MANAGER_REQUEST("Events.NetworkManager.Request"),
    NETWORK_MANAGER_RESPONSE("Events.NetworkManager.Response"),
    NETWORK_MANAGER_REQUEST_FAILED("Events.NetworkManager.RequestFailed"),
    NETWORK_MANAGER_REQUEST_FINISHED("Events.NetworkManager.RequestFinished"),

    FRAME_MANAGER_FRAME_ATTACHED("Events.FrameManager.FrameAttached"),
    FRAME_MANAGER_FRAME_NAVIGATED("Events.FrameManager.FrameNavigated"),
    FRAME_MANAGER_FRAME_DETACHED("Events.FrameManager.FrameDetached"),
    FRAME_MANAGER_LIFECYCLE_EVENT("Events.FrameManager.LifecycleEvent"),
    FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT("Events.FrameManager.FrameNavigatedWithinDocument"),
    FRAME_MANAGER_EXECUTION_CONTEXTCREATED("Events.FrameManager.ExecutionContextCreated"),
    FRAME_MANAGER_EXECUTION_CONTEXTDESTROYED("Events.FrameManager.ExecutionContextDestroyed"),

    CONNECTION_DISCONNECTED("Events.Connection.Disconnected"),
    CDPSESSION_DISCONNECTED("Events.CDPSession.Disconnected");

    private String name;

    Events(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
