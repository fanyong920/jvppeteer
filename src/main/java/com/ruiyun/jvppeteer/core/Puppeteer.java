package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.launch.ChromeLauncher;
import com.ruiyun.jvppeteer.launch.FirefoxLauncher;
import com.ruiyun.jvppeteer.launch.Launcher;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
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

    public String productName = null;

    private Launcher launcher;

    private Environment env = null;

    private String projectRoot = System.getProperty("user.dir");

    private String preferredRevision;

    private boolean isPuppeteerCore;

    public Puppeteer() {

    }

    public Puppeteer(String projectRoot, String preferredRevision, boolean isPuppeteerCore, String productName) {
        this.projectRoot = projectRoot;
        this.preferredRevision = preferredRevision;
        this.isPuppeteerCore = isPuppeteerCore;
        this.productName = productName;
    }

    /**
     * 以默认参数启动浏览器
     * <br/>
     * launch Browser by default options
     *
     * @return 浏览器
     */
    public static Browser launch() throws IOException {
        return Puppeteer.rawLaunch();
    }

    public static Browser launch(boolean headless) throws IOException {
        return Puppeteer.rawLaunch(headless);
    }

    public static Browser launch(LaunchOptions options) throws IOException {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(options, puppeteer);
    }

    private static Browser rawLaunch() throws IOException {
        return Puppeteer.rawLaunch(true);
    }

    private static Browser rawLaunch(boolean headless) throws IOException {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(new OptionsBuilder().withHeadless(headless).build(), puppeteer);
    }

    /**
     * 连接一个已经存在的浏览器实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     *
     * @param options 连接的浏览器选项
     * @param browserWSEndpoint websocket http transport 三选一
     * @param browserURL        websocket http transport 三选一
     * @param transport  websocket http transport 三选一
     * @param product 谷歌还是火狐
     * @return 浏览器实例
     */
    public static Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport, String product) {
        Puppeteer puppeteer = new Puppeteer();

        if (StringUtil.isNotEmpty(product))
            puppeteer.setProductName(product);
		adapterLauncher(puppeteer);
		return puppeteer.getLauncher().connect(options, browserWSEndpoint, browserURL, transport);
    }

    /**
     * The method launches a browser instance with given arguments. The browser will
     * be closed when the parent java process is closed.
     */
    private static Browser rawLaunch(LaunchOptions options, Puppeteer puppeteer) throws IOException {
        if (!StringUtil.isNotBlank(options.getProduct())) {
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
                launcher = new FirefoxLauncher(puppeteer.getIsPuppeteerCore());
            case "chrome":
            default:
                launcher = new ChromeLauncher(puppeteer.getProjectRoot(),puppeteer.getPreferredRevision(),puppeteer.getIsPuppeteerCore());
        }
        puppeteer.setLauncher(launcher);
    }

    public List<String> defaultArgs(ChromeArgOptions options) {
        List<String> chromeArguments = new ArrayList<>();
        this.getLauncher().defaultArgs(options, chromeArguments);
        return chromeArguments;
    }

    public String executablePath() throws IOException {
        return this.getLauncher().executablePath();
    }

    public BrowserFetcher createBrowserFetcher() {
        return new BrowserFetcher(this.getProjectRoot(), new FetcherOptions());
    }

    public BrowserFetcher createBrowserFetcher(FetcherOptions options) {
        return new BrowserFetcher(this.getProjectRoot(), options);
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
