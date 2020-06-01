package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.log.DialogType;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class Dialog {

    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

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
        this.handled = false;
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

    /**
     * 接受对话框
     * @param promptText 在提示中输入的文本。如果对话框type不提示，则不会引起任何影响
     * @return 对话框关闭后返回
     */
    public Future<Boolean> accept(String promptText) {
        return Helper.commonExecutor().submit(() -> {
            try {
                ValidateUtil.assertArg(!this.handled, "Cannot accept dialog which is already handled!");
                this.handled = true;
                Map<String, Object> params = new HashMap<>();
                params.put("accept", true);
                params.put("promptText", promptText);
                this.client.send("Page.handleJavaScriptDialog", params, true);
            } catch (Exception e) {
                log.error("Dialog accept error ",e);
                return false;
            }
            return true;
        });
    }

    /**
     *不接受对话框的内容
     * @return 对话框关闭后返回
     */
    public Future<Boolean> dismiss() {
        return Helper.commonExecutor().submit(() -> {
            try {
                ValidateUtil.assertArg(!this.handled, "Cannot dismiss dialog which is already handled!");
                this.handled = true;
                Map<String, Object> params = new HashMap<>();
                params.put("accept", false);
                this.client.send("Page.handleJavaScriptDialog", params, true);
            } catch (Exception e) {
                log.error("Dialog dismiss error ",e);
                return false;
            }
            return true;
        });

    }

    @Override
    public String toString() {
        return "Dialog{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", handled=" + handled +
                '}';
    }
}
