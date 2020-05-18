package com.ruiyun.test;

import com.ruiyun.jvppeteer.util.HttpClienUtil;
import com.ruiyun.jvppeteer.util.FileUtil;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StreamUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownLoadUtil {
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
     * 跳过ssl验证配置
     */
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 下载文件的方法
     *
     * @param url
     * @param folder 文件存放的目录
     */

    public static void download(String url, String folder) throws IOException, InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        long contentLength = getContentLength(url);
        System.out.println("contentLength: " + contentLength);
        long taskCount = contentLength % CHUNK_SIZE == 0 ? contentLength / CHUNK_SIZE : contentLength / CHUNK_SIZE + 1;
        int index = url.lastIndexOf("/");
        String fileName = null;
        if (index > -1) {
            fileName = url.substring(index + 1);
            DownLoadUtil.createFile(Helper.join(folder, fileName), contentLength);
        } else {
            throw new IllegalArgumentException("download url does not have name");
        }
        ThreadPoolExecutor executor = DownLoadUtil.getExecutor();
        ExecutorCompletionService service = new ExecutorCompletionService<>(executor);
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            String finalFileName = fileName;
            Future future = service.submit(() -> {
                //偏移量
                boolean isSuccess = true;
                RandomAccessFile file = null;
                FileChannel channel = null;
                HttpURLConnection conn = null;
                HttpsURLConnection conns = null;
                ReadableByteChannel readableByteChannel = null;
                int increment = atomicInteger.getAndIncrement();
                int seekLength = increment * CHUNK_SIZE;
                try {
                    file = new RandomAccessFile(finalFileName, "rw");
                    file.seek(seekLength);
                    channel = file.getChannel();
                    CloseableHttpClient httpclient = HttpClienUtil.getHttpclient();
                    HttpGet httpGet = new HttpGet(url);
                    CloseableHttpResponse execute = httpclient.execute(httpGet);
                    HttpEntity entity = execute.getEntity();
                     readableByteChannel = Channels.newChannel(entity.getContent());
                    channel.transferFrom(readableByteChannel,0,Long.MAX_VALUE);
                    return true;
                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } finally {
                    System.out.println("块[" + increment + "]处理下载字节【" + seekLength + "】-" + "【" + (seekLength + CHUNK_SIZE) + "】完成，下载结果：" + isSuccess);
                    if (!isSuccess) {
                        executor.shutdownNow();
                    }
                    StreamUtil.closeQuietly(file);
                    StreamUtil.closeQuietly(channel);
                    StreamUtil.closeQuietly(readableByteChannel);
                    if (conn != null) {
                        conn.disconnect();
                        conn = null;
                    }

                }
                return false;
            });
            futures.add(future);
        }
        executor.shutdown();
        for (int j = 0; j < taskCount; j++) {
            service.take();
        }
    }

    /**
     * 获取下载得文件长度
     *
     * @return
     */
    public static final long getContentLength(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClienUtil.getHttpclient();
        HttpHead httpHead = new HttpHead(url);
        CloseableHttpResponse response = httpclient.execute(httpHead);
        try {
            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 204) {
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if ("content-length".equalsIgnoreCase(header.getName())) {
                        String value = header.getValue();
                        return Long.parseLong(value);
                    }
                }
                throw new RuntimeException(url + " response not found content-length header: " + response.getStatusLine().getStatusCode());
            } else {
                throw new RuntimeException(url + " response code: " + response.getStatusLine().getStatusCode());
            }
        } finally {
            response.close();
        }
    }

    /**
     * 创建线程池
     *
     * @return
     */
    public static ThreadPoolExecutor getExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

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
     * 安装证书访问SSL地址
     * 证书要（ssecacerts文件）放入%JAVA_HOME%\jre\lib\security 下即可。
     * 这种方法比较麻烦，这里只给出代码，不使用。
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

