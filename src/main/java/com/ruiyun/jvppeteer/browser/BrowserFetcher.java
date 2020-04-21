package com.ruiyun.jvppeteer.browser;

import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.util.*;
import com.sun.javafx.PlatformUtil;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    public static final Map<String, Map<String, String>> downloadURLs = new HashMap() {
        private static final long serialVersionUID = -6918778699407093058L;

        {
            put("chrome", new HashMap<String, String>() {
                private static final long serialVersionUID = 3441562966233820720L;

                {
                    put("host", "https://storage.googleapis.com");
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

    public BrowserFetcher(String projectRoot, FetcherOptions options) {
        this.product = (StringUtil.isNotEmpty(options.getProduct()) ? options.getProduct() : "chromium").toLowerCase();
        ValidateUtil.assertBoolean("chromium".equals(product) || "firefox".equals(product), "Unkown product: " + options.getProduct());
        this.downloadsFolder = StringUtil.isNotEmpty(options.getPath()) ? options.getPath() : Helper.join(projectRoot, ".local-browser");
        this.downloadHost = StringUtil.isNotEmpty(options.getHost()) ? options.getHost() : downloadURLs.get(this.product).get("host");
        this.platform = StringUtil.isNotEmpty(options.getPlatform()) ? options.getPlatform() : null;
        if (platform == null) {
            if (PlatformUtil.isMac())
                this.platform = "mac";
            else if (PlatformUtil.isLinux())
                this.platform = "linux";
            else if (PlatformUtil.isWindows())
                this.platform = Helper.isWin64() ? "x64" : "win32";
            ValidateUtil.notNull(this.platform, "Unsupported platform: " + Helper.paltform());
        }
        ValidateUtil.notNull(downloadURLs.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

	/**
	 * 检测对应的浏览器版本是否可以下载
	 * @param revision 浏览器版本
	 * @param proxy cant be null
	 * @return boolean
	 */
    public boolean canDownload(String revision, Proxy proxy) {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        return httpRequest(proxy, url, "HEAD");
    }

	/**
	 * 发送一个http请求
	 * @param proxy 代理
	 * @param url 请求的url
	 * @param method 请求方法 get post head
	 * @return boolean
	 */
    private boolean httpRequest(Proxy proxy, String url, String method) {
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

        } catch (IOException e) {
            LOGGER.error("Request " + url + " is bad ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return false;
    }

	/**
	 * 下载浏览器
	 * @param revision 浏览器版本
	 * @param progressCallback 下载回调
	 * @return RevisionInfo
	 * @throws IOException 异常
	 * @throws InterruptedException 异常
	 */
    public RevisionInfo download(String revision, BiConsumer<Integer, Integer> progressCallback) throws IOException, InterruptedException {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        Path fileName = Paths.get(url).getFileName();
        String archivePath = Helper.join(this.downloadsFolder, fileName.toString());
        String folderPath = this.getFolderPath(revision);
        if (existsAsync(folderPath))
            return this.revisionInfo(revision);
        if (!(existsAsync(this.downloadsFolder)))
            mkdirAsync(this.downloadsFolder);
        try {
            downloadFile(url, archivePath, progressCallback);
            install(archivePath, folderPath);
        } finally {
            unlinkAsync(archivePath);
        }
        RevisionInfo revisionInfo = this.revisionInfo(revision);
        if (revisionInfo != null)
            chmodAsync(revisionInfo.getExecutablePath(), "775");
        return revisionInfo;
    }

	/**
	 * 本地存在的浏览器版本
	 * @return Set<String>
	 * @throws IOException 异常
	 */
	public Set<String> localRevisions() throws IOException {
        if (!existsAsync(this.downloadsFolder))
            return new HashSet<>();
        Path path = Paths.get(this.downloadsFolder);
        Stream<Path> fileNames = this.readdirAsync(path);
        return fileNames.map(fileName -> parseFolderPath(this.product, fileName)).filter(entry -> entry != null && this.platform.equals(entry.getPlatform())).map(entry -> entry.getRevision()).collect(Collectors.toSet());
    }

	/**
	 * 删除指定版本的浏览器文件
	 * @param revision 版本
	 * @throws IOException 异常
	 */
	public void remove(String revision) throws IOException {
        String folderPath = this.getFolderPath(revision);
        ValidateUtil.assertBoolean(existsAsync(folderPath), "Failed to remove: revision " + revision + " is not downloaded");
        Files.delete(Paths.get(folderPath));
    }

	/**
	 * 根据给定的浏览器产品以及文件夹解析浏览器版本和平台
	 * @param product win linux mac
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
	 * {@link com.ruiyun.jvppeteer.browser.BrowserFetcher#parseFolderPath(java.lang.String, java.nio.file.Path)}用到
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
            this.platform = platform;
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
	 * @param downloadsFolder 下载文件夹
	 * @return Stream<Path> Stream<Path>
	 * @throws IOException 异常
	 */
	private Stream<Path> readdirAsync(Path downloadsFolder) throws IOException {
        ValidateUtil.assertBoolean(Files.isDirectory(downloadsFolder), "downloadsFolder " + downloadsFolder.toString() + " is not Directory");
        return Files.list(downloadsFolder);
    }

	/**
	 * 修改文件权限，与linux上chmod命令一样，并非异步，只是方法名为了与nodejs的puppeteer库一样
	 * @param executablePath
	 * @param perms 权限字符串，例如"775",与linux上文件权限一样
	 * @throws IOException
	 */
    private void chmodAsync(String executablePath, String perms) throws IOException {
        Helper.chmod(executablePath, perms);
    }

	/**
	 * 删除压缩文件
	 * @param archivePath
	 * @throws IOException
	 */
	private void unlinkAsync(String archivePath) throws IOException {
        Files.deleteIfExists(Paths.get(archivePath));
    }

	/**
	 * intall archive file: *.zip,*.tar.bz2,*.dmg
	 * @param archivePath
	 * @param folderPath
	 * @throws IOException
	 * @throws InterruptedException
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
	 * @param archivePath
	 * @param folderPath
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
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
			StreamUtil.closeStream(reader);
			StreamUtil.closeStream(stringWriter);
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
				}
				reader.close();

				reader = new BufferedReader(new InputStreamReader(process2.getErrorStream()));
				while ((line = reader.readLine()) != null) {
					LOGGER.error(line);
				}
				process2.waitFor();
				process2.destroyForcibly();
			} finally {
				StreamUtil.closeStream(reader);
			}
		}
		return mountPath;
	}

	/**
	 * Install *.app directory from dmg file
	 * @param archivePath
	 * @param folderPath
	 * @throws IOException
	 * @throws InterruptedException
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
	 * @param mountPath
	 * @throws IOException
	 * @throws InterruptedException
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
				}
				reader.close();
				reader = new BufferedReader(new InputStreamReader(process3.getErrorStream()));
				while ((line = reader.readLine()) != null) {
					LOGGER.error(line);
				}
				process3.waitFor();
				process3.destroyForcibly();
			} finally {
				StreamUtil.closeStream(reader);
			}
		}
	}

	/**
	 * 解压tar文件
	 * @param archivePath
	 * @param folderPath
	 * @throws IOException
	 */
	private void extractTar(String archivePath, String folderPath) throws IOException {
        BufferedOutputStream wirter = null;
        BufferedInputStream reader = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(archivePath));
            ArchiveEntry nextEntry ;
            while ((nextEntry = tarArchiveInputStream.getNextEntry()) != null) {
                String name = nextEntry.getName();
                Path path = Paths.get(folderPath, name);
                File file = path.toFile();
                if (nextEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    reader = new BufferedInputStream(tarArchiveInputStream);
                    int bufferSize = 8192;
                    int perReadcount = -1;
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
            StreamUtil.closeStream(wirter);
            StreamUtil.closeStream(reader);
            StreamUtil.closeStream(tarArchiveInputStream);
        }
    }

	/**
	 * 解压zip文件
	 * @param archivePath
	 * @param folderPath
	 * @throws IOException
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
                    int bufferSize = 8192;
                    int perReadcount = -1;
                    byte[] buffer = new byte[bufferSize];
                    wirter = new BufferedOutputStream(new FileOutputStream(path.toString()));
                    while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
                        wirter.write(buffer, 0, perReadcount);
                    }
                    wirter.flush();
                }

            }
        } finally {
            StreamUtil.closeStream(wirter);
            StreamUtil.closeStream(reader);
            StreamUtil.closeStream(zipFile);
        }
    }

	/**
	 * 下载浏览器到具体的路径
	 * @param url
	 * @param archivePath
	 * @param progressCallback
	 */
    private void downloadFile(String url, String archivePath, BiConsumer<Integer, Integer> progressCallback) {
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            LOGGER.info("Downloading binary from " + url);
            URL urlR = new URL(url);
            URLConnection urlConnection = urlR.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            // true -- will setting parameters
            httpURLConnection.setDoOutput(true);
            // true--will allow read in from
            httpURLConnection.setDoInput(true);
            // will not use caches
            httpURLConnection.setUseCaches(false);
            // setting serialized
//				httpURLConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
            // default is GET
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charsert", "UTF-8");
            // 1 min
            httpURLConnection.setConnectTimeout(60000);

            // connect to server (tcp)
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Download failed: server returned code " + httpURLConnection.getResponseCode() + ". URL:" + url);
            }
            int totalBytes = 0;
            int downloadedBytes = 0;
            totalBytes = Integer.parseInt(httpURLConnection.getHeaderField("content-length"), 10);
            bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            // server
            File file = new File(archivePath);
            if (!file.exists()) {
                FileUtil.createNewFile(file);
            }
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            int readLength = -1;
            while ((readLength = bufferedInputStream.read(buffer, 0, bufferSize)) > 0) {
                bufferedOutputStream.write(buffer, 0, readLength);
                downloadedBytes += readLength;
                progressCallback.accept(downloadedBytes, totalBytes);
            }
        } catch (Exception e) {
            LOGGER.error("Download failed: ", e);
        } finally {
            StreamUtil.closeStream(bufferedOutputStream);
            StreamUtil.closeStream(bufferedInputStream);
        }
    }

	/**
	 * 创建文件夹
	 * @param downloadsFolder
	 * @throws IOException
	 */
	private void mkdirAsync(String downloadsFolder) throws IOException {
        File file = new File(downloadsFolder);
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }

	/**
	 * 根据浏览器版本获取对应浏览器路径
	 * @param revision 浏览器版本
	 * @return
	 */
	public String getFolderPath(String revision) {
        return Paths.get(this.downloadsFolder, this.platform + "-" + revision).toString();
    }

	/**
	 * 获取浏览器版本相关信息
	 * @param revision
	 * @return
	 */
	public RevisionInfo revisionInfo(String revision) {
        String folderPath = this.getFolderPath(revision);
        String executablePath = "";
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
	 * @param filePath
	 * @return
	 */
	public boolean existsAsync(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

	/**
	 * 根据平台信息和版本信息确定要下载的浏览器压缩包
	 * @param product
	 * @param platform
	 * @param revision
	 * @return
	 */
    public String archiveName(String product, String platform, String revision) {
        if ("chromium".equals(product)) {
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
	 * @param product 产品：chrome or firefox
	 * @param platform win linux mac
	 * @param host 域名地址
	 * @param revision 版本
	 * @return
	 */
    public String downloadURL(String product, String platform, String host, String revision) {
        return String.format(downloadURLs.get(product).get(platform), host, revision, archiveName(product, platform, revision));
    }

    public String getDownloadHost() {
        return downloadHost;
    }

    public void setDownloadHost(String downloadHost) {
        this.downloadHost = downloadHost;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDownloadsFolder() {
        return downloadsFolder;
    }

    public void setDownloadsFolder(String downloadsFolder) {
        this.downloadsFolder = downloadsFolder;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
