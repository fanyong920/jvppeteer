package com.ruiyun.jvppeteer.types.page;

import com.ruiyun.jvppeteer.types.page.DOM.ElementHandle;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.protocol.page.FileChooserOpenedPayload;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.List;

public class FileChooser {

    private CDPSession client;

    private ElementHandle element;

    private boolean handled;

    private boolean multiple;

    public FileChooser() {
    }

    public FileChooser(CDPSession client, ElementHandle element, FileChooserOpenedPayload event) {
        this.client = client;
        this.element = element;
        this.multiple = !"selectSingle".equals(event.getMode());
        this.handled = false;
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * @param {!Array<string>} filePaths
     */
    public void accept(List<String> filePaths) {
        ValidateUtil.assertBoolean(!this.handled, "Cannot accept FileChooser which is already handled!");
        this.handled = true;
        this.element.uploadFile(filePaths);
    }


    public void cancel() {
        ValidateUtil.assertBoolean (!this.handled,"Cannot cancel FileChooser which is already handled!");
        this.handled = true;
    }
}
