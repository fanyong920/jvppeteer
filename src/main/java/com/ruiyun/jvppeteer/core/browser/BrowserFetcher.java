package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import com.sun.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ruiyun.jvppeteer.core.Constant.*;


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
     * <p>
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
     * <p>
     * {@link Constant#VERSION}有默认版本号
     * <p>
     * 第三个%s是压缩包名称 ,假如是win平台，那么第三个s%的值是
     * <blockquote><pre>
     *     chrome-win.zip
     * </pre></blockquote>
     * <p>
     * {@link BrowserFetcher#archiveName(String, String)} 用这个方法根据平台类型确定压缩包名称
     * <p>
     * 最后拼接成下载的url为https://registry.npmmirror.com/-/binary/chromium-browser-snapshots/Win_x64/722234/chrome-win.zip
     */
    public static final Map<String, Map<String, String>> downloadURLs = new HashMap<String, Map<String, String>>() {
        private static final long serialVersionUID = -6918778699407093058L;

        {
            put("chrome", new HashMap<String, String>() {
                private static final long serialVersionUID = 3441562966233820720L;

                {
                    put("host", "https://registry.npmmirror.com/-/binary");
                    put(LINUX, "%s/chrome-for-testing/%s/linux64/%s.zip");
                    put(MAC_ARM64, "%s/chrome-for-testing/%s/mac-arm64/%s.zip");
                    put(MAC_X64, "%s/chrome-for-testing/%s/mac-x64/%s.zip");
                    put(WIN32, "%s/chrome-for-testing/%s/win32/%s.zip");
                    put(WIN64, "%s/chrome-for-testing/%s/win64/%s.zip");
                }
            });
        }
    };

    /**
     * 平台 win linux mac
     */
    private String platform;

    /**
     * 下载的域名
     */
    private String downloadHost;

    /**
     * 下载的文件夹
     */
    private String downloadsFolder;

    /**
     * 目前支持两种产品：chrome or firefix
     */
    private String product;

    public BrowserFetcher() {
        this.product = "chrome";
        this.downloadsFolder = Helper.join(System.getProperty("user.dir"), ".local-browser");
        this.downloadHost = downloadURLs.get(this.product).get("host");
        setPlatform();
        ValidateUtil.notNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    private void setPlatform() {
        if (platform == null) {
            if (Platform.isMac())
                this.platform = Platform.is64Bit() ? MAC_ARM64 : MAC_X64;
            else if (Platform.isLinux())
                this.platform = LINUX;
            else if (Platform.isWindows())
                this.platform = Platform.is64Bit() ? WIN64 : WIN32;
            ValidateUtil.notNull(this.platform, "Unsupported platform: " + Helper.platform());
        }
    }

    /**
     * 创建 BrowserFetcher 对象
     *
     * @param projectRoot 根目录，储存浏览器得根目录
     * @param options     下载浏览器得一些配置
     */
    public BrowserFetcher(String projectRoot, FetcherOptions options) {
        this.product = (StringUtil.isNotEmpty(options.getProduct()) ? options.getProduct() : "chrome").toLowerCase();
        ValidateUtil.assertArg("chrome".equals(product) || "firefox".equals(product), "Unkown product: " + options.getProduct());
        this.downloadsFolder = StringUtil.isNotEmpty(options.getPath()) ? options.getPath() : Helper.join(projectRoot, ".local-browser");
        this.downloadHost = StringUtil.isNotEmpty(options.getHost()) ? options.getHost() : downloadURLs.get(this.product).get("host");
        this.platform = StringUtil.isNotEmpty(options.getPlatform()) ? options.getPlatform() : null;
        setPlatform();
        ValidateUtil.notNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    /**
     * <p>下载默认配置版本(722234)的浏览器，下载到项目目录下</p>
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     * @throws IOException          异常
     */
    public static RevisionInfo downloadIfNotExist() throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        return downloadIfNotExist(null);
    }

    /**
     * <p>下载浏览器，如果项目目录下不存在对应版本时</p>
     * <p>如果不指定版本，则使用默认配置版本</p>
     *
     * @param version 浏览器版本
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     * @throws IOException          异常
     */
    public static RevisionInfo downloadIfNotExist(String version) throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        BrowserFetcher fetcher = new BrowserFetcher();
        String downLoadVersion = StringUtil.isEmpty(version) ? Constant.VERSION : version;
        RevisionInfo revisionInfo = fetcher.revisionInfo(downLoadVersion);
        if (!revisionInfo.getLocal()) {
            return fetcher.download(downLoadVersion);
        }
        return revisionInfo;
    }

    /**
     * 检测对应的浏览器版本是否可以下载
     *
     * @param revision 浏览器版本
     * @param proxy    cant be null
     * @return boolean
     * @throws IOException 异常
     */
    public boolean canDownload(String revision, Proxy proxy) throws IOException {
        String url = getDownloadURL(this.product, this.platform, this.downloadHost, revision);
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
     * @param revision         浏览器版本
     * @return RevisionInfo
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    public RevisionInfo download(String revision) throws IOException, InterruptedException {
        String url = getDownloadURL(this.product, this.platform, this.downloadHost, revision);
        //存放浏览器得文件夹 带有平台标识和版本标识
        String folderPath = this.getFolderPath(revision);
        if (existsAsync(folderPath))
            return this.revisionInfo(revision);
        if (!(existsAsync(this.downloadsFolder))){
            mkdirAsync(this.downloadsFolder);
        }
        if(!existsAsync(folderPath)){
            mkdirAsync(folderPath);
        }
        int lastIndexOf = url.lastIndexOf("/");
        String archiveName = url.substring(lastIndexOf);
        //用shell下载，不用java代码下载了
        shell(url,folderPath,archiveName);
        RevisionInfo revisionInfo = this.revisionInfo(revision);
        if (revisionInfo != null) {
            try {
                File executableFile = new File(revisionInfo.getExecutablePath());
                executableFile.setExecutable(true, false);
            } catch (Exception e) {
                LOGGER.error("Set executablePath:{} file executation permission fail.", revisionInfo.getExecutablePath());
            }
        }
        return revisionInfo;
    }

    /**
     * 下载最新的浏览器版本（使用自带的下载回调）
     *
     * @return 浏览器版本
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    public RevisionInfo download() throws IOException, InterruptedException, URISyntaxException {
        return this.download(Constant.VERSION);
    }



    /**
     * 本地存在的浏览器版本
     *
     * @return 版本集合
     * @throws IOException 异常
     */
    public List<String> localRevisions() throws IOException {
        if (!existsAsync(this.downloadsFolder))
            return new ArrayList<>();
        Path path = Paths.get(this.downloadsFolder);
        Stream<Path> fileNames = this.readdirAsync(path);
        return fileNames.map(fileName -> parseFolderPath(this.product, fileName)).filter(entry -> entry != null && this.platform.equals(entry.getPlatform())).map(RevisionEntry::getRevision).collect(Collectors.toList());
    }

    /**
     * 删除指定版本的浏览器文件
     *
     * @param revision 版本
     * @throws IOException 异常
     */
    public void remove(String revision) throws IOException {
        String folderPath = this.getFolderPath(revision);
        ValidateUtil.assertArg(existsAsync(folderPath), "Failed to remove: revision " + revision + " is not downloaded");
        Files.delete(Paths.get(folderPath));
    }

    /**
     * 根据给定的浏览器产品以及文件夹解析浏览器版本和平台
     *
     * @param product    win linux mac
     * @param folderPath 文件夹路径
     * @return RevisionEntry 版本实体
     */
    private RevisionEntry parseFolderPath(String product, Path folderPath) {
        Path fileName = folderPath.getFileName();
        String[] split = fileName.toString().split("-");
        if (split.length != 2)
            return null;
        if (downloadURLs.get(product).get(split[0]) == null)
            return null;
        RevisionEntry entry = new RevisionEntry();
        entry.setPlatform(split[0]);
        entry.setProduct(product);
        entry.setRevision(split[1]);
        return entry;
    }

    /**
     * 静态内部类，描述谷歌版本相关内容,在这里
     * {@link BrowserFetcher#parseFolderPath(java.lang.String, java.nio.file.Path)}用到
     */
    public static class RevisionEntry {

        private String product;

        private String platform;

        private String revision;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            if (StringUtil.isNotEmpty(platform)) {
                this.platform = platform;
                return;
            }
            if (platform == null) {
                if (Platform.isMac())
                    this.platform = Platform.is64Bit() ? MAC_ARM64 : MAC_X64;
                else if (Platform.isLinux())
                    this.platform = LINUX;
                else if (Platform.isWindows())
                    this.platform = Platform.is64Bit() ? WIN64 : WIN32;
                ValidateUtil.notNull(this.platform, "Unsupported platform: " + Helper.platform());
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
     * 获取文件夹下所有项目，深度：一级
     *
     * @param downloadsFolder 下载文件夹
     * @return Stream<Path> Stream<Path>
     * @throws IOException 异常
     */
    private Stream<Path> readdirAsync(Path downloadsFolder) throws IOException {
        ValidateUtil.assertArg(Files.isDirectory(downloadsFolder), "downloadsFolder " + downloadsFolder + " is not Directory");
        return Files.list(downloadsFolder);
    }

    /**
     * 下载浏览器到具体的路径
     * ContentTypeapplication/x-zip-compressed
     *
     * @param url              url
     * @param archivePath      zip路径
     */
    private void shell(String url, String archivePath, String archiveName) throws IOException, InterruptedException {
        Process process;
        Path shellPath = null;
        BufferedReader stderrReader = null;
        BufferedReader stdoutReader = null;
        try {
            if(Platform.isLinux()){
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_LINUX);
                process = new ProcessBuilder("/bin/sh","-c", shellPath.toAbsolutePath() + " "+ archivePath + " " + url).redirectErrorStream(true).start();
            } else if (Platform.isWindows()) {
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_WIN);
                process = new ProcessBuilder("powershell.exe","-ExecutionPolicy","Bypass","-File", shellPath.toAbsolutePath().toString(),"-url",url,"-savePath",archivePath,"-archiveName",archiveName).redirectErrorStream(true).start();
            } else if (Platform.isMac()) {
                shellPath = copyShellFile(INSTALL_CHROME_FOR_TESTING_MAC);
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",shellPath.toAbsolutePath().toString()});
            }else {
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

            if(shellPath != null){
                shellPath.toFile().delete();
                shellPath.getParent().toFile().delete();
            }

        }
    }

    private Path copyShellFile(String path) throws IOException {
        Path tempDirectory = Paths.get(FileUtil.createProfileDir(SHELLS_PREFIX));
        Path shellPath = tempDirectory.resolve(path);
        if(Platform.isMac() || Platform.isLinux()){
            Files.createFile(shellPath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
        }else if(Platform.isWindows()){
            Files.createFile(shellPath);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(shellPath.toFile()));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream("/scripts/" + path))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }  finally {
            writer.close();
        }
        return shellPath;
    }

    /**
     * 创建文件夹
     *
     * @param folder 要创建的文件夹
     * @throws IOException 创建文件失败
     */
    private void mkdirAsync(String folder) throws IOException {
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
    public String getFolderPath(String revision) {
        return Paths.get(this.downloadsFolder, this.platform + "-" + revision).toString();
    }

    /**
     * 获取浏览器版本相关信息
     *
     * @param revision 版本
     * @return RevisionInfo
     */
    public RevisionInfo revisionInfo(String revision) {
        String folderPath = this.getFolderPath(revision);
        String executablePath;
        if ("chrome".equals(this.product)) {
            if (MAC_ARM64.equals(this.platform) || MAC_X64.equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform), "Chromium.app", "Contents", "MacOS", "Chromium");
            } else if (LINUX.equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform), "chrome");
            } else if (WIN32.equals(this.platform) || WIN64.equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform), "chrome.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if ("firefox".equals(this.product)) {
            if ("mac".equals(this.platform))
                executablePath = Helper.join(folderPath, "Firefox Nightly.app", "Contents", "MacOS", "firefox");
            else if ("linux".equals(this.platform))
                executablePath = Helper.join(folderPath, "firefox", "firefox");
            else if ("win32".equals(this.platform) || "win64".equals(this.platform))
                executablePath = Helper.join(folderPath, "firefox", "firefox.exe");
            else
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
        } else {
            throw new IllegalArgumentException("Unsupported product: " + this.product);
        }
        String url = getDownloadURL(this.product, this.platform, this.downloadHost, revision);
        boolean local = this.existsAsync(folderPath);
        LOGGER.info("revision:{}，executablePath:{}，folderPath:{}，local:{}，url:{}，product:{}", revision, executablePath, folderPath, local, url, this.product);
        return new RevisionInfo(revision, executablePath, folderPath, local, url, this.product);
    }

    /**
     * 检测给定的路径是否存在
     *
     * @param filePath 文件路径
     * @return boolean
     */
    public boolean existsAsync(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 根据平台信息和版本信息确定要下载的浏览器压缩包名称
     *
     * @param product  产品
     * @param platform 平台
     * @return 压缩包名字
     */
    public String archiveName(String product, String platform) {
        if ("chrome".equals(product)) {
            if (LINUX.equals(platform))
                return "chrome-linux64";
            if (MAC_ARM64.equals(platform))
                return "chrome-mac-arm64";
            if (MAC_X64.equals(platform))
                return "chrome-mac-x64";
            if (WIN32.equals(platform) )
                return  "chrome-win32";
            if (WIN64.equals(platform))
                return "chrome-win64";
        } else if ("firefox".equals(product)) {
            if ("linux".equals(platform))
                return "firefox-linux";
            if ("mac".equals(platform))
                return "firefox-mac";
            if ("win32".equals(platform) || "win64".equals(platform))
                return "firefox-" + platform;
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
    public String getDownloadURL(String product, String platform, String host, String revision) {
        return String.format(downloadURLs.get(product).get(platform), host, revision, archiveName(product, platform));
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

    public String product() {
        return product;
    }

}
