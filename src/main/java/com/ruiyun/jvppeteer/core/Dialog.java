package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.entities.DialogType;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dialog.class);
    private CDPSession client;
    private String type;
    private String message;
    private String defaultValue = "";
    private boolean handled;
    public Dialog() {
        super();
    }

    public Dialog(CDPSession client, DialogType type, String message, String defaultValue) {
        super();
        this.client = client;
        this.type = type.getType();
        this.message = message;
        if (StringUtil.isNotEmpty(defaultValue))
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
    public String defaultValue() {
        return this.defaultValue;
    }

    private Future<Boolean> handle(boolean accept, String text) {
        ValidateUtil.assertArg(!this.handled, "Cannot accept dialog which is already handled!");
        return ForkJoinPool.commonPool().submit(() -> {
            try {
                this.handled = true;
                Map<String, Object> params = ParamsFactory.create();
                params.put("accept", accept);
                params.put("promptText", text);
                this.client.send("Page.handleJavaScriptDialog", params);
            } catch (Exception e) {
                LOGGER.error("Dialog accept error ", e);
                return false;
            }
            return true;
        });
    }

    /**
     * 接受对话框
     *
     * @param promptText 在提示中输入的文本。如果对话框type不提示，则不会引起任何影响
     * @return 对话框关闭后返回
     */
    public Future<Boolean> accept(String promptText) {
        return this.handle(true, promptText);
    }

    /**
     * 不接受对话框的内容
     *
     * @return 对话框关闭后返回
     */
    public Future<Boolean> dismiss() {
        return this.handle(false, "");
    }

}
