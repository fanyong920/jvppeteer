package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.cdp.entities.DialogType;
import com.ruiyun.jvppeteer.util.StringUtil;

public abstract class Dialog {
    protected String type;
    protected String message;
    protected String defaultValue = "";
    protected boolean handled;

    public Dialog() {
        super();
    }

    public Dialog(DialogType type, String message, String defaultValue) {
        super();
        this.type = type.getType();
        this.message = message;
        if (StringUtil.isNotEmpty(defaultValue))
            this.defaultValue = defaultValue;
    }

    /**
     * 返回 dialog 的类型
     *
     * @return {string}
     */
    public String type() {
        return this.type;
    }

    /**
     * 返回 dialog 的信息
     *
     * @return {string}
     */
    public String message() {
        return this.message;
    }

    /**
     * 返回 dialog 的默认值
     *
     * @return {string}
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * 处理 dialog
     *
     * @param accept 是否接受 dialog
     * @param text   根据 text 来处理 dialog 比如 text = 确定,就点击确定
     * @return 处理的结果
     */
    protected abstract boolean handle(boolean accept, String text);

    /**
     * 接受对话框
     *
     * @param promptText 在提示中输入的文本。如果对话框type不提示，则不会引起任何影响
     * @return 对话框关闭后返回
     */
    public boolean accept(String promptText) {
        return this.handle(true, promptText);
    }

    /**
     * 不接受对话框的内容
     *
     * @return 对话框关闭后返回
     */
    public Boolean dismiss() {
        return this.handle(false, "");
    }

}
