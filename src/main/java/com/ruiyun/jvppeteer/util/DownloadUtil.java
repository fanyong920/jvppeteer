package com.ruiyun.jvppeteer.util;

import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.transport.Connection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class DownloadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtil.class);
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

    private static final String FAIL_RESULT = "-1";

    /**
     * 下载文件的方法
     *
     * @param url
     * @param filePath 文件路径
     */

    public static void download(String url, String filePath, BiConsumer<Integer, Integer> progressCallback) throws IOException, ExecutionException, InterruptedException {
        long contentLength = getContentLength(url);

        long taskCount = contentLength % CHUNK_SIZE == 0 ? contentLength / CHUNK_SIZE : contentLength / CHUNK_SIZE + 1;
        DownloadUtil.createFile(filePath, contentLength);

        ThreadPoolExecutor executor = DownloadUtil.getExecutor();
        CompletionService completionService = new ExecutorCompletionService(executor);
        List<Future> futureList = new ArrayList<>();
        int downloadCount = 0;
        if (contentLength <= CHUNK_SIZE) {
            Future future = completionService.submit(new DownloadCallable(0, contentLength, filePath, url));
            futureList.add(future);
        } else {
            for (int i = 0; i < taskCount; i++) {
                if (i == taskCount - 1) {
                    Future future = completionService.submit(new DownloadCallable(i * CHUNK_SIZE, contentLength, filePath, url));
                    futureList.add(future);
                } else {
                    Future future = completionService.submit(new DownloadCallable(i * CHUNK_SIZE, (i + 1) * CHUNK_SIZE, filePath, url));
                    futureList.add(future);
                }
            }
        }
        executor.shutdown();
        for (Future future1 : futureList) {
            String result = (String) future1.get();
            if (FAIL_RESULT.equals(result)) {
                LOGGER.error("download fail,url:"+url);
                Files.delete(Paths.get(filePath));
                executor.shutdownNow();
            } else {
                try {
                    downloadCount +=Integer.parseInt(result);
                    if (progressCallback != null){
                        progressCallback.accept(downloadCount,(int) (contentLength / 1024 / 1024));
                    }
                }catch (Exception e){
                    LOGGER.error("ProgressCallback has some problem",e);
                }
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

    /**
     * 创建一个用于下载chrome的线程池
     * @return 线程池
     */
    public static ThreadPoolExecutor getExecutor() {
        return new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

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


    static class DownloadCallable implements Callable<String> {

        private long startPosition;

        private long endPosition;

        private String filePath;

        private String url;

        public DownloadCallable(long startPosition, long endPosition, String filePath, String url) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.filePath = filePath;
            this.url = url;
        }

        @Override
        public String call() throws Exception {
            RandomAccessFile file = null;
            HttpURLConnection conn = null;
            BufferedInputStream reader = null;
            try {
                file = new RandomAccessFile(this.filePath, "rw");
                file.seek(this.startPosition);
                URL uRL = new URL(this.url);
                conn = (HttpURLConnection) uRL.openConnection();
                conn.setConnectTimeout(CONNECT_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                conn.setRequestMethod("GET");
                String range = "bytes=" + startPosition + "-" + endPosition;
                conn.addRequestProperty("Range", range);
                conn.addRequestProperty("accept-encoding", "gzip, deflate, br");
                ByteBuffer buffer = ByteBuffer.allocate(Constant.DEFAULT_BUFFER_SIZE);
                FileChannel channel = file.getChannel();
                for (int j = 0; j < RETRY_TIMES; j++) {
                    try {
                        conn.connect();
                        InputStream inputStream = conn.getInputStream();
                        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                        while (readableByteChannel.read(buffer) != -1) {
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                            buffer.clear();
                        }

                        return String.valueOf((endPosition - startPosition) / 1024 / 1024);
                    } catch (Exception e) {
                        if (j == RETRY_TIMES - 1) {
                            LOGGER.error("download url[{}] bytes[{}] fail.",url,range);
                        }
                        continue;
                    }
                }
                return FAIL_RESULT;
            } catch (Exception e) {
                LOGGER.error("download url[{}] bytes[{}] fail.",url,startPosition+"-"+endPosition);
                return FAIL_RESULT;
            } finally {
                StreamUtil.closeQuietly(reader);
                StreamUtil.closeQuietly(file);
                if (conn != null) {
                    conn.disconnect();
                }

            }
        }
    }

}




