package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class DownloadChromiumExample {
    /**
     * 演示下载chrome功能
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Puppeteer puppeteer = new Puppeteer();
        BrowserFetcher browserFetcher = puppeteer.createBrowserFetcher();
        BiConsumer<Integer,Integer> processCallback = (a,b) -> {
//            NumberFormat percent = NumberFormat.getPercentInstance();
//            percent.setMaximumFractionDigits(2);
            System.out.println("下载进度："+ a+":"+b);
        };
        //指定下载版本下载，也可以不指定下载版本，如果不知道下载版本，则默认下载最新版本
        browserFetcher.download("737027",processCallback);
    }
}
