package com.ruiyun.jvppeteer.cdp.entities;

public enum ErrorReasons {

    ABORTED("Aborted"),
    ACCESSDENIED("AccessDenied"),
    ADDRESSUNREACHABLE("AddressUnreachable"),
    BLOCKEDBYCLIENT("BlockedByClient"),
    BLOCKEDBYRESPONSE("BlockedByResponse"),
    CONNECTIONABORTED("ConnectionAborted"),
    CONNECTIONCLOSED("ConnectionClosed"),
    CONNECTIONFAILED("ConnectionFailed"),
    CONNECTIONREFUSED("ConnectionRefused"),
    CONNECTIONRESET("ConnectionReset"),
    INTERNETDISCONNECTED("InternetDisconnected"),
    NAMENOTRESOLVED("NameNotResolved"),
    TIMEDOUT("TimedOut"),
    FAILED("Failed");

    private String name;

    ErrorReasons(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
