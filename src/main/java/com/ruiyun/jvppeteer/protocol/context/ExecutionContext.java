package com.ruiyun.jvppeteer.protocol.context;


import com.ruiyun.jvppeteer.protocol.dom.DOMWorld;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.regex.Pattern;

public class ExecutionContext {
    public static final String EVALUATION_SCRIPT_URL = "__puppeteer_evaluation_script__" ;
    //TODO 验证表达式有效性
    public static final Pattern SOURCE_URL_REGEX = Pattern.compile("^[\\040\\t]*//[@#] sourceURL=\\s*(\\S*?)\\s*$");


    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, DOMWorld world) {
    }
}
