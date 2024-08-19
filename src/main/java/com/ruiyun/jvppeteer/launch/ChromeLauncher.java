package com.ruiyun.jvppeteer.launch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.browser.BrowserRunner;
import com.ruiyun.jvppeteer.core.browser.RevisionInfo;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.WebSocketTransport;
import com.ruiyun.jvppeteer.util.FileUtil;
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
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChromeLauncher implements Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeLauncher.class);
    private String projectRoot;
    private String preferredRevision;
    public ChromeLauncher(String projectRoot, String preferredRevision) {
        super();
        this.projectRoot = projectRoot;
        this.preferredRevision = preferredRevision;
    }

    public ChromeLauncher() {
    }

    @Override
    public Browser launch(LaunchOptions options)  {
        if(options.getArgs() == null){
            options.setArgs(new ArrayList<>());
        }
        String temporaryUserDataDir = options.getUserDataDir();
        List<String> chromeArguments = new ArrayList<>();
        List<String> ignoreDefaultArgs;
        if(!options.getIgnoreAllDefaultArgs()){
            chromeArguments.addAll(defaultArgs(options));
        } else if (ValidateUtil.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {
            chromeArguments.addAll(defaultArgs(options));
            chromeArguments.removeAll(ignoreDefaultArgs);
        }
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
            chromeArguments.add(options.getPipe() ? "--remote-debugging-pipe" : "--remote-debugging-port="+options.getDebuggingPort());
        }

        String chromeExecutable;
        try {
            chromeExecutable = resolveExecutablePath(options.getExecutablePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean usePipe = chromeArguments.contains("--remote-debugging-pipe");

        LOGGER.trace("Calling {}{}", chromeExecutable, String.join(" ", chromeArguments));
        BrowserRunner runner = new BrowserRunner(chromeExecutable, chromeArguments, temporaryUserDataDir);//
        try {
            runner.start(options);
            Connection connection = runner.setUpConnection(usePipe, options.getProtocolTimeout(), options.getSlowMo(), options.getDumpio());
            Runnable closeCallback = runner::closeBrowser;
            Browser browser = Browser.create("chrome",connection, new ArrayList<>(), options.getAcceptInsecureCerts(), options.getDefaultViewport(), runner.getProcess(), closeCallback,options.getTargetFilter(),null,true);
           if(options.getWaitForInitialPage()){
               browser.waitForTarget(t -> TargetType.PAGE.equals(t.type()), options.getTimeout());
           }
            return browser;
        } catch (IOException | InterruptedException e) {
            runner.kill();
            LOGGER.error("Failed to launch the browser process:{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 返回默认的启动参数
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
        if(!turnOnExperimentalFeaturesForTesting){
            disabledFeatures.add("ProcessPerSiteUpToMainFrameThreshold");
            disabledFeatures.add("IsolateSandboxedIframes");
        }
        disabledFeatures.addAll(userDisabledFeatures);

        List<String> userEnabledFeatures = getFeatures("--enable-features", options.getArgs());
        if (ValidateUtil.isNotEmpty(options.getArgs()) && !userEnabledFeatures.isEmpty()) {
            removeMatchingFlags(options, "--enable-features");
        }
        List<String> enabledFeatures = new ArrayList<>();
        enabledFeatures.add("PdfOopif");
        enabledFeatures.addAll(userEnabledFeatures);

        List<String> chromeArguments = new ArrayList<>(Constant.DEFAULT_ARGS);
        chromeArguments.add("--disable-features=" + String.join(",", disabledFeatures));
        chromeArguments.add("--enable-features=" + String.join(",", enabledFeatures));
        if(StringUtil.isNotEmpty(options.getUserDataDir())){
            chromeArguments.add("--user-data-dir="+options.getUserDataDir());
        }
        boolean devtools = options.getDevtools();
        boolean headless = options.getHeadless();
        if (devtools) {
            chromeArguments.add("--auto-open-devtools-for-tabs");
            //如果打开devtools，那么headless强制变为false
            headless = false;
        }
        if (headless) {
            if (options.getHeadlessShell()) {
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
        }
        return chromeArguments;
    }

    private void removeMatchingFlags(BrowserLaunchArgumentOptions options, String flag) {
        Pattern regex =  Pattern.compile("^" + flag + "\\S*");
        options.setArgs(options.getArgs().stream().filter(s -> !regex.matcher(s).matches()).collect(Collectors.toList()));
    }

    private List<String> getFeatures(String flag, List<String> options) {
        String prefix = flag.endsWith("=") ? flag : flag + "=";
        return options.stream()
                .filter(s -> s.startsWith(prefix))
                .map(s -> {
                    String[] splitArray = s.split(flag + "\\s*");
                    if(splitArray.length > 1){
                        if(StringUtil.isNotEmpty(splitArray[1])){
                            return splitArray[1].trim();
                        }else{
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
    public String resolveExecutablePath(String chromeExecutable) throws IOException {
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(this.product());
        BrowserFetcher browserFetcher = new BrowserFetcher(this.projectRoot, fetcherOptions);
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
        /*环境变量中配置了chrome版本，就用环境变量中的版本*/
        String revision = env.getEnv(Constant.PUPPETEER_CHROMIUM_REVISION_ENV);
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
        RevisionInfo revisionInfo = browserFetcher.revisionInfo(this.preferredRevision);
        if (!revisionInfo.getLocal())
            throw new LaunchException(MessageFormat.format("Could not find browser revision {0}. Pleaze download a browser binary.", this.preferredRevision));
        return revisionInfo.getExecutablePath();
    }

    @Override
    public Browser connect(ConnectOptions options) {
        final Connection connection;
        try {
            if (options.getTransport() != null) {
                connection = new Connection("", options.getTransport(), options.getSlowMo(),options.getProtocolTimeout());
            } else if (StringUtil.isNotEmpty(options.getBrowserWSEndpoint())) {
                WebSocketTransport connectionTransport = WebSocketTransport.create(options.getBrowserWSEndpoint());
                connection = new Connection(options.getBrowserWSEndpoint(), connectionTransport, options.getSlowMo(),options.getTimeout());
            } else if (StringUtil.isNotEmpty(options.getBrowserURL())) {
                String connectionURL = getWSEndpoint(options.getBrowserURL());
                WebSocketTransport connectionTransport = WebSocketTransport.create(connectionURL);
                connection = new Connection(connectionURL, connectionTransport, options.getSlowMo(),options.getTimeout());
            } else {
                throw new IllegalArgumentException("Exactly one of browserWSEndpoint, browserURL or transport must be passed to puppeteer.connect");
            }
            JsonNode result = connection.send("Target.getBrowserContexts");
            JavaType javaType = Constant.OBJECTMAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            List<String> browserContextIds;
            Runnable closeFunction = () -> connection.send("Browser.close");
            browserContextIds = Constant.OBJECTMAPPER.readerFor(javaType).readValue(result.get("browserContextIds"));
            return Browser.create("chrome",connection, browserContextIds, options.getAcceptInsecureCerts(), options.getDefaultViewport(), null, closeFunction,options.getTargetFilter(),options.getIsPageTarget(),true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /**
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
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("browserURL: " + browserURL + ",HTTP " + responseCode);
        }
        String result = StreamUtil.toString(conn.getInputStream());
        JsonNode jsonNode = Constant.OBJECTMAPPER.readTree(result);
        return jsonNode.get("webSocketDebuggerUrl").asText();
    }
    @Override
    public String executablePath() throws IOException {
        return resolveExecutablePath(null);
    }

    public String product() {
        return "chrome";
    }


}
