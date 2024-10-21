package com.ruiyun.jvppeteer.launch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.BrowserFetcher;
import com.ruiyun.jvppeteer.core.BrowserRunner;
import com.ruiyun.jvppeteer.entities.BrowserLaunchArgumentOptions;
import com.ruiyun.jvppeteer.entities.ConnectOptions;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChromeLauncher implements Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeLauncher.class);
    private Product product;
    private String cacheDir;
    private String chromeExecutable;

    public ChromeLauncher(String cacheDir, Product product) {
        super();
        this.cacheDir = cacheDir;
        this.product = product;
    }

    public ChromeLauncher() {
    }

    @Override
    public Browser launch(LaunchOptions options) throws IOException {
        if (options.getArgs() == null) {
            options.setArgs(new ArrayList<>());
        }
        this.chromeExecutable = this.lookForExecutablePath(options.getExecutablePath(), options.getPreferredRevision());
        String temporaryUserDataDir = options.getUserDataDir();
        List<String> defaultArgs = this.defaultArgs(options);
        List<String> chromeArguments = new ArrayList<>(defaultArgs);
        boolean isCustomUserDir = false;
        boolean isCustomRemoteDebugger = false;
        for (String arg : chromeArguments) {
            if (arg.startsWith("--remote-debugging-")) {
                isCustomRemoteDebugger = true;
            } else if (arg.startsWith("--user-data-dir")) {
                isCustomUserDir = true;
            }
        }
        if (!isCustomUserDir) {
            temporaryUserDataDir = FileUtil.createProfileDir(Constant.PROFILE_PREFIX);
            chromeArguments.add("--user-data-dir=" + temporaryUserDataDir);
        }
        if (!isCustomRemoteDebugger) {
            if (options.getPipe()) {
                ValidateUtil.assertArg(options.getDebuggingPort() == 0, "Browser should be launched with either pipe or debugging port - not both.");
                chromeArguments.add("--remote-debugging-pipe");
            } else {
                chromeArguments.add("--remote-debugging-port=" + options.getDebuggingPort());
            }

        }

        boolean usePipe = chromeArguments.contains("--remote-debugging-pipe");
        LOGGER.info("Calling {} {}", chromeExecutable, String.join(" ", chromeArguments));
        LOGGER.trace("Calling {} {}", this.chromeExecutable, String.join(" ", chromeArguments));
        BrowserRunner runner = new BrowserRunner(this.chromeExecutable, chromeArguments, temporaryUserDataDir);
        try {
            runner.start();
            Connection connection = runner.setUpConnection(usePipe, options.getProtocolTimeout(), options.getSlowMo(), options.getDumpio());
            Runnable closeCallback = runner::closeBrowser;
            Browser browser = Browser.create(options.getProduct(), connection, new ArrayList<>(), options.getAcceptInsecureCerts(), options.getDefaultViewport(), runner.getProcess(), closeCallback, options.getTargetFilter(), null, true);
            browser.setExecutablePath(this.chromeExecutable);
            browser.setDefaultArgs(defaultArgs);
            if (options.getWaitForInitialPage()) {
                browser.waitForTarget(t -> TargetType.PAGE.equals(t.type()), options.getTimeout());
            }
//            runner.setPid(getBrowserPid(connection, runner.getProcess()));
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
    private String getBrowserPid(Connection connection, Process process) {
        long pid = -1;
        try {
            JsonNode result = connection.send("SystemInfo.getProcessInfo");
            Iterator<JsonNode> processInfos = result.get("processInfo").elements();
            while (processInfos.hasNext()) {
                JsonNode processInfo = processInfos.next();
                if (processInfo.get(Constant.TYPE).asText().equals("browser")) {
                    pid = processInfo.get(Constant.ID).asLong();
                    break;
                }
            }
            if (pid == -1) {
                pid = Helper.getPidForLinuxOrMac(process);
            }
        } catch (Exception e) {
            LOGGER.error("get browser pid error: ", e);
        }
        return String.valueOf(pid);
    }

    /**
     * 返回默认的启动参数
     *
     * @param options 自定义的参数
     * @return 默认的启动参数
     */
    @Override
    public List<String> defaultArgs(LaunchOptions options) {
        List<String> userDisabledFeatures = getFeatures("--disable-features", options.getArgs());
        if (ValidateUtil.isNotEmpty(options.getArgs()) && !userDisabledFeatures.isEmpty()) {
            removeMatchingFlags(options, "--disable-features");
        }
        boolean turnOnExperimentalFeaturesForTesting = "true".equals(env.getEnv("PUPPETEER_TEST_EXPERIMENTAL_CHROME_FEATURES"));
        List<String> disabledFeatures = new ArrayList<>();
        disabledFeatures.add("Translate");
        disabledFeatures.add("AcceptCHFrame");
        disabledFeatures.add("MediaRouter");
        disabledFeatures.add("OptimizationHints");
        if (!turnOnExperimentalFeaturesForTesting) {
            disabledFeatures.add("ProcessPerSiteUpToMainFrameThreshold");
            disabledFeatures.add("IsolateSandboxedIframes");
        }
        disabledFeatures.addAll(userDisabledFeatures);
        disabledFeatures = disabledFeatures.stream().filter(feature -> !"".equals(feature)).collect(Collectors.toList());
        List<String> userEnabledFeatures = getFeatures("--enable-features", options.getArgs());
        if (ValidateUtil.isNotEmpty(options.getArgs()) && !userEnabledFeatures.isEmpty()) {
            removeMatchingFlags(options, "--enable-features");
        }
        List<String> enabledFeatures = new ArrayList<>();
        enabledFeatures.add("PdfOopif");
        enabledFeatures.addAll(userEnabledFeatures);
        enabledFeatures = enabledFeatures.stream().filter(feature -> !"".equals(feature)).collect(Collectors.toList());
        List<String> chromeArguments;
        List<String> ignoreDefaultArgs;
        if (!options.getIgnoreAllDefaultArgs()) {//不忽略默认参数
            chromeArguments = new ArrayList<>(Constant.DEFAULT_ARGS);
            chromeArguments.add("--disable-features=" + String.join(",", disabledFeatures));
            chromeArguments.add("--enable-features=" + String.join(",", enabledFeatures));
        } else if (ValidateUtil.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {//指定忽略的默认参数
            chromeArguments = new ArrayList<>(Constant.DEFAULT_ARGS);
            chromeArguments.add("--disable-features=" + String.join(",", disabledFeatures));
            chromeArguments.add("--enable-features=" + String.join(",", enabledFeatures));
            chromeArguments.removeAll(ignoreDefaultArgs);
        } else {//忽略全部默认参数
            chromeArguments = new ArrayList<>();
        }

        if (StringUtil.isNotEmpty(options.getUserDataDir())) {
            chromeArguments.add("--user-data-dir=" + options.getUserDataDir());
        }
        boolean devtools = options.getDevtools();
        boolean headless = options.getHeadless();
        if (devtools) {
            chromeArguments.add("--auto-open-devtools-for-tabs");
            //如果打开devtools，那么headless强制变为false
            headless = false;
        }
        if (headless) {
            if (Product.CHROMEHEADLESSSHELL.equals(options.getProduct()) || this.chromeExecutable.contains(Product.CHROMEHEADLESSSHELL.getProduct())) {
                chromeArguments.add("--headless");
            } else {
                chromeArguments.add("--headless=new");
                chromeArguments.add("--hide-scrollbars");
                chromeArguments.add("--mute-audio");
            }
        }
        List<String> args;
        if (ValidateUtil.isNotEmpty(args = options.getArgs())) {
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    chromeArguments.add("about:blank");
                    break;
                }
            }
            chromeArguments.addAll(args);
        } else {
            chromeArguments.add("about:blank");
        }
        return chromeArguments;
    }

    private void removeMatchingFlags(BrowserLaunchArgumentOptions options, String flag) {
        Pattern regex = Pattern.compile("^" + flag + "=.*");
        options.setArgs(options.getArgs().stream().filter(s -> !regex.matcher(s).find()).collect(Collectors.toList()));
    }

    private List<String> getFeatures(String flag, List<String> options) {
        String prefix = flag.endsWith("=") ? flag : flag + "=";
        return options.stream()
                .filter(s -> s.startsWith(prefix))
                .map(s -> {
                    String[] splitArray = s.split(flag + "\\s*");
                    if (splitArray.length > 1) {
                        if (StringUtil.isNotEmpty(splitArray[1])) {
                            return splitArray[1].trim();
                        } else {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 解析可执行的chrome路径
     *
     * @param chromeExecutable 指定的可执行路径
     * @return 返回解析后的可执行路径
     */
    @Override
    public String lookForExecutablePath(String chromeExecutable, String preferredRevision) throws IOException {
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(this.product);
        fetcherOptions.setCacheDir(this.cacheDir);
        BrowserFetcher browserFetcher = new BrowserFetcher(fetcherOptions);
        /*指定了启动路径，则启动指定路径的chrome*/
        if (StringUtil.isNotEmpty(chromeExecutable)) {
            boolean assertDir = FileUtil.assertExecutable(Paths.get(chromeExecutable).normalize().toAbsolutePath().toString());
            if (!assertDir) {
                throw new IllegalArgumentException("given chromeExecutable \"" + chromeExecutable + "\" is not executable");
            }
            return chromeExecutable;
        }
        /*环境变量中配置了chromeExecutable，就使用环境变量中的路径*/
        for (int i = 0; i < Constant.EXECUTABLE_ENV.length; i++) {
            chromeExecutable = env.getEnv(Constant.EXECUTABLE_ENV[i]);
            if (StringUtil.isNotEmpty(chromeExecutable)) {
                boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
                if (!assertDir) {
                    throw new IllegalArgumentException("given chromeExecutable is not is not executable");
                }
                return chromeExecutable;
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
        String revision = env.getEnv(Constant.JVPPETEER_CHROMIUM_REVISION_ENV);
        if (StringUtil.isNotEmpty(revision)) {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(revision);
            if (!revisionInfo.getLocal()) {
                throw new LaunchException(
                        "Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
                                + revisionInfo.getExecutablePath());
            }
            return revisionInfo.getExecutablePath();
        }
        /*如果下载了chrome，就使用下载的chrome*/
        List<String> localRevisions = browserFetcher.localRevisions();
        if (ValidateUtil.isNotEmpty(localRevisions)) {
            localRevisions.sort(Comparator.reverseOrder());
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(localRevisions.get(0));
            if (!revisionInfo.getLocal()) {
                throw new LaunchException(
                        "Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
                                + revisionInfo.getExecutablePath());
            }
            return revisionInfo.getExecutablePath();
        }
        /*寻找可能存在的启动路径*/
        for (int i = 0; i < Constant.PROBABLE_CHROME_EXECUTABLE_PATH.length; i++) {
            chromeExecutable = Constant.PROBABLE_CHROME_EXECUTABLE_PATH[i];
            if (StringUtil.isNotEmpty(chromeExecutable)) {
                boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
                if (assertDir) {
                    return chromeExecutable;
                }
            }
        }
        throw new LaunchException("Could not find anyone browser executablePath");
    }

    @Override
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
        return Browser.create(Product.CHROME, connection, browserContextIds, options.getAcceptInsecureCerts(), options.getDefaultViewport(), null, closeFunction, options.getTargetFilter(), options.getIsPageTarget(), true);
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

    @Override
    public String executablePath() {
        return this.chromeExecutable;
    }

}
