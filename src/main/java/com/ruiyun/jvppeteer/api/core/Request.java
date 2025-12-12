package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.cdp.core.CdpResponse;
import com.ruiyun.jvppeteer.cdp.entities.ContinueRequestOverrides;
import com.ruiyun.jvppeteer.cdp.entities.ErrorReasons;
import com.ruiyun.jvppeteer.cdp.entities.HeaderEntry;
import com.ruiyun.jvppeteer.cdp.entities.Initiator;
import com.ruiyun.jvppeteer.cdp.entities.InterceptResolutionAction;
import com.ruiyun.jvppeteer.cdp.entities.InterceptResolutionState;
import com.ruiyun.jvppeteer.cdp.entities.Interception;
import com.ruiyun.jvppeteer.cdp.entities.ResourceType;
import com.ruiyun.jvppeteer.cdp.entities.ResponseForRequest;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    protected volatile String interceptionId;
    protected volatile String failureText;
    protected volatile CdpResponse response;
    protected volatile boolean fromMemoryCache;
    protected volatile List<Request> redirectChain;
    protected final Interception interception = new Interception();

    public Request() {
    }

    public abstract CDPSession client();

    public abstract String id();

    /**
     * 请求的 URL
     *
     * @return 请求的 URL
     */
    public abstract String url();

    /**
     * 如果允许继续拦截（即不调用 abort() 和 respond()），返回 continueRequest 的 ContinueRequestOverrides。
     *
     * @return ContinueRequestOverrides
     */
    public ContinueRequestOverrides continueRequestOverrides() {
        return this.interception.getRequestOverrides();
    }

    /**
     * 如果允许拦截响应（即不调用 abort()），则使用 ResponseForRequest。
     *
     * @return ResponseForRequest
     */
    public ResponseForRequest responseForRequest() {
        return this.interception.getResponse();
    }

    /**
     * 最近中止请求的原因
     *
     * @return 最近中止请求的原因
     */
    public ErrorReasons abortErrorReason() {
        return this.interception.getAbortReason();
    }

    /**
     * 描述当前解析操作和优先级的 InterceptResolutionState 对象。
     * <p>
     * InterceptResolutionState 包含：行动：InterceptResolutionAction 优先级？：数字
     * <p>
     * InterceptResolutionAction 是以下之一：abort、respond、continue、disabled、none 或 already-handled。
     *
     * @return InterceptResolutionState 对象
     */
    public InterceptResolutionState interceptResolutionState() {
        InterceptResolutionState state = new InterceptResolutionState();
        if (!this.interception.getEnabled()) {
            state.setAction(InterceptResolutionAction.DISABLED);
            return state;
        }
        if (this.interception.getHandled()) {
            state.setAction(InterceptResolutionAction.ALREADY_HANDLED);
            return state;
        }
        return this.interception.getResolutionState();
    }

    /**
     * 如果拦截解析已处理，则为 true，否则为 false。
     *
     * @return 如果拦截解析已处理，则为 true，否则为 false。
     */
    public boolean isInterceptResolutionHandled() {
        return this.interception.getHandled();
    }

    /**
     * 将异步请求处理程序添加到处理队列。延迟处理程序不保证以任何特定顺序执行，但保证它们在请求拦截完成之前解析。
     *
     * @param pendingHandler Runnable
     */
    public void enqueueInterceptAction(Runnable pendingHandler) {
        this.interception.getHandlers().add(pendingHandler);
    }

    protected abstract void _abort(ErrorReasons errorCode);

    /**
     * 使用给定的响应来满足请求。
     *
     * @param response 响应
     */
    public abstract void _respond(ResponseForRequest response);


    /**
     * 拦截请求时，该方法表示放行请求
     *
     * @param overrides 重写请求的信息。
     */
    public abstract void _continue(ContinueRequestOverrides overrides);

    /**
     * 等待挂起的拦截处理程序，然后决定如何完成请求拦截。
     */
    public void finalizeInterceptions() {
        this.interception.getHandlers().forEach(Runnable::run);
        this.interception.setHandlers(new ArrayList<>());
        InterceptResolutionAction action = this.interceptResolutionState().getAction();
        switch (action) {
            case ABORT:
                this._abort(this.interception.getAbortReason());
                break;
            case CONTINUE:
                this._continue(this.interception.getRequestOverrides());
                break;
            case RESPOND:
                if (this.interception.getResponse() == null) {
                    throw new JvppeteerException("Response is missing for the interception");
                }
                this._respond(this.interception.getResponse());
                break;
        }
    }

    /**
     * 包含渲染引擎感知到的请求的资源类型。
     *
     * @return 资源类型
     */
    public abstract ResourceType resourceType();

    /**
     * 使用的方法（GET、POST 等）
     *
     * @return 方法
     */
    public abstract String method();

    /**
     * 请求体数据
     *
     * @return 请求体
     */
    public abstract String postData();

    /**
     * 是否有请求体
     * <p>
     * 当请求有 POST 数据时为 true。
     * <p>
     * 请注意，当数据太长或不易以解码形式提供时，当此标志为真时，HTTPRequest.postData() 可能仍然未定义。
     * <p>
     * 在这种情况下，请使用 HTTPRequest.fetchPostData()。
     *
     * @return 是否有请求体
     */
    public abstract boolean hasPostData();

    /**
     * 从浏览器获取请求的 POST 数据。
     *
     * @return POST 数据
     */
    public abstract String fetchPostData();


    /**
     * 具有与请求关联的 HTTP 标头的对象。所有标头名称均为小写。
     *
     * @return Map
     */
    public abstract List<HeaderEntry> headers();

    /**
     * request 对应的响应
     *
     * @return Response
     */
    public abstract Response response();

    public abstract Frame frame();

    /**
     * 如果请求是当前帧导航的驱动程序，则为 True。
     *
     * @return boolean
     */
    public abstract boolean isNavigationRequest();

    /**
     * 请求的发起者。
     *
     * @return Initiator
     */
    public abstract Initiator initiator();

    /**
     * 重定向链
     * <p>
     * redirectChain 是为获取资源而发起的请求链。
     * </p>
     * 请求链 - 如果服务器至少响应一个重定向，则该链将包含所有重定向的请求。
     *
     * @return List
     */
    public abstract <T extends Request> List<T> redirectChain();

    /**
     * 访问有关请求失败的信息。
     *
     * @return 如果请求失败，则可以返回一个带有 errorText 的对象，其中包含人类可读的错误消息，例如 net::ERR_FAILED，不保证会有失败文本。<p>请求成功，返回null
     */
    public abstract String failure();

    protected void verifyInterception(){
        ValidateUtil.assertArg(this.interception.getEnabled(), "Request Interception is not enabled!");
        ValidateUtil.assertArg(!this.interception.getHandled(), "Request is already handled!");
    }
    protected abstract boolean canBeIntercepted();

    /**
     * 继续请求
     */
    public void continueRequest() {
        this.continueRequest(new ContinueRequestOverrides(), null);
    }

    /**
     * 通过可选的请求覆盖继续请求。
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     *
     * @param overrides （可选）应用于请求的可选覆盖。
     */
    public void continueRequest(ContinueRequestOverrides overrides) {
        this.continueRequest(overrides, null);
    }

    /**
     * 拦截请求时，该方法表示放行请求
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     *
     * @param priority  如果提供，则使用协作处理规则来解决拦截。否则，拦截将立即解决。
     * @param overrides 重写请求的信息。
     */
    public void continueRequest(ContinueRequestOverrides overrides, Integer priority) {
        this.verifyInterception();
        if (!this.canBeIntercepted()) {
            return;
        }
        if (priority == null) {
            this._continue(overrides);
            return;
        }
        this.interception.setRequestOverrides(overrides);
        if (this.interception.getResolutionState().getPriority() == null || priority > this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.CONTINUE, priority));
            return;
        }
        if (priority.equals(this.interception.getResolutionState().getPriority())) {
            if (this.interception.getResolutionState().getAction() == InterceptResolutionAction.ABORT || this.interception.getResolutionState().getAction() == InterceptResolutionAction.RESPOND) {
                return;
            }
            this.interception.getResolutionState().setAction(InterceptResolutionAction.CONTINUE);
        }
    }

    /**
     * 使用给定的响应来满足请求。
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     *
     * @param response 满足请求的响应。
     */
    public void respond(ResponseForRequest response) {
        this.respond(response, null);
    }

    /**
     * 使用给定的响应来满足请求。
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     *
     * @param response 满足请求的响应。
     * @param priority 可选的）如果提供，则使用协作处理规则来解决拦截。否则，拦截将立即解决。
     */
    public void respond(ResponseForRequest response, Integer priority) {
        this.verifyInterception();
        if (!this.canBeIntercepted()) {
            return;
        }
        if (priority == null) {
            this._respond(response);
            return;
        }
        this.interception.setResponse(response);
        if (this.interception.getResolutionState().getPriority() == null || priority > this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.RESPOND, priority));
            return;
        }
        if (priority.equals(this.interception.getResolutionState().getPriority())) {
            if (this.interception.getResolutionState().getAction() == InterceptResolutionAction.ABORT) {
                return;
            }
            this.interception.getResolutionState().setAction(InterceptResolutionAction.RESPOND);
        }
    }

    /**
     * 中止请求。
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     */
    public void abort() {
        this.abort(ErrorReasons.FAILED, null);
    }

    /**
     * 中止请求。
     * <p>
     * 要使用此功能，应使用 Page.setRequestInterception() 启用请求拦截。
     *
     * @param errorCode （可选）提供可选的错误代码。
     * @param priority  （可选的）如果提供，则使用协作处理规则来解决拦截。否则，拦截将立即解决。
     */
    public void abort(ErrorReasons errorCode, Integer priority) {
        this.verifyInterception();
        if (!this.canBeIntercepted()) {
            return;
        }
        if (priority == null) {
            this._abort(errorCode);
            return;
        }
        this.interception.setAbortReason(errorCode);
        if (this.interception.getResolutionState().getPriority() == null || priority >= this.interception.getResolutionState().getPriority()) {
            this.interception.setResolutionState(new InterceptResolutionState(InterceptResolutionAction.ABORT, priority));
        }
    }

    protected void handleError(Exception e) {
        // Firefox throws an invalid argument error with a message starting with
        // 'Expected "header" [...]'.
        if (e instanceof ProtocolException) {
            boolean flag = e.getMessage().contains("Invalid header") || e.getMessage().contains("Unsafe header") || e.getMessage().contains("Expected \"header\"")
                    // WebDriver BiDi error for invalid values, for example, headers.
                    || e.getMessage().contains("invalid argument");
            if (flag) {
                throw (ProtocolException) e;
            }
        }
        // In certain cases, protocol will return error if the request was
        // already canceled or the page was closed. We should tolerate these
        // errors.
        LOGGER.error("request error:", e);
    }

    public void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
    }

    protected static final Map<Integer, String> STATUS_TEXTS = new HashMap<>();


    static {
        // List taken from https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml with extra 306 and 418 codes.
        STATUS_TEXTS.put(100, "Continue");
        STATUS_TEXTS.put(101, "Switching Protocols");
        STATUS_TEXTS.put(102, "Processing");
        STATUS_TEXTS.put(103, "Early Hints");
        STATUS_TEXTS.put(200, "OK");
        STATUS_TEXTS.put(201, "Created");
        STATUS_TEXTS.put(202, "Accepted");
        STATUS_TEXTS.put(203, "Non-Authoritative Information");
        STATUS_TEXTS.put(204, "No Content");
        STATUS_TEXTS.put(205, "Reset Content");
        STATUS_TEXTS.put(206, "Partial Content");
        STATUS_TEXTS.put(207, "Multi-Status");
        STATUS_TEXTS.put(208, "Already Reported");
        STATUS_TEXTS.put(226, "IM Used");
        STATUS_TEXTS.put(300, "Multiple Choices");
        STATUS_TEXTS.put(301, "Moved Permanently");
        STATUS_TEXTS.put(302, "Found");
        STATUS_TEXTS.put(303, "See Other");
        STATUS_TEXTS.put(304, "Not Modified");
        STATUS_TEXTS.put(305, "Use Proxy");
        STATUS_TEXTS.put(306, "Switch Proxy");
        STATUS_TEXTS.put(307, "Temporary Redirect");
        STATUS_TEXTS.put(308, "Permanent Redirect");
        STATUS_TEXTS.put(400, "Bad Request");
        STATUS_TEXTS.put(401, "Unauthorized");
        STATUS_TEXTS.put(402, "Payment Required");
        STATUS_TEXTS.put(403, "Forbidden");
        STATUS_TEXTS.put(404, "Not Found");
        STATUS_TEXTS.put(405, "Method Not Allowed");
        STATUS_TEXTS.put(406, "Not Acceptable");
        STATUS_TEXTS.put(407, "Proxy Authentication Required");
        STATUS_TEXTS.put(408, "Request Timeout");
        STATUS_TEXTS.put(409, "Conflict");
        STATUS_TEXTS.put(410, "Gone");
        STATUS_TEXTS.put(411, "Length Required");
        STATUS_TEXTS.put(412, "Precondition Failed");
        STATUS_TEXTS.put(413, "Payload Too Large");
        STATUS_TEXTS.put(414, "URI Too Long");
        STATUS_TEXTS.put(415, "Unsupported Media Type");
        STATUS_TEXTS.put(416, "Range Not Satisfiable");
        STATUS_TEXTS.put(417, "Expectation Failed");
        STATUS_TEXTS.put(418, "I'm a teapot");
        STATUS_TEXTS.put(421, "Misdirected Request");
        STATUS_TEXTS.put(422, "Unprocessable Entity");
        STATUS_TEXTS.put(423, "Locked");
        STATUS_TEXTS.put(424, "Failed Dependency");
        STATUS_TEXTS.put(425, "Too Early");
        STATUS_TEXTS.put(426, "Upgrade Required");
        STATUS_TEXTS.put(428, "Precondition Required");
        STATUS_TEXTS.put(429, "Too Many Requests");
        STATUS_TEXTS.put(431, "Request Header Fields Too Large");
        STATUS_TEXTS.put(451, "Unavailable For Legal Reasons");
        STATUS_TEXTS.put(500, "Internal Server Error");
        STATUS_TEXTS.put(501, "Not Implemented");
        STATUS_TEXTS.put(502, "Bad Gateway");
        STATUS_TEXTS.put(503, "Service Unavailable");
        STATUS_TEXTS.put(504, "Gateway Timeout");
        STATUS_TEXTS.put(505, "HTTP Version Not Supported");
        STATUS_TEXTS.put(506, "Variant Also Negotiates");
        STATUS_TEXTS.put(507, "Insufficient Storage");
        STATUS_TEXTS.put(508, "Loop Detected");
        STATUS_TEXTS.put(510, "Not Extended");
        STATUS_TEXTS.put(511, "Network Authentication Required");
    }

}
