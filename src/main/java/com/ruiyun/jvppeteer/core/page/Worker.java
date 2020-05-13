package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;
import com.ruiyun.jvppeteer.protocol.runtime.ExceptionDetails;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.protocol.runtime.ConsoleAPICalledPayload;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class Worker extends EventEmitter {

    private CDPSession client;

    private String url;

    private ConsoleAPI consoleAPICalled;

    private Consumer<ExceptionDetails> exceptionThrown;

    private ExecutionContext context;

    private CountDownLatch contextLatch;

    public Worker(CDPSession client, String url, ConsoleAPI consoleAPICalled, Consumer<ExceptionDetails> exceptionThrown) {
        super();
        this.client = client;
        this.url = url;
        DefaultBrowserListener<JsonNode> listener = new DefaultBrowserListener<JsonNode>(){
            @Override
            public void onBrowserEvent(JsonNode event) {
                try {
                    Worker worker = (Worker)this.getTarget();
                    ExecutionContextDescription contextDescription = Constant.OBJECTMAPPER.treeToValue(event.get("context"), ExecutionContextDescription.class);
                    ExecutionContext executionContext = new ExecutionContext(client,contextDescription,null);
                    worker.executionContextCallback(executionContext);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        };
        listener.setMothod("Runtime.executionContextCreated");
        listener.setTarget(this);
        this.client.once(listener.getMothod(),listener);

        this.client.send("Runtime.enable",null,false);

        DefaultBrowserListener<ConsoleAPICalledPayload> consoleLis = new DefaultBrowserListener<ConsoleAPICalledPayload>(){
            @Override
            public void onBrowserEvent(ConsoleAPICalledPayload event) {
                consoleAPICalled.call(event.getType(),event.getArgs().stream().map(item -> jsHandleFactory(item)).collect(Collectors.toList()),event.getStackTrace());
            }
        };
        consoleLis.setMothod("Runtime.consoleAPICalled");
        this.client.on(consoleLis.getMothod(),consoleLis);

        DefaultBrowserListener<JsonNode> exceptionLis = new DefaultBrowserListener<JsonNode>(){
            @Override
            public void onBrowserEvent(JsonNode event) {
                try {
                    ExceptionDetails exceptionDetails = Constant.OBJECTMAPPER.treeToValue(event.get("exceptionDetails"), ExceptionDetails.class);
                    exceptionThrown.accept(exceptionDetails);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        };
        exceptionLis.setMothod("Runtime.exceptionThrown");
        this.client.on(exceptionLis.getMothod(),exceptionLis);
    }

    public JSHandle jsHandleFactory(RemoteObject remoteObject){
        return new JSHandle(this.context, client, remoteObject);
    }
    protected void executionContextCallback(ExecutionContext executionContext){
        this.setContext(executionContext);
    }
    private ExecutionContext executionContextPromise() throws InterruptedException {
        if(context == null){
            this.setContextLatch(new CountDownLatch(1)) ;
            boolean await = this.getContextLatch().await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if(!await){
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

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) throws InterruptedException, JsonProcessingException {
        return this.executionContextPromise().evaluate(pageFunction,type, args);
    }

    public Object evaluateHandle(String pageFunction,PageEvaluateType type, Object... args) throws InterruptedException, JsonProcessingException {
        return this.executionContextPromise().evaluateHandle(pageFunction,type ,args);
    }


}


