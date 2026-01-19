package com.ruiyun.jvppeteer.launch;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static com.ruiyun.jvppeteer.common.Constant.BACKUP_SUFFIX;
import static com.ruiyun.jvppeteer.common.Constant.PREFS_JS;
import static com.ruiyun.jvppeteer.common.Constant.USER_JS;

public class FirefoxLauncher extends com.ruiyun.jvppeteer.launch.BrowserLauncher {

    public FirefoxLauncher(String cacheDir, Product product) {
        super(cacheDir, product);
    }

    @Override
    public Browser launch(LaunchOptions options) throws IOException {
        if (Objects.isNull(options.getArgs())) {
            options.setArgs(new ArrayList<>());
        }
        this.executablePath = this.computeExecutablePath(options.getExecutablePath(), options.getPreferredRevision());

        //临时的 UserDataDir
        String temporaryUserDataDir = null;
        //自定义的 UserDataDir
        String customizedUserDataDir = null;
        List<String> defaultArgs = this.defaultArgs(options);
        List<String> firefoxArguments = new ArrayList<>(defaultArgs);
        boolean isCustomUserDir = false;
        boolean isCustomRemoteDebugger = false;
        for (String arg : firefoxArguments) {
            if (arg.startsWith("--remote-debugging-")) {
                isCustomRemoteDebugger = true;
                break;
            }
        }
        int profileIndex = firefoxArguments.indexOf("-profile");
        int profileIndex2 = firefoxArguments.indexOf("--profile");
        if (profileIndex != -1) {
            isCustomUserDir = true;
            customizedUserDataDir = firefoxArguments.get(profileIndex + 1);
        }
        if (profileIndex2 != -1) {
            isCustomUserDir = true;
            customizedUserDataDir = firefoxArguments.get(profileIndex2 + 1);
        }
        if (!isCustomUserDir) {
            temporaryUserDataDir = FileUtil.createProfileDir(Constant.FIREFOX_PROFILE_PREFIX);
            firefoxArguments.add("--profile");
            firefoxArguments.add(temporaryUserDataDir);
        }
        if (!isCustomRemoteDebugger) {
            if (options.getPipe()) {
                ValidateUtil.assertArg(options.getDebuggingPort() == 0, "Browser should be launched with either pipe or debugging port - not both.");
                firefoxArguments.add("--remote-debugging-pipe");
            } else {
                firefoxArguments.add("--remote-debugging-port=" + options.getDebuggingPort());
            }

        }
        createProfile(StringUtil.isNotEmpty(temporaryUserDataDir) ? temporaryUserDataDir : customizedUserDataDir, getPreferences(options));
        boolean usePipe = firefoxArguments.contains("--remote-debugging-pipe");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calling {} {}", this.executablePath, String.join(" ", firefoxArguments));
        }
        Browser browser = createBrowser(options, firefoxArguments, temporaryUserDataDir, usePipe, defaultArgs, customizedUserDataDir);
        LOGGER.info("Browser started successfully, executablePath is {}, protocol is {},version is {}", this.executablePath, options.getProtocol(),browser.version());
        return browser;
    }

    private Map<String, Object> getPreferences(LaunchOptions options) {
        Map<String, Object> prefs = new HashMap<>();
        if (Objects.nonNull(options.getExtraPrefsFirefox())) {
            prefs.putAll(options.getExtraPrefsFirefox());
        }
        // Only enable the WebDriver BiDi protocol
        prefs.put("remote.active-protocols", 1);
        // Force all web content to use a single content process. TODO: remove
        // this once Firefox supports mouse event dispatch from the main frame
        // context. Once this happens, webContentIsolationStrategy should only
        // be set for CDP. See
        // https://bugzilla.mozilla.org/show_bug.cgi?id=1773393
        prefs.put("fission.webContentIsolationStrategy", 0);
        return prefs;
    }

    private void createProfile(String userDir, Map<String, Object> preferences) throws IOException {
        Path path = Paths.get(userDir);
        if (!Files.exists(path)) {
            FileUtil.createDirs(path);
        }
        Map<String, Object> defaultProfilePreferences = defaultProfilePreferences();
        defaultProfilePreferences.putAll(preferences);
        String prefsPath = Helper.join(userDir, PREFS_JS);
        String userPath = Helper.join(userDir, USER_JS);
        backupFile(userPath);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : defaultProfilePreferences.entrySet()) {
            sb.append("user_pref(\"").append(entry.getKey()).append("\", ").append(Constant.OBJECTMAPPER.writeValueAsString(entry.getValue())).append(");\n");
        }
        Files.write(Paths.get(userPath), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        backupFile(prefsPath);
    }

    private void backupFile(String input) throws IOException {
        Path source = Paths.get(input);
        if (!Files.exists(source)) {
            return;
        }
        Files.copy(source, Paths.get(input + BACKUP_SUFFIX), StandardCopyOption.REPLACE_EXISTING);
    }

    private Map<String, Object> defaultProfilePreferences() {
        String server = "dummy.test";
        Map<String, Object> defaultPrefs = new LinkedHashMap<>();

        defaultPrefs.put("app.normandy.api_url", "");
        defaultPrefs.put("app.update.checkInstallTime", false);
        defaultPrefs.put("app.update.disabledForTesting", true);

        defaultPrefs.put("apz.content_response_timeout", 60000);

        defaultPrefs.put("browser.contentblocking.features.standard", "-tp,tpPrivate,cookieBehavior0,-cryptoTP,-fp");
        defaultPrefs.put("browser.dom.window.dump.enabled", true);
        defaultPrefs.put("browser.newtabpage.activity-stream.feeds.system.topstories", false);
        defaultPrefs.put("browser.newtabpage.enabled", false);
        defaultPrefs.put("browser.pagethumbnails.capturing_disabled", true);

        defaultPrefs.put("browser.safebrowsing.blockedURIs.enabled", false);
        defaultPrefs.put("browser.safebrowsing.downloads.enabled", false);
        defaultPrefs.put("browser.safebrowsing.malware.enabled", false);
        defaultPrefs.put("browser.safebrowsing.phishing.enabled", false);

        defaultPrefs.put("browser.search.update", false);
        defaultPrefs.put("browser.sessionstore.resume_from_crash", false);
        defaultPrefs.put("browser.shell.checkDefaultBrowser", false);

        defaultPrefs.put("browser.startup.homepage", "about:blank");
        defaultPrefs.put("browser.startup.homepage_override.mstone", "ignore");
        defaultPrefs.put("browser.startup.page", 0);

        defaultPrefs.put("browser.tabs.disableBackgroundZombification", false);
        defaultPrefs.put("browser.tabs.warnOnCloseOtherTabs", false);
        defaultPrefs.put("browser.tabs.warnOnOpen", false);

        defaultPrefs.put("browser.translations.automaticallyPopup", false);

        defaultPrefs.put("browser.uitour.enabled", false);
        defaultPrefs.put("browser.urlbar.suggest.searches", false);
        defaultPrefs.put("browser.usedOnWindows10.introURL", "");
        defaultPrefs.put("browser.warnOnQuit", false);

        defaultPrefs.put("datareporting.healthreport.documentServerURI", "http://" + server + "/dummy/healthreport/");
        defaultPrefs.put("datareporting.healthreport.logging.consoleEnabled", false);
        defaultPrefs.put("datareporting.healthreport.service.enabled", false);
        defaultPrefs.put("datareporting.healthreport.service.firstRun", false);
        defaultPrefs.put("datareporting.healthreport.uploadEnabled", false);

        defaultPrefs.put("datareporting.policy.dataSubmissionEnabled", false);
        defaultPrefs.put("datareporting.policy.dataSubmissionPolicyBypassNotification", true);

        defaultPrefs.put("devtools.jsonview.enabled", false);

        defaultPrefs.put("dom.disable_open_during_load", false);

        defaultPrefs.put("dom.file.createInChild", true);

        defaultPrefs.put("dom.ipc.reportProcessHangs", false);

        defaultPrefs.put("dom.max_chrome_script_run_time", 0);
        defaultPrefs.put("dom.max_script_run_time", 0);

        defaultPrefs.put("extensions.autoDisableScopes", 0);
        defaultPrefs.put("extensions.enabledScopes", 5);

        defaultPrefs.put("extensions.getAddons.cache.enabled", false);

        defaultPrefs.put("extensions.installDistroAddons", false);

        defaultPrefs.put("extensions.update.enabled", false);

        defaultPrefs.put("extensions.update.notifyUser", false);

        defaultPrefs.put("extensions.webservice.discoverURL", "http://" + server + "/dummy/discoveryURL");

        defaultPrefs.put("focusmanager.testmode", true);

        defaultPrefs.put("general.useragent.updates.enabled", false);

        defaultPrefs.put("geo.provider.testing", true);

        defaultPrefs.put("geo.wifi.scan", false);

        defaultPrefs.put("hangmonitor.timeout", 0);

        defaultPrefs.put("javascript.options.showInConsole", true);

        defaultPrefs.put("media.gmp-manager.updateEnabled", false);

        defaultPrefs.put("media.sanity-test.disabled", true);

        defaultPrefs.put("network.cookie.sameSite.laxByDefault", false);

        defaultPrefs.put("network.http.prompt-temp-redirect", false);

        defaultPrefs.put("network.http.speculative-parallel-limit", 0);

        defaultPrefs.put("network.manage-offline-status", false);

        defaultPrefs.put("network.sntp.pools", server);

        defaultPrefs.put("plugin.state.flash", 0);

        defaultPrefs.put("privacy.trackingprotection.enabled", false);

        defaultPrefs.put("remote.enabled", true);

        // Until Bug 1999693 is resolved, this preference needs to be set to allow
        // Webdriver BiDi to automatically dismiss file pickers.
        defaultPrefs.put("remote.bidi.dismiss_file_pickers.enabled", true);

        defaultPrefs.put("security.certerrors.mitm.priming.enabled", false);

        defaultPrefs.put("security.fileuri.strict_origin_policy", false);

        defaultPrefs.put("security.notification_enable_delay", 0);

        defaultPrefs.put("services.settings.server", "http://" + server + "/dummy/blocklist/");

        defaultPrefs.put("signon.autofillForms", false);

        defaultPrefs.put("signon.rememberSignons", false);

        defaultPrefs.put("startup.homepage_welcome_url", "about:blank");

        defaultPrefs.put("startup.homepage_welcome_url.additional", "");

        defaultPrefs.put("toolkit.cosmeticAnimations.enabled", false);

        defaultPrefs.put("toolkit.startup.max_resumed_crashes", -1);
        return defaultPrefs;

    }

    @Override
    public List<String> defaultArgs(LaunchOptions options) {
        List<String> firefoxArguments = new ArrayList<>();
        if (Helper.isMac()) {
            firefoxArguments.add("--foreground");
        } else if (Helper.isWindows()) {
            firefoxArguments.add("--wait-for-browser");
        }
        if (StringUtil.isNotBlank(options.getUserDataDir())) {
            firefoxArguments.add("--profile");
            firefoxArguments.add(options.getUserDataDir());
        }
        if (options.getHeadless()) {
            firefoxArguments.add("--headless");
        }
        if (options.getDevtools()) {
            firefoxArguments.add("--devtools");
        }
        List<String> args;
        if (ValidateUtil.isNotEmpty(args = options.getArgs())) {
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    firefoxArguments.add("about:blank");
                    break;
                }
            }
            firefoxArguments.addAll(args);
        } else {
            firefoxArguments.add("about:blank");
        }
        return firefoxArguments;
    }

}
