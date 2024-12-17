package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.transport.CallbackRegistry;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.JV_HANDLE_MESSAGE_THREAD;

public abstract class Connection extends EventEmitter<ConnectionEvents> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    protected final String url;
    protected final ConnectionTransport transport;
    protected final int delay;
    protected final int timeout;
    protected final Map<String, CdpCDPSession> sessions = new ConcurrentHashMap<>();
    protected volatile boolean closed;
    protected final Set<String> manuallyAttached = new HashSet<>();
    protected final CallbackRegistry callbacks = new CallbackRegistry();//并发
    protected final AtomicLong id = new AtomicLong(1);
    protected AtomicLong messageThreadId = new AtomicLong(1);
    protected ExecutorService handleMessageExecutorService = Executors.newSingleThreadExecutor(r -> new Thread(r, JV_HANDLE_MESSAGE_THREAD + messageThreadId.getAndIncrement()));
    Runnable closeRunner;

    public Connection(String url, ConnectionTransport transport, int delay, int timeout) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        this.timeout = timeout;
        this.transport.setConnection(this);
    }

    public JsonNode send(String method) {
        return send(method, null);
    }

    public JsonNode send(String method, Object params) {
        return this.send(method, params, null, true);
    }

    public JsonNode send(String method, Object params, Integer timeout, boolean isBlocking) {
        return this.rawSend(method, params, null, timeout, isBlocking);
    }

    public abstract JsonNode rawSend(String method, Object params, String sessionId, Integer timeout, boolean isBlocking);

    public abstract void onMessage(String message);

    public abstract String url();

    public abstract void dispose();

    public abstract boolean closed();

    public abstract List<ProtocolException> getPendingProtocolErrors();

    public abstract CDPSession session(String sessionId);

    public abstract boolean isAutoAttached(String targetId);

    public abstract CDPSession _createSession(TargetInfo targetInfo, boolean isAutoAttachEmulated);

    public abstract void onClose();

    protected Runnable handleMessageRunnable(JsonNode response) {
        return () -> {
        };
    }

    public void setCloseRunner(Runnable closeRunner) {
        this.closeRunner = closeRunner;
    }

    public Runnable closeRunner() {
        return this.closeRunner;
    }
}