package com.ruiyun.jvppeteer.cdp.events;

public class WebMCPAnnotation {
    /**
     * A hint indicating that the tool does not modify any state.
     */
    private Boolean readOnly;

    /**
     * If the declarative tool was declared with the autosubmit attribute.
     */
    private Boolean autosubmit;

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getAutosubmit() {
        return autosubmit;
    }

    public void setAutosubmit(Boolean autosubmit) {
        this.autosubmit = autosubmit;
    }
}
