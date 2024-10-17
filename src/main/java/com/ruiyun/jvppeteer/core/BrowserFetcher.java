package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.entities.FetcherOptions;
import com.ruiyun.jvppeteer.entities.RevisionInfo;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ruiyun.jvppeteer.common.Constant.INSTALL_CHROME_FOR_TESTING_LINUX;
import static com.ruiyun.jvppeteer.common.Constant.INSTALL_CHROME_FOR_TESTING_MAC;
import static com.ruiyun.jvppeteer.common.Constant.INSTALL_CHROME_FOR_TESTING_WIN;
import static com.ruiyun.jvppeteer.common.Constant.SHELLS_PREFIX;


/**
 * 用于下载chrome浏览器
 */
public class BrowserFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserFetcher.class);
    private static final String LINUX = "linux64";
    private static final String MAC_ARM64 = "mac-arm64";
    private static final String MAC_X64 = "mac-x64";
    private static final String WIN32 = "win32";
    private static final String WIN64 = "win64";
    /**
     * 该map装有默认的下载chrome的host及不同平台的下载路径，最后拼接成下载的url
     * <p>
     * 比如 host 如下
     * <blockquote><pre>
     *    https://registry.npmmirror.com/-/binary
     * </pre></blockquote><p>
     * 如果是win64平台，那么下载路径
     * <blockquote><pre>
     *     %s/chromium-browser-snapshots/Win_x64/%s/%s.zip
     * </pre></blockquote>
     * <p>
     * 下载路径中第一个 %s是host，第一个%s的值是
     * <blockquote><pre>
     *     https://registry.npmmirror.com/-/binary
     * </pre></blockquote>
     * <p>
     * 第二个%s版本号，假如版本号是722234，那么第二个s%的值是
     * <blockquote><pre>
     *     722234
     * </pre></blockquote>
     * {@link Constant#VERSION}有默认版本号
     * <p>
     * 第三个%s是压缩包名称 ,假如是win平台，那么第三个s%的值是
     * <blockquote><pre>
     *     chrome-win.zip
     * </pre></blockquote>
     * {@link BrowserFetcher#archive(Product, String, String)} 用这个方法根据平台类型确定压缩包名称
     * <p>
     * 最后拼接成下载的url为https://registry.npmmirror.com/-/binary/chromium-browser-snapshots/Win_x64/722234/chrome-win.zip
     */
    public static final Map<Product, Map<String, String>> downloadURLs = new HashMap<>();

    static {
        Map<String, String> chrome = new HashMap<>();
        chrome.put("host", "https://storage.googleapis.com");
        chrome.put(LINUX, "%s/chrome-for-testing-public/%s/linux64/%s.zip");
        chrome.put(MAC_ARM64, "%s/chrome-for-testing-public/%s/mac-arm64/%s.zip");
        chrome.put(MAC_X64, "%s/chrome-for-testing-public/%s/mac-x64/%s.zip");
        chrome.put(WIN32, "%s/chrome-for-testing-public/%s/win32/%s.zip");
        chrome.put(WIN64, "%s/chrome-for-testing-public/%s/win64/%s.zip");
        downloadURLs.put(Product.CHROME, chrome);

        Map<String, String> chromium = new HashMap<>();
        chromium.put("host", "https://storage.googleapis.com");
        chromium.put(LINUX, "%s/chromium-browser-snapshots/Linux_x64/%s/%s.zip");
        chromium.put(MAC_ARM64, "%s/chromium-browser-snapshots/Mac_Arm/%s/%s.zip");
        chromium.put(MAC_X64, "%s/chromium-browser-snapshots/Mac/%s/%s.zip");
        chromium.put(WIN32, "%s/chromium-browser-snapshots/Win/%s/%s.zip");
        chromium.put(WIN64, "%s/chromium-browser-snapshots/Win_x64/%s/%s.zip");
        downloadURLs.put(Product.CHROMIUM, chromium);


        Map<String, String> chromedriver = new HashMap<>();
        chromedriver.put("host", "https://storage.googleapis.com");
        chromedriver.put(LINUX, "%s/chrome-for-testing-public/%s/linux64/%s.zip");
        chromedriver.put(MAC_ARM64, "%s/chrome-for-testing-public/%s/mac-arm64/%s.zip");
        chromedriver.put(MAC_X64, "%s/chrome-for-testing-public/%s/mac-x64/%s.zip");
        chromedriver.put(WIN32, "%s/chrome-for-testing-public/%s/win32/%s.zip");
        chromedriver.put(WIN64, "%s/chrome-for-testing-public/%s/win64/%s.zip");
        downloadURLs.put(Product.CHROMEDRIVER, chromedriver);

        Map<String, String> chrome_headless_shell = new HashMap<>();
        chrome_headless_shell.put("host", "https://storage.googleapis.com");
        chrome_headless_shell.put(LINUX, "%s/chrome-for-testing-public/%s/linux64/%s.zip");
        chrome_headless_shell.put(MAC_ARM64, "%s/chrome-for-testing-public/%s/mac-arm64/%s.zip");
        chrome_headless_shell.put(MAC_X64, "%s/chrome-for-testing-public/%s/mac-x64/%s.zip");
        chrome_headless_shell.put(WIN32, "%s/chrome-for-testing-public/%s/win32/%s.zip");
        chrome_headless_shell.put(WIN64, "%s/chrome-for-testing-public/%s/win64/%s.zip");
        downloadURLs.put(Product.CHROMEHEADLESSSHELL, chrome_headless_shell);
    }

    private final String version;


    /**
     * 平台 win linux mac
     */
    private String platform;

    /**
     * 下载的域名
     */
    private final String downloadHost;

    /**
     * 下载的文件夹
     */
    private String downloadsFolder;

    /**
     * 目前支持两种产品：chrome or firefix
     */
    private final Product product;
    /**
     * 目前支持两种产品：chrome or firefix
     */
    private Proxy proxy;

    BrowserFetcher() {
        this.product = Product.CHROME;
        this.downloadsFolder = Helper.join(System.getProperty("user.dir"), ".local-browser");
        this.downloadHost = downloadURLs.get(this.product).get("host");
        this.version = Constant.VERSION;
        detectBrowserPlatform();
         Objects.requireNonNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    /**
     * 创建 BrowserFetcher 对象
     *
     * @param options 下载浏览器得一些配置
     */
    public BrowserFetcher(FetcherOptions options) {
        this.product = options.getProduct() != null ? options.getProduct() : Product.CHROME;
        this.downloadsFolder = options.getCacheDir();
        this.downloadHost = StringUtil.isNotEmpty(options.getHost()) ? options.getHost() : downloadURLs.get(this.product).get("host");
        this.platform = StringUtil.isNotEmpty(options.getPlatform()) ? options.getPlatform() : detectBrowserPlatform();
        this.version = options.getVersion();
        this.proxy = options.getProxy();
         Objects.requireNonNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    private static String detectBrowserPlatform() {
        if (Helper.isMac())
            return Helper.is64() ? MAC_ARM64 : MAC_X64;
        else if (Helper.isLinux())
            return LINUX;
        else if (Helper.isWindows())
            return Helper.is64() ? WIN64 : WIN32;
        else
            throw new JvppeteerException("Unsupported platform: " + Helper.platform());
    }


    /**
     * <p>下载浏览器，如果项目目录下不存在对应版本时</p>
     * <p>如果不指定版本，则使用默认配置版本</p>
     *
     * @return 浏览器本地信息
     * @throws InterruptedException 异常
     * @throws IOException          异常
     */
    public RevisionInfo downloadBrowser() throws InterruptedException, IOException {
        ValidateUtil.assertArg(StringUtil.isNotBlank(version), "Browser version must be specified");
        RevisionInfo revisionInfo = this.revisionInfo(this.version);
        if (!revisionInfo.getLocal()) {
            return this.download(this.version);
        }
        return revisionInfo;
    }

    /**
     * 检测对应的浏览器版本是否可以下载
     *
     * @param url   下载地址
     * @param proxy cant be null
     * @return boolean 是否能下载
     * @throws IOException 异常
     */
    public boolean canDownload(String url, Proxy proxy) throws IOException {
        return httpRequest(proxy, url, "HEAD");
    }

    /**
     * 发送一个http请求
     *
     * @param proxy  代理 可以为null
     * @param url    请求的url
     * @param method 请求方法 get post head
     * @return boolean
     */
    private boolean httpRequest(Proxy proxy, String url, String method) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL urlSend = new URL(url);
            if (proxy != null) {
                conn = (HttpURLConnection) urlSend.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) urlSend.openConnection();
            }
            conn.setRequestMethod(method);
            conn.connect();
            if (conn.getResponseCode() >= 300 && conn.getResponseCode() <= 400 && StringUtil.isNotEmpty(conn.getHeaderField("Location"))) {
                httpRequest(proxy, conn.getHeaderField("Location"), method);
            } else {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }

    /**
     * 根据给定得浏览器版本下载浏览器，可以利用下载回调显示下载进度
     *
     * @param revision 浏览器版本
     * @return RevisionInfo
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private RevisionInfo download(String revision) throws IOException, InterruptedException {
        String url = getDownloadURL(this.product, this.platform, this.downloadHost, revision);
        boolean canDownload = this.canDownload(url, this.proxy);
        if (!canDownload) {
            throw new JvppeteerException("The URL: " + url + " cannot be downloaded");
        }
        //存放浏览器得文件夹 带有平台标识和版本标识
        String folderPath = this.relativeVersionPath(revision);
        if (!(exists(this.downloadsFolder))) {
            mkdir(this.downloadsFolder);
        }
        if (!exists(folderPath)) {
            mkdir(folderPath);
        }
        //用shell下载，不用java代码下载了
        shell(url, folderPath, archive(this.product, this.platform, revision), executableName());
        RevisionInfo revisionInfo = this.revisionInfo(revision);
        if (revisionInfo != null) {
            File executableFile = new File(revisionInfo.getExecutablePath());
            executableFile.setExecutable(true, false);
        }
        return revisionInfo;
    }

    /**
     * 本地存在的浏览器版本
     *
     * @return 版本集合
     * @throws IOException 异常
     */
    public List<String> localRevisions() throws IOException {
        if (!exists(this.downloadsFolder))
            return new ArrayList<>();
        return Files.list(Paths.get(this.downloadsFolder)).map(revisionsPath -> parseRevisionsPath(this.product, revisionsPath)).filter(entry -> entry != null && this.platform.equals(entry.getPlatform())).map(RevisionEntry::getRevision).collect(Collectors.toList());
    }

    /**
     * 删除指定版本的浏览器文件
     *
     * @param revision 版本
     * @throws IOException 异常
     */
    public void remove(String revision) throws IOException {
        String folderPath = this.relativeVersionPath(revision);
        ValidateUtil.assertArg(exists(folderPath), "Failed to remove: revision " + revision + " is not downloaded");
        Files.delete(Paths.get(folderPath));
    }

    /**
     * 根据给定的浏览器产品以及版本路径解析得到浏览器版本和平台
     *
     * @param product       win linux mac
     * @param revisionsPath 版本路径
     * @return RevisionEntry 浏览器版本信息的实体
     */
    private RevisionEntry parseRevisionsPath(Product product, Path revisionsPath) {
        Path fileName = revisionsPath.getFileName();
        String[] split = fileName.toString().split("-");

        if (split.length != 2)
            return null;

        if (downloadURLs.get(product).get(split[0]) == null)
            return null;

        if (product.equals(Product.CHROME) || product.equals(Product.CHROMEHEADLESSSHELL) || product.equals(Product.CHROMEDRIVER)) {
            Pattern pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+(.\\d+)?$");
            Matcher matcher = pattern.matcher(split[1]);
            if (!matcher.matches())
                return null;
        }
        if (product.equals(Product.CHROMIUM)) {
            Pattern pattern = Pattern.compile("^\\d+$");
            Matcher matcher = pattern.matcher(split[1]);
            if (!matcher.matches())
                return null;
        }

        try {
            List<Path> products = Files.list(revisionsPath).filter(path -> path.getFileName().toString().contains(archive(product, split[0], split[1]))).collect(Collectors.toList());
            if (products.isEmpty()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        RevisionEntry entry = new RevisionEntry();
        entry.setPlatform(split[0]);
        entry.setProduct(product);
        entry.setRevision(split[1]);
        return entry;
    }

    /**
     * 静态内部类，描述谷歌版本相关内容,在这里
     */
    public static class RevisionEntry {

        private Product product;

        private String platform;

        private String revision;

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            if (StringUtil.isNotEmpty(platform)) {
                this.platform = platform;
            } else {
                this.platform = BrowserFetcher.detectBrowserPlatform();
            }
        }

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }
    }

    /**
     * 下载浏览器到具体的路径
     *
     * @param url        url
     * @param folderPath 安装路径
     */
    private void shell(String url, String folderPath, String archiveName, String executableName) throws IOException, InterruptedException {
        Process process;
        Path shellPath = null;
        BufferedReader stderrReader = null;
        BufferedReader stdoutReader = null;
        try {
            if (Helper.isLinux()) {
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_LINUX);
                process = new ProcessBuilder("/bin/sh", "-c", shellPath.toAbsolutePath() + " " + folderPath + " " + url + " " + archiveName + " " + executableName).redirectErrorStream(true).start();
            } else if (Helper.isWindows()) {
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_WIN);
                process = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", shellPath.toAbsolutePath().toString(), "-url", url, "-savePath", folderPath, "-archive", archiveName, "-executableName", executableName).redirectErrorStream(true).start();
            } else if (Helper.isMac()) {
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_MAC);
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shellPath.toAbsolutePath().toString()});
            } else {
                throw new JvppeteerException("Unsupported platform: " + Helper.platform());
            }
            // 读取输出流
            if (process != null) {
                stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
                String stdoutLine;
                while ((stdoutLine = stdoutReader.readLine()) != null) {
                    LOGGER.info(stdoutLine);
                }
                // 等待进程完成
                boolean exitCode = process.waitFor(10L, TimeUnit.MINUTES);
                if (!exitCode) {
                    process.destroy();
                    throw new JvppeteerException("install chrome for testing failed");
                }
            }
        } finally {
            // 关闭资源
            if (stdoutReader != null) {
                stdoutReader.close();
            }
            if (stderrReader != null) {
                stderrReader.close();
            }
            if (shellPath != null) {
                shellPath.toFile().delete();
                shellPath.getParent().toFile().delete();
            }
        }
    }

    private String executableName() {
        String executableName;
        if (Product.CHROME.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executableName = "Google Chrome for Testing";
            } else if (LINUX.equals(this.platform)) {
                executableName = "chrome";
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executableName = "chrome.exe";
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMIUM.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executableName = "Chromium";
            } else if (LINUX.equals(this.platform)) {
                executableName = "chrome";
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executableName = "chrome.exe";
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMEDRIVER.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executableName = "chromedriver";
            } else if (LINUX.equals(this.platform)) {
                executableName = "chromedriver";
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executableName = "chromedriver.exe";
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMEHEADLESSSHELL.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executableName = "chrome-headless-shell";
            } else if (LINUX.equals(this.platform)) {
                executableName = "chrome-headless-shell";
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executableName = "chrome-headless-shell.exe";
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else {
            throw new IllegalArgumentException("Unsupported product: " + this.product);
        }
        return executableName;
    }

    private Path copyShellFile(String path) throws IOException {
        Path tempDirectory = Paths.get(FileUtil.createProfileDir(SHELLS_PREFIX));
        Path shellPath = tempDirectory.resolve(path);
        if (Helper.isMac() || Helper.isLinux()) {
            Files.createFile(shellPath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
        } else if (Helper.isWindows()) {
            Files.createFile(shellPath);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(shellPath.toFile())); BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream("/scripts/" + path))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
        return shellPath;
    }

    /**
     * 创建文件夹
     *
     * @param folder 要创建的文件夹
     * @throws IOException 创建文件失败
     */
    private void mkdir(String folder) throws IOException {
        File file = new File(folder);
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }

    /**
     * 根据浏览器版本获取对应浏览器路径
     *
     * @param revision 浏览器版本
     * @return string
     */
    public String relativeVersionPath(String revision) {
        return Helper.join(this.downloadsFolder, this.platform + "-" + revision);
    }

    /**
     * 获取浏览器版本相关信息
     *
     * @param revision 版本
     * @return RevisionInfo
     */
    public RevisionInfo revisionInfo(String revision) {
        String versionPath = this.relativeVersionPath(revision);
        String executablePath;
        if (Product.CHROME.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "Google Chrome for Testing.app", "Contents", "MacOS", "Google Chrome for Testing");
            } else if (LINUX.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome");
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMIUM.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "Chromium.app", "Contents", "MacOS", "Chromium");
            } else if (LINUX.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome");
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMEDRIVER.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chromedriver");
            } else if (LINUX.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chromedriver");
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chromedriver.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if (Product.CHROMEHEADLESSSHELL.equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome-headless-shell");
            } else if (LINUX.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome-headless-shell");
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executablePath = Helper.join(versionPath, archive(this.product, this.platform, revision), "chrome-headless-shell.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else {
            throw new IllegalArgumentException("Unsupported product: " + this.product);
        }
        String url = getDownloadURL(this.product, this.platform, this.downloadHost, revision);
        boolean local = this.exists(executablePath);
        LOGGER.info("revision:{}, executablePath:{}, folderPath:{}, local:{}, url:{}, product:{}", revision, executablePath, versionPath, local, url, this.product);
        return new RevisionInfo(revision, executablePath, versionPath, local, url, this.product);
    }

    /**
     * 检测给定的路径是否存在
     *
     * @param filePath 文件路径
     * @return boolean
     */
    public boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 根据平台信息和版本信息确定要下载的浏览器压缩包名称
     *
     * @param product  产品
     * @param platform 平台
     * @param version  版本
     * @return 压缩包名字
     */
    public String archive(Product product, String platform, String version) {
        boolean match = LINUX.equals(platform) || MAC_X64.equals(platform) || MAC_ARM64.equals(platform) || WIN32.equals(platform) || WIN64.equals(platform);
        if (Product.CHROME.equals(product)) {
            if (match)
                return "chrome-" + platform;
            else throw new JvppeteerException("Unsupported platform: " + platform);
        } else if (Product.CHROMIUM.equals(product)) {
            if (LINUX.equals(platform))
                return "chrome-linux";
            if (MAC_ARM64.equals(platform) || MAC_X64.equals(platform))
                return "chrome-mac";
            if (WIN32.equals(platform) || WIN64.equals(platform)) {
                if (Integer.parseInt(version, 10) > 591479)
                    return "chrome-win";
                else
                    return "chrome-win32";
            } else throw new JvppeteerException("Unsupported platform: " + platform);
        } else if (Product.CHROMEDRIVER.equals(product)) {
            if (match)
                return "chromedriver-" + platform;
            else throw new JvppeteerException("Unsupported platform: " + platform);
        } else if (Product.CHROMEHEADLESSSHELL.equals(product)) {
            if (match)
                return "chrome-headless-shell-" + platform;
            else throw new JvppeteerException("Unsupported platform: " + platform);
        }
        return null;
    }

    /**
     * 将几个字符串拼接成浏览器的下载路径
     *
     * @param product  产品：chrome or firefox
     * @param platform win linux mac
     * @param host     域名地址
     * @param revision 版本
     * @return 下载浏览器的url
     */
    public String getDownloadURL(Product product, String platform, String host, String revision) {
        return String.format(downloadURLs.get(product).get(platform), host, revision, archive(product, platform, revision));
    }

    public String host() {
        return downloadHost;
    }


    public String platform() {
        return platform;
    }

    public String getDownloadsFolder() {
        return downloadsFolder;
    }

    public void setDownloadsFolder(String downloadsFolder) {
        this.downloadsFolder = downloadsFolder;
    }

    public Product product() {
        return product;
    }

}
