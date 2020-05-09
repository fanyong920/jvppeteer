package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.types.page.Frame;

/**
 * 在${@link Frame#addScriptTag(ScriptTagOptions)}用到
 */
public class StyleTagOptions {

    private String url;

    private String path;

    private String content;

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
}
