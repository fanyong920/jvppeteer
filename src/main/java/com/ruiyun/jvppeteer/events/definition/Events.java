package com.ruiyun.jvppeteer.events.definition;

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

    NETWORKMANAGER_REQUEST("Events.NetworkManager.Request"),
    NETWORKMANAGER_RESPONSE("Events.NetworkManager.Response"),
    NETWORKMANAGER_REQUESTFAILED("Events.NetworkManager.RequestFailed"),
    NETWORKMANAGER_REQUESTFINISHED("Events.NetworkManager.RequestFinished"),

    FRAMEMANAGER_FRAMEATTACHED("Events.FrameManager.FrameAttached"),
    FRAMEMANAGER_FRAMENAVIGATED("Events.FrameManager.FrameNavigated"),
    FRAMEMANAGER_FRAMEDETACHED("Events.FrameManager.FrameDetached"),
    FRAMEMANAGER_LIFECYCLEEVENT("Events.FrameManager.LifecycleEvent"),
    FRAMEMANAGER_FRAMENAVIGATEDWITHINDOCUMENT("Events.FrameManager.FrameNavigatedWithinDocument"),
    FRAMEMANAGER_EXECUTIONCONTEXTCREATED("Events.FrameManager.ExecutionContextCreated"),
    FRAMEMANAGER_EXECUTIONCONTEXTDESTROYED("Events.FrameManager.ExecutionContextDestroyed"),

    CONNECTION_DISCONNECTED("Events.Connection.Disconnected"),
    CDPSESSION_DISCONNECTED("Events.CDPSession.Disconnected");

    private String name;

    Events(String name) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
