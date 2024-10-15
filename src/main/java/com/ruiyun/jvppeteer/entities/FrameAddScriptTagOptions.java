package com.ruiyun.jvppeteer.entities;

/**
 * 用于添加脚本标签的选项。
 */
public class FrameAddScriptTagOptions {
    /**
     * 要添加的脚本的URL。
     */
    private String url;
    /**
     * 要注入到框架中的JavaScript文件的路径。
     * <p>
     */
    private String path;
    /**
     * 要注入到框架中的JavaScript代码。
     */
    private String content;
    /**
     * 设置脚本的`type`。使用`module`以加载ES2015模块。
     */
    private String type;
    /**
     * Sets the `id` of the script.
     */
    private String id;

    public FrameAddScriptTagOptions() {
        super();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
