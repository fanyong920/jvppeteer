package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.transport.ConnectionTransport;
import com.ruiyun.jvppeteer.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PipeTransport implements ConnectionTransport {

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
            System.out.println("pipe send message："+message);
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
        StreamUtil.closeStream(pipeWriter);
        StreamUtil.closeStream(pipeReader);
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
                    e.printStackTrace();
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
                System.out.println("11111111--------------------1111111111111111");
                try {

                    int read = ((PipedInputStream)pipeReader).read();

                    if((char)read != '\0'){
                        pendingMessage.append((char)read);

                    }else{
                        String message = pendingMessage.toString();
                        pendingMessage.delete(0,pendingMessage.length());
                        onMessage(message);
                    }
                    System.out.println("阻塞了吗"+pendingMessage.toString());


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
