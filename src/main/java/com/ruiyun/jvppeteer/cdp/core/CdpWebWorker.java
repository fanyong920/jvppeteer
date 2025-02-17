package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.common.ConsoleAPI;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessageType;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class CdpWebWorker extends WebWorker {
    private final IsolatedWorld world;
    private final CDPSession client;
    private final String id;
    private final TargetType targetType;

    public CdpWebWorker(CDPSession client, String url, String targetId, TargetType targetType, ConsoleAPI consoleAPICalled, Consumer<ExceptionThrownEvent> exceptionThrown) {
        super(url);
        this.id = targetId;
        this.client = client;
        this.targetType = targetType;
        this.world = new IsolatedWorld(null, this, new TimeoutSettings());
        this.client.once(ConnectionEvents.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) event -> this.world.setContext(new ExecutionContext(client, event.getContext(), world)));
        this.world.emitter().on(IsolatedWorldEmitter.IsolatedWorldEventType.Consoleapicalled, (Consumer<ConsoleAPICalledEvent>) event -> consoleAPICalled.call(ConsoleMessageType.valueOf(event.getType().toUpperCase()), event.getArgs().stream().map((object) -> new CdpJSHandle(world, object)).collect(Collectors.toList()), event.getStackTrace()));
        this.client.on(ConnectionEvents.Runtime_exceptionThrown, exceptionThrown);
        this.client.once(ConnectionEvents.CDPSession_Disconnected, (ignored) -> this.world.dispose());
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
                Connection connection = this.client.connection();
                if (Objects.nonNull(connection)) {
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




}


