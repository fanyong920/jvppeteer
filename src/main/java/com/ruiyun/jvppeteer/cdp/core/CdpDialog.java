package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Dialog;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.DialogType;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdpDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdpDialog.class);
    private final CDPSession client;

    public CdpDialog(CDPSession primaryTargetClient, DialogType type, String message, String defaultPrompt) {
        super(type, message, defaultPrompt);
        this.client = primaryTargetClient;
    }

    protected boolean handle(boolean accept, String text) {
        ValidateUtil.assertArg(!this.handled, "Cannot accept dialog which is already handled!");
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
    }
}
