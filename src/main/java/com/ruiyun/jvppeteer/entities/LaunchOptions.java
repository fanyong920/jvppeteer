package com.ruiyun.jvppeteer.entities;


import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Environment;
import com.ruiyun.jvppeteer.common.Product;

import java.util.List;

public class LaunchOptions extends BrowserConnectOptions {

    public LaunchOptions() {
        super();
    }

    /**
     * 设置chrome浏览器的路径
     * <br/>
     * Path to a Chromium executable to run instead of bundled Chromium. If
     * executablePath is a relative path, then it is resolved relative to current
     * working directory.
     */
    private String executablePath;

    /**
     * 如果是true，代表忽略所有默认的启动参数，默认的启动参数见{@link Constant#DEFAULT_ARGS}，默认是false
     */
    private boolean ignoreAllDefaultArgs;

    /**
     * 忽略指定的默认启动参数
     */
    private List<String> ignoreDefaultArgs;


    /**
     * 将cheome的标准输出流输入流转换到java程序的标准输入输出,java默认已经将子进程的输入和错误流通过管道重定向了，现在这个参数暂时用不上
     * <br/>
     * Whether to pipe browser process stdout and stderr into process.stdout and
     * process.stderr.
     * 默认是 false
     */
    private boolean dumpio = false;

    /**
     * ָSystem.getEnv()
     * <br/>
     * Specify environment variables that will be visible to Chromium.
     * 默认是 `process.env`.
     */
    private Environment env;

    /**
     * ͨfalse代表使用websocket通讯，true代表使用websocket通讯
     * Connects to the browser over a pipe instead of a WebSocket.
     * 默认是  false
     */
    private boolean pipe;

    /**
     * chrome or chromium or chromedriver or chrome-headless-shell
     */
    private Product product;
    /**
     * Whether to wait for the initial page to be ready.
     * Useful when a user explicitly disables that (e.g. `--no-startup-window` for Chrome).
     */
    private boolean waitForInitialPage = true;
    /**
     * 启动的首选版本
     */
    private String preferredRevision;
    /**
     * 浏览器的存档目录，对应下载的时候的目录，如果不指定，那么将默认将项目根目录下的.local-browser文件夹作为存档目录
     * <p>
     * 如果不配置启动路径，那么会扫面存放目录下是否有chrome浏览器
     */
    private String cacheDir;

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public boolean getIgnoreAllDefaultArgs() {
        return ignoreAllDefaultArgs;
    }

    public void setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
        this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
    }

    public List<String> getIgnoreDefaultArgs() {
        return ignoreDefaultArgs;
    }

    public void setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
        this.ignoreDefaultArgs = ignoreDefaultArgs;
    }

    public boolean getDumpio() {
        return dumpio;
    }

    public void setDumpio(boolean dumpio) {
        this.dumpio = dumpio;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public boolean getPipe() {
        return pipe;
    }

    public void setPipe(boolean pipe) {
        this.pipe = pipe;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean getWaitForInitialPage() {
        return waitForInitialPage;
    }

    public void setWaitForInitialPage(boolean waitForInitialPage) {
        this.waitForInitialPage = waitForInitialPage;
    }

    public String getPreferredRevision() {
        return preferredRevision;
    }

    public void setPreferredRevision(String preferredRevision) {
        this.preferredRevision = preferredRevision;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }
}
