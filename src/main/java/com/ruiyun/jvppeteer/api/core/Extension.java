package com.ruiyun.jvppeteer.api.core;

import java.util.List;

/**
 * {@link Extension} represents a browser extension installed in the browser.
 * It provides access to the extension's ID, name, and version, as well as
 * methods for interacting with the extension's background workers and pages.
 *
 * <p>示例：获取浏览器中安装的所有扩展：
 * <pre>{@code
 * Map<String, Extension> extensions = await browser.extensions();
 * for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
 *   System.out.println(entry.getValue().getName() + " " + entry.getKey());
 * }
 * }</pre>
 *
 */
public abstract class Extension {
    private final String id;
    private final String version;
    private final String name;
    private final String path;
    private final boolean enabled;

    /**
     * 内部构造方法。
     */
    public Extension(String id, String version, String name, String path, boolean enabled) {
        if (id == null || id.isEmpty() || version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Extension ID and version are required");
        }

        this.id = id;
        this.version = version;
        this.name = name;
        this.path = path;
        this.enabled = enabled;
    }

    /**
     * Whether the extension is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * The path in the file system where the extension is located.
     */
    public String getPath() {
        return path;
    }

    /**
     * The version of the extension as specified in its manifest.
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * The name of the extension as specified in its manifest.
     */
    public String getName() {
        return name;
    }

    /**
     * The unique identifier of the extension.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a list of the currently active service workers belonging
     * to the extension.
     */
    public abstract List<WebWorker> workers();

    /**
     * Returns a list of the currently active and visible pages belonging
     * to the extension.
     */
    public abstract List<Page> pages();

    /**
     * Triggers the default action of the extension for a specified page.
     * This typically simulates a user clicking the extension's action icon
     * in the browser toolbar, potentially opening a popup or executing an action script.
     *
     * @param page The page to trigger the action on.
     */
    public abstract void triggerAction(Page page);
}
