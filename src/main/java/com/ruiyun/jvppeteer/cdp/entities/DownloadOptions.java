package com.ruiyun.jvppeteer.cdp.entities;

public class DownloadOptions {
    /**
     * 下载行为，是接受还是拒绝，或者是默认的 chrome 下载行为
     * <p>
     * 默认值是DownloadBehavior.Default
     */
    private DownloadPolicy behavior;

    /**
     * 设置 哪个BrowserContext 下载行为。省略时，将使用默认浏览器上下文
     */
    private String browserContextId;
    /**
     * 存放下载文件的村法国路径
     */
    private String downloadPath;
    /**
     * 是否接收下载事件，默认不接受
     */
    private boolean eventsEnabled;

    public DownloadOptions(DownloadPolicy behavior, String browserContextId, String downloadPath, boolean eventsEnabled) {
        this.behavior = behavior;
        this.browserContextId = browserContextId;
        this.downloadPath = downloadPath;
        this.eventsEnabled = eventsEnabled;
    }

    public DownloadPolicy getBehavior() {
        return behavior;
    }

    public void setBehavior(DownloadPolicy behavior) {
        this.behavior = behavior;
    }

    public String getBrowserContextId() {
        return browserContextId;
    }

    public void setBrowserContextId(String browserContextId) {
        this.browserContextId = browserContextId;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public boolean getEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }
}
