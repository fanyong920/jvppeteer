package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.core.page.Worker;
import com.ruiyun.jvppeteer.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 与chromuim通过pipe通信暂时没实现
 */
public class PipeTransport implements ConnectionTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private InputStream pipeReader;

    private OutputStream pipeWriter;

    private Thread readThread;

    private Thread writerThread;

    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue();

    private StringBuffer pendingMessage = new StringBuffer();


    public PipeTransport() {

    }

    public PipeTransport(InputStream pipeReader, OutputStream pipeWriter) {
        this.pipeReader = pipeReader;
        this.pipeWriter = pipeWriter;
        readThread = new Thread(new PipeReaderThread());
        readThread.start();
        writerThread = new Thread(new PipeWriterThread());
        writerThread.start();
    }

    @Override
    public void send(String message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("pipe message: "+message);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void close() {
        StreamUtil.closeQuietly(pipeWriter);
        StreamUtil.closeQuietly(pipeReader);
    }

    private class PipeWriterThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //take()方法会阻塞，知道拿到位置
                    String message = messageQueue.take();
                    pipeWriter.write(message.getBytes());
                    pipeWriter.write('\0');
                    pipeWriter.flush();
                } catch (InterruptedException | IOException e) {
                    LOGGER.error("pipe transport send message fail ",e);
                }
            }
        }
    }

    /**
     * 读取管道中的消息线程
     */
    private class PipeReaderThread implements Runnable{

        @Override
        public void run() {
            while (true){
                try {
                    int read = pipeReader.read();
                    if((char)read != '\0'){
                        pendingMessage.append((char)read);

                    }else{
                        String message = pendingMessage.toString();
                        pendingMessage.delete(0,pendingMessage.length());
                        onMessage(message);
                    }
                    System.out.println("阻塞了吗"+pendingMessage.toString());

                } catch (IOException e) {
                    LOGGER.error("read message from chrome error ",e);
                }
            }
        }
    }
}
