package com.ruiyun.jvppeteer.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDownloader.class);

    /**
     * 获取操作系统类型
     */
    private static String getOSType() {
        if (Helper.isWindows()) {
            return "win";
        } else if (Helper.isMac()) {
            return "darwin";
        } else if (Helper.isLinux()) {
            return "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + Helper.platform());
        }
    }

    /**
     * 获取架构类型（排除 x86）
     */
    private static String getArchitecture() {
        return "x64";
//        if (Helper.arch().contains("amd64") || Helper.arch().contains("x86_64")) {
//            return "x64";
//        } else if (Helper.arch().contains("aarch64") || Helper.arch().contains("arm64")) {
//            return "arm64";
//        } else if (Helper.arch().contains("x86") || Helper.arch().contains("i386")) {
//            // 不支持 x86 架构，抛出异常
//            throw new UnsupportedOperationException("Node.js x86 architecture is not supported, please use x64 or arm64 instead: " + Helper.arch());
//        } else {
//            throw new UnsupportedOperationException("Unsupported architecture: " + Helper.arch());
//        }
    }

    private static final String NODE_VERSION = "24.12.0";
    private static final String NODE_DOWNLOAD_BASE_URL = "https://nodejs.org/dist/v" + NODE_VERSION + "/";

    /**
     * 根据操作系统获取对应的 Node.js 下载 URL
     */
    public static String getNodeDownloadUrl() {
        String osType = getOSType();
        String arch = getArchitecture();
        String extension = osType.equals("win") ? ".zip" : ".tar.gz";

        return NODE_DOWNLOAD_BASE_URL + "node-v" + NODE_VERSION + "-" + osType + "-" + arch + extension;
    }

    /**
     * 下载 Node.js 到指定目录
     */
    public static Path downloadNode(String downloadDir) throws IOException {
        String downloadUrl = getNodeDownloadUrl();
        String fileName = getFileNameFromUrl(downloadUrl);
        Path downloadPath = Paths.get(downloadDir, fileName);

        LOGGER.info("Downloading Node.js from: {}", downloadUrl);
        LOGGER.info("Download path: {}", downloadPath);

        // 确保下载目录存在
        Files.createDirectories(Paths.get(downloadDir));

        // 执行下载
        downloadFile(downloadUrl, downloadPath);

        // 解压文件
        Path extractedPath = extractNode(downloadPath, downloadDir);

        LOGGER.info("Node.js downloaded and extracted to: {}", extractedPath);

        return extractedPath;
    }

    /**
     * 从 URL 中获取文件名
     */
    private static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * 下载文件
     */
    private static void downloadFile(String downloadUrl, Path downloadPath) throws IOException {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; jvppeteer)");
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Download failed with response code: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(downloadPath.toFile())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long startTime = System.currentTimeMillis();
            long contentLength = connection.getContentLength();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 每5秒打印一次下载进度
                if (System.currentTimeMillis() - startTime > 5000) {
                    if (contentLength > 0) {
                        double progress = (double) totalBytesRead / contentLength * 100;
                        LOGGER.info("Downloaded {} bytes of {} ({}%)", totalBytesRead, contentLength, String.format("%.2f", progress));
                    } else {
                        LOGGER.info("Downloaded {} bytes", totalBytesRead);
                    }
                    startTime = System.currentTimeMillis();
                }
            }
            LOGGER.info("Download completed. Total size: {} bytes", totalBytesRead);
        }
    }

    /**
     * 解压 Node.js 压缩包
     */
    private static Path extractNode(Path archivePath, String extractDir) throws IOException {
        String fileName = archivePath.getFileName().toString();

        if (fileName.endsWith(".zip")) {
            return extractZip(archivePath, extractDir);
        } else if (fileName.endsWith(".tar.gz")) {
            return extractTarGz(archivePath, extractDir);
        } else {
            throw new IOException("Unsupported archive format: " + fileName);
        }
    }

    /**
     * 解压 ZIP 文件
     */
    private static Path extractZip(Path zipPath, String extractDir) throws IOException {
        String extractedDirName = zipPath.getFileName().toString().replace(".zip", "");
        Path extractPath = Paths.get(extractDir, extractedDirName);

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = Paths.get(extractDir, entry.getName()).normalize();

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Path parentDir = entryPath.getParent();
                    if (parentDir != null) {
                        Files.createDirectories(parentDir);
                    }
                    Files.copy(zipInputStream, entryPath);
                }
                zipInputStream.closeEntry();
            }
        }
        return extractPath;
    }

    /**
     * 解压 TAR.GZ 文件（使用系统命令）
     */
    private static Path extractTarGz(Path tarGzPath, String extractDir) throws IOException {
        String osType = getOSType();
        String tarCommand;

        if ("darwin".equals(osType) || "linux".equals(osType)) {
            tarCommand = "tar -xzf " + tarGzPath.toAbsolutePath() + " -C " + extractDir;
        } else {
            throw new UnsupportedOperationException("Automatic tar.gz extraction not supported on Windows. Consider using a library like Apache Commons Compress.");
        }

        Process process = Runtime.getRuntime().exec(tarCommand);
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Extraction failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Extraction interrupted", e);
        }

        // 返回解压后的目录
        String extractedDirName = tarGzPath.getFileName().toString().replace(".tar.gz", "");
        return Paths.get(extractDir, extractedDirName);
    }

    /**
     * 获取 Node.js 可执行文件路径
     */
    public static Path getNodeExecutablePath(Path nodeDir) {
        String osType = getOSType();

        if ("win".equals(osType)) {
            return nodeDir.resolve("node.exe");
        } else {
            return nodeDir.resolve("bin").resolve("node");
        }
    }
}
