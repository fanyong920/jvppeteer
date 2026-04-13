package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Realm;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.api.events.WebWorkerEvent;
import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.IsolatedWorldEmitter;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.util.Helper.createConsoleMessage;

/**
 * The events `workercreated` and `workerdestroyed` are emitted on the page object to signal the worker lifecycle.
 */
public class CdpWebWorker extends WebWorker {
    private final IsolatedWorld world;
    private final CDPSession client;
    private final String id;
    private final TargetType targetType;
    private EventEmitter<WebWorkerEvent> emitter = new EventEmitter<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(CdpWebWorker.class);

    public CdpWebWorker(CDPSession client, String url, String targetId, TargetType targetType, Consumer<ExceptionThrownEvent> exceptionThrown, NetworkManager networkManager) {
        super(url);
        this.id = targetId;
        this.client = client;
        this.targetType = targetType;
        this.world = new IsolatedWorld(null, this, new TimeoutSettings());
        this.client.once(ConnectionEvents.Runtime_executionContextCreated, (Consumer<ExecutionContextCreatedEvent>) event -> this.world.setContext(new ExecutionContext(client, event.getContext(), world)));
        this.world.emitter().on(IsolatedWorldEmitter.IsolatedWorldEventType.Consoleapicalled, (Consumer<ConsoleAPICalledEvent>) event -> {
            try {
                List<JSHandle> values = event.getArgs().stream().map(arg -> this.world.createCdpHandle(arg)).collect(Collectors.toList());
                boolean noInternalListeners = this.emitter.listenerCount(WebWorkerEvent.Console) == 0;
                boolean noWorkerListeners = this.listenerCount(WebWorkerEvent.Console) == 0;
                if (noInternalListeners && noWorkerListeners) {
                    // eslint-disable-next-line max-len -- The comment is long.
                    // eslint-disable-next-line @puppeteer/use-using -- These are not owned by this function.
                    for (JSHandle value : values) {
                        try {
                            value.dispose();
                        } catch (Exception e) {
                            LOGGER.error("jvppeteer error", e);
                        }
                    }
                    return;
                }
                ConsoleMessage consoleMessages = createConsoleMessage(event, values, this.id);
                this.emitter.emit(WebWorkerEvent.Console, consoleMessages);
                if (!noWorkerListeners) {
                    this.emit(WebWorkerEvent.Console, consoleMessages);
                }
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        });
        this.client.on(ConnectionEvents.Runtime_exceptionThrown, exceptionThrown);
        this.client.once(ConnectionEvents.CDPSession_Disconnected, (ignored) -> this.world.dispose());
        // This might fail if the target is closed before we receive all execution contexts.
        try {
            Optional.of(networkManager).ifPresent(manager -> manager.addClient(this.client));
            this.client.send("Runtime.enable");
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        }
    }

    public EventEmitter<WebWorkerEvent> internalEmitter() {
        return emitter;
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


