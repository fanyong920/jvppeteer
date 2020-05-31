package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.util.*;
import com.sun.javafx.PlatformUtil;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * 用于下载chrome浏览器
 */
public class BrowserFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserFetcher.class);

    public static final Map<String, Map<String, String>> downloadURLs = new HashMap<String, Map<String, String>>() {
        private static final long serialVersionUID = -6918778699407093058L;

        {
            put("chrome", new HashMap<String, String>() {
                private static final long serialVersionUID = 3441562966233820720L;

                {
                    put("host", "https://npm.taobao.org/mirrors");
                    put("linux", "%s/chromium-browser-snapshots/Linux_x64/%s/%s.zip");
                    put("mac", "%s/chromium-browser-snapshots/Mac/%s/%s.zip");
                    put("win32", "%s/chromium-browser-snapshots/Win/%s/%s.zip");
                    put("win64", "%s/chromium-browser-snapshots/Win_x64/%s/%s.zip");
                }
            });
            put("firefox", new HashMap<String, String>() {
                private static final long serialVersionUID = 2053771138227029401L;

                {
                    put("host", "https://github.com/puppeteer/juggler/releases");
                    put("linux", "%s/download/%s/%s.zip");
                    put("mac", "%s/download/%s/%s.zip");
                    put("win32", "%s/download/%s/%s.zip");
                    put("win64", "%s/download/%s/%s.zip");
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
        if (platform == null) {
            if (PlatformUtil.isMac())
                this.platform = "mac";
            else if (PlatformUtil.isLinux())
                this.platform = "linux";
            else if (PlatformUtil.isWindows())
                this.platform = Helper.isWin64() ? "win64" : "win32";
            ValidateUtil.notNull(this.platform, "Unsupported platform: " + Helper.paltform());
        }
        ValidateUtil.notNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    /**
     * 检测对应的浏览器版本是否可以下载
     *
     * @param revision 浏览器版本
     * @param proxy    cant be null
     * @return boolean
     */
    public boolean canDownload(String revision, Proxy proxy) throws IOException {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
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
     * @param progressCallback 下载回调
     * @return RevisionInfo
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    public RevisionInfo download(String revision, BiConsumer<Integer, Integer> progressCallback) throws IOException, InterruptedException, ExecutionException {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        int lastIndexOf = url.lastIndexOf("/");
        String archivePath = Helper.join(this.downloadsFolder, url.substring(lastIndexOf));
        String folderPath = this.getFolderPath(revision);
        if (existsAsync(folderPath))
            return this.revisionInfo(revision);
        if (!(existsAsync(this.downloadsFolder)))
            mkdirAsync(this.downloadsFolder);
        try {
            if (progressCallback == null) {
                progressCallback = defaultDownloadCallback();
            }

            downloadFile(url, archivePath, progressCallback);
            install(archivePath, folderPath);
        } finally {
            unlinkAsync(archivePath);
        }
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
     * 指定版本下载chromuim
     * @param revision 版本
     * @return 下载后的chromuim包有关信息
     * @throws IOException 异常
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public RevisionInfo download(String revision) throws IOException, InterruptedException, ExecutionException {
       return this.download(revision,null);
    }
    /**
     * 默认的下载回调
     *
     * @return 回调函数
     */
    private BiConsumer<Integer, Integer> defaultDownloadCallback() {
        return (integer1, integer2) -> {
            BigDecimal decimal1 = new BigDecimal(integer1);
            BigDecimal decimal2 = new BigDecimal(integer2);
            int percent = decimal1.divide(decimal2).multiply(new BigDecimal(100)).intValue();
            LOGGER.info("Download progress: total[{}],downloaded[{}],{}", decimal2, decimal1, percent + "%");
            //System.out.println("Download progress: total[" + decimal2 + "],downloaded[" + decimal1 + "]," + percent + "%");
        };
    }

    /**
     * 下载最新的浏览器版本
     *
     * @param progressCallback 下载回调
     * @return 浏览器版本
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public RevisionInfo download(BiConsumer<Integer, Integer> progressCallback) throws IOException, InterruptedException, ExecutionException {
        return this.download(fetchRevision(), progressCallback);
    }

    /**
     * 下载最新的浏览器版本（使用自带的下载回调）
     *
     * @return 浏览器版本
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public RevisionInfo download() throws IOException, InterruptedException, ExecutionException {
        return this.download(fetchRevision(), null);
    }

    private String fetchRevision() throws IOException {
        String downloadUrl = downloadURLs.get(product).get(platform);
        URL urlSend = new URL(String.format(downloadUrl.substring(0, downloadUrl.length() - 9), this.downloadHost));
        URLConnection conn = urlSend.openConnection();
        conn.setConnectTimeout(DownloadUtil.CONNECT_TIME_OUT);
        conn.setReadTimeout(DownloadUtil.READ_TIME_OUT);
        String pageContent = StreamUtil.toString(conn.getInputStream());
        return parseRevision(pageContent);
    }

    /**
     * 解析得到最新的浏览器版本
     *
     * @param pageContent 页面内容
     * @return 浏览器版本
     */
    private String parseRevision(String pageContent) {
        String result = null;
        Pattern pattern = Pattern.compile("<a href=\"/mirrors/chromium-browser-snapshots/(.*)?/\">");
        Matcher matcher = pattern.matcher(pageContent);
        while (matcher.find()) {
            result = matcher.group(1);
        }
        String[] split = result.split("/");
        if (split.length == 2) {
            result = split[1];
        } else {
            throw new RuntimeException("cant't find latest revision from pageConten:" + pageContent);
        }
        return result;
    }

    /**
     * 本地存在的浏览器版本
     *
     * @return Set<String>
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
     * @return RevisionEntry RevisionEntry
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
                if (PlatformUtil.isMac())
                    this.platform = "mac";
                else if (PlatformUtil.isLinux())
                    this.platform = "linux";
                else if (PlatformUtil.isWindows())
                    this.platform = Helper.isWin64() ? "win64" : "win32";
                ValidateUtil.notNull(this.platform, "Unsupported platform: " + Helper.paltform());
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
        ValidateUtil.assertArg(Files.isDirectory(downloadsFolder), "downloadsFolder " + downloadsFolder.toString() + " is not Directory");
        return Files.list(downloadsFolder);
    }

    /**
     * 修改文件权限，与linux上chmod命令一样，并非异步，只是方法名为了与nodejs的puppeteer库一样
     *
     * @param executablePath 执行路径
     * @param perms          权限字符串，例如"775",与linux上文件权限一样
     * @throws IOException 异常
     */
    private void chmodAsync(String executablePath, String perms) throws IOException {
        Helper.chmod(executablePath, perms);
    }

    /**
     * 删除压缩文件
     *
     * @param archivePath zip路径
     * @throws IOException 异常
     */
    private void unlinkAsync(String archivePath) throws IOException {
        Files.deleteIfExists(Paths.get(archivePath));
    }

    /**
     * intall archive file: *.zip,*.tar.bz2,*.dmg
     *
     * @param archivePath zip路径
     * @param folderPath  存放的路径
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void install(String archivePath, String folderPath) throws IOException, InterruptedException {
        LOGGER.info("Installing " + archivePath + " to " + folderPath);
        if (archivePath.endsWith(".zip")) {
            extractZip(archivePath, folderPath);
        } else if (archivePath.endsWith(".tar.bz2")) {
            extractTar(archivePath, folderPath);
        } else if (archivePath.endsWith(".dmg")) {
            mkdirAsync(folderPath);
            installDMG(archivePath, folderPath);
        } else {
            throw new IllegalArgumentException("Unsupported archive format: " + archivePath);
        }
    }

    /**
     * mount and copy
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @return string
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private String mountAndCopy(String archivePath, String folderPath) throws IOException, InterruptedException {
        String mountPath = null;
        BufferedReader reader = null;
        String line;
        StringWriter stringWriter = null;
        try {
            List<String> arguments = new ArrayList<>();
            arguments.add("hdiutil");
            arguments.add("attach");
            arguments.add("-nobrowse");
            arguments.add("-noautoopen");
            arguments.add(archivePath);
            ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Pattern pattern = Pattern.compile("/Volumes/(.*)", Pattern.MULTILINE);
            stringWriter = new StringWriter();
            while ((line = reader.readLine()) != null) {
                stringWriter.write(line);
            }
            process.waitFor();
            process.destroyForcibly();
            Matcher matcher = pattern.matcher(stringWriter.toString());
            while (matcher.find()) {
                mountPath = matcher.group();
            }
        } finally {
            StreamUtil.closeQuietly(reader);
            StreamUtil.closeQuietly(stringWriter);
        }
        if (StringUtil.isEmpty(mountPath)) {
            throw new RuntimeException("Could not find volume path in [" + stringWriter.toString() + "]");
        }
        Optional<Path> optionl = this.readdirAsync(Paths.get(mountPath)).filter(item -> item.toString().endsWith(".app")).findFirst();
        if (optionl.isPresent()) {
            try {
                Path path = optionl.get();
                String copyPath = path.toString();
                LOGGER.info("Copying " + copyPath + " to " + folderPath);
                List<String> arguments = new ArrayList<>();
                arguments.add("cp");
                arguments.add("-R");
                arguments.add(copyPath);
                arguments.add(folderPath);
                ProcessBuilder processBuilder2 = new ProcessBuilder().command(arguments);
                Process process2 = processBuilder2.start();
                reader = new BufferedReader(new InputStreamReader(process2.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(line);
                    }
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(process2.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    LOGGER.error(line);
                }
                process2.waitFor();
                process2.destroyForcibly();
            } finally {
                StreamUtil.closeQuietly(reader);
            }
        }
        return mountPath;
    }

    /**
     * Install *.app directory from dmg file
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void installDMG(String archivePath, String folderPath) throws IOException, InterruptedException {
        String mountPath = null;
        try {
            mountPath = mountAndCopy(archivePath, folderPath);
        } finally {
            unmount(mountPath);
        }
        throw new RuntimeException("Cannot find app in " + mountPath);
    }

    /**
     * unmount finally
     *
     * @param mountPath mount Path
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void unmount(String mountPath) throws IOException, InterruptedException {
        BufferedReader reader = null;
        if (StringUtil.isNotEmpty(mountPath)) {
            List<String> arguments = new ArrayList<>();
            arguments.add("hdiutil");
            arguments.add("detach");
            arguments.add(mountPath);
            arguments.add("-quiet");
            try {
                ProcessBuilder processBuilder3 = new ProcessBuilder().command(arguments);
                Process process3 = processBuilder3.start();
                LOGGER.info("Unmounting " + mountPath);
                String line;
                reader = new BufferedReader(new InputStreamReader(process3.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(line);
                    }
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(process3.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    LOGGER.error(line);
                }
                process3.waitFor();
                process3.destroyForcibly();
            } finally {
                StreamUtil.closeQuietly(reader);
            }
        }
    }

    /**
     * 解压tar文件
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException 异常
     */
    private void extractTar(String archivePath, String folderPath) throws IOException {
        BufferedOutputStream wirter = null;
        BufferedInputStream reader = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(archivePath));
            ArchiveEntry nextEntry;
            while ((nextEntry = tarArchiveInputStream.getNextEntry()) != null) {
                String name = nextEntry.getName();
                Path path = Paths.get(folderPath, name);
                File file = path.toFile();
                if (nextEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    reader = new BufferedInputStream(tarArchiveInputStream);
                    int bufferSize = 8192;
                    int perReadcount;
                    FileUtil.createNewFile(file);
                    byte[] buffer = new byte[bufferSize];
                    wirter = new BufferedOutputStream(new FileOutputStream(file));
                    while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
                        wirter.write(buffer, 0, perReadcount);
                    }
                    wirter.flush();
                }
            }
        } finally {
            StreamUtil.closeQuietly(wirter);
            StreamUtil.closeQuietly(reader);
            StreamUtil.closeQuietly(tarArchiveInputStream);
        }
    }

    /**
     * 解压zip文件
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException 异常
     */
    private void extractZip(String archivePath, String folderPath) throws IOException {
        BufferedOutputStream wirter = null;
        BufferedInputStream reader = null;
        ZipFile zipFile = new ZipFile(archivePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                Path path = Paths.get(folderPath, name);
                if (zipEntry.isDirectory()) {
                    path.toFile().mkdirs();
                } else {
                    reader = new BufferedInputStream(zipFile.getInputStream(zipEntry));

                    int perReadcount;
                    byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
                    wirter = new BufferedOutputStream(new FileOutputStream(path.toString()));
                    while ((perReadcount = reader.read(buffer, 0, Constant.DEFAULT_BUFFER_SIZE)) != -1) {
                        wirter.write(buffer, 0, perReadcount);
                    }
                    wirter.flush();
                }

            }
        } finally {
            StreamUtil.closeQuietly(wirter);
            StreamUtil.closeQuietly(reader);
            StreamUtil.closeQuietly(zipFile);
        }
    }

    /**
     * 下载浏览器到具体的路径
     * ContentTypeapplication/x-zip-compressed
     *
     * @param url              url
     * @param archivePath      zip路径
     * @param progressCallback 回调函数
     */
    private void downloadFile(String url, String archivePath, BiConsumer<Integer, Integer> progressCallback) throws IOException, ExecutionException, InterruptedException {
        LOGGER.info("Downloading binary from " + url);
        DownloadUtil.download(url, archivePath, progressCallback);
        LOGGER.info("Download successfully from " + url);
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
            if ("mac".equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform, revision), "Chromium.app", "Contents", "MacOS", "Chromium");
            } else if ("linux".equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform, revision), "chrome");
            } else if ("win32".equals(this.platform) || "win64".equals(this.platform)) {
                executablePath = Helper.join(folderPath, archiveName(this.product, this.platform, revision), "chrome.exe");
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
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
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
     * 根据平台信息和版本信息确定要下载的浏览器压缩包
     *
     * @param product  产品
     * @param platform 平台
     * @param revision 版本
     * @return 压缩包名字
     */
    public String archiveName(String product, String platform, String revision) {
        if ("chrome".equals(product)) {
            if ("linux".equals(platform))
                return "chrome-linux";
            if ("mac".equals(platform))
                return "chrome-mac";
            if ("win32".equals(platform) || "win64".equals(platform)) {
                // Windows archive name changed at r591479.
                return Integer.parseInt(revision, 10) > 591479 ? "chrome-win" : "chrome-win32";
            }
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
     * 确定下载的路径
     *
     * @param product  产品：chrome or firefox
     * @param platform win linux mac
     * @param host     域名地址
     * @param revision 版本
     * @return 下载浏览器的url
     */
    public String downloadURL(String product, String platform, String host, String revision) {
        return String.format(downloadURLs.get(product).get(platform), host, revision, archiveName(product, platform, revision));
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
