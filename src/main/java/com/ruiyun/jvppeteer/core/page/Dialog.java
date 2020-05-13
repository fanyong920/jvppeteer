package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.log.DialogType;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.HashMap;
import java.util.Map;

public class Dialog {

    private CDPSession client;

    private String type;

    private String message;

    private String defaultValue = "";

    private boolean handled;

    public Dialog() {
    }

    public Dialog(CDPSession client, DialogType type, String message, String defaultValue) {
        this.client = client;
        this.type = type.getType();
        this.message = message;
        this.handled = false;
        if(StringUtil.isNotEmpty(defaultValue))
        this.defaultValue = defaultValue;
    }

    /**
     * @return {string}
     */
    public String type() {
        return this.type;
    }

    /**
     * @return {string}
     */
    public String message() {
        return this.message;
    }

    /**
     * @return {string}
     */
    public String  defaultValue() {
        return this.defaultValue;
    }

    /**
     * @param {string=} promptText
     */
    public void  accept(String promptText) {
        ValidateUtil.assertBoolean(!this.handled, "Cannot accept dialog which is already handled!");
        this.handled = true;
        Map<String,Object> params = new HashMap<>();
        params.put("accept",true);
        params.put("promptText",promptText);
        this.client.send("Page.handleJavaScriptDialog", params,true);
    }

    public void  dismiss() {
        ValidateUtil.assertBoolean(!this.handled, "Cannot dismiss dialog which is already handled!");
        this.handled = true;
        Map<String,Object> params = new HashMap<>();
        params.put("accept",false);
         this.client.send("Page.handleJavaScriptDialog",params,true);
    }
}
