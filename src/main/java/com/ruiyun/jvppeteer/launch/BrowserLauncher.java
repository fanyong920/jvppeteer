package com.ruiyun.jvppeteer.launch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.bidi.core.BidiBrowser;
import com.ruiyun.jvppeteer.bidi.core.BidiConnection;
import com.ruiyun.jvppeteer.cdp.core.BrowserFetcher;
import com.ruiyun.jvppeteer.cdp.core.BrowserRunner;
import com.ruiyun.jvppeteer.cdp.core.CdpBrowser;
import com.ruiyun.jvppeteer.cdp.entities.ConnectOptions;
import com.ruiyun.jvppeteer.cdp.entities.FetcherOptions;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.Protocol;
import com.ruiyun.jvppeteer.cdp.entities.RevisionInfo;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import com.ruiyun.jvppeteer.transport.CdpConnection;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.transport.PipeTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.transport.WebSocketTransportFactory;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BrowserLauncher {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BrowserLauncher.class);
    protected Product product;
    protected String cacheDir;
    protected String executablePath;
    private static final Pattern WS_ENDPOINT_PATTERN = Pattern.compile("^DevTools listening on (ws://.*)$");
    private static final Pattern BiDi_ENDPOINT_PATTERN = Pattern.compile("^WebDriver BiDi listening on (ws://.*)$");

    public BrowserLauncher(String cacheDir, Product product) {
        super();
        this.cacheDir = cacheDir;
        this.product = product;
    }


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
            preferredExecutablePath = System.getProperty(Constant.EXECUTABLE_ENV[i]);
            if (StringUtil.isNotEmpty(preferredExecutablePath)) {
                boolean assertDir = FileUtil.assertExecutable(preferredExecutablePath);
                if (!assertDir) {
                    throw new IllegalArgumentException("executablePath that in the environment is not executable");
                }
                return preferredExecutablePath;
            }
        }
        BrowserFetcher browserFetcher = new BrowserFetcher(fetcherOptions);
        /*指定了首选版本*/
        if (StringUtil.isNotEmpty(preferredRevision)) {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(preferredRevision.replace("stable_", ""));
            if (!revisionInfo.getLocal())
                throw new LaunchException(MessageFormat.format("Could not find browser preferredRevision {0}. Please download a browser binary.", preferredRevision));
            return revisionInfo.getExecutablePath();
        }

        /*环境变量中配置了版本，就用环境变量中的版本*/
        String revision = System.getProperty(Constant.JVPPETEER_PRODUCT_REVISION_ENV);
        if (StringUtil.isNotEmpty(revision)) {
            revision = revision.replace("stable_", "");
        }
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

    protected Browser createBrowser(LaunchOptions options, List<String> chromeArguments, String temporaryUserDataDir, boolean usePipe, List<String> defaultArgs, String customizedUserDataDir) {
        BrowserRunner runner = new BrowserRunner(this.executablePath, chromeArguments, temporaryUserDataDir, options.getProduct(), options.getProtocol(), customizedUserDataDir,options.getEnv(),usePipe);
        try {
            Connection connection;
            if (usePipe) {
                runner.start();
                PipeTransport pipeTransport = new PipeTransport(runner.getProcess().getInputStream(), runner.getProcess().getOutputStream());
                connection = new CdpConnection("", pipeTransport, options.getSlowMo(), options.getProtocolTimeout());
                runner.setConnection(connection);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Connect to browser by pipe");
                }
                return createCdpBrowser(options, defaultArgs, runner, connection);
            } else {
                if (Protocol.CDP.equals(options.getProtocol())) {
                    runner.start();
                    String endpoint = this.waitForWSEndpoint(options.getTimeout(), options.getDumpio(), options.getProtocol(), runner.getProcess());
                    ConnectionTransport transport = WebSocketTransportFactory.create(endpoint);
                    connection = new CdpConnection(endpoint, transport, options.getSlowMo(), options.getProtocolTimeout());
                    runner.setConnection(connection);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Connect to browser by websocket url: {}", endpoint);
                    }
                    return createCdpBrowser(options, defaultArgs, runner, connection);
                } else {
                    if (Objects.equals(options.getProduct(), Product.Firefox)) {
                        runner.start();
                        String endpoint = this.waitForWSEndpoint(options.getTimeout(), options.getDumpio(), options.getProtocol(), runner.getProcess());
                        ConnectionTransport transport = WebSocketTransportFactory.create(endpoint + "/session");
                        connection = new BidiConnection(endpoint + "/session", transport, options.getSlowMo(), options.getProtocolTimeout());
                        runner.setConnection(connection);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Connect to browser by webDriverBidi url: {}", endpoint + "/session");
                        }
                        Runnable closeCallback = runner::closeBrowser;
                        return createBiDiBrowser((BidiConnection) connection, closeCallback, runner.getProcess(), options);
                    } else {
                        throw new LaunchException("Chrome dont not support protocol: " + options.getProtocol() + " yet");
                    }
                }
            }
        } catch (Exception e) {
            runner.closeBrowser();
            if (Objects.nonNull(e.getMessage()) && e.getMessage().contains("Failed to create a ProcessSingleton for your profile directory")) {
                throw new LaunchException("The browser is already running for " + customizedUserDataDir + ". Use a different `userDataDir` or stop the running browser first.");
            }
            if (Objects.nonNull(e.getMessage()) && e.getMessage().contains("Missing X server") && !options.getHeadless()) {
                throw new LaunchException("Missing X server to start the headful browser. set headless to true");
            }
            throw new LaunchException("Failed to launch the browser process: " + e.getMessage(), e);
        }
    }

    /**
     * waiting for browser ws url
     *
     * @param timeout 等待超时时间
     * @param dumpio  是否用标准输出打印 chrome 进程的输出流
     * @return 连接websocket的url
     */
    private String waitForWSEndpoint(int timeout, boolean dumpio, Protocol protocol, Process process) {
        return new StreamReader(timeout, dumpio, process.getInputStream(), protocol).waitFor();
    }

    static class StreamReader {
        private final StringBuilder chromeOutputBuilder = new StringBuilder();
        private volatile String wsEndpoint = null;
        private final int timeout;
        private final boolean dumpio;
        private final InputStream inputStream;
        private final Protocol protocol;

        public StreamReader(int timeout, boolean dumpio, InputStream inputStream, Protocol protocol) {
            this.timeout = timeout;
            this.dumpio = dumpio;
            this.inputStream = inputStream;
            this.protocol = protocol;
        }

        public String waitFor() {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                long now = System.currentTimeMillis();
                long base = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    long remaining = timeout - base;
                    if (remaining <= 0) {
                        throw new TimeoutException("Failed to launch the browser process!" + "Chrome output: " + chromeOutputBuilder);
                    }
                    if (dumpio) {
                        System.out.println(line);
                    }
                    //只要是 Product.Firefox 就是 用 webdriver-bidi
                    Matcher matcher = Objects.equals(Protocol.WebDriverBiDi, this.protocol) ? BiDi_ENDPOINT_PATTERN.matcher(line) : WS_ENDPOINT_PATTERN.matcher(line);
                    if (matcher.find()) {
                        wsEndpoint = matcher.group(1);
                        return wsEndpoint;
                    }
                    chromeOutputBuilder.append(line).append(System.lineSeparator());
                    base = System.currentTimeMillis() - now;
                }
                throw new LaunchException("Failed to launch the browser process! Browser process Output: " + chromeOutputBuilder);
            } catch (Exception e) {
                throw new LaunchException("Failed to launch the browser process! " + e.getMessage() + chromeOutputBuilder, e);
            }
        }
    }

    private CdpBrowser createCdpBrowser(LaunchOptions options, List<String> defaultArgs, BrowserRunner runner, Connection connection) {
        Runnable closeCallback = () -> {
            if(options.getPipe()){
                runner.destroyProcess(runner.getProcess());
                connection.onClose();
            }else {
                runner.closeBrowser();
            }
        };
        CdpBrowser cdpBrowser = CdpBrowser.create(connection, new ArrayList<>(), options.getAcceptInsecureCerts(), options.getDefaultViewport(), runner.getProcess(), closeCallback, options.getTargetFilter(), null, true, options.getNetworkEnabled(), options.getHandleDevToolsAsPage());
        cdpBrowser.setExecutablePath(this.executablePath);
        cdpBrowser.setDefaultArgs(defaultArgs);
        if (options.getWaitForInitialPage()) {
            cdpBrowser.waitForTarget(t -> TargetType.PAGE.equals(t.type()), options.getTimeout());
        }
        connection.setCloseRunner(() -> {
            if (!cdpBrowser.autoClose) {
                LOGGER.info("CdpConnection has been closed,now shutting down browser process");
                cdpBrowser.disconnect();
                try {
                    cdpBrowser.close();
                } catch (Exception e) {
                    LOGGER.trace("jvppeteer error", e);
                }
            }
        });
        runner.setPid(getBrowserPid(connection, runner.getProcess()));
        return cdpBrowser;
    }

    private Browser createBiDiBrowser(BidiConnection connection, Runnable closeCallback, Process process, LaunchOptions options) throws IOException {
        return BidiBrowser.create(process, closeCallback, connection, null, options.getDefaultViewport(), options.getAcceptInsecureCerts(), null, options.getNetworkEnabled());
    }

    /**
     * 通过cdp的SystemInfo.getProcessInfo获取浏览器pid，如果通过cdp没获取pid，并且是mac或者linux平台，那么尝试通过反射获取pid
     */
    private String getBrowserPid(Connection connection, Process process) {
        long pid = -1;
        try {
            JsonNode response = connection.send("SystemInfo.getProcessInfo");
            Iterator<JsonNode> processInfos = response.get("processInfo").elements();
            while (processInfos.hasNext()) {
                JsonNode processInfo = processInfos.next();
                if (processInfo.get(Constant.TYPE).asText().equals("browser")) {
                    pid = processInfo.get(Constant.ID).asLong();
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("get browser pid error by cdp: ", e);
        }
        try {
            if (pid == -1) {
                pid = Helper.getPidForUnixLike(process);
            }
        } catch (Exception e) {
            LOGGER.error("get browser pid error by reflection: ", e);
        }
        return String.valueOf(pid);
    }

    public String executablePath() {
        return this.executablePath;
    }

    public Browser connect(ConnectOptions options) throws Exception {
        ConnectionTransport connectionTransport;
        String endpointUrl;
        if (options.getTransport() != null) {
            connectionTransport = options.getTransport();
            endpointUrl = "";
        } else if (StringUtil.isNotEmpty(options.getBrowserWSEndpoint())) {
            connectionTransport = WebSocketTransportFactory.create(options.getBrowserWSEndpoint(), options.getHeaders(), options.getProtocolTimeout());
            endpointUrl = options.getBrowserWSEndpoint();
        } else if (StringUtil.isNotEmpty(options.getBrowserURL())) {
            endpointUrl = getWSEndpoint(options.getBrowserURL());
            connectionTransport = WebSocketTransport.create(endpointUrl);
        } else {
            throw new IllegalArgumentException("Exactly one of browserWSEndpoint, browserURL or transport must be passed to puppeteer.connect");
        }
        if (Objects.equals(options.getProtocol(), Protocol.WebDriverBiDi)) {
            return connectToBiDiBrowse(connectionTransport, endpointUrl, options);
        } else {
            return connectToCdpBrowser(connectionTransport, endpointUrl, options);
        }
    }

    private CdpBrowser connectToCdpBrowser(ConnectionTransport connectionTransport, String url, ConnectOptions options) throws IOException {
        Connection connection = new CdpConnection(url, connectionTransport, options.getSlowMo(), options.getProtocolTimeout());
        JsonNode result = connection.send("Target.getBrowserContexts");
        JavaType javaType = Constant.OBJECTMAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
        List<String> browserContextIds;
        Runnable closeCallback = () -> connection.send("Browser.close");
        browserContextIds = Constant.OBJECTMAPPER.readerFor(javaType).readValue(result.get("browserContextIds"));
        return CdpBrowser.create(connection, browserContextIds, options.getAcceptInsecureCerts(), options.getDefaultViewport(), null, closeCallback, options.getTargetFilter(), options.getIsPageTarget(), true, options.getNetworkEnabled(), options.getHandleDevToolsAsPage());
    }

    private BidiBrowser connectToBiDiBrowse(ConnectionTransport connectionTransport, String url, ConnectOptions options) throws JsonProcessingException {
        // Try pure BiDi first.
        BidiConnection pureBidiConnection = new BidiConnection(url, connectionTransport, options.getSlowMo(), options.getProtocolTimeout());
        try {
            JsonNode result = pureBidiConnection.send("session.status", Collections.emptyMap());
            if (result.has("type") && Objects.equals(result.get("type").asText(), "success")) {
                return BidiBrowser.create(null, null, pureBidiConnection, null, options.getDefaultViewport(), options.getAcceptInsecureCerts(), options.getCapabilities(), options.getNetworkEnabled());
            }
        } catch (Exception e) {
            if (!(e instanceof ProtocolException)) {
                throw e;
            }
        }
        throw new JvppeteerException("Fail to connect Browser by options " + options);
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
