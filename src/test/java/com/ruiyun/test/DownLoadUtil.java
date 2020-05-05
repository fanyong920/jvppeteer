package com.ruiyun.test;

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
     * 下载文件的方法
     *
     * @param url
     * @param folder 文件存放的目录
     */

    public static void download(String url, String folder) throws IOException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        long contentLength = getContentLength(url);

        long taskCount = contentLength % CHUNK_SIZE == 0 ? contentLength / CHUNK_SIZE : contentLength / CHUNK_SIZE + 1;
        int index = url.lastIndexOf("/");
        String fileName = null;
        if (index > -1) {
            fileName = url.substring(index + 1);
            DownLoadUtil.createFile(Helper.join(folder,fileName),contentLength);
        } else {
            throw new IllegalArgumentException("download url does not have name");
        }
        trustAllHosts();
        for (int i = 0; i < taskCount; i++) {
            String finalFileName = fileName;
            DownLoadUtil.getExecutor().execute(() -> {
                //偏移量
                RandomAccessFile file = null;
                FileChannel channel = null;
                HttpURLConnection conn = null;
                ReadableByteChannel readableByteChannel = null;
                int increment = atomicInteger.getAndIncrement();
                int seekLength =  increment* CHUNK_SIZE;
                try {
                    file = new RandomAccessFile(finalFileName, "rw");
                    file.seek(seekLength);
                    channel = file.getChannel();

                    URL uRL = new URL(url);
                    conn = (HttpURLConnection) uRL.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.addRequestProperty("Range", "bytes=" + seekLength + "-" + (seekLength + CHUNK_SIZE));

                    for (int j = 0; j < RETRY_TIMES; j++) {
                        try {
                            conn.connect();
                            break;
                        }catch (SocketTimeoutException e){
                            continue;
                        }
                    }

                    readableByteChannel = Channels.newChannel(conn.getInputStream());
                    channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("块["+increment+"]下载字节【"+seekLength+"】-"+"【"+(seekLength+CHUNK_SIZE)+"】完成");
                    StreamUtil.closeStream(file);
                    StreamUtil.closeStream(channel);
                    StreamUtil.closeStream(readableByteChannel);
                    if (conn != null) {
                        conn.disconnect();
                        conn = null;
                    }

                }
            });
        }

    }

    /**
     * 获取下载得文件长度
     *
     * @return
     */
    public static final long getContentLength(String url) throws IOException {
        URL uuuRl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uuuRl.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode <= 204) {
           return conn.getContentLengthLong();
        } else {
            throw new RuntimeException(url + " responseCode: " + responseCode);
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
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
            }
        } };

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

