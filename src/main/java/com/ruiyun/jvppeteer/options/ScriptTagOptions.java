package com.ruiyun.jvppeteer.options;

/**
 * дк${@link com.ruiyun.jvppeteer.protocol.page.frame.Frame#addScriptTag(ScriptTagOptions)}
 */
public class ScriptTagOptions {

    private String url;

    private String path;

    private String content;

    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
