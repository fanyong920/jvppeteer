package com.ruiyun.test;

import org.junit.Test;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessTest {

    @Test
    public  void processTest(){
        Runtime runtime = Runtime.getRuntime();

        int processorNum = runtime.availableProcessors();

        System.out.println(processorNum);
    }

    @Test
    public  void SemaphoreTest(){
        Semaphore semaphore = new Semaphore(1);
        ThreadPoolExecutor s= new ThreadPoolExecutor(2,2,30, TimeUnit.MINUTES,new LinkedBlockingDeque<>());
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            s.execute(() -> {
                try {
                    semaphore.acquire();

                    Thread.sleep(6000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            });
        }

    }
}
