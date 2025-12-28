package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.util.StreamUtil;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 与chromuim通过pipe通信暂时没实现
 */
public class PipeTransport implements ConnectionTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipeTransport.class);
    private final DataInputStream pipeReader;
    private final OutputStream pipeWriter;
    private Connection connection = null;
    private final BlockingQueue<String> sendQueue = new ArrayBlockingQueue<>(1000);
    private final Thread readThread;
    private final Thread writerThread;
    private volatile boolean closed = false;
    volatile boolean remote = false;

    public PipeTransport(InputStream pipeReader, OutputStream pipeWriter) {
        this.pipeReader = new DataInputStream(new BufferedInputStream(pipeReader));
        this.pipeWriter = pipeWriter;
        readThread = new PipeReaderThread();
        readThread.setName("PipeReaderThread");
        readThread.start();
        writerThread = new PipeWriterThread();
        writerThread.setName("PipeWriterThread");
        writerThread.start();
    }

    @Override
    public void send(String message) {
        try {
            sendQueue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(String message) {
        Objects.requireNonNull(this.connection, "Connection may be closed!");
        this.connection.onMessage(message);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        LOGGER.info("Pipe connection closed by " + (remote ? "remote peer" : "us"));
        this.closed = true;
        StreamUtil.closeQuietly(pipeWriter);
        StreamUtil.closeQuietly(pipeReader);
        //浏览器意外关闭时候，connection不为空
        Optional.ofNullable(this.connection).map(Connection::closeRunner).ifPresent(Runnable::run);
        readThread.interrupt();
        writerThread.interrupt();
    }

    private class PipeWriterThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    String take = sendQueue.take();
                    sendMessage(take);
                } catch (IOException e) {
                    if (!isInterrupted())
                        LOGGER.error("Pipe writer message to browser error ", e);
                    break;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void sendMessage(String message) throws IOException {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            pipeWriter.write(bytes);
            pipeWriter.write(0);
            pipeWriter.flush();
        }
    }

    /**
     * 读取管道中的消息线程
     */
    private class PipeReaderThread extends Thread {
        List<byte[]> pendingMessage = new ArrayList<>();

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    readMessage();
                } catch (Exception e) {
                    break;
                }
            }
        }


        private void readMessage() throws IOException {
            // 直接读取字节流，不假设第一个字节为长度
            byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
            int bytesRead = pipeReader.read(buffer);
            if (bytesRead > 0) {
                byte[] actualData = new byte[bytesRead];
                System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                dispatch(actualData);
            }
        }

        private void dispatch(byte[] buffer) {
            // Concatenate all pending messages
            pendingMessage.add(buffer);
            int totalLength = pendingMessage.stream().mapToInt(b -> b.length).sum();
            if (totalLength == 0) {
                return;
            }

            byte[] concatBuffer = new byte[totalLength];
            int position = 0;
            for (byte[] buf : pendingMessage) {
                System.arraycopy(buf, 0, concatBuffer, position, buf.length);
                position += buf.length;
            }

            // Reset pending messages
            pendingMessage.clear();

            int start = 0;
            int end = findNullByte(concatBuffer, start);

            // Process all complete messages
            while (end != -1) {
                String message = new String(concatBuffer, start, end - start, StandardCharsets.UTF_8);
                //browser-launch-pipe.js 发送的pipe断开的消息
                if (message.equals("{\"method\":\"Browser.close\",\"id\":25}")) {
                    remote = true;
                    close();
                    return;
                }
                onMessage(message);
                start = end + 1;
                end = findNullByte(concatBuffer, start);
            }

            // Store any remaining data
            if (start < concatBuffer.length) {
                byte[] remaining = new byte[concatBuffer.length - start];
                System.arraycopy(concatBuffer, start, remaining, 0, remaining.length);
                pendingMessage.add(remaining);
            }
        }

        private int findNullByte(byte[] buffer, int startIndex) {
            for (int i = startIndex; i < buffer.length; i++) {
                if (buffer[i] == 0) { // Compare with integer 0 for null byte
                    return i;
                }
            }
            return -1;
        }
    }
}
