package com.ruiyun.jvppeteer.types.page.payload;

/**
 * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) is about to
 open.
 */
public class JavascriptDialogOpeningPayload {

    /**
     * Frame url.
     */
    private String url;
    /**
     * Message that will be displayed by the dialog.
     */
    private String message;
    /**
     * Dialog type.
     * "alert"|"confirm"|"prompt"|"beforeunload"
     */
    private String type;
    /**
     * True iff browser is capable showing or acting on the given dialog. When browser has no
     dialog handler for given target, calling alert while Page domain is engaged will stall
     the page execution. Execution can be resumed via calling Page.handleJavaScriptDialog.
     */
    private boolean hasBrowserHandler;
    /**
     * Default dialog prompt.
     */

    private String defaultPrompt;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHasBrowserHandler() {
        return hasBrowserHandler;
    }

    public void setHasBrowserHandler(boolean hasBrowserHandler) {
        this.hasBrowserHandler = hasBrowserHandler;
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    public void setDefaultPrompt(String defaultPrompt) {
        this.defaultPrompt = defaultPrompt;
    }
}
