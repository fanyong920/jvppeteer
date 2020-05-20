package com.ruiyun.test;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownLoadUtil {

    private static ThreadPoolExecutor executor = null;

    /**
     * 线程池数量
     */
    private static final int THREAD_COUNT = 5;

    /**
     * 每条线程下载的文件块大小 10M
     */
    private static final int CHUNK_SIZE = 1024 * 1024 * 5;

    /**
     * 重试次数
     */
    private static final int RETRY_TIMES = 5;

    /**
     * 读取数据超时
     */
    private static final int READ_TIME_OUT = 10000;

    /**
     * 连接超时设置
     */
    private static final int CONNECT_TIME_OUT = 10000;

    /**
     * 下载文件的方法
     *
     * @param url
     * @param folder 文件存放的目录
     */

    public static void download(String url, String folder) throws IOException, ExecutionException, InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        long contentLength = getContentLength(url);

        long taskCount = contentLength % CHUNK_SIZE == 0 ? contentLength / CHUNK_SIZE : contentLength / CHUNK_SIZE + 1;
        int index = url.lastIndexOf("/");
        String fileName = null;
        if (index > -1) {
            fileName = url.substring(index + 1);
            DownLoadUtil.createFile(Helper.join(folder, fileName), contentLength);
        } else {
            throw new IllegalArgumentException("download url does not have name");
        }
//        trustAllHosts();
        ThreadPoolExecutor executor = DownLoadUtil.getExecutor();
        CompletionService completionService = new ExecutorCompletionService(executor);
        List<Future> futureList = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            String finalFileName = fileName;
            Future future = completionService.submit(() -> {
                //偏移量
                RandomAccessFile file = null;
                FileChannel channel = null;
                HttpURLConnection conn = null;
                BufferedInputStream reader = null;
                int increment = atomicInteger.getAndIncrement();
                int seekLength = increment * CHUNK_SIZE;
                try {
                    file = new RandomAccessFile(finalFileName, "rw");
                    file.seek(seekLength);

                    URL uRL = new URL(url);
                    conn = (HttpURLConnection) uRL.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(10000);
                    conn.setRequestMethod("GET");
                    String range = "bytes=" + seekLength + "-" + (seekLength + CHUNK_SIZE);
                    System.out.println("range:"+range);
                    conn.setRequestProperty("Range", range);

                    for (int j = 0; j < RETRY_TIMES; j++) {
                        try {
                            conn.connect();
                            reader = new BufferedInputStream(conn.getInputStream());
                            byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
                            int read;
                            while ((read = reader.read(buffer, 0, Constant.DEFAULT_BUFFER_SIZE)) != -1) {
                                file.write(buffer, 0, read);
//                                System.out.println(new String(buffer));
                            }
                            System.out.println("下载完成：" + increment);
                            return true;
                        } catch (Exception e) {
                            if (j == RETRY_TIMES - 1) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    StreamUtil.closeQuietly(file);
                    StreamUtil.closeQuietly(channel);
                    StreamUtil.closeQuietly(readableByteChannel);
                    if (conn != null) {
                        conn.disconnect();
                        conn = null;
                    }

                }
            });
            futureList.add(future);
        }
        executor.shutdown();
        for (Future future1 : futureList) {
            Object result = future1.get();
            if (!(boolean) result) {
                System.out.println("下载失败。。。");
                executor.shutdownNow();
            }
        }

    }

    /**
     * 获取下载得文件长度
     *
     * @return
     */
    public static final long getContentLength(String url) throws IOException {
        URL uuuRl = new URL(url);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uuuRl.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(READ_TIME_OUT);
            conn.setReadTimeout(CONNECT_TIME_OUT);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode <= 204) {
                return conn.getContentLengthLong();
            } else {
                throw new RuntimeException(url + " responseCode: " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            synchronized (DownLoadUtil.class) {
                if (executor == null) {
                    executor = new
                            ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
                }
            }
        }
        return executor;
    }

    /**
     * 创建固定大小的文件
     *
     * @param path
     * @param length
     * @return
     */
    public static void createFile(String path, long length) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            FileUtil.createNewFile(file);
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
        randomAccessFile.setLength(length);
        randomAccessFile.close();
    }

    /**
     * 信任所有网站
     */
    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }
                }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL", "SunJSSE");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

