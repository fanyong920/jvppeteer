package com.ruiyun.jvppeteer.cdp.entities;


import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LaunchOptions extends ConnectOptions {

    // 私有构造方法，只能通过构建器创建对象
    private LaunchOptions(Builder builder) {
        super();
        this.executablePath = builder.executablePath;
        this.ignoreAllDefaultArgs = builder.ignoreAllDefaultArgs;
        this.ignoreDefaultArgs = builder.ignoreDefaultArgs;
        this.dumpio = builder.dumpio;
        this.env = builder.env;
        this.pipe = builder.pipe;
        this.product = builder.product;
        this.waitForInitialPage = builder.waitForInitialPage;
        this.preferredRevision = builder.preferredRevision;
        this.cacheDir = builder.cacheDir;
        this.extraPrefsFirefox = builder.extraPrefsFirefox;
        this.setAcceptInsecureCerts(builder.acceptInsecureCerts);
        this.setDefaultViewport(builder.defaultViewport);
        this.setSlowMo(builder.slowMo);
        this.setTargetFilter(builder.targetFilter);
        this.setIsPageTarget(builder.isPageTarget);
        this.setProtocolTimeout(builder.protocolTimeout);
        this.setHeadless(builder.headless);
        this.setArgs(builder.args);
        this.setUserDataDir(builder.userDataDir);
        this.setDevtools(builder.devtools);
        this.setDebuggingPort(builder.debuggingPort);
        this.setTimeout(builder.timeout);
        this.setProtocol(builder.protocol);
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
     * 最大导航时间是30000ms,0表示无限等待
     * <br/>
     * Maximum navigation time in milliseconds, pass 0 to disable timeout.
     * 默认是 30000
     */
    private int timeout = Constant.DEFAULT_TIMEOUT;

    /**
     * 将cheome的标准输出流输入流转换到java程序的标准输入输出
     * <br/>
     * Whether to pipe browser process stdout and stderr into process.stdout and
     * process.stderr.
     * 默认是 false
     */
    private boolean dumpio;

    /**
     * Specify environment variables that will be visible to Chromium.
     */
    private Map<String,String> env;

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
     * {@link <a href="https://searchfox.org/mozilla-release/source/modules/libpref/init/all.js">| Additional preferences</a>} that can be passed when launching with Firefox.
     */
    private Map<String, Object> extraPrefsFirefox;
    /**
     * Whether to wait for the initial page to be ready.
     * Useful when a user explicitly disables that (e.g. `--no-startup-window` for Chrome).
     */
    private boolean waitForInitialPage;

    /**
     * 是否是无厘头
     * <br/>
     * 默认是 true
     */
    private boolean headless = true;

    /**
     * 用户数据存储的目录
     * <br/>
     * Path to a User Data Directory.
     */
    private String userDataDir;
    /**
     * 是否打开devtool,也就是F12打开的开发者工具
     * <br/>
     */
    private boolean devtools;
    /**
     * Specify the debugging port number to use
     */
    private int debuggingPort;
    /**
     * 其他参数，点击
     * <a href="https://peter.sh/experiments/chromium-command-line-switches/">这里 </a>可以看到参数
     * <br/>
     */
    private List<String> args = new ArrayList<>();
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


    public boolean getHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getUserDataDir() {
        return userDataDir;
    }

    public void setUserDataDir(String userDataDir) {
        this.userDataDir = userDataDir;
    }

    public boolean getDevtools() {
        return devtools;
    }

    public void setDevtools(boolean devtools) {
        this.devtools = devtools;
    }

    public int getDebuggingPort() {
        return debuggingPort;
    }

    public void setDebuggingPort(int debuggingPort) {
        this.debuggingPort = debuggingPort;
    }

    public static Builder builder() {
        return new Builder();
    }


    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    // 内部静态构建器类
    public static class Builder {
        private boolean acceptInsecureCerts;
        private Viewport defaultViewport = new Viewport();
        private int slowMo;
        private Function<Target, Boolean> targetFilter;
        private Function<Target, Boolean> isPageTarget;
        private int protocolTimeout = Constant.DEFAULT_TIMEOUT;
        private boolean headless = true;
        private List<String> args = new ArrayList<>();
        private String userDataDir;
        private boolean devtools;
        private int debuggingPort;
        private int timeout = Constant.DEFAULT_TIMEOUT;
        private String executablePath;
        private boolean ignoreAllDefaultArgs;
        private List<String> ignoreDefaultArgs;
        private boolean dumpio = false;
        private Map<String,String> env;
        private boolean pipe;
        private Product product;
        private boolean waitForInitialPage = true;
        private String preferredRevision;
        private String cacheDir;
        private Map<String, Object> extraPrefsFirefox;
        private Protocol protocol;

        private Builder() {
        }

        public Builder executablePath(String executablePath) {
            this.executablePath = executablePath;
            return this;
        }

        public Builder ignoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
            this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
            return this;
        }

        public Builder ignoreDefaultArgs(List<String> ignoreDefaultArgs) {
            this.ignoreDefaultArgs = ignoreDefaultArgs;
            return this;
        }

        public Builder dumpio(boolean dumpio) {
            this.dumpio = dumpio;
            return this;
        }

        public Builder env(Map<String,String> env) {
            this.env = env;
            return this;
        }

        public Builder pipe(boolean pipe) {
            this.pipe = pipe;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder waitForInitialPage(boolean waitForInitialPage) {
            this.waitForInitialPage = waitForInitialPage;
            return this;
        }

        public Builder preferredRevision(String preferredRevision) {
            this.preferredRevision = preferredRevision;
            return this;
        }

        public Builder cacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder acceptInsecureCerts(boolean acceptInsecureCerts) {
            this.acceptInsecureCerts = acceptInsecureCerts;
            return this;
        }

        public Builder args(List<String> args) {
            this.args = args;
            return this;
        }

        public Builder defaultViewport(Viewport defaultViewport) {
            this.defaultViewport = defaultViewport;
            return this;
        }

        public Builder debuggingPort(int debuggingPort) {
            this.debuggingPort = debuggingPort;
            return this;
        }

        public Builder devtools(boolean devtools) {
            this.devtools = devtools;
            return this;
        }

        public Builder headless(boolean headless) {
            this.headless = headless;
            return this;
        }

        public Builder isPageTarget(Function<Target, Boolean> isPageTarget) {
            this.isPageTarget = isPageTarget;
            return this;
        }

        public Builder protocolTimeout(int protocolTimeout) {
            this.protocolTimeout = protocolTimeout;
            return this;
        }

        public Builder slowMo(int slowMo) {
            this.slowMo = slowMo;
            return this;
        }

        public Builder targetFilter(Function<Target, Boolean> targetFilter) {
            this.targetFilter = targetFilter;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder userDataDir(String userDataDir) {
            this.userDataDir = userDataDir;
            return this;
        }

        public Builder extraPrefsFirefox(Map<String, Object> extraPrefsFirefox) {
            this.extraPrefsFirefox = extraPrefsFirefox;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public LaunchOptions build() {
            return new LaunchOptions(this);
        }
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public boolean getIgnoreAllDefaultArgs() {
        return ignoreAllDefaultArgs;
    }

    public List<String> getIgnoreDefaultArgs() {
        return ignoreDefaultArgs;
    }

    public boolean getDumpio() {
        return dumpio;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public boolean getPipe() {
        return pipe;
    }

    public Product getProduct() {
        return product;
    }

    public boolean getWaitForInitialPage() {
        return waitForInitialPage;
    }

    public String getPreferredRevision() {
        return preferredRevision;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public void setPreferredRevision(String preferredRevision) {
        this.preferredRevision = preferredRevision;
    }

    public void setWaitForInitialPage(boolean waitForInitialPage) {
        this.waitForInitialPage = waitForInitialPage;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setPipe(boolean pipe) {
        this.pipe = pipe;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public void setDumpio(boolean dumpio) {
        this.dumpio = dumpio;
    }

    public void setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
        this.ignoreDefaultArgs = ignoreDefaultArgs;
    }

    public void setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
        this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
    }

    public Map<String, Object> getExtraPrefsFirefox() {
        return extraPrefsFirefox;
    }

    public void setExtraPrefsFirefox(Map<String, Object> extraPrefsFirefox) {
        this.extraPrefsFirefox = extraPrefsFirefox;
    }

}
