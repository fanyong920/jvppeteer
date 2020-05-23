package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.core.page.Frame;

/**
 * 用在${@link Frame#addScriptTag(ScriptTagOptions)}
 */
public class ScriptTagOptions {

    private String url;

    private String path;

    private String content;

    private String type = "";

    public ScriptTagOptions() {
        super();
    }

    public ScriptTagOptions(String url) {
        this.url = url;
    }

    public ScriptTagOptions(String url, String path, String content, String type) {
        this.url = url;
        this.path = path;
        this.content = content;
        this.type = type;
    }

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
