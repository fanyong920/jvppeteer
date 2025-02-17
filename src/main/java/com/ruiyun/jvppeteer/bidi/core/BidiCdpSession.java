package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.transport.CdpConnection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


import static com.ruiyun.jvppeteer.common.Constant.RESULT;
import static com.ruiyun.jvppeteer.common.Constant.SESSION;

public class BidiCdpSession extends CDPSession {
    static Map<String, BidiCdpSession> sessions = new ConcurrentHashMap<>();
    final BidiFrame frame;
    private Connection connection;
    private boolean detached;
    AwaitableResult<String> sessionIdResult = new AwaitableResult<>();

    public BidiCdpSession(BidiFrame frame, String sessionId) {
        super();
        this.frame = frame;
        if (!this.frame.page().browser().cdpSupported()) {
            return;
        }
        Connection connection = this.frame.page().browser().connection();
        this.connection = connection;
        if (StringUtil.isEmpty(sessionId)) {
            this.sessionIdResult.complete(sessionId);
            BidiCdpSession.sessions.put(sessionId, this);
        } else {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("context", frame.id());
                JsonNode res = connection.send("goog:cdp.getSession", params);
                String session_id = res.get(RESULT).get(SESSION).asText();
                this.sessionIdResult.complete(session_id);
                BidiCdpSession.sessions.put(session_id, this);
            } catch (Exception e) {
                this.sessionIdResult.complete("error " + e.getMessage());
            }
        }
    }

    @Override
    public CdpConnection connection() {
        return null;
    }

    @Override
    public String id() {
        String sessionId = this.sessionIdResult.get();
        if (StringUtil.isEmpty(sessionId) && !sessionId.startsWith("error ")) {
            return sessionId;
        }
        return "";
    }

    @Override
    public void detach() {
        if (Objects.isNull(this.connection) || this.connection.closed() || this.detached) {
            return;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("sessionId", this.id());
        try {
            this.frame.client().send("Target.detachFromTarget", params);
        } finally {
            this.onClosed();
        }
    }

    @Override
    public void onClosed() {
        BidiCdpSession.sessions.remove(sessionIdResult.get());
        this.detached = true;
    }

    @Override
    public JsonNode send(String method, Object params, Integer timeout, boolean isBlocking) {
        Objects.requireNonNull(this.connection, "CDP support is required for this feature. The current browser does not support CDP.");
        ValidateUtil.assertArg(!this.detached, "Protocol error (" + method + "): Session closed. Most likely the page has been closed.");
        String session_id = this.sessionIdResult.waitingGetResult();
        if (session_id.startsWith("error")) {
            throw new JvppeteerException(session_id.replace("error ", ""));
        }
        Map<String, Object> paramsMap = ParamsFactory.create();
        paramsMap.put(Constant.METHOD, method);
        paramsMap.put(Constant.PARAMS, params);
        paramsMap.put(SESSION, session_id);
        return this.connection.send("goog:cdp.sendCommand", paramsMap, timeout, true);
    }

}
