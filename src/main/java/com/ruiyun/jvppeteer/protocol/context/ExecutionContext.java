package com.ruiyun.jvppeteer.protocol.context;


import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.dom.DOMWorld;
import com.ruiyun.jvppeteer.protocol.dom.ElementHandle;
import com.ruiyun.jvppeteer.protocol.js.JSHandle;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.regex.Pattern;

public class ExecutionContext {
    public static final String EVALUATION_SCRIPT_URL = "__puppeteer_evaluation_script__" ;
    //TODO 验证表达式有效性
    public static final Pattern SOURCE_URL_REGEX = Pattern.compile("^[\\040\\t]*//[@#] sourceURL=\\s*(\\S*?)\\s*$",Pattern.MULTILINE);

    private DOMWorld world;
    private String contextId;

    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, DOMWorld world) {

        this.world = world;
    }

    public DOMWorld getWorld() {
        return world;
    }

    public void setWorld(DOMWorld world) {
        this.world = world;
    }

    public ElementHandle adoptElementHandle(ElementHandle handle) {
        return null;
    }

    public JSHandle evaluateHandle(String pageFunction, PageEvaluateType type, Object... args) {
        String suffix = "//# sourceURL="+ExecutionContext.EVALUATION_SCRIPT_URL;
        if(PageEvaluateType.STRING.equals(type)){
            String contextId = this.contextId;
            String expression = pageFunction;
            String expressionWithSourceUrl = ExecutionContext.SOURCE_URL_REGEX.matcher(expression).find() ? expression : expression + "\n" + suffix;
        }
        return null;
    }

    public Object evaluate(String pageFunction, PageEvaluateType type, Object... args) {
        return  null;
    }

    public JSHandle evaluateInternal(boolean b, String pageFunction, PageEvaluateType type, Object[] args) {
        return  null;
    }
}
