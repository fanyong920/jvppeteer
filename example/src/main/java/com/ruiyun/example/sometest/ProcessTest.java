package com.ruiyun.example.sometest;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.transport.SendMsg;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessTest {

    @Test
    public void processTest() {
        Runtime runtime = Runtime.getRuntime();

        int processorNum = runtime.availableProcessors();

        System.out.println(processorNum);
    }

    @Test
    public void SemaphoreTest() {
        Semaphore semaphore = new Semaphore(1);
        ThreadPoolExecutor s = new ThreadPoolExecutor(2, 2, 30, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            s.execute(() -> {
                try {
                    semaphore.acquire();

                    Thread.sleep(6000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            });
        }

    }

    @Test
    public void test3() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("C:/Program Files (x86)/Google/Chrome/Application/chrome.exe", "about:blank");
        Process process = builder.start();
        Thread chrome = new Thread(() -> {
            try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {

                System.out.println(line);


            }

            Map<String, Object> params = new HashMap<>();
            params.put("discover", true);

            SendMsg sendMsg = new SendMsg();
            sendMsg.setId(1);
            sendMsg.setMethod("Target.setDiscoverTargets");
            sendMsg.setParams(params);
            String string = Constant.OBJECTMAPPER.writeValueAsString(sendMsg);
            process.getOutputStream().write(string.getBytes());
            process.getOutputStream().write("\0".getBytes());

                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        chrome.start();
        chrome.join();
    }
    }
