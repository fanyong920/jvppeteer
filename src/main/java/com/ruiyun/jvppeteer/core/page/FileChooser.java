package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.page.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.List;

/**
 * FileChooser objects are returned via the 'page.waitForFileChooser' method.
 *
 * File choosers let you react to the page requesting for a file.
 */
public class FileChooser {

    private CDPSession client;

    private ElementHandle element;

    private boolean handled;

    private boolean multiple;

    public FileChooser() {
    }

    public FileChooser(CDPSession client, ElementHandle element, FileChooserOpenedEvent event) {
        this.client = client;
        this.element = element;
        this.multiple = !"selectSingle".equals(event.getMode());
        this.handled = false;
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * @param filePaths 选择的文件路径
     */
    public void accept(List<String> filePaths) {
        ValidateUtil.assertArg(!this.handled, "Cannot accept FileChooser which is already handled!");
        this.handled = true;
        this.element.uploadFile(filePaths);
    }


    public void cancel() {
        ValidateUtil.assertArg(!this.handled,"Cannot cancel FileChooser which is already handled!");
        this.handled = true;
    }
}
