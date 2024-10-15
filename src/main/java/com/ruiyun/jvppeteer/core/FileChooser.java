package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.events.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.List;

/**
 * FileChooser objects are returned via the 'page.waitForFileChooser' method.
 * <p>
 * File choosers let you react to the page requesting for a file.
 */
public class FileChooser {

    private ElementHandle element;

    private volatile boolean handled;

    private boolean multiple;

    public FileChooser() {
    }

    public FileChooser(ElementHandle element, FileChooserOpenedEvent event) {
        this.element = element;
        this.multiple = !"selectSingle".equals(event.getMode());
        this.handled = false;
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * 选择文件路径。
     *
     * @param filePaths 选择的文件路径
     * @throws JsonProcessingException json异常
     * @throws EvaluateException       执行异常
     */
    public void accept(List<String> filePaths) throws JsonProcessingException, EvaluateException {
        ValidateUtil.assertArg(!this.handled, "Cannot accept FileChooser which is already handled!");
        this.handled = true;
        this.element.uploadFile(filePaths);
    }

    /**
     * 关闭文件选择器而不选择任何文件。
     */
    public void cancel() {
        ValidateUtil.assertArg(!this.handled, "Cannot cancel FileChooser which is already handled!");
        this.handled = true;
    }
}
