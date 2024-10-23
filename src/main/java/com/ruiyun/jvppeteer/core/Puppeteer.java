package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Environment;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.entities.ConnectOptions;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
import com.ruiyun.jvppeteer.entities.LaunchOptions;
import com.ruiyun.jvppeteer.entities.RevisionInfo;
import com.ruiyun.jvppeteer.launch.ChromeLauncher;
import com.ruiyun.jvppeteer.launch.Launcher;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;

import static com.ruiyun.jvppeteer.common.Constant.PRODUCT_ENV;

/**
 * Puppeteer 也可以用来控制 Chrome 浏览器， 但它与绑定的 Chrome for Testing
 * 版本在一起使用效果最好。不能保证它可以与任何其他版本一起使用。谨慎地使用 executablePath 选项。 如果 Google
 * Chrome（而不是Chromium）是首选，一个 Chrome Canary 或 Dev Channel 版本是建议的
 *
 * @author fff
 */
public class Puppeteer {

    private Product product = Product.CHROME;
    private Launcher launcher;
    private Environment env = null;
    private String cacheDir;

    public Puppeteer() {
        this.cacheDir = Helper.join(System.getProperty("user.dir"), ".local-browser");
    }

    public Puppeteer(String cacheDir, Product product) {
        this.cacheDir = StringUtil.isBlank(cacheDir) ? Helper.join(System.getProperty("user.dir"), ".local-browser") : cacheDir;
        this.product = product;
    }

    /**
     * 以默认参数启动浏览器
     * launch Browser by default options
     *
     * @return 浏览器
     * @throws IOException IO异常
     */
    public static Browser launch() throws IOException {
        return launch(true);
    }

    public static Browser launch(boolean headless) throws IOException {
        return launch(LaunchOptions.builder().headless(headless).build());
    }

    public static Browser launch(LaunchOptions options) throws IOException {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(options, puppeteer);
    }

    /**
     * The method launches a browser instance with given arguments. The browser will
     * be closed when the parent java process is closed.
     *
     * @param options   launch options
     *                  browserWSEndpoint、browserURL、transport三者至少选一个
     * @param puppeteer puppeteer实例
     * @return 浏览器实例
     * @throws IOException IO异常
     */
    public static Browser rawLaunch(LaunchOptions options, Puppeteer puppeteer) throws IOException {
        if (options.getProduct() != null) {
            puppeteer.setProduct(options.getProduct());
        }
        if (StringUtil.isNotBlank(options.getCacheDir())) {
            puppeteer.setCacheDir(options.getCacheDir());
        }
        adoptLauncher(puppeteer);
        return puppeteer.getLauncher().launch(options);
    }

    /**
     * 连接一个已经存在的浏览器实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     * <p>browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取</p>
     * <p>browserURL: 类似 localhost:8080 这个地址</p>
     * <p>transport: 之前已经创建好的 ConnectionTransport</p>
     *
     * @param options 连接的浏览器选项
     * @return 浏览器实例
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    private static Browser connect(ConnectOptions options) throws IOException, InterruptedException {
        Puppeteer puppeteer = new Puppeteer();
        adoptLauncher(puppeteer);
        return puppeteer.getLauncher().connect(options);
    }


    /**
     * 连接一个已经存在的 Browser 实例
     * <p>browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取</p>
     * <p>browserURL: 类似 localhost:8080 这个地址</p>
     *
     * @param browserWSEndpointOrURL 一个Browser实例对应一个browserWSEndpoint
     * @return 浏览器实例
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    public static Browser connect(String browserWSEndpointOrURL) throws IOException, InterruptedException {
        ConnectOptions options = new ConnectOptions();
        if (browserWSEndpointOrURL.startsWith("ws:")) {
            options.setBrowserWSEndpoint(browserWSEndpointOrURL);
        } else {
            options.setBrowserURL(browserWSEndpointOrURL);
        }
        return Puppeteer.connect(options);
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * <p>transport: 之前已经创建好的 ConnectionTransport</p>
     *
     * @param transport websocket http transport 三选一
     * @return 浏览器实例
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    public static Browser connect(ConnectionTransport transport) throws IOException, InterruptedException {
        ConnectOptions options = new ConnectOptions();
        options.setTransport(transport);
        return Puppeteer.connect(options);
    }


    /**
     * 适配chrome or firefox 浏览器
     */
    private static void adoptLauncher(Puppeteer puppeteer) {
        Product product;
        Launcher launcher;
        Environment env;
        if ((product = puppeteer.getProduct()) == null) {
            if ((env = puppeteer.getEnv()) == null) {
                puppeteer.setEnv(env = System::getenv);
            }
            for (String envProductName : PRODUCT_ENV) {
                String productName = env.getEnv(envProductName);
                if (StringUtil.isNotEmpty(productName)) {
                    product = Product.valueOf(productName);
                    puppeteer.setProduct(product);
                    break;
                }
            }
        }
        if (product == null) {
            product = Product.CHROME;
            puppeteer.setProduct(product);
        }
        switch (product) {
            case FIREFOX:
                throw new IllegalArgumentException("Firefox browser is not supported yet!");
            case CHROME:
            case CHROMIUM:
            case CHROMEDRIVER:
            case CHROMEHEADLESSSHELL:
            default:
                launcher = new ChromeLauncher(puppeteer.getCacheDir(), product);
        }
        puppeteer.setLauncher(launcher);
    }

    /**
     * 采用默认配置(Product#CHROME,Constant#VERSION)下载浏览器
     * <p>
     * 本方法负责下载用于自动化测试的浏览器该方法使用Puppeteer类来初始化浏览器Fetcher对象，
     * 并通过该对象下载浏览器到指定的项目根目录下
     *
     * @return 返回一个RevisionInfo对象，包含下载浏览器的版本信息
     * @throws IOException          如果在下载过程中发生I/O错误
     * @throws InterruptedException 如果在下载过程中线程被中断
     */
    public static RevisionInfo downloadBrowser() throws IOException, InterruptedException {
        return Puppeteer.downloadBrowser(Constant.VERSION);
    }

    /**
     * 通过自定义配置选项下载浏览器
     * <p>
     * 根据提供的选项下载浏览器此方法首先检查选项中提供的安装路径是否为空如果为空，
     * 则将其默认设置为当前用户目录然后，创建一个BrowserFetcher对象并使用指定的路径和选项
     * 开始下载过程最后，返回下载的浏览器信息
     *
     * @param options FetcherOptions对象，包含浏览器下载的配置选项
     * @return RevisionInfo对象，包含下载的浏览器的信息
     * @throws IOException          如果在下载过程中发生I/O错误
     * @throws InterruptedException 如果在下载过程中线程被中断
     */
    public static RevisionInfo downloadBrowser(FetcherOptions options) throws IOException, InterruptedException {
        if (StringUtil.isBlank(options.getCacheDir())) {
            options.setCacheDir(Helper.join(System.getProperty("user.dir"), ".local-browser"));
        }
        BrowserFetcher fetcher = new BrowserFetcher(options);
        return fetcher.downloadBrowser();
    }

    /**
     * 下载Product#CHROME的某个版本的浏览器
     * <p>
     * 本方法用于下载指定版本的浏览器它首先验证版本号是否有效，然后使用 FetcherOptions 配置下载过程，
     * 最后通过 BrowserFetcher 执行实际的下载操作
     *
     * @param version 要下载的浏览器版本号版本号不能为空
     * @return 返回一个 RevisionInfo 对象，包含下载浏览器的版本信息
     * @throws IOException          如果在下载过程中发生 I/O 错误
     * @throws InterruptedException 如果在下载过程中线程被中断
     */
    public static RevisionInfo downloadBrowser(String version) throws IOException, InterruptedException {
        ValidateUtil.assertArg(StringUtil.isNotBlank(version), "Browser version must be specified");
        FetcherOptions options = new FetcherOptions();
        options.setVersion(version);
        return Puppeteer.downloadBrowser(options);
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

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
