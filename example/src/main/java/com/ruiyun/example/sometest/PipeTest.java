package com.ruiyun.example.sometest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.transport.SendMsg;
import com.ruiyun.jvppeteer.util.StreamUtil;
import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class PipeTest {

    public static void main(String[] args) throws Exception {
        NuProcessBuilder pb = new NuProcessBuilder(Arrays.asList("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe","--remote-debugging-pipe"));

        ProcessHandler handler = new ProcessHandler();
        pb.setProcessListener(handler);
        NuProcess process = pb.start();
        process.wantWrite();

//        process.writeStdin(buffer);


        process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite
        Thread.sleep(10000L);
    }
    @Test
    public void test() throws Exception {
        //
//        List<String> arguments = new ArrayList<>();
//        arguments.add("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe");
//        arguments.add("--remote-debugging-pipe");
//
//
//        ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
//        Process start = processBuilder.start();
//        InputStream inputStream = start.getInputStream();
//        start(inputStream);

        NuProcessBuilder pb = new NuProcessBuilder(Arrays.asList("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe"));
        ProcessHandler handler = new ProcessHandler();
        pb.setProcessListener(handler);
        NuProcess process = pb.start();

        ByteBuffer buffer = ByteBuffer.wrap("Hello, World!".getBytes());
        process.writeStdin(buffer);

        process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite

    }


}
class ProcessHandler extends NuAbstractProcessHandler {
    private NuProcess nuProcess;

    @Override
    public boolean onStdinReady(ByteBuffer buffer) {
//        byte[] bytes = new byte[buffer.remaining()];
        // You must update buffer.position() before returning (either implicitly,
        // like this, or explicitly) to indicate how many bytes your handler has consumed.
//        buffer.get(bytes);
//        System.out.println(new String(bytes));
//        buffer.flip();
        Map<String, Object> params = new HashMap<>();
        params.put("url", "about:blank");
        SendMsg message = new SendMsg();
        message.setMethod("Target.createTarget");
        message.setParams(params);
        message.setId(1L);
        String sendMsg = null;
        try {
            sendMsg = Constant.OBJECTMAPPER.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
         buffer.put(sendMsg.getBytes());
        buffer.flip();
        return false; // false means we have nothing else to write at this time
    }

    @Override
    public void onStart(NuProcess nuProcess) {
        this.nuProcess = nuProcess;
    }

    public void onStdout(ByteBuffer buffer, boolean closed) {
//        System.out.println();
        byte[] bytes = new byte[buffer.remaining()];
        // You must update buffer.position() before returning (either implicitly,
        // like this, or explicitly) to indicate how many bytes your handler has consumed.
        buffer.get(bytes);
        System.out.println(new String(bytes));
//        if (!closed) {
//            byte[] bytes = new byte[buffer.remaining()];
//            // You must update buffer.position() before returning (either implicitly,
//            // like this, or explicitly) to indicate how many bytes your handler has consumed.
//            buffer.get(bytes);
//            System.out.println(new String(bytes));
//
//            // For this example, we're done, so closing STDIN will cause the "cat" process to exit
////            nuProcess.closeStdin(true);
//        }
    }
}

