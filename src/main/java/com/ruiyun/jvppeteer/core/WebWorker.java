package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.common.ConsoleAPI;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.transport.Connection;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class WebWorker {
    private final IsolatedWorld world;
    private final CDPSession client;
    private final String id;
    private final TargetType targetType;
    private final String url;
    private ExecutionContext context;


    public WebWorker(CDPSession client, String url, String targetId, TargetType targetType, ConsoleAPI consoleAPICalled, Consumer<ExceptionThrownEvent> exceptionThrown) {
        super();
        this.url = url;
        this.id = targetId;
        this.client = client;
        this.targetType = targetType;
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        this.world = new IsolatedWorld(null, this, timeoutSettings);
        this.client.once(CDPSession.CDPSessionEvent.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) event -> this.world.setContext(new ExecutionContext(client, event.getContext(), world)));
        this.world.emitter().on(IsolatedWorldEmitter.IsolatedWorldEventType.Consoleapicalled, (Consumer<ConsoleAPICalledEvent>) event -> consoleAPICalled.call(ConsoleMessageType.valueOf(event.getType().toUpperCase()), event.getArgs().stream().map((object) -> new JSHandle(world, object)).collect(Collectors.toList()), event.getStackTrace()));
        this.client.on(CDPSession.CDPSessionEvent.Runtime_exceptionThrown, exceptionThrown);
        this.client.once(CDPSession.CDPSessionEvent.CDPSession_Disconnected, (ignored) -> this.world.dispose());
    }

    public Realm mainRealm() {
        return this.world;
    }

    public CDPSession client() {
        return this.client;
    }

    public void close() throws EvaluateException, JsonProcessingException {
        switch (this.targetType) {
            case SERVICE_WORKER:
            case SHARED_WORKER: {
                Connection connection = this.client.getConnection();
                if (connection != null) {
                    Map<String, Object> params = ParamsFactory.create();
                    params.put(Constant.TARGET_ID, this.id);
                    connection.send("Target.closeTarget", params);
                    params.clear();
                    params.put(Constant.SESSION_ID, this.client.id());
                    connection.send("Target.detachFromTarget", params, null, false);
                }
                break;
            }
            default: {
                this.evaluate("() => {\n" +
                        "          self.close();\n" +
                        "        }");
            }

        }
    }

    /**
     * 此 Web Worker 的 URL。
     *
     * @return URL
     */
    public String url() {
        return this.url;
    }

    /**
     * 根据经验，如果给定函数的返回值比 JSON 对象（例如大多数类）更复杂，那么 evaluate _ 可能 _ 返回一些截断值（或 {}）。这是因为我们返回的不是实际的返回值，而是通过协议将返回值传输到 Puppeteer 的结果的反序列化版本。
     * <p>
     * 一般来说，如果 evaluate 无法正确序列化返回值或者你需要一个可变的 handle 作为返回对象，则应该使用 evaluateHandle。
     *
     * @param pageFunction 要执行的 JavaScript 函数
     * @return pageFunction 执行结果
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     * @throws JsonProcessingException 如果在序列化返回值时发生错误
     */
    public Object evaluate(String pageFunction) throws EvaluateException, JsonProcessingException {
        return this.evaluate(pageFunction, null);
    }

    public JSHandle evaluateHandle(String pageFunction) throws EvaluateException, JsonProcessingException {
        return this.evaluateHandle(pageFunction, null);
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) throws EvaluateException, JsonProcessingException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pageFunction);
        return this.mainRealm().evaluateHandle(pageFunction, args);
    }

    public Object evaluate(String pageFunction, List<Object> args) throws EvaluateException, JsonProcessingException {
        pageFunction = withSourcePuppeteerURLIfNone("evaluate", pageFunction);
        return this.mainRealm().evaluate(pageFunction, args);
    }


}


