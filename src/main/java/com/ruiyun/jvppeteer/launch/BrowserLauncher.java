package com.ruiyun.jvppeteer.launch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Environment;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserFetcher;
import com.ruiyun.jvppeteer.core.BrowserRunner;
import com.ruiyun.jvppeteer.entities.ConnectOptions;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
import com.ruiyun.jvppeteer.entities.GetVersionResponse;
import com.ruiyun.jvppeteer.entities.LaunchOptions;
import com.ruiyun.jvppeteer.entities.RevisionInfo;
import com.ruiyun.jvppeteer.entities.TargetType;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.Helper.getVersion;

public abstract class BrowserLauncher {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BrowserLauncher.class);
    protected Product product;
    protected String cacheDir;
    protected String executablePath;

    public BrowserLauncher(String cacheDir, Product product) {
        super();
        this.cacheDir = cacheDir;
        this.product = product;
    }

    Environment env = System::getenv;

    public abstract Browser launch(LaunchOptions options) throws IOException;

    public abstract List<String> defaultArgs(LaunchOptions options);

    /**
     * 解析可执行的chrome路径
     *
     * @param preferredExecutablePath 指定的可执行路径
     * @return 返回解析后的可执行路径
     */
    public String computeExecutablePath(String preferredExecutablePath, String preferredRevision) throws IOException {
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(this.product);
        fetcherOptions.setCacheDir(this.cacheDir);
        BrowserFetcher browserFetcher = new BrowserFetcher(fetcherOptions);
        /*指定了启动路径，则启动指定路径的chrome*/
        if (StringUtil.isNotEmpty(preferredExecutablePath)) {
            boolean assertDir = FileUtil.assertExecutable(Paths.get(preferredExecutablePath).normalize().toAbsolutePath().toString());
            if (!assertDir) {
                throw new IllegalArgumentException("preferredExecutablePath \"" + preferredExecutablePath + "\" is not executable");
            }
            return preferredExecutablePath;
        }
        /*环境变量中配置了chromeExecutable，就使用环境变量中的路径*/
        for (int i = 0; i < Constant.EXECUTABLE_ENV.length; i++) {
            preferredExecutablePath = env.getEnv(Constant.EXECUTABLE_ENV[i]);
            if (StringUtil.isNotEmpty(preferredExecutablePath)) {
                boolean assertDir = FileUtil.assertExecutable(preferredExecutablePath);
                if (!assertDir) {
                    throw new IllegalArgumentException("executablePath that in the environment is not executable");
                }
                return preferredExecutablePath;
            }
        }
        /*指定了首选版本*/
        if (StringUtil.isNotEmpty(preferredRevision)) {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(preferredRevision);
            if (!revisionInfo.getLocal())
                throw new LaunchException(MessageFormat.format("Could not find browser preferredRevision {0}. Please download a browser binary.", preferredRevision));
            return revisionInfo.getExecutablePath();
        }

        /*环境变量中配置了版本，就用环境变量中的版本*/
        String revision = env.getEnv(Constant.JVPPETEER_PRODUCT_REVISION_ENV);
        if (StringUtil.isNotEmpty(revision)) {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(revision);
            if (!revisionInfo.getLocal()) {
                throw new LaunchException(
                        "Tried to use JVPPETEER_PRODUCT_REVISION_ENV env variable to launch browser but did not find executable at: "
                                + revisionInfo.getExecutablePath());
            }
            return revisionInfo.getExecutablePath();
        }
        /*如果下载了chrome，就使用下载的chrome*/
        List<String> localRevisions = browserFetcher.localRevisions();
        if (ValidateUtil.isNotEmpty(localRevisions)) {
            localRevisions.sort(Comparator.reverseOrder());
            for (String localRevision : localRevisions) {
                RevisionInfo revisionInfo = browserFetcher.revisionInfo(localRevision);
                if (revisionInfo.getLocal() && StringUtil.isNotEmpty(revisionInfo.getExecutablePath()) && FileUtil.assertExecutable(revisionInfo.getExecutablePath())) {
                    return revisionInfo.getExecutablePath();
                }
            }
        }
        /*寻找可能存在的启动路径*/
        for (int i = 0; i < Constant.PROBABLE_CHROME_EXECUTABLE_PATH.length; i++) {
            preferredExecutablePath = Constant.PROBABLE_CHROME_EXECUTABLE_PATH[i];
            if (StringUtil.isNotEmpty(preferredExecutablePath)) {
                boolean assertDir = FileUtil.assertExecutable(preferredExecutablePath);
                if (assertDir) {
                    return preferredExecutablePath;
                }
            }
        }
        throw new LaunchException("Could not find anyone browser executablePath");
    }

    protected Browser run(LaunchOptions options, List<String> chromeArguments, String temporaryUserDataDir, boolean usePipe, List<String> defaultArgs) {
        BrowserRunner runner = new BrowserRunner(this.executablePath, chromeArguments, temporaryUserDataDir);
        try {
            runner.start();
            Connection connection = runner.setUpConnection(usePipe, options.getProtocolTimeout(), options.getSlowMo(), options.getDumpio());
            Runnable closeCallback = runner::closeBrowser;
            Browser browser = Browser.create(options.getProduct(), connection, new ArrayList<>(), options.getAcceptInsecureCerts(), options.getDefaultViewport(), runner.getProcess(), closeCallback, options.getTargetFilter(), null, true);
            browser.setExecutablePath(this.executablePath);
            browser.setDefaultArgs(defaultArgs);
            if (options.getWaitForInitialPage()) {
                browser.waitForTarget(t -> TargetType.PAGE.equals(t.type()), options.getTimeout());
            }
            connection.setBrowser(browser);
            runner.setPid(getBrowserPid(runner.getProcess()));
            return browser;
        } catch (IOException | InterruptedException e) {
            runner.closeBrowser();
            LOGGER.error("Failed to launch the browser process:{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过cdp的SystemInfo.getProcessInfo获取浏览器pid，如果通过cdp没获取pid，并且是mac或者linux平台，那么尝试通过反射获取pid
     */
    private String getBrowserPid(Process process) {
        long pid = -1;
        try {
            pid = Helper.getPidForLinuxOrMac(process);
        } catch (Exception e) {
            LOGGER.error("get browser pid error: ", e);
        }
        return String.valueOf(pid);
    }

    public String executablePath() {
        return this.executablePath;
    }

    public Browser connect(ConnectOptions options) throws IOException, InterruptedException {
        final Connection connection;
        if (options.getTransport() != null) {
            connection = new Connection("", options.getTransport(), options.getSlowMo(), options.getProtocolTimeout());
        } else if (StringUtil.isNotEmpty(options.getBrowserWSEndpoint())) {
            WebSocketTransport connectionTransport = WebSocketTransport.create(options.getBrowserWSEndpoint());
            connection = new Connection(options.getBrowserWSEndpoint(), connectionTransport, options.getSlowMo(), options.getTimeout());
        } else if (StringUtil.isNotEmpty(options.getBrowserURL())) {
            String connectionURL = getWSEndpoint(options.getBrowserURL());
            WebSocketTransport connectionTransport = WebSocketTransport.create(connectionURL);
            connection = new Connection(connectionURL, connectionTransport, options.getSlowMo(), options.getTimeout());
        } else {
            throw new IllegalArgumentException("Exactly one of browserWSEndpoint, browserURL or transport must be passed to puppeteer.connect");
        }
        JsonNode result = connection.send("Target.getBrowserContexts");
        JavaType javaType = Constant.OBJECTMAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
        List<String> browserContextIds;
        Runnable closeFunction = () -> connection.send("Browser.close");
        browserContextIds = Constant.OBJECTMAPPER.readerFor(javaType).readValue(result.get("browserContextIds"));
        GetVersionResponse version = getVersion(connection);
        Product product = version.getProduct().toLowerCase().contains("firefox") ? Product.FIREFOX : Product.CHROME;
        Browser browser = Browser.create(product, connection, browserContextIds, options.getAcceptInsecureCerts(), options.getDefaultViewport(), null, closeFunction, options.getTargetFilter(), options.getIsPageTarget(), true);
        connection.setBrowser(browser);
        return browser;
    }

    /**
     * 启动浏览器的时候配置{@link LaunchOptions#setDebuggingPort(int)}，根据主机地址和DebuggingPort可以得到browserURL
     * <p>
     * 通过格式为 http://${host}:${port} 的地址发送 GET 请求获取浏览器的 WebSocket 连接端点
     *
     * @param browserURL 浏览器地址
     * @return WebSocket 连接端点
     * @throws IOException 请求出错
     */
    private String getWSEndpoint(String browserURL) throws IOException {
        URI uri = URI.create(browserURL).resolve("/json/version");
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        JsonNode jsonNode;
        try {
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new JvppeteerException("browserURL: " + browserURL + ",HTTP " + responseCode);
            }
            String result = StreamUtil.toString(conn.getInputStream());
            jsonNode = Constant.OBJECTMAPPER.readTree(result);
        } finally {
            conn.disconnect();
        }
        return jsonNode.get("webSocketDebuggerUrl").asText();
    }

}
