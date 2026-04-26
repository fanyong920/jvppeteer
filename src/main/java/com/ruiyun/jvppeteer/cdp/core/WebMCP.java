package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.WebMCPTool;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPTool;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPToolInvokedEvent;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPToolRespondedEvent;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPToolsAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.ProtocolWebMCPToolsRemovedEvent;
import com.ruiyun.jvppeteer.cdp.events.WebMCPEvents;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolCall;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolCallResult;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolsAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolsRemovedEvent;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The experimental WebMCP class provides an API for the WebMCP API.
 * <p>
 * See the
 * <a href="https://pptr.dev/guides/webmcp">WebMCP</a> guide
 * for more details.
 * </p>
 * <pre>{@code
 * page.goto("https://www.example.com");
 * List<WebMCPTool> tools = page.webmcp.tools();
 * for (WebMCPTool tool : tools) {
 *   System.out.println("Tool found: " + tool.name + "-" + tool.description);
 * }}</pre>
 *
 *
 */
public class WebMCP extends EventEmitter<WebMCPEvents> {
    private static final Logger logger = LoggerFactory.getLogger(WebMCP.class);

    private CDPSession client;
    private FrameManager frameManager;
    private Map<String, Map<String, WebMCPTool>> tools;
    private Map<String, WebMCPToolCall> pendingCalls;

    /**
     * 内部构造函数
     *
     * @param client       CDP 会话客户端
     * @param frameManager 帧管理器
     */
    public WebMCP(CDPSession client, FrameManager frameManager) {
        super();
        this.client = client;
        this.frameManager = frameManager;
        this.tools = new ConcurrentHashMap<>();
        this.pendingCalls = new ConcurrentHashMap<>();

        // 绑定帧导航事件
        this.frameManager.on(FrameManager.FrameManagerEvent.FrameNavigated, onFrameNavigated);

        // 绑定监听器
        bindListeners();
    }

    /**
     * 处理工具添加事件
     *
     * @param event 协议层工具添加事件数据
     */
    private Consumer<ProtocolWebMCPToolsAddedEvent> onToolsAdded = (ProtocolWebMCPToolsAddedEvent event) -> {
        List<ProtocolWebMCPTool> eventTools = event.getTools();
        if (ValidateUtil.isEmpty(eventTools)) {
            return;
        }

        List<WebMCPTool> addedTools = new ArrayList<>();
        for (ProtocolWebMCPTool tool : eventTools) {
            String frameId = tool.getFrameId();
            if (StringUtil.isEmpty(frameId)) {
                continue;
            }

            Frame frame = this.frameManager.frame(frameId);
            if (Objects.isNull(frame)) {
                continue;
            }

            // 获取或创建该帧的工具映射
            Map<String, WebMCPTool> frameTools = this.tools.computeIfAbsent(frameId, k -> new ConcurrentHashMap<>());

            // 创建新工具
            WebMCPTool addedTool = new WebMCPTool(tool, frame);
            frameTools.put(tool.getName(), addedTool);
            addedTools.add(addedTool);
        }
        WebMCPToolsAddedEvent toolsAddedEvent = new WebMCPToolsAddedEvent();
        toolsAddedEvent.setTools(addedTools);
        this.emit(WebMCPEvents.toolsadded, toolsAddedEvent);
    };

    /**
     * 处理工具移除事件
     *
     * @param event 协议层工具移除事件数据
     */
    private Consumer<ProtocolWebMCPToolsRemovedEvent> onToolsRemoved = (ProtocolWebMCPToolsRemovedEvent event) -> {

        List<ProtocolWebMCPTool> eventTools = event.getTools();
        if (ValidateUtil.isEmpty(eventTools)) {
            return;
        }
        List<WebMCPTool> removedTools = new ArrayList<>();

        for (ProtocolWebMCPTool tool : eventTools) {
            String frameId = tool.getFrameId();
            String toolName = tool.getName();

            if (StringUtil.isEmpty(frameId)) {
                continue;
            }

            Map<String, WebMCPTool> frameTools = this.tools.get(frameId);
            if (Objects.nonNull(frameTools)) {
                WebMCPTool removedTool = frameTools.remove(toolName);
                if (Objects.nonNull(removedTool)) {
                    removedTools.add(removedTool);
                }
            }
        }
        if (!removedTools.isEmpty()) {
            WebMCPToolsRemovedEvent toolsRemovedEvent = new WebMCPToolsRemovedEvent();
            toolsRemovedEvent.setTools(removedTools);
            this.emit(WebMCPEvents.toolsremoved, toolsRemovedEvent);
        }
    };

    private Consumer<ProtocolWebMCPToolInvokedEvent> onToolInvoked = (ProtocolWebMCPToolInvokedEvent event) -> {
        Map<String, WebMCPTool> frameTools = this.tools.get(event.getFrameId());
        if (Objects.isNull(frameTools)) {
            return;
        }
        WebMCPTool tool = frameTools.get(event.getToolName());
        if (Objects.isNull(tool)) {
            return;
        }
        WebMCPToolCall call = new WebMCPToolCall(event.getInvocationId(), tool, event.getInput());
        this.pendingCalls.put(call.id(), call);
        tool.emit(WebMCPEvents.toolinvoked, call);
        this.emit(WebMCPEvents.toolinvoked, call);
    };

    private Consumer<ProtocolWebMCPToolRespondedEvent> onToolResponded = (ProtocolWebMCPToolRespondedEvent event) -> {
        WebMCPToolCall call = this.pendingCalls.remove(event.getInvocationId());
        WebMCPToolCallResult response = new WebMCPToolCallResult(
                event.getInvocationId(),
                call,
                event.getStatus(),
                event.getOutput(),
                event.getErrorText(),
                event.getException()
        );
        this.emit(WebMCPEvents.toolresponded, response);
    };

    private Consumer<Frame> onFrameNavigated = (Frame frame) -> {
        this.pendingCalls.clear();
        Map<String, WebMCPTool> frameTools = this.tools.get(frame.id());
        if (Objects.isNull(frameTools)) {
            return;
        }
        Collection<WebMCPTool> toolsList = frameTools.values();
        this.tools.remove(frame.id());

        if (!toolsList.isEmpty()) {
            WebMCPToolsRemovedEvent toolsRemovedEvent = new WebMCPToolsRemovedEvent();
            toolsRemovedEvent.setTools(toolsList.stream().collect(Collectors.toList()));
            this.emit(WebMCPEvents.toolsremoved, toolsRemovedEvent);
        }
    };

    public void initialize() {
        // Send enable command to WebMCP
        try {
            // @ts-expect-error WebMCP is not yet in the Protocol types.
            this.client.send("WebMCP.enable");
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        }
    }

    /**
     * Gets all WebMCP tools defined by the page.
     */
    public List<WebMCPTool> tools() {
        return this.tools.values().stream()
                .flatMap(map -> map.values().stream())
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }

    /**
     * 绑定 CDP 事件监听器
     */
    public void bindListeners() {
        this.client.on(ConnectionEvents.WebMCP_toolsAdded, onToolsAdded);
        this.client.on(ConnectionEvents.WebMCP_toolsRemoved, onToolsRemoved);
        this.client.on(ConnectionEvents.WebMCP_toolInvoked, onToolInvoked);
        this.client.on(ConnectionEvents.WebMCP_toolResponded, onToolResponded);
    }

    public void updateClient(CDPSession client) {
        this.client.off(ConnectionEvents.WebMCP_toolsAdded, onToolsAdded);
        this.client.off(ConnectionEvents.WebMCP_toolsRemoved, onToolsRemoved);
        this.client.off(ConnectionEvents.WebMCP_toolInvoked, onToolInvoked);
        this.client.off(ConnectionEvents.WebMCP_toolResponded, onToolResponded);
        this.client = client;
        this.bindListeners();
    }
}
