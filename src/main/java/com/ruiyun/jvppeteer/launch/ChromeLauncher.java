package com.ruiyun.jvppeteer.launch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.browser.BrowserRunner;
import com.ruiyun.jvppeteer.core.browser.RevisionInfo;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.options.BrowserOptions;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.transport.ConnectionTransport;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ChromeLauncher implements Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeLauncher.class);

    private boolean isPuppeteerCore;

    private String projectRoot;

    private String preferredRevision;

    public ChromeLauncher(String projectRoot, String preferredRevision, boolean isPuppeteerCore) {
        super();
        this.projectRoot = projectRoot;
        this.preferredRevision = preferredRevision;
        this.isPuppeteerCore = isPuppeteerCore;
    }

    public ChromeLauncher() {
    }

    @Override
    public Browser launch(LaunchOptions options) {
        List<String> chromeArguments = new ArrayList<>();
        String temporaryUserDataDir = defaultArgs(options, chromeArguments);
        String chromeExecutable = resolveExecutablePath(options.getExecutablePath());
        boolean usePipe = chromeArguments.contains("--remote-debugging-pipe");

        LOGGER.info("Calling " + chromeExecutable + String.join(" ", chromeArguments));
        BrowserRunner runner = new BrowserRunner(chromeExecutable, chromeArguments, temporaryUserDataDir);//
        try {
            runner.start(options.getHandleSIGINT(), options.getHandleSIGTERM(), options.getHandleSIGHUP(), options.getDumpio(), usePipe);
            Connection connection = runner.setUpConnection(usePipe, options.getTimeout(), options.getSlowMo(), "");
            Function closeCallback = (s) -> {
                runner.closeQuietly();
                return null;
            };
            Browser browser = Browser.create(connection, null, options.getIgnoreHTTPSErrors(), options.getViewport(), runner.getProcess(), closeCallback, options.getTimeout());
            browser.waitForTarget(t -> "page".equals(t.type()), options);
            return browser;
        } catch (IOException | InterruptedException e) {
            runner.kill();
            throw new LaunchException("Failed to launch the browser process:" + e.getMessage(), e);
        }
    }

    /**
     * @param options
     * @param chromeArguments
     * @return
     */
    @Override
    public String defaultArgs(ChromeArgOptions options, List<String> chromeArguments) {
        String temporaryUserDataDir = null;
        boolean pipe = false;
        LaunchOptions launchOptions = null;
        if (options instanceof LaunchOptions) {
            launchOptions = (LaunchOptions) options;
            pipe = launchOptions.getPipe();
            if (!launchOptions.getIgnoreAllDefaultArgs()) {
                chromeArguments.addAll(Constant.DEFAULT_ARGS);
            }
        }

        List<String> args = null;
        if (ValidateUtil.isNotEmpty(args = options.getArgs())) {
            chromeArguments.add("about:blank");
            chromeArguments.addAll(args);
        }

        boolean devtools = options.getDevtools();
        boolean headless = options.getHeadless();
        if (devtools) {
            chromeArguments.add("--auto-open-devtools-for-tabs");
            headless = false;
        }

        if (headless) {
            chromeArguments.add("--headless");
            chromeArguments.add("--hide-scrollbars");
            chromeArguments.add("--mute-audio");
        }
        List<String> ignoreDefaultArgs;
        if (launchOptions != null && ValidateUtil.isNotEmpty(ignoreDefaultArgs = launchOptions.getIgnoreDefaultArgs())) {
            chromeArguments.removeAll(ignoreDefaultArgs);
        }

        boolean isCustomUserDir = false;
        boolean isCustomRemoteDebugger = false;
        for (String arg : chromeArguments) {
            if (arg.startsWith("--remote-debugging-")) {
                isCustomRemoteDebugger = true;
            }
            if (arg.startsWith("--user-data-dir")) {
                isCustomUserDir = true;
            }
        }
        if (!isCustomUserDir) {
            temporaryUserDataDir = FileUtil.createProfileDir(Constant.PROFILE_PREFIX);
            chromeArguments.add("--user-data-dir=" + temporaryUserDataDir);
        }
        if (!isCustomRemoteDebugger) {
            chromeArguments.add(pipe ? "--remote-debugging-pipe" : "--remote-debugging-port=0");
        }
        return temporaryUserDataDir;
    }

    /**
     * 解析可执行的chrome路径
     *
     * @param chromeExecutable
     * @return
     */
    @Override
    public String resolveExecutablePath(String chromeExecutable) {
        boolean puppeteerCore = getIsPuppeteerCore();
        if (!puppeteerCore) {
            if (StringUtil.isNotEmpty(chromeExecutable)) {
                boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
                if (!assertDir) {
                    throw new IllegalArgumentException("given chromeExecutable \"" + chromeExecutable + "\" is not executable");
                }
                return chromeExecutable;
            } else {
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

                for (int i = 0; i < Constant.PROBABLE_CHROME_EXECUTABLE_PATH.length; i++) {
                    chromeExecutable = Constant.PROBABLE_CHROME_EXECUTABLE_PATH[i];
                    if (StringUtil.isNotEmpty(chromeExecutable)) {
                        boolean assertDir = FileUtil.assertExecutable(chromeExecutable);
                        if (assertDir) {
                            return chromeExecutable;
                        }
                    }
                }

                throw new RuntimeException(
                        "Tried to use PUPPETEER_EXECUTABLE_PATH env variable to launch browser but did not find any executable");
            }
        }
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(this.product());
        BrowserFetcher browserFetcher = new BrowserFetcher(this.projectRoot, fetcherOptions);
        String revision = env.getEnv(Constant.PUPPETEER_CHROMIUM_REVISION_ENV);
        if (StringUtil.isNotEmpty(revision)) {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(revision);
            if (!revisionInfo.getLocal()) {
                throw new LaunchException(
                        "Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
                                + revisionInfo.getExecutablePath());
            }
            return revisionInfo.getExecutablePath();
        } else {
            RevisionInfo revisionInfo = browserFetcher.revisionInfo(this.preferredRevision);
            if (!revisionInfo.getLocal())
                throw new LaunchException(MessageFormat.format("Could not find browser revision {0}. Pleaze download a browser binary.", this.preferredRevision));
            return revisionInfo.getExecutablePath();
        }

    }

    @Override
    public Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, ConnectionTransport transport) {
        final Connection connection;
        try {
            if (transport != null) {
                connection = new Connection("", transport, options.getSlowMo());
            } else if (StringUtil.isNotEmpty(browserWSEndpoint)) {
                WebSocketTransport connectionTransport = WebSocketTransport.create(browserWSEndpoint);
                connection = new Connection(browserWSEndpoint, connectionTransport, options.getSlowMo());
            } else if (StringUtil.isNotEmpty(browserURL)) {
                String connectionURL = getWSEndpoint(browserURL);
                WebSocketTransport connectionTransport = WebSocketTransport.create(connectionURL);
                connection = new Connection(connectionURL, connectionTransport, options.getSlowMo());
            } else {
                throw new IllegalArgumentException("Exactly one of browserWSEndpoint, browserURL or transport must be passed to puppeteer.connect");
            }
            JsonNode result = connection.send("Target.getBrowserContexts", null, true);

            JavaType javaType = Constant.OBJECTMAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            List<String> browserContextIds = null;
            Function closeFunction = (t) -> {
                connection.send("Browser.close", null, false);
                return null;
            };

            browserContextIds = (List<String>) Constant.OBJECTMAPPER.readerFor(javaType).readValue(result.get("browserContextIds"));
            return Browser.create(connection, browserContextIds, options.getIgnoreHTTPSErrors(), options.getViewport(), null, closeFunction, options.getTimeout());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

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

    public boolean getIsPuppeteerCore() {
        return isPuppeteerCore;
    }

    public void setIsPuppeteerCore(boolean isPuppeteerCore) {
        this.isPuppeteerCore = isPuppeteerCore;
    }


    @Override
    public String executablePath() {
        return resolveExecutablePath(null);
    }

    public String product() {
        return "chrome";
    }


}
