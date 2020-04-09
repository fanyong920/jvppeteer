package com.ruiyun.jvppeteer.protocol.work;

import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.function.Consumer;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class Worker {

    private CDPSession client;

    private String url;

    private ConsoleAPI consoleAPICalled;

    private Consumer<ExceptionDetails> exceptionThrown;

    public Worker(CDPSession client, String url, ConsoleAPI consoleAPICalled, Consumer<ExceptionDetails> exceptionThrown) {
        this.client = client;
        this.url = url;
        this.consoleAPICalled = consoleAPICalled;
        this.exceptionThrown = exceptionThrown;
    }
}


