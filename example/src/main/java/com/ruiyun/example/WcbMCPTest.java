package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.core.WebMCP;
import com.ruiyun.jvppeteer.cdp.entities.WebMCPInvocationStatus;
import com.ruiyun.jvppeteer.cdp.entities.WebMCPTool;
import com.ruiyun.jvppeteer.cdp.events.WebMCPEvents;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolCall;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolCallResult;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolsAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.WebMCPToolsRemovedEvent;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WcbMCPTest {
    @Test
    public void test1() throws Exception {
        List<String> args = Collections.singletonList("--enable-features=WebMCPTesting,DevToolsWebMCPSupport");
        LAUNCHOPTIONS.setArgs(args);
        LAUNCHOPTIONS.setAcceptInsecureCerts(true);
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        //必须goto一个页面后 webmcp才可用
        page.goTo("https://www.example.com");
        WebMCP webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        AtomicInteger count = new AtomicInteger();
        AwaitableResult<Void> awaitableResult = AwaitableResult.create();
        webmcp.on(WebMCPEvents.toolsadded, (Consumer<WebMCPToolsAddedEvent>) event -> {
            int i = count.incrementAndGet();
            if (i == 2) {
                awaitableResult.complete();
            }
        });
        // Register an imperative WebMCP tool
        page.evaluate("() => {\n" +
                "      (window).navigator.modelContext.registerTool({\n" +
                "        name: 'test-tool-1',\n" +
                "        description: 'A test tool 1',\n" +
                "        inputSchema: {\n" +
                "          type: 'object',\n" +
                "          properties: {\n" +
                "            text: {type: 'string', description: 'Some text'},\n" +
                "          },\n" +
                "          required: ['text'],\n" +
                "        },\n" +
                "        execute: () => {},\n" +
                "        annotations: {readOnlyHint: true},\n" +
                "      });\n" +
                "    }");
        // Register a declarative WebMCP tool.
        page.evaluate("() => {\n" +
                "      const form = document.createElement('form');\n" +
                "      form.setAttribute('toolname', 'declarative tool name');\n" +
                "      form.setAttribute('tooldescription', 'tool description');\n" +
                "      window.document.body.appendChild(form);\n" +
                "    }");
        awaitableResult.waiting();
        List<WebMCPTool> tools = page.webmcp().tools();
        assertEquals(2, tools.size());
        assertEquals("test-tool-1", tools.get(0).name());
        assertEquals("A test tool 1", tools.get(0).description());
        System.out.println(tools.get(0).inputSchema());
        Assert.assertNotNull(tools.get(0).annotations());
        Assert.assertTrue(tools.get(0).annotations().getReadOnly());
        assertEquals(page.mainFrame(), tools.get(0).frame());
        Assert.assertNull(tools.get(0).formElement());
        Assert.assertNotNull(tools.get(0).location());
        assertEquals("declarative tool name", tools.get(1).name());
        assertEquals("tool description", tools.get(1).description());
        Assert.assertNull(tools.get(1).annotations());
        assertEquals(page.mainFrame(), tools.get(1).frame());
        Assert.assertNotNull(tools.get(1).formElement());
        Assert.assertNull(tools.get(1).location());
        page.close();

        //should fire toolsadded events
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        AwaitableResult<List<WebMCPTool>> imperativeToolAdded = AwaitableResult.create();
        webmcp.once(WebMCPEvents.toolsadded, (Consumer<WebMCPToolsAddedEvent>) event -> imperativeToolAdded.complete(event.getTools()));

        // Register an imperative WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContext.registerTool({\n" +
                "        name: 'test-tool-1',\n" +
                "        description: 'A test tool 1',\n" +
                "        inputSchema: {\n" +
                "          type: 'object',\n" +
                "          properties: {\n" +
                "            text: {type: 'string', description: 'Some text'},\n" +
                "          },\n" +
                "          required: ['text'],\n" +
                "        },\n" +
                "        execute: () => {},\n" +
                "      });\n" +
                "    }");
        List<WebMCPTool> addedTools = imperativeToolAdded.waitingGetResult();
        assertEquals(1, addedTools.size());
        assertEquals("test-tool-1", addedTools.get(0).name());
        assertEquals("A test tool 1", addedTools.get(0).description());
        System.out.println(tools.get(0).inputSchema());
        assertNull(addedTools.get(0).annotations());
        assertEquals(page.mainFrame(), addedTools.get(0).frame());
        assertNull(addedTools.get(0).formElement());
        assertNotNull(addedTools.get(0).location());
        page.close();

        //should fire toolsremoved events
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        // Register an imperative WebMCP tool.
        JSHandle controllerHandle = page.evaluateHandle("() => {\n" +
                "      const controller = new AbortController();\n" +
                "      window.navigator.modelContext.registerTool(\n" +
                "        {\n" +
                "          name: 'test-tool-1',\n" +
                "          description: 'A test tool 1',\n" +
                "          inputSchema: {\n" +
                "            type: 'object',\n" +
                "            properties: {\n" +
                "              text: {type: 'string', description: 'Some text'},\n" +
                "            },\n" +
                "            required: ['text'],\n" +
                "          },\n" +
                "          execute: () => {},\n" +
                "        },\n" +
                "        {signal: controller.signal},\n" +
                "      );\n" +
                "      return controller;\n" +
                "    }");
        page.evaluate("() => {\n" +
                "      const form = document.createElement('form');\n" +
                "      form.setAttribute('toolname', 'declarative tool name');\n" +
                "      form.setAttribute('tooldescription', 'tool description');\n" +
                "      window.document.body.appendChild(form);\n" +
                "    }");
        AwaitableResult<List<WebMCPTool>> imperativeToolRemoved = AwaitableResult.create();
        webmcp.once(WebMCPEvents.toolsremoved, (Consumer<WebMCPToolsRemovedEvent>) event -> imperativeToolRemoved.complete(event.getTools()));
        // Unregister imperative WebMCP tool.
        controllerHandle.evaluate("el => {\n" +
                "      window.navigator.modelContext.unregisterTool?.('test-tool-1');\n" +
                "      el.abort();\n" +
                "    }");
        List<WebMCPTool> removedTools = imperativeToolRemoved.waitingGetResult();
        assertEquals(1, removedTools.size());
        assertEquals("test-tool-1", removedTools.get(0).name());
        assertEquals("A test tool 1", removedTools.get(0).description());
        System.out.println(removedTools.get(0).inputSchema());
        assertNull(removedTools.get(0).annotations());
        assertEquals(page.mainFrame(), removedTools.get(0).frame());
        assertNull(removedTools.get(0).formElement());
        assertNotNull(removedTools.get(0).location());
        AwaitableResult<List<WebMCPTool>> declarativeToolRemoved = AwaitableResult.create();
        webmcp.once(WebMCPEvents.toolsremoved, (Consumer<WebMCPToolsRemovedEvent>) event -> declarativeToolRemoved.complete(event.getTools()));
        page.evaluate("() => {\n" +
                "      document.querySelector('form').remove();\n" +
                "    }");
        removedTools = declarativeToolRemoved.waitingGetResult();
        assertEquals(1, removedTools.size());
        assertEquals("declarative tool name", removedTools.get(0).name());
        assertEquals("tool description", removedTools.get(0).description());
        System.out.println(removedTools.get(0).inputSchema());
        assertNull(removedTools.get(0).annotations());
        assertEquals(page.mainFrame(), removedTools.get(0).frame());
        assertNotNull(removedTools.get(0).formElement());
        assertNull(removedTools.get(0).location());
        page.close();

        //should remove tools on frame navigation
        page = browser.newPage();
        page.goTo("https://www.example.com");
        AwaitableResult<List<WebMCPTool>> toolsAddedPromise = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolsadded, (Consumer<WebMCPToolsAddedEvent>) event -> toolsAddedPromise.onSuccess(event.getTools()));

        // Register an imperative WebMCP tool.
        page.evaluate("() => {\n" +
                "      const form = document.createElement('form');\n" +
                "      form.setAttribute('toolname', 'declarative tool name');\n" +
                "      form.setAttribute('tooldescription', 'tool description');\n" +
                "      document.body.appendChild(form);\n" +
                "    }");

        toolsAddedPromise.waiting();
        AwaitableResult<List<WebMCPTool>> toolsRemovedPromise = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolsremoved, (Consumer<WebMCPToolsRemovedEvent>) event -> toolsRemovedPromise.complete(event.getTools()));
        // Reload page forces frame navigation.
        page.goTo("https://www.example.com");
        removedTools = toolsRemovedPromise.waitingGetResult();
        assertEquals(1, removedTools.size());
        assertEquals(0, page.webmcp().tools().size());
        assertEquals("declarative tool name", removedTools.get(0).name());
        page.close();

        //should fire toolinvoked events
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        AwaitableResult<List<WebMCPTool>> toolAdded = AwaitableResult.create();
        webmcp.once(WebMCPEvents.toolsadded, (Consumer<WebMCPToolsAddedEvent>) event -> toolAdded.complete(event.getTools()));

        // Register an imperative WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContext.registerTool({\n" +
                "        name: 'test-tool-1',\n" +
                "        description: 'A test tool 1',\n" +
                "        inputSchema: {\n" +
                "          type: 'object',\n" +
                "          properties: {\n" +
                "            text: {type: 'string', description: 'Some text'},\n" +
                "          },\n" +
                "          required: ['text'],\n" +
                "        },\n" +
                "        execute: () => {},\n" +
                "      });\n" +
                "    }");
        WebMCPTool addedTool = toolAdded.waitingGetResult().get(0);
        AwaitableResult<WebMCPToolCall> addedToolCalled = AwaitableResult.create();
        addedTool.once(WebMCPEvents.toolinvoked, (Consumer<WebMCPToolCall>) addedToolCalled::complete);
        AwaitableResult<WebMCPToolCall> toolCalled = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolinvoked, (Consumer<WebMCPToolCall>) toolCalled::complete);
        // Execute WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContextTesting.executeTool(\n" +
                "        'test-tool-1',\n" +
                "        JSON.stringify({text: 'test'}),\n" +
                "      );\n" +
                "    }");
        WebMCPToolCall addedToolCall = addedToolCalled.waitingGetResult();
        WebMCPToolCall toolCall = toolCalled.waitingGetResult();
        assertNotNull(addedToolCall.id());
        assertNotNull(addedToolCall.tool());
        assertEquals("test-tool-1", addedToolCall.tool().name());
        assertEquals("A test tool 1", addedToolCall.tool().description());
        System.out.println(addedToolCall.tool().inputSchema());
        assertEquals(page.mainFrame(), addedToolCall.tool().frame());
        assertNull(addedToolCall.tool().formElement());
        assertNotNull(addedToolCall.tool().location());
        System.out.println(addedToolCall.input());

        assertNotNull(toolCall.id());
        assertNotNull(toolCall.tool());
        assertEquals("test-tool-1", toolCall.tool().name());
        assertEquals("A test tool 1", toolCall.tool().description());
        System.out.println(toolCall.tool().inputSchema());
        assertEquals(page.mainFrame(), toolCall.tool().frame());
        assertNull(toolCall.tool().formElement());
        assertNotNull(toolCall.tool().location());
        System.out.println(toolCall.input());
        page.close();

        //should fire toolresponded event with success
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        // Register a WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContext.registerTool({\n" +
                "        name: 'test-tool-1',\n" +
                "        description: 'A test tool 1',\n" +
                "        inputSchema: {\n" +
                "          type: 'object',\n" +
                "          properties: {\n" +
                "            text: {type: 'string', description: 'Some text'},\n" +
                "          },\n" +
                "          required: ['text'],\n" +
                "        },\n" +
                "        execute: () => {\n" +
                "          return 'hello world';\n" +
                "        },\n" +
                "      });\n" +
                "    }");

        AwaitableResult<WebMCPToolCall> toolCalled2 = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolinvoked, (Consumer<WebMCPToolCall>) toolCalled2::complete);
        AwaitableResult<WebMCPToolCallResult> toolResponded2 = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolresponded, (Consumer<WebMCPToolCallResult>) toolResponded2::complete);

        page.evaluate("() => {\n" +
                "      window.navigator.modelContextTesting.executeTool(\n" +
                "        'test-tool-1',\n" +
                "        JSON.stringify({text: 'test'}),\n" +
                "      );\n" +
                "    }");
        WebMCPToolCall call2 = toolCalled2.waitingGetResult();
        WebMCPToolCallResult response2 = toolResponded2.waitingGetResult();
        assertEquals(response2.getId(), call2.id());
        assertEquals(response2.getCall(), call2);
        assertEquals(WebMCPInvocationStatus.Completed, response2.getStatus());
        assertEquals("hello world", response2.getOutput());
        assertNull(response2.getErrorText());
        assertNull(response2.getException());
        page.close();

        //should fire toolresponded event with exception
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        // Register a WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContext.registerTool({\n" +
                "        name: 'raise-exception-tool',\n" +
                "        description: 'A tool that raises JS exception',\n" +
                "        execute: () => {\n" +
                "          throw new Error('sorry!');\n" +
                "        },\n" +
                "      });\n" +
                "    }");
        AwaitableResult<WebMCPToolCall> toolCalled3 = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolinvoked, (Consumer<WebMCPToolCall>) toolCalled3::complete);
        AwaitableResult<WebMCPToolCallResult> toolResponded3 = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolresponded, (Consumer<WebMCPToolCallResult>) event -> toolResponded3.complete(event));
        // Execute WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContextTesting.executeTool(\n" +
                "        'raise-exception-tool',\n" +
                "        '{}',\n" +
                "      );\n" +
                "    }");
        WebMCPToolCall call3 = toolCalled3.waitingGetResult();
        WebMCPToolCallResult response3 = toolResponded3.waitingGetResult();
        assertEquals(response3.getId(), call3.id());
        assertEquals(response3.getCall(), call3);
        assertEquals(WebMCPInvocationStatus.Error, response3.getStatus());

        assertNull(response3.getOutput());
        assertEquals("", response3.getErrorText());
        assertNotNull(response3.getException());
        assertTrue(response3.getException().getDescription().contains("sorry"));
        page.close();

        //should fire toolresponded event with errorText
        page = browser.newPage();
        page.goTo("https://www.example.com");
        webmcp = page.webmcp();
        Assert.assertNotNull(webmcp);
        // Register a WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContext.registerTool({\n" +
                "        name: 'test-tool-1',\n" +
                "        description: 'A test tool 1',\n" +
                "        inputSchema: {\n" +
                "          type: 'object',\n" +
                "          properties: {\n" +
                "            text: {type: 'string', description: 'Some text'},\n" +
                "          },\n" +
                "          required: ['text'],\n" +
                "        },\n" +
                "        execute: () => {},\n" +
                "      });\n" +
                "    }");
        AwaitableResult<WebMCPToolCallResult> toolResponded4 = AwaitableResult.create();
        page.webmcp().once(WebMCPEvents.toolresponded, (Consumer<WebMCPToolCallResult>) toolResponded4::complete);
        // Execute unknown WebMCP tool.
        page.evaluate("() => {\n" +
                "      window.navigator.modelContextTesting.executeTool(\n" +
                "        'unknown-tool-name',\n" +
                "        '{}',\n" +
                "      );\n" +
                "    }");
        WebMCPToolCallResult response4 = toolResponded4.waitingGetResult();
        assertNotNull(response4.getId());
        assertNull(response4.getCall());
        assertEquals(WebMCPInvocationStatus.Error, response4.getStatus());
        assertNull(response4.getOutput());
        assertEquals("Tool not found: unknown-tool-name", response4.getErrorText());
        assertNull(response4.getException());
        System.out.println("测试完成");
    }
}
