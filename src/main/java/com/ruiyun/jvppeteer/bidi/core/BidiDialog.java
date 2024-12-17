package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.Dialog;
import com.ruiyun.jvppeteer.cdp.entities.DialogType;

public class BidiDialog extends Dialog {

    private final UserPrompt prompt;

    public BidiDialog(UserPrompt prompt) {
        super(DialogType.valueOf(prompt.info().getType().name()), prompt.info().getMessage(), prompt.info().getDefaultValue());
        this.prompt = prompt;
        this.handled = prompt.handled();
    }

    public static BidiDialog from(UserPrompt prompt) {
        return new BidiDialog(prompt);
    }

    @Override
    protected boolean handle(boolean accept, String userText) {
        return this.prompt.handle(accept, userText) != null;
    }
}
