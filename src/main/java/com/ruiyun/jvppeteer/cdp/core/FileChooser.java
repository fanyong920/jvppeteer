package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.List;

/**
 * FileChooser objects are returned via the 'page.waitForFileChooser' method.
 * <p>
 * File choosers let you react to the page requesting for a file.
 */
public class FileChooser {

    private final ElementHandle element;

    private volatile boolean handled;

    private final boolean multiple;

    public FileChooser(ElementHandle element, boolean multiple) {
        this.element = element;
        this.multiple = multiple;
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
    public void cancel() throws JsonProcessingException {
        ValidateUtil.assertArg(!this.handled, "Cannot cancel FileChooser which is already handled!");
        this.handled = true;
        // XXX: These events should converted to trusted events. Perhaps do this
        // in `DOM.setFileInputFiles`?
        this.element.evaluate("element => {\n" +
                "      element.dispatchEvent(new Event('cancel', {bubbles: true}));\n" +
                "    }");
    }
}
