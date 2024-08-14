package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.options.TargetType;
import com.ruiyun.jvppeteer.protocol.runtime.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);
    private final CDPSession client;
    private final String url;
    private ExecutionContext context;
    private CountDownLatch contextLatch;
    private final Consumer<ExceptionThrownEvent> exceptionThrown;
    private final ConsoleAPI consoleAPICalled;

    public Worker(CDPSession client, String url, String targetId, TargetType targetType, ConsoleAPI consoleAPICalled, Consumer<ExceptionThrownEvent> exceptionThrown) {
        super();
        this.client = client;
        this.url = url;
        this.exceptionThrown = exceptionThrown;
        this.consoleAPICalled = consoleAPICalled;
        this.client.once(CDPSession.CDPSessionEvent.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) this::onExecutionContextCreated);
        this.client.send("Runtime.enable");
        this.client.on(CDPSession.CDPSessionEvent.Runtime_consoleAPICalled, (Consumer<ConsoleAPICalledEvent>) this::onConsoleAPICalled);
        this.client.on(CDPSession.CDPSessionEvent.Runtime_exceptionThrown,(Consumer<ExceptionThrownEvent>) this::onExceptionThrown);
    }

    private void onExceptionThrown(ExceptionThrownEvent event) {
        exceptionThrown.accept(event);
    }

    private void onConsoleAPICalled(ConsoleAPICalledEvent event) {
        this.consoleAPICalled.call(event.getType(), event.getArgs().stream().map(this::jsHandleFactory).collect(Collectors.toList()), event.getStackTrace());
    }

    private void onExecutionContextCreated(ExecutionContextCreatedEvent event) {
        ExecutionContextDescription contextDescription = event.getContext();
        ExecutionContext executionContext = new ExecutionContext(client, contextDescription, null);
        this.executionContextCallback(executionContext);
    }

    public JSHandle jsHandleFactory(RemoteObject remoteObject) {
        return new JSHandle(this.context, client, remoteObject);
    }

    protected void executionContextCallback(ExecutionContext executionContext) {
        this.setContext(executionContext);
    }

    private ExecutionContext executionContextPromise() throws InterruptedException {
        if (context == null) {
            this.setContextLatch(new CountDownLatch(1));
            boolean await = this.getContextLatch().await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new TimeoutException("Wait for ExecutionContext timeout");
            }
        }
        return context;
    }

    private CountDownLatch getContextLatch() {
        return contextLatch;
    }

    private void setContextLatch(CountDownLatch contextLatch) {
        this.contextLatch = contextLatch;
    }

    public void setContext(ExecutionContext context) {
        this.context = context;
    }

    public String url() {
        return this.url;
    }
    public ExecutionContext executionContext() throws InterruptedException {
        return this.executionContextPromise();
    }
    public Object evaluate(String pageFunction, List<Object> args) throws InterruptedException {
        return this.executionContextPromise().evaluate(pageFunction, args);
    }
    public Object evaluateHandle(String pageFunction, List<Object> args) throws InterruptedException {
        return this.executionContextPromise().evaluateHandle(pageFunction, args);
    }

}


