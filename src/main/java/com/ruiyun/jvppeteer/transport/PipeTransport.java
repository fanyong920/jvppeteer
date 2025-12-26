package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.util.StreamUtil;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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

    public PipeTransport(InputStream pipeReader, OutputStream pipeWriter) {
        this.pipeReader = new DataInputStream(new BufferedInputStream(pipeReader));
        this.pipeWriter = pipeWriter;
        Thread readThread = new PipeReaderThread();
        readThread.start();
        Thread writerThread = new PipeWriterThread();
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
        StreamUtil.closeQuietly(pipeWriter);
        StreamUtil.closeQuietly(pipeReader);
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
        StringBuilder builder = new StringBuilder();

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    readMessage();
                } catch (IOException e) {
                    break;
                }
            }
        }


        private void readMessage() throws IOException {
            // 直接读取字节流，不假设第一个字节为长度
            byte[] buffer = new byte[1024];
            int bytesRead = pipeReader.read(buffer);
            if (bytesRead > 0) {
                byte[] actualData = new byte[bytesRead];
                System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                dispatch(actualData);
            }
        }

        public void dispatch(byte[] buffer) {
            builder.append(new String(buffer, StandardCharsets.UTF_8));
            int firstNullIndex = builder.indexOf("\0");
            while (firstNullIndex != -1) {
                // 截取第一个 \0 到第二个 \0 之间的字符串（不包含第二个 \0）
                String extracted = builder.substring(0, firstNullIndex);
                builder.delete(0, firstNullIndex + 1);
                onMessage(extracted);
                firstNullIndex = builder.indexOf("\0");
            }

        }
    }
}
