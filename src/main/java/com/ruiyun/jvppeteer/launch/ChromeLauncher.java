package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import static com.ruiyun.jvppeteer.common.Constant.JVPPETEER_TEST_EXPERIMENTAL_CHROME_FEATURES;

public class ChromeLauncher extends BrowserLauncher {


    public ChromeLauncher(String cacheDir, Product product) {
        super(cacheDir, product);
    }

    @Override
    public Browser launch(LaunchOptions options) throws IOException {
        if (options.getArgs() == null) {
            options.setArgs(new ArrayList<>());
        }
        this.executablePath = this.computeExecutablePath(options.getExecutablePath(), options.getPreferredRevision());

        //临时的 UserDataDir
        String temporaryUserDataDir = null;
        //自定义的 UserDataDir
        String customizedUserDataDir = null;
        List<String> defaultArgs = this.defaultArgs(options);
        List<String> chromeArguments = new ArrayList<>(defaultArgs);
        boolean isCustomUserDir = false;
        boolean isCustomRemoteDebugger = false;
        for (String arg : chromeArguments) {
            if (arg.startsWith("--remote-debugging-")) {
                isCustomRemoteDebugger = true;
            }
            if (arg.startsWith("--user-data-dir")) {
                isCustomUserDir = true;
                customizedUserDataDir = arg.replace("--user-data-dir=", "");
            }
        }
        if (!isCustomUserDir) {
            temporaryUserDataDir = FileUtil.createProfileDir(Constant.CHROME_PROFILE_PREFIX);
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
        LOGGER.trace("Calling {} {}", this.executablePath, String.join(" ", chromeArguments));
        Browser browser = createBrowser(options, chromeArguments, temporaryUserDataDir, usePipe, defaultArgs, customizedUserDataDir);
        LOGGER.info("Browser started successfully, executablePath is {}, protocol is {}({}),version is {}", this.executablePath, options.getProtocol(), usePipe ? "pipe" : "websocket", browser.version());
        return browser;
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
        boolean turnOnExperimentalFeaturesForTesting = "true".equals(System.getProperty(JVPPETEER_TEST_EXPERIMENTAL_CHROME_FEATURES));
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
        //忽略全部默认参数
        if (options.getIgnoreAllDefaultArgs()) {
            chromeArguments = new ArrayList<>();
        } else {
            chromeArguments = new ArrayList<>(Constant.DEFAULT_ARGS);
            chromeArguments.add("--disable-features=" + String.join(",", disabledFeatures));
            chromeArguments.add("--enable-features=" + String.join(",", enabledFeatures));
            //默认参数基础上再忽略指定参数
            if (ValidateUtil.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {
                chromeArguments.removeAll(ignoreDefaultArgs);
            }
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
            if (Product.Chrome_headless_shell.equals(options.getProduct()) || this.executablePath.contains(Product.Chrome_headless_shell.getProduct())) {
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

    private void removeMatchingFlags(LaunchOptions options, String flag) {
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


}
