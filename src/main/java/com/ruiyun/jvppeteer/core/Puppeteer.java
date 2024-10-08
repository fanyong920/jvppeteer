package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.launch.ChromeLauncher;
import com.ruiyun.jvppeteer.launch.Launcher;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.io.IOException;
import java.util.List;

import static com.ruiyun.jvppeteer.core.Constant.PRODUCT_ENV;

/**
 * Puppeteer 也可以用来控制 Chrome 浏览器， 但它与绑定的 Chromium
 * 版本在一起使用效果最好。不能保证它可以与任何其他版本一起使用。谨慎地使用 executablePath 选项。 如果 Google
 * Chrome（而不是Chromium）是首选，一个 Chrome Canary 或 Dev Channel 版本是建议的
 *
 * @author fff
 */
public class Puppeteer {

    private String productName = null;

    private Launcher launcher;

    private Environment env = null;

    private String projectRoot = System.getProperty("user.dir");

    private String preferredRevision = Constant.VERSION;

    private boolean isPuppeteerCore;

    public Puppeteer() {

    }

    public Puppeteer(String projectRoot, String preferredRevision, boolean isPuppeteerCore, String productName) {
        this.projectRoot = projectRoot;
        this.preferredRevision = StringUtil.isEmpty(preferredRevision) ? Constant.VERSION : preferredRevision;
        this.isPuppeteerCore = isPuppeteerCore;
        this.productName = productName;
    }

    /**
     * 以默认参数启动浏览器
     * launch Browser by default options
     * @throws IOException 异常
     * @return 浏览器
     */
    public static Browser launch(){
        return Puppeteer.rawLaunch();
    }

    public static Browser launch(boolean headless) {
        return Puppeteer.rawLaunch(headless);
    }

    public static Browser launch(LaunchOptions options){
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(options, puppeteer);
    }

    private static Browser rawLaunch() {
        return Puppeteer.rawLaunch(true);
    }

    private static Browser rawLaunch(boolean headless) {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(new LaunchOptionsBuilder().withHeadless(headless).build(), puppeteer);
    }

    /**
     * 连接一个已经存在的浏览器实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     * <p>browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取</p>
     * <p>browserURL: 类似 localhost:8080 这个地址</p>
     * <p>transport: 之前已经创建好的 ConnectionTransport</p>
     * @param options 连接的浏览器选项
     * @return 浏览器实例
     */
    private static Browser connect(ConnectOptions options) {
        Puppeteer puppeteer = new Puppeteer();
		adapterLauncher(puppeteer);
		return puppeteer.getLauncher().connect(options);
    }


    /**
     * 连接一个已经存在的 Browser 实例
     * <p>browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取</p>
     * <p>browserURL: 类似 localhost:8080 这个地址</p>
     * @param browserWSEndpointOrURL 一个Browser实例对应一个browserWSEndpoint
     * @return 浏览器实例
     */
    public static Browser connect(String browserWSEndpointOrURL) {
        ConnectOptions options = new ConnectOptions();
        if(browserWSEndpointOrURL.contains(":")){
            options.setBrowserURL(browserWSEndpointOrURL);
            return Puppeteer.connect(options);
        }else {
            options.setBrowserWSEndpoint(browserWSEndpointOrURL);
            return Puppeteer.connect(options);
        }
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * <p>transport: 之前已经创建好的 ConnectionTransport</p>
     * @param transport  websocket http transport 三选一
     * @return 浏览器实例
     */
    public static Browser connect(ConnectionTransport transport) {
        ConnectOptions options = new ConnectOptions();
        options.setTransport(transport);
        return Puppeteer.connect(options);
    }

    /**
     * The method launches a browser instance with given arguments. The browser will
     * be closed when the parent java process is closed.
     */
    private static Browser rawLaunch(LaunchOptions options, Puppeteer puppeteer) {
        if (StringUtil.isNotBlank(options.getProduct())) {
            puppeteer.setProductName(options.getProduct());
        }
        adapterLauncher(puppeteer);
        return puppeteer.getLauncher().launch(options);
    }

    /**
     * 适配chrome or firefox 浏览器
     */
    private static void adapterLauncher(Puppeteer puppeteer) {
        String productName;
        Launcher launcher;
        Environment env;
        if (StringUtil.isEmpty(productName = puppeteer.getProductName()) && !puppeteer.getIsPuppeteerCore()) {

            if ((env = puppeteer.getEnv()) == null) {
                puppeteer.setEnv(env = System::getenv);
            }
            for (int i = 0; i < PRODUCT_ENV.length; i++) {
                String envProductName = PRODUCT_ENV[i];
                productName = env.getEnv(envProductName);
                if (StringUtil.isNotEmpty(productName)) {
                    puppeteer.setProductName(productName);
                    break;
                }
            }
        }
        if (StringUtil.isEmpty(productName)) {
            productName = "chrome";
            puppeteer.setProductName(productName);
        }
        switch (productName) {
            case "firefox":
            case "chrome":
            default:
                launcher = new ChromeLauncher(System.getProperty("user.dir"),puppeteer.getPreferredRevision());
        }
        puppeteer.setLauncher(launcher);
    }

    /**
     * 指定启动版本，开启浏览器
     * @param options 启动参数
     * @param version 浏览器版本
     * @return 浏览器实例
     */
    public static Browser launch(LaunchOptions options, String version) {
        Puppeteer puppeteer = new Puppeteer();
        if(StringUtil.isNotEmpty(version)){
            puppeteer.setPreferredRevision(version);
        }
        return Puppeteer.rawLaunch(options, puppeteer);
    }

    /**
     * 返回默认的运行的参数
     * @param options 可自己添加的选项
     * @return 默认参数集合
     */
    public List<String> defaultArgs(LaunchOptions options) {
        return this.getLauncher().defaultArgs(options);
    }

    public String executablePath() throws IOException {
        return this.getLauncher().executablePath();
    }

    public BrowserFetcher createBrowserFetcher() {
        return new BrowserFetcher(this.projectRoot, new FetcherOptions());
    }

    public BrowserFetcher createBrowserFetcher(FetcherOptions options) {
        return new BrowserFetcher(this.projectRoot, options);
    }

    private String getProductName() {
        return productName;
    }

    private void setProductName(String productName) {
        this.productName = productName;
    }

    private boolean getIsPuppeteerCore() {
        return isPuppeteerCore;
    }


    private Launcher getLauncher() {
        return launcher;
    }

    private void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    private Environment getEnv() {
        return env;
    }

    private void setEnv(Environment env) {
        this.env = env;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public String getPreferredRevision() {
        return preferredRevision;
    }

    public void setPreferredRevision(String preferredRevision) {
        this.preferredRevision = preferredRevision;
    }

}
